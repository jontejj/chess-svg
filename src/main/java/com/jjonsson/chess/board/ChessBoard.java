package com.jjonsson.chess.board;

import static com.jjonsson.chess.moves.Position.BLACK_PAWN_ROW;
import static com.jjonsson.chess.moves.Position.BLACK_STARTING_ROW;
import static com.jjonsson.chess.moves.Position.WHITE_PAWN_ROW;
import static com.jjonsson.chess.moves.Position.WHITE_STARTING_ROW;
import static com.jjonsson.chess.moves.Position.Column.A;
import static com.jjonsson.chess.moves.Position.Column.B;
import static com.jjonsson.chess.moves.Position.Column.C;
import static com.jjonsson.chess.moves.Position.Column.D;
import static com.jjonsson.chess.moves.Position.Column.E;
import static com.jjonsson.chess.moves.Position.Column.F;
import static com.jjonsson.chess.moves.Position.Column.G;
import static com.jjonsson.chess.moves.Position.Column.H;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.utilities.Bits.containBits;
import static com.jjonsson.utilities.Loggers.STDERR;
import static com.jjonsson.utilities.Loggers.STDOUT;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.evaluators.statistics.StatisticsAction;
import com.jjonsson.chess.evaluators.statistics.StatisticsTracker;
import com.jjonsson.chess.exceptions.DuplicatePieceError;
import com.jjonsson.chess.exceptions.InvalidBoardException;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.listeners.ChessBoardListener;
import com.jjonsson.chess.listeners.MoveListener;
import com.jjonsson.chess.listeners.StatisticsListener;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.Position.Column;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.persistence.BoardLoader;
import com.jjonsson.chess.persistence.ChessFileFilter;
import com.jjonsson.chess.persistence.MoveLogger;
import com.jjonsson.chess.persistence.MoveLoggerFactory;
import com.jjonsson.chess.persistence.PersistanceLogging;
import com.jjonsson.chess.persistence.PersistenceLogger;
import com.jjonsson.chess.pieces.Bishop;
import com.jjonsson.chess.pieces.BlackPawn;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Knight;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.pieces.WhitePawn;

public final class ChessBoard
{
	public static final byte BOARD_SIZE = 8;
	public static final byte MOVES_IN_ONE_DIRECTION = BOARD_SIZE - 1;

	/**
	 * Defines how important it is to keep available moves for one's king
	 * King Mobility = KING_MOBILITY_FACTOR * "the number of available moves for one's king"
	 */
	public static final int KING_MOBILITY_FACTOR = 20;

	/**
	 * In principle this means that at least 2 steps ahead is evaluated for every move
	 */
	private static final int DEFAULT_DIFFICULTY = 1;
	/**
	 * If set it means that it's Blacks turn
	 */
	private static final byte BLACKS_TURN_BIT = (byte) (1 << 7);
	/**
	 * If set it means that moves is stored in the buffer, right after the settings bit.
	 */
	private static final byte READ_MOVE_HISTORY_BIT = (byte) (1 << 6);

	private short myBlackAvailableMovesCount;
	private short myWhiteAvailableMovesCount;

	private Map<Piece, Map<ImmutablePosition, Move>> myPieceToPositionAvailableMoves;
	private Map<Piece, Map<ImmutablePosition, Move>> myPieceToPositionNonAvailableMoves;

	private Set<Move> myWhiteAvailableMoves;
	private Set<Move> myBlackAvailableMoves;

	private boolean myCurrentPlayer;

	private Set<ChessBoardListener> myBoardListeners;
	private Set<MoveListener> myMoveListeners;
	private StatisticsTracker myStatisticsTracker;

	private MoveLogger myMoveLogger;
	private PersistenceLogger myPersistenceLogger;

	private King	myBlackKing;

	private King	myWhiteKing;

	private ChessState	myCurrentGameState;
	private Set<Move> myMovesThatStopsKingFromBeingChecked;

	//Manages the possibility of automatic moves (used during move reverting)
	private boolean myAllowsMoves;

	/**
	 * This is a counter that keeps track of how many black moves that protects black pieces
	 * Note: May be more than the number of black pieces due to pieces being protected by two different moves
	 */
	private long myBlackProtectedPiecesCount;
	/**
	 * This is a counter that keeps track of how many white moves that protects white pieces
	 * Note: May be more than the number of white pieces due to pieces being protected by two different moves
	 */
	private long myWhiteProtectedPiecesCount;

	/**
	 * This is a counter that keeps track of the accumulated (for the current state) take over value for all black moves
	 */
	private long myBlackTakeOverPiecesCount;

	/**
	 * This is a counter that keeps track of the accumulated (for the current state) take over value for all white moves
	 */
	private long myWhiteTakeOverPiecesCount;

	/**
	 * The total piece value for all the white pieces
	 */
	private int myWhitePieceValueCount;
	/**
	 * The total piece value for all the black pieces
	 */
	private int myBlackPieceValueCount;

	/**
	 * The difficulty the user has chosen (Good values are 1-5) defaults to {@link ChessBoard.DEFAULT_DIFFICULTY}
	 */
	private int	myDifficulty;

	private Set<Piece> myPieces;
	private PositionContainer[][] myPositions;

	private Set<Move> myScheduledMoveUpdates;

	/**
	 * Constructs the chess board
	 * @param placeInitialPieces if true, all the pieces is set to their default locations
	 */
	public ChessBoard(final PiecePlacement piecePlacement)
	{
		myDifficulty = DEFAULT_DIFFICULTY;
		myAllowsMoves = true;
		myMovesThatStopsKingFromBeingChecked = ImmutableSet.of();
		myBoardListeners = Sets.newIdentityHashSet();
		myMoveListeners = Sets.newIdentityHashSet();
		myMoveLogger = MoveLoggerFactory.createMoveLogger();
		myScheduledMoveUpdates = Sets.newIdentityHashSet();
		addMoveListener(myMoveLogger);

		myPieces = Sets.newIdentityHashSet();

		myPieceToPositionAvailableMoves = Maps.newHashMap();
		myPieceToPositionNonAvailableMoves = Maps.newHashMap();
		myWhiteAvailableMoves = Sets.newIdentityHashSet();
		myBlackAvailableMoves = Sets.newIdentityHashSet();

		if(piecePlacement.shouldPlacePieces())
		{
			this.reset();
		}
		else
		{
			setupPositionContainers();
		}
	}

	public ChessBoard(final PiecePlacement piecePlacement, final PersistanceLogging persistenceLogging)
	{
		this(piecePlacement);
		if(persistenceLogging.usePersistanceLogging())
		{
			addPersistenceLogger(MoveLoggerFactory.createPersistenceLogger());
			if(piecePlacement.shouldPlacePieces())
			{
				//This means that we now have a board for the persistence logger to start from
				updatePersistenceLogger();
			}
		}
	}

	public void performStatisticsAction(final StatisticsAction action)
	{
		if(myStatisticsTracker != null)
		{
			myStatisticsTracker.perform(action);
		}
	}

	/**
	 * Constructs a default ChessBoard (places pieces in their original positions),
	 * with the possibility to save moves as well.
	 */
	public ChessBoard()
	{
		this(PiecePlacement.PLACE_PIECES, PersistanceLogging.USE_PERSISTANCE_LOGGING);
	}

	private void scheduleMoveForUpdate(final Move move)
	{
		myScheduledMoveUpdates.add(move);
	}

	public void setupPositionContainers()
	{
		myPositions = new PositionContainer[ChessBoard.BOARD_SIZE][ChessBoard.BOARD_SIZE];
		for(byte r = ChessBoard.BOARD_SIZE - 1; r >= 0; r--)
		{
			for(byte c = ChessBoard.BOARD_SIZE - 1; c >= 0; c--)
			{
				myPositions[r][c] = new PositionContainer(this);
			}
		}
	}

	/**
	 * 
	 * @param moveListener
	 * @return true if the listener was added
	 */
	public boolean addMoveListener(final MoveListener moveListener)
	{
		return myMoveListeners.add(moveListener);
	}

	public void setStatisticsListener(final StatisticsListener statisticsListener)
	{
		myStatisticsTracker = new StatisticsTracker(statisticsListener);
	}

	public StatisticsTracker getStatisticsTracker()
	{
		return myStatisticsTracker;
	}

	public void addPersistenceLogger(final PersistenceLogger persistenceMoveLogger)
	{
		myPersistenceLogger = persistenceMoveLogger;
		addMoveListener(persistenceMoveLogger);
	}

	public void setDifficulty(final int newDifficulty)
	{
		myDifficulty = newDifficulty;
	}

	/**
	 * Makes a copy of the board without copying the listeners
	 * <br><b>Note</b>: this does not copy the made moves on the board, so an undo operation on the returned board would always fail
	 * @param withMoveHistory true if the made moves from this board should be included in the copy
	 * @return the new board or null if the copy failed
	 */
	public ChessBoard copy(final PersistanceLogging persistanceLogging)
	{
		ChessBoard newBoard = new ChessBoard(PiecePlacement.DONT_PLACE_PIECES, persistanceLogging);
		ByteBuffer buffer = ByteBuffer.allocate(getPersistenceSize(persistanceLogging));
		try
		{
			writePersistenceData(buffer, persistanceLogging);
			buffer.flip();
			if(BoardLoader.loadBufferIntoBoard(buffer, newBoard))
			{
				newBoard.copyMoveCounters(this);
				newBoard.myMoveLogger.setMovesMadeOffset(myMoveLogger.getMovesMade());
				newBoard.myStatisticsTracker = myStatisticsTracker;
			}
			else
			{
				BoardLoader.saveBoard(this, "faulty_boards/board_failed_to_copy_" + System.currentTimeMillis() + ChessFileFilter.FILE_ENDING);
				STDERR.error("Failed to copy board");
				newBoard = null;
			}
		}
		catch (IOException e)
		{
			newBoard = null;
		}

		return newBoard;
	}

	/**
	 * Copies the number of times the moves has been made from the given board
	 * @param chessBoard the board to copy the move counters from
	 */
	private void copyMoveCounters(final ChessBoard chessBoard)
	{
		for(Piece p : getPieces())
		{
			//Note that if the arrays differ only the common moves will be copied, the rest is ignored
			for(Move currentMove : p.getPossibleMoves())
			{
				Move fromMove = chessBoard.getMove(currentMove);
				if(fromMove != null)
				{
					currentMove.copyMoveCounter(fromMove);
				}
				/*else
				{
					LOGGER.warning("Failed to copy moves from: " + currentMove + ", didn't find a matching move for it in the given board");
				}*/
			}
		}
	}

	/**
	 * Resets the number of times the moves has been made to zero
	 */
	public void resetMoveCounters()
	{
		for(Piece p : getPieces())
		{
			for(Move m : p.getPossibleMoves())
			{
				m.resetMoveCounter();
			}
		}
	}

	/**
	 * @return the last move that was mode on this board
	 */
	public Move getLastMove()
	{
		return myMoveLogger.getLastMove();
	}

	/**
	 * Sets this board to it's initial state
	 */
	public void reset()
	{
		clear();
		setupWhitePieces();
		setupBlackPieces();
		setupCastlingMoves();

		//Set lists of all the possible moves for all of the pieces
		setPossibleMoves();
		myCurrentPlayer = Piece.WHITE;

		for(ChessBoardListener listener : myBoardListeners)
		{
			listener.loadingOfBoardDone();
		}

		updateGameState();
	}

	public void setMovesThatStopsKingFromBeingChecked(final Set<Move> moves)
	{
		myMovesThatStopsKingFromBeingChecked = moves;
	}

	public Set<Move> getMovesThatStopsKingFromBeingChecked()
	{
		return myMovesThatStopsKingFromBeingChecked;
	}

	/*
	 * A king's move must check if the threatening moves also threatens the square behind the king
	 */
	public boolean isMoveUnavailableDueToCheck(final KingMove kingMove)
	{
		if(kingMove.getDestination() == null)
		{
			return false;
		}
		Collection<Move> movesBehindKing = getNonAvailableMoves(kingMove.getDestination(), !kingMove.getAffinity());
		for(Move behindMove : movesBehindKing)
		{
			if(behindMove instanceof DependantMove)
			{
				DependantMove move = ((DependantMove) behindMove).getMoveThatIDependUpon();
				if(move != null)
				{
					Position destinationForMove = move.getDestination();
					//Is the move this belongs to the one that is threatening the king?
					if(destinationForMove != null && destinationForMove.equals(kingMove.getCurrentPosition())
							&& getAvailableMoves(move.getDestination(),move.getAffinity()).contains(move))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean isMoveUnavailableDueToCheck(final Move move)
	{
		//This should only limit the current player and when he is under a check
		if(getCurrentState() != ChessState.CHECK || move.getAffinity() != getCurrentPlayer())
		{
			return false;
		}

		return !getMovesThatStopsKingFromBeingChecked().contains(move);
	}

	public void addChessBoardListener(final ChessBoardListener listener)
	{
		myBoardListeners.add(listener);
	}

	public King getCurrentKing()
	{
		if(myCurrentPlayer == Piece.BLACK)
		{
			return myBlackKing;
		}

		return myWhiteKing;
	}

	public ChessState getCurrentState()
	{
		return myCurrentGameState;
	}

	/**
	 * 
	 * @return true if black, false if white
	 */
	public boolean getCurrentPlayer()
	{
		return myCurrentPlayer;
	}

	/**
	 * 
	 * @return Black if it's blacks turn, White if it's whites turn
	 */
	public String getCurrentPlayerString()
	{
		return myCurrentPlayer ? "Black" : "White";
	}

	/**
	 * 
	 * @return the inverse of getCurrentPlayerString()
	 */
	public String getPreviousPlayerString()
	{
		return myCurrentPlayer ? "White" : "Black";
	}

	public void nextPlayer()
	{
		myCurrentPlayer = !myCurrentPlayer;

		for(Move m : ImmutableList.copyOf(myScheduledMoveUpdates))
		{
			m.updatePossibility(this, true);
			m.syncCountersWithBoard(this);
		}
		myScheduledMoveUpdates.clear();
		//TODO(jontejj): remove this when the caching of this has been confirmed
		/*for(Move m : getCurrentKing().getPossibleMoves())
		{
			m.updatePossibility(this, true);
			m.syncCountersWithBoard(this);
		}*/

		updateGameState();

		for(ChessBoardListener listener : myBoardListeners)
		{
			listener.nextPlayer();
		}
	}
	/**
	 * Performs a random move for the current player
	 * @throws NoMovesAvailableException if no move could be made
	 */
	public void performRandomMove() throws NoMovesAvailableException
	{
		/**
		 * Necessary check because {@link Move#canBeMade(ChessBoard)} doesn't check this (optimization)
		 */
		if(!ChessBoardEvaluator.inPlay(this))
		{
			throw new NoMovesAvailableException();
		}

		Set<Move> moves = getAvailableMoves(getCurrentPlayer());

		List<Move> shuffledMoves = Arrays.asList(moves.toArray(new Move[moves.size()]));
		Collections.shuffle(shuffledMoves);

		for(Move randomMove : shuffledMoves)
		{
			//Avoid making a move that's part of a castling move
			if(randomMove.shouldBeIncludedInMoveTable())
			{
				Piece piece = randomMove.getPiece();
				if(piece.performMove(randomMove, this))
				{
					return;
				}
			}
		}
		throw new NoMovesAvailableException();
	}

	public void addPiece(final Piece p, final boolean initializePossibleMoves, final boolean loadingInProgress)
	{
		if(p instanceof King)
		{
			King k = (King)p;
			if(k.isBlack())
			{
				myBlackKing = k;
			}
			else
			{
				myWhiteKing = k;
			}
		}
		ImmutablePosition currentPosition = p.getCurrentPosition();
		Piece oldPiece = getPositionContainer(currentPosition).setCurrentPiece(p);
		//TODO: why is there pieces in the position container that doesn't have the same position as the container?
		if(oldPiece != null & oldPiece != p && oldPiece.getCurrentPosition() == p.getCurrentPosition())
		{
			Error e = new DuplicatePieceError(oldPiece, p);
			BoardLoader.saveBoard(this, "faulty_boards/board_before_duplicate_piece_is_put_into_it_" + System.currentTimeMillis() + ChessFileFilter.FILE_ENDING);
			STDERR.error("", e);
			throw e;
		}
		addPieceToPositionMaps(p);
		if(initializePossibleMoves)
		{
			p.initilizePossibilityOfMoves(this);
		}

		notifyListenersAboutPiecePlacement(p, loadingInProgress);
	}

	public void addPieceToPositionMaps(final Piece piece)
	{
		if(myPieces.add(piece))
		{
			pieceValueChanged(piece.getValue(), piece.getAffinity());
		}
		myPieceToPositionAvailableMoves.put(piece, new HashMap<ImmutablePosition, Move>());
		myPieceToPositionNonAvailableMoves.put(piece, new HashMap<ImmutablePosition, Move>());
	}

	public void notifyListenersAboutPiecePlacement(final Piece placedPiece, final boolean loadingInProgress)
	{
		if(loadingInProgress)
		{
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.piecePlacedLoadingInProgress(placedPiece);
			}
		}
		else
		{
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.piecePlaced(placedPiece);
			}
		}
	}

	/**
	 * Construct and place the white pieces
	 */
	private void setupWhitePieces()
	{
		for(int column = 0; column < ChessBoard.BOARD_SIZE; column++)
		{
			addPiece(new WhitePawn(MutablePosition.from(WHITE_PAWN_ROW, column), this), false, true);
		}

		addPiece(new Knight(MutablePosition.from(WHITE_STARTING_ROW, B), WHITE, this), false, true);
		addPiece(new Knight(MutablePosition.from(WHITE_STARTING_ROW, G), WHITE, this), false, true);

		addPiece(new Rock(MutablePosition.from(WHITE_STARTING_ROW, A), WHITE, this), false, true);
		addPiece(new Rock(MutablePosition.from(WHITE_STARTING_ROW, H), WHITE, this), false, true);

		addPiece(new Queen(MutablePosition.from(WHITE_STARTING_ROW, D), WHITE, this), false, true);

		addPiece(new Bishop(MutablePosition.from(WHITE_STARTING_ROW, C), WHITE, this), false, true);
		addPiece(new Bishop(MutablePosition.from(WHITE_STARTING_ROW, F), WHITE, this), false, true);

		addPiece(new King(MutablePosition.from(WHITE_STARTING_ROW, E), WHITE, this), false, true);
	}

	/**
	 * Construct and place the black pieces
	 */
	private void setupBlackPieces()
	{
		for(Column column : Column.values())
		{
			addPiece(new BlackPawn(MutablePosition.from(BLACK_PAWN_ROW, column), this), false, true);
		}

		addPiece(new Knight(MutablePosition.from(BLACK_STARTING_ROW, B), BLACK, this), false, true);
		addPiece(new Knight(MutablePosition.from(BLACK_STARTING_ROW, G), BLACK, this), false, true);

		addPiece(new Rock(MutablePosition.from(BLACK_STARTING_ROW, A), BLACK, this), false, true);
		addPiece(new Rock(MutablePosition.from(BLACK_STARTING_ROW, H), BLACK, this), false, true);

		addPiece(new Queen(MutablePosition.from(BLACK_STARTING_ROW, D), BLACK, this), false, true);

		addPiece(new Bishop(MutablePosition.from(BLACK_STARTING_ROW, C), BLACK, this), false, true);
		addPiece(new Bishop(MutablePosition.from(BLACK_STARTING_ROW, F), BLACK, this), false, true);

		addPiece(new King(MutablePosition.from(BLACK_STARTING_ROW, E), BLACK, this), false, true);
	}

	private void setupCastlingMoves()
	{
		if(myBlackKing.isAtStartingPosition())
		{
			myBlackKing.setLeftRock(getPiece(ImmutablePosition.position(BLACK_STARTING_ROW , A)));
			myBlackKing.setRightRock(getPiece(ImmutablePosition.position(BLACK_STARTING_ROW, H)));
		}
		if(myWhiteKing.isAtStartingPosition())
		{
			myWhiteKing.setLeftRock(getPiece(ImmutablePosition.position(WHITE_STARTING_ROW , A)));
			myWhiteKing.setRightRock(getPiece(ImmutablePosition.position(WHITE_STARTING_ROW, H)));
		}
	}

	public void setPossibleMoves()
	{
		for(Piece p : getPieces())
		{
			p.initilizePossibilityOfMoves(this);
		}

		//The king's moves needs to be evaluated one more time as they rely on that all other moves have been updated
		myWhiteKing.initilizePossibilityOfMoves(this);
		myBlackKing.initilizePossibilityOfMoves(this);
	}

	public void removePiece(final Piece p)
	{
		ImmutablePosition currentPosition = p.getCurrentPosition();
		if(myPieces.remove(p))
		{
			pieceValueChanged(-p.getValue(), p.getAffinity());
		}
		getPositionContainer(currentPosition).setCurrentPiece(null);
		myPieceToPositionAvailableMoves.remove(p);
		myPieceToPositionNonAvailableMoves.remove(p);
		for(MoveListener ml : myMoveListeners)
		{
			ml.pieceRemoved(p);
		}
	}

	/**
	 * Removes the pawn from the board and replaces him with a new piece
	 * @param pawn the pawn to replace
	 * @return the piece that replaced the pawn
	 */
	public Piece promotePawn(final Piece pawn)
	{
		pawn.promote();
		//Remove pawn
		pawn.removeFromBoard(this);

		Piece newPiece = null;

		//Asks the GUI for a replacement piece
		for(ChessBoardListener cbl : myBoardListeners)
		{
			if(cbl.supportsPawnPromotionDialog())
			{
				newPiece = cbl.getPawnPromotionFromDialog();
				//TODO(jontejj): set position
				break;
			}
		}
		if(newPiece == null)
		{
			//Replace him with a Queen
			newPiece = new Queen(pawn.getPosition().copy(), pawn.getAffinity(), this);
		}
		addPiece(newPiece, true, false);
		return newPiece;
	}

	/**
	 * 
	 * @param position
	 * @return the {@link PositionContainer} object for the given position
	 * @throws NullPointerException if position is null
	 * @throws ArrayIndexOutOfBoundsException if position is an invalid position
	 */
	public PositionContainer getPositionContainer(final ImmutablePosition position)
	{
		return myPositions[position.getRow()][position.getColumn()];
	}

	public void removeAvailableMove(final ImmutablePosition pos, final Piece piece, final Move move)
	{
		if(pos != null)
		{
			if(getPositionContainer(pos).removeAvailableMove(move))
			{
				decrementAvailableMoves(piece.getAffinity());
				getAvailableMoves(piece.getAffinity()).remove(move);
			}
			if(move.shouldBeIncludedInMoveTable())
			{
				Map<ImmutablePosition, Move> map = myPieceToPositionAvailableMoves.get(piece);
				Move removedMove = map.remove(pos);
				if(removedMove != move && removedMove != null)
				{
					map.put(pos, removedMove);
				}
			}
			//Check if the opposite king previously couldn't move into this position, if so maybe he can now?
			King oppositeKing = getOppositeKing(piece.getAffinity());
			Move kingMove = myPieceToPositionNonAvailableMoves.get(oppositeKing).get(pos);
			if(kingMove != null)
			{
				scheduleMoveForUpdate(kingMove);
			}
		}
	}

	public void removeNonAvailableMove(final ImmutablePosition pos, final Piece piece, final Move move)
	{
		if(pos != null)
		{
			getPositionContainer(pos).removeNonAvailableMove(move);
			if(move.shouldBeIncludedInMoveTable())
			{
				Map<ImmutablePosition, Move> map = myPieceToPositionNonAvailableMoves.get(piece);
				Move removedMove = map.remove(pos);
				if(removedMove != move && removedMove != null)
				{
					map.put(pos, removedMove);
				}
			}
			//Check if the opposite king previously couldn't move into this position, if so then maybe he can now?
			King oppositeKing = getOppositeKing(piece.getAffinity());
			Move kingMove = myPieceToPositionNonAvailableMoves.get(oppositeKing).get(pos);
			if(kingMove != null)
			{
				scheduleMoveForUpdate(kingMove);
			}
		}
	}

	public void addAvailableMove(final ImmutablePosition pos, final Piece piece, final Move move)
	{
		if(pos != null)
		{
			if(getPositionContainer(pos).addAvailableMove(move))
			{
				incrementAvailableMoves(piece.getAffinity());
				getAvailableMoves(piece.getAffinity()).add(move);
			}
			if(move.shouldBeIncludedInMoveTable())
			{
				Map<ImmutablePosition, Move> map = myPieceToPositionAvailableMoves.get(piece);
				map.put(pos, move);
				//Check if the opposite king previously could move into this position, if so remove that move because now he can't
				King oppositeKing = getOppositeKing(piece.getAffinity());
				Move kingMove = getAvailableMove(oppositeKing, pos);
				if(kingMove != null)
				{
					scheduleMoveForUpdate(kingMove);
				}
			}
		}
	}

	public void addNonAvailableMove(final ImmutablePosition pos, final Piece piece, final Move move)
	{
		//Out of bounds moves aren't handled here
		if(pos != null)
		{
			getPositionContainer(pos).addNonAvailableMove(move);
			if(move.shouldBeIncludedInMoveTable())
			{
				Map<ImmutablePosition, Move> map = myPieceToPositionNonAvailableMoves.get(piece);
				map.put(pos, move);

				//Check if the opposite king previously couldn't move into this position, if so then maybe he can now?
				updateKingMovesForPosition(getOppositeKing(piece.getAffinity()), pos);
			}
		}
	}

	private void updateKingMovesForPosition(final King king, final ImmutablePosition position)
	{
		Move kingMove = myPieceToPositionAvailableMoves.get(king).get(position);
		if(kingMove != null)
		{
			scheduleMoveForUpdate(kingMove);
		}
		kingMove = myPieceToPositionNonAvailableMoves.get(king).get(position);
		if(kingMove != null)
		{
			scheduleMoveForUpdate(kingMove);
		}
	}

	private void incrementAvailableMoves(final boolean affinity)
	{
		if(affinity == BLACK)
		{
			myBlackAvailableMovesCount++;
		}
		else
		{
			myWhiteAvailableMovesCount++;
		}
	}

	private void decrementAvailableMoves(final boolean affinity)
	{
		if(affinity == BLACK)
		{
			myBlackAvailableMovesCount--;
		}
		else
		{
			myWhiteAvailableMovesCount--;
		}
	}

	/**
	 * Note this also runs canBeMade on the move before returning it
	 * @param p
	 * @param pos
	 * @return a move that the given piece can make to the given position
	 */
	public Move getAvailableMove(final Piece p, final Position pos)
	{
		Move availableMove = myPieceToPositionAvailableMoves.get(p).get(pos);
		if(availableMove != null && availableMove.canBeMade(this))
		{
			return availableMove;
		}
		return null;
	}

	/**
	 * Note this also runs canBeMade on the move before returning it
	 * @param p
	 * @param pos
	 * @return a move that the given piece can NOT make to the given position
	 */
	public Move getNonAvailableMove(final Piece p, final Position pos)
	{
		Move nonAvailableMove = myPieceToPositionNonAvailableMoves.get(p).get(pos);
		if(nonAvailableMove != null && !nonAvailableMove.canBeMade(this))
		{
			return nonAvailableMove;
		}
		return null;
	}

	public King getOppositeKing(final boolean affinity)
	{
		if(affinity == Piece.BLACK)
		{
			return myWhiteKing;
		}

		return myBlackKing;
	}


	public King getKing(final boolean affinity)
	{
		if(affinity == Piece.BLACK)
		{
			return myBlackKing;
		}

		return myWhiteKing;
	}

	public MoveLogger getMoveLogger()
	{
		return myMoveLogger;
	}

	/**
	 * Note that this may contain false positives as the game may be in check.
	 * @param affinity the affinity of the player's moves that should be returned
	 * @return the available moves for the given affinity
	 */
	public Set<Move> getAvailableMoves(final boolean affinity)
	{
		if(affinity == WHITE)
		{
			return myWhiteAvailableMoves;
		}
		return myBlackAvailableMoves;
	}


	/**
	 * Note this also runs canBeMade on the moves before returning them
	 * @return a collection of moves that the current player can make
	 */
	public Collection<Move> getAvailableMoves()
	{
		List<Move> actualMoves = Lists.newArrayList();
		for(Move m : getAvailableMoves(myCurrentPlayer))
		{
			if(m.canBeMade(this))
			{
				actualMoves.add(m);
			}
		}
		return actualMoves;
	}

	/**
	 * Note that this may contain false positives as the game may be in check.
	 * @param position the wanted position
	 * @param affinity the affinity of the player that should be able to move into the position
	 * @throws NullPointerException if position is null
	 */
	public Collection<Move> getAvailableMoves(final ImmutablePosition position, final boolean affinity)
	{
		return getPositionContainer(position).getAvailableMoves(affinity);
	}

	/**
	 * Note that this may return a false positive as the game may be in check.
	 * @param position the wanted position
	 * @param affinity the affinity of the player that should be able to move into the position
	 * @return the first available move for the given position and the given affinity
	 * @exception NoSuchElementException if no available move exists
	 * @throws NullPointerException if position is null
	 */
	public Move getAvailableMove(final ImmutablePosition position, final boolean affinity)
	{
		return getAvailableMoves(position, affinity).iterator().next();
	}

	/**
	 * Note because of performance issues this returns a modifiable map that you really shouldn't modify :)
	 * @param position
	 * @param affinity
	 * @throws NullPointerException if position is null
	 */
	public Set<Move> getNonAvailableMoves(final ImmutablePosition position, final boolean affinity)
	{
		return getPositionContainer(position).getNonAvailableMoves(affinity);
	}

	/**
	 * This method checks for a move that could reach the given position by the other player in one move
	 * @param position the position to check
	 * @param affinity the affinity of the threatening player
	 * @param pieceThatWonders
	 * @param passThroughKing true if it's possible to pass through the king(i.e false if it's a castling move as the rock is going to protect the king)
	 * @return a move if the player with the given affinity could move into position in one move, otherwise null
	 */
	public Move moveThreateningPosition(final ImmutablePosition position, final boolean affinity, final Piece pieceThatWonders, final boolean passThroughKing)
	{
		Collection<Move> moves = getAvailableMoves(position, affinity);

		//Pieces may be able to move into this position if a piece moves there so
		//we check all pieces that has a currently non possible move that leads to this position
		Collection<Move> possibleTakeOverMoves = getNonAvailableMoves(position, affinity);

		for(Move m : possibleTakeOverMoves)
		{
			if(!m.canBeTakeOverMove())
			{
				continue; //No threatening move
			}
			else if(m instanceof PawnTakeOverMove)
			{
				return m;
			}
			else if(passThroughKing && !m.isPieceBlockingMe(position, pieceThatWonders.getCurrentPosition()))
			{
				return m;
			}
		}

		//Select first move that would take this square over
		for(Move threateningMove : moves)
		{
			//A {pawn move | castling move} can't be made if there is something standing in this square and thus is it not threatening this square
			if(threateningMove.canBeTakeOverMove())
			{
				//We have found our first move that really is threatening this square
				return threateningMove;
			}
		}

		return null;
	}

	/**
	 * This method checks for a how many moves that could reach the given position by the other player in one move and take over a piece standing there
	 * @param position the position to check
	 * @param affinity the affinity of the threatening player
	 * @return a move if the player with the given affinity could move into position in one move, otherwise null
	 */
	public int getNumberOfMovesThreateningPosition(final ImmutablePosition position, final boolean affinity, final Piece pieceThatWonders)
	{
		int numberOfMoves = 0;
		Collection<Move> moves = getAvailableMoves(position, affinity);

		//Pieces may be able to move into this position if a piece moves there so
		//we check all pieces that has a currently non possible move that leads to this position
		Collection<Move> possibleTakeOverMoves = getNonAvailableMoves(position, affinity);
		for(Move m : possibleTakeOverMoves)
		{
			//Check for possibly threatening moves
			if(m instanceof PawnTakeOverMove)
			{
				numberOfMoves++;
			}
			if(m.canBeTakeOverMove() && !m.isPieceBlockingMe(position, pieceThatWonders.getCurrentPosition()))
			{
				numberOfMoves++;
			}
		}

		for(Move m : moves)
		{
			//A pawn move can't be made if there is something standing in this square and thus is it not threatening this square
			if(m.canBeTakeOverMove())
			{
				numberOfMoves++;
			}
		}

		return numberOfMoves;
	}

	/**
	 * @param atPosition
	 * @return the Piece that is at the position provided, returns null if the position is free
	 * @throws NullPointerException if atPosition is null
	 */
	public Piece getPiece(final ImmutablePosition atPosition)
	{
		return getPositionContainer(atPosition).getCurrentPiece();
	}

	/**
	 * Looks at the given move's properties and retrieves a move from this board that does the same
	 * @param from
	 * @return a move on this board that looks like the one given or null if no such move existed
	 */
	public Move getMove(final Move from)
	{
		Piece piece = getPiece(from.getCurrentPosition());
		if(piece != null)
		{
			return piece.getMove(from);
		}
		return null;
	}

	public Collection<Piece> getPieces()
	{
		return myPieces;
	}

	/**
	 * 
	 * @return the number of pieces on this board
	 */
	public int getTotalPieceCount()
	{
		return myPieces.size();
	}

	public boolean movePiece(final Piece pieceToMove, final Move moveToPerform)
	{
		ImmutablePosition newPosition = moveToPerform.getDestination();
		ImmutablePosition oldPosition = pieceToMove.getCurrentPosition();

		Piece assumedTakeOverPiece = moveToPerform.getPieceAtDestination();
		Piece currentPieceAtDestination = getPositionContainer(newPosition).getCurrentPiece();
		if(assumedTakeOverPiece != currentPieceAtDestination && !moveToPerform.isEnPassant())
		{
			//The move was out of sync, lets fix it
			//TODO: this wouldn't be needed if the moves were updated properly
			//STDOUT.info("Move out of sync, Old piece at destination:" + assumedTakeOverPiece +
			//		"Actual piece at destination:" + currentPieceAtDestination);
			moveToPerform.setPieceAtDestination(currentPieceAtDestination);
			moveToPerform.updatePossibility(this, false);
			if(!moveToPerform.canBeMade(this))
			{
				BoardLoader.saveBoard(this, "faulty_boards/board_with_move_thats_out_of_sync_" + System.currentTimeMillis() + ChessFileFilter.FILE_ENDING);
				STDOUT.info("Due to the move being out of sync it was thought to be available when in fact it wasn't. Faulty move: " + moveToPerform);
				return false;
			}
		}
		if(moveToPerform.getPieceAtDestination() != null)
		{
			//Take over is happening
			moveToPerform.getPieceAtDestination().removeFromBoard(this);
		}

		getPositionContainer(oldPosition).setCurrentPiece(null);
		getPositionContainer(newPosition).setCurrentPiece(pieceToMove);

		if(moveToPerform instanceof RevertingMove)
		{
			for(MoveListener ml : myMoveListeners)
			{
				ml.moveReverted((RevertingMove) moveToPerform);
			}
		}
		else
		{
			for(MoveListener ml : myMoveListeners)
			{
				ml.movePerformed(moveToPerform);
			}
		}
		return true;
	}

	public void move(final ImmutablePosition from, final ImmutablePosition to) throws UnavailableMoveItem
	{
		Piece piece = getPiece(from);
		if(piece == null)
		{
			throw new UnavailableMoveItem("Couldn't find a piece at " + from, from, to);
		}
		Move move = getAvailableMove(piece, to);
		if(move == null)
		{
			throw new UnavailableMoveItem("Couldn't find a move to " + to + " for the piece " + piece, from, to);
		}
		if(!piece.performMove(move, this, false))
		{
			throw new UnavailableMoveItem("Couldn't perform: " + move, from, to);
		}
	}

	/**
	 * 
	 * @param from a string like "1E"
	 * @param to a string like "1G"
	 * @throws UnavailableMoveException when the move isn't available even if the caching mechanism thought it was
	 * @throws UnavailableMoveItem if the caching mechanism didn't consider the move possible
	 */
	public void move(final String from, final String to) throws UnavailableMoveItem
	{
		move(ImmutablePosition.position(from), ImmutablePosition.position(to));
	}

	/**
	 * This method should be called every time a position has been taken over by a piece or when a piece has been removed from this position
	 * @param position the position that has been changed
	 */
	public void updatePossibilityOfMovesForPosition(final ImmutablePosition position)
	{
		if(position != null)
		{
			getPositionContainer(position).updatePossibiltyForSetOfMoves();
		}
	}

	public void updateGameState()
	{
		ChessState oldState = myCurrentGameState;
		ChessState newState = ChessBoardEvaluator.getState(this);
		if(!newState.equals(oldState))
		{
			myCurrentGameState = newState;
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.gameStateChanged(newState);
			}
		}
	}

	/**
	 * Removes all the pieces from the board
	 */
	public void clear()
	{
		myBlackAvailableMovesCount = 0;
		myWhiteAvailableMovesCount = 0;
		myWhitePieceValueCount = 0;
		myBlackPieceValueCount = 0;
		myPieces.clear();
		myWhiteAvailableMoves.clear();
		myBlackAvailableMoves.clear();

		setupPositionContainers();
		for(MoveListener ml :  myMoveListeners)
		{
			ml.reset();
		}
		myWhiteKing = null;
		myBlackKing = null;
	}

	/**
	 * 
	 * @return true if this board can save moves if not this will throw an IllegalStateException
	 * @throws IllegalStateException if persistence isn't activated
	 */
	private boolean checkPersistencePossibility()
	{
		if(myPersistenceLogger == null)
		{
			throw new IllegalStateException("ChessBoard without perstistance activated tried to read board with persistence activated. Programming error.");
		}
		return true;
	}

	public boolean hasPersistencePossibility()
	{
		return myPersistenceLogger != null;
	}

	/**
	 * 
	 * @param includeMoves true if moves is to be saved as well
	 * @return the number of bytes needed to save this board
	 */
	public int getPersistenceSize(final PersistanceLogging persistanceLogging)
	{
		if(persistanceLogging.usePersistanceLogging() && checkPersistencePossibility())
		{
			return myPersistenceLogger.getPersistenceSize();
		}
		return 1 + myPieces.size() * Piece.BYTES_PER_PIECE; //Settings byte + Pieces
	}

	/**
	 * First this writes the state of the game to the given stream and then
	 * it writes the position, affinity and type of each piece
	 * @param buffer the buffer to write to
	 * @throws IOException
	 */
	public void writePersistenceData(final ByteBuffer buffer, final PersistanceLogging persistanceLogging) throws IOException
	{
		if(persistanceLogging.usePersistanceLogging() && checkPersistencePossibility())
		{
			myPersistenceLogger.writeMoveHistory(buffer);
		}
		else
		{
			buffer.put(getGameStateSettingsByte(persistanceLogging));
			writePieces(buffer);
		}
	}

	public void writePieces(final ByteBuffer buffer)
	{
		for(Piece p : getPieces())
		{
			buffer.putShort(p.getPersistenceData());
		}
	}

	public byte getGameStateSettingsByte(final PersistanceLogging persistanceLogging)
	{
		byte settings = 0;
		if(myCurrentPlayer == BLACK)
		{
			settings |= BLACKS_TURN_BIT;
		}
		if(persistanceLogging.usePersistanceLogging())
		{
			settings |= READ_MOVE_HISTORY_BIT;
		}
		return settings;
	}

	/**
	 * This reads a setting byte, move history and piece information
	 * @param buffer the buffer to read from
	 * @throws InvalidBoardException if a King is missing
	 * @throws ArrayIndexOutOfBoundsException if a position for a piece is invalid
	 * @throws DuplicatePieceError if two pieces are found at the same position
	 */
	public void readPersistenceData(final ByteBuffer buffer) throws InvalidBoardException
	{
		//Read the state of the game
		readGameStatePersistenceData(buffer);

		//Read each piece from the buffer
		while(buffer.remaining() > 0)
		{
			addPiece(Piece.getPieceFromPersistenceData(buffer, this), false, true);
		}
		if(myWhiteKing == null || myBlackKing == null)
		{
			throw new InvalidBoardException();
		}
		setupCastlingMoves();
	}

	private void readGameStatePersistenceData(final ByteBuffer buffer)
	{
		byte settings = buffer.get();
		if(containBits(settings, BLACKS_TURN_BIT))
		{
			myCurrentPlayer = BLACK;
		}
		else
		{
			myCurrentPlayer = WHITE;
		}
		if(containBits(settings, READ_MOVE_HISTORY_BIT) && checkPersistencePossibility())
		{
			myPersistenceLogger.readMoveHistory(buffer);
		}
	}

	/**
	 * 
	 * @return a descriptive string of the status for this board (who's turn it is etc.)
	 */
	public String getStatusString()
	{
		String status = "";
		ChessState state = getCurrentState();
		switch(state)
		{
			case CHECK:
				status = "Check. ";
			case PLAYING:
				status += getCurrentPlayerString() + "s turn";
				break;
			case CHECKMATE:
				status = "Check mate. " + getPreviousPlayerString() + " won. ";
				break;
			case STALEMATE:
				status = "Stalemate! Draw. ";
				break;
		}

		if(getLastMove() != null)
		{
			status += " (Last Move: " + getLastMove().logMessageForLastMove() + ")";
		}
		return status;
	}

	/**
	 * Undo the given number of moves
	 * @param movesToUndo the number of moves to undo
	 * @return moves that could be reverted
	 */
	public int undoMoves(final int movesToUndo)
	{
		return undoMoves(movesToUndo, true);
	}
	/**
	 * Undo the given number of moves
	 * @param movesToUndo the number of moves to undo
	 * @return moves that could be reverted
	 * @throws NullPointerException no moves have been made
	 */
	public int undoMoves(final int movesToUndo, final boolean printOuts)
	{
		myAllowsMoves = false;
		int movesReverted = 0;
		while(movesReverted < movesToUndo)
		{
			Move lastMove = myMoveLogger.getLastMove();
			if(lastMove == null)
			{
				break;
			}
			if(!lastMove.getPiece().performMove(lastMove.getRevertingMove(), this, printOuts))
			{
				break;
			}
			if(!lastMove.isPartOfAnotherMove())
			{
				movesReverted++;
			}
		}
		Move lastMove = myMoveLogger.getLastMove();
		if(lastMove != null)
		{
			lastMove.onceAgainLastMoveThatWasMade(this);
		}
		myAllowsMoves = true;

		for(ChessBoardListener listener : myBoardListeners)
		{
			listener.undoDone();
		}

		return movesReverted;
	}

	/**
	 * If the given move was the last one to be made it is undone by this function
	 * @param moveToUndo the move to undo
	 * @return true if the move could be undone
	 * * @throws NullPointerException if moveToUndo is null
	 */
	public boolean undoMove(final Move moveToUndo, final boolean printOuts)
	{
		boolean wasUndone = false;
		boolean wasPartOfAnotherMove = false;

		//TODO: Mostly called because of it's side effect, not nice :)
		Move lastMove = getLastMove();
		if(lastMove == null)
		{
			return false;
		}

		myAllowsMoves = false;

		RevertingMove revertingMove = moveToUndo.getRevertingMove();
		if(revertingMove == lastMove.getRevertingMove() && moveToUndo.getPiece().performMove(revertingMove, this, printOuts))
		{
			if(revertingMove.isPartOfAnotherMove())
			{
				wasPartOfAnotherMove = true;
				wasUndone = (undoMoves(1, false) == 1);
			}
			else
			{
				wasUndone = true;
			}
		}

		myAllowsMoves = true;

		if(wasUndone && !wasPartOfAnotherMove)
		{
			lastMove = myMoveLogger.getLastMove();
			if(lastMove != null)
			{
				lastMove.onceAgainLastMoveThatWasMade(this);
			}
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.undoDone();
			}
		}

		return wasUndone;
	}

	/**
	 * @return false if no automatic moves is allowed
	 */
	public boolean allowsMoves()
	{
		return myAllowsMoves;
	}

	public Move popLastMoveIfEqual(final Move moveToPop)
	{
		Move lastMove = getLastMove();
		if(lastMove == moveToPop)
		{
			return myMoveLogger.popMove();
		}

		return null;
	}

	public void decreaseProtectedPiecesCounter(final boolean affinity, final int decrementValue)
	{
		if(affinity == BLACK)
		{
			myBlackProtectedPiecesCount -= decrementValue;
		}
		else
		{
			myWhiteProtectedPiecesCount -= decrementValue;
		}
	}

	public void increaseProtectedPiecesCounter(final boolean affinity, final int incrementValue)
	{
		if(affinity == BLACK)
		{
			myBlackProtectedPiecesCount += incrementValue;
		}
		else
		{
			myWhiteProtectedPiecesCount += incrementValue;
		}
	}

	public long getProtectedPiecesCount(final boolean affinity)
	{
		if(affinity == BLACK)
		{
			return myBlackProtectedPiecesCount;
		}

		return myWhiteProtectedPiecesCount;
	}

	public void decreaseTakeOverPiecesCounter(final boolean affinity, final int decrementValue)
	{
		if(affinity == BLACK)
		{
			myBlackTakeOverPiecesCount -= decrementValue;
		}
		else
		{
			myWhiteTakeOverPiecesCount -= decrementValue;
		}
	}

	public void increaseTakeOverPiecesCounter(final boolean affinity, final int incrementValue)
	{
		if(affinity == BLACK)
		{
			myBlackTakeOverPiecesCount += incrementValue;
		}
		else
		{
			myWhiteTakeOverPiecesCount += incrementValue;
		}
	}

	public long getTakeOverPiecesCount(final boolean affinity)
	{
		if(affinity == BLACK)
		{
			return myBlackTakeOverPiecesCount;
		}

		return myWhiteTakeOverPiecesCount;
	}

	public short getAvailableMovesCount(final boolean affinity)
	{
		if(affinity == BLACK)
		{
			return myBlackAvailableMovesCount;
		}
		return myWhiteAvailableMovesCount;
	}

	public long getMeasuredStatusForPlayer(final boolean affinity)
	{
		int playerNrOfAvailableMoves = getAvailableMovesCount(affinity);
		//int playerNrOfNonAvailableMoves = getNonAvailableMoves(affinity).size();
		long playerProtectiveMoves = getProtectedPiecesCount(affinity);
		long playerTakeOverCount = getTakeOverPiecesCount(affinity);
		long totalPieceValue = getTotalPieceValueForAffinity(affinity);
		//Counts the available moves for the king
		int kingMobility = this.getKing(affinity).getAvailableMoves().size() * KING_MOBILITY_FACTOR;
		return playerNrOfAvailableMoves + playerProtectiveMoves + playerTakeOverCount + totalPieceValue + kingMobility;
	}

	private long getTotalPieceValueForAffinity(final boolean affinity)
	{
		if(affinity == BLACK)
		{
			return myBlackPieceValueCount;
		}
		return myWhitePieceValueCount;
	}

	public int getDifficulty()
	{
		return myDifficulty;
	}

	public void applyMoveHistory() throws UnavailableMoveItem
	{
		myAllowsMoves = false;
		if(myPersistenceLogger == null)
		{
			myAllowsMoves = true;
			return;
		}
		myPersistenceLogger.applyMoveHistory(this);
		myAllowsMoves = true;
	}

	/**
	 * Called when pieces have been placed in order for the persistence logger to know from what to start it's logging from
	 */
	public void updatePersistenceLogger()
	{
		if(myPersistenceLogger == null)
		{
			return;
		}
		myPersistenceLogger.setStartBoard(this);
	}

	public void pieceValueChanged(final int valueChange, final boolean affinity)
	{
		if(affinity == BLACK)
		{
			myBlackPieceValueCount += valueChange;
		}
		else
		{
			myWhitePieceValueCount += valueChange;
		}
	}
}
