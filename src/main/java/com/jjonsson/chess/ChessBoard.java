package com.jjonsson.chess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.common.primitives.Shorts;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.DuplicatePieceException;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.listeners.ChessBoardListener;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.persistance.MoveLogger;
import com.jjonsson.chess.pieces.Bishop;
import com.jjonsson.chess.pieces.BlackPawn;
import com.jjonsson.chess.pieces.Knight;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.pieces.WhitePawn;

import static com.jjonsson.utilities.Logger.LOGGER;
import static com.jjonsson.chess.pieces.Piece.*;
import static com.jjonsson.chess.moves.Position.*;

public class ChessBoard implements Cloneable
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
	
	private Map<Position, Piece> myWhitePieces;
	private Map<Position, Piece> myBlackPieces;
	/**
	 * A map that keeps track of every position reachable by a move by the black player
	 * To each possible position there is a sorted map (sorted by the pieces value) of pieces 
	 * 	and the move that would reach this position
	 */
	private Multimap<Position, Move> myBlackAvailableMoves;
	/**
	 * A map that keeps track of every position reachable by a move by the white player
	 * To each possible position there is a sorted map (sorted by the pieces value) of pieces 
	 * 	and the move that would reach this position
	 */
	private Multimap<Position, Move> myWhiteAvailableMoves;
	/**
	 * A map that keeps track of every move by the black player that isn't available right now
	 * To each possible position there is a sorted map (by the pieces value) of pieces 
	 * 	and the move that would reach this position
	 */
	private Multimap<Position, Move> myBlackNonAvailableMoves;
	private Multimap<Position, Move> myWhiteNonAvailableMoves;
	
	Map<Piece, Map<Position, Move>> myPieceToPositionAvailableMoves;
	Map<Piece, Map<Position, Move>> myPieceToPositionNonAvailableMoves;
	
	private boolean myCurrentPlayer;
	
	private Set<ChessBoardListener> myBoardListeners;
	private MoveLogger myMoveLogger;

	private King	myBlackKing;

	private King	myWhiteKing;

	private ChessState	myCurrentGameState;
	private Set<Move> myMovesThatStopsKingFromBeingChecked;
	
	//Manages the possibility of automatic moves (used during move reverting)
	private boolean myAllowsMoves;
	
	private ChessBoard myOriginatingBoard;
	
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
	 * The difficulty the user has chosen (Good values are 1-5) defaults to {@link ChessBoard.DEFAULT_DIFFICULTY}
	 */
	private int	myDifficulty;

	/**
	 * Constructs the chess board 
	 * @param placeInitialPieces if true, all the pieces is set to their default locations
	 */
	public ChessBoard(boolean placeInitialPieces)
	{
		myDifficulty = DEFAULT_DIFFICULTY;
		myOriginatingBoard = this;
		myAllowsMoves = true;
		myMovesThatStopsKingFromBeingChecked = ImmutableSet.of();
		myBoardListeners = Sets.newHashSet();
		myMoveLogger = new MoveLogger();
		addChessBoardListener(myMoveLogger);
		
		myBlackPieces = Maps.newHashMap();
		myBlackAvailableMoves = HashMultimap.create();
		myBlackNonAvailableMoves = HashMultimap.create();
		
		myWhitePieces = Maps.newHashMap();
		myWhiteAvailableMoves = HashMultimap.create();
		myWhiteNonAvailableMoves = HashMultimap.create();
		
		myPieceToPositionAvailableMoves = Maps.newHashMap();
		myPieceToPositionNonAvailableMoves = Maps.newHashMap();
		
		if(placeInitialPieces)
		{
			this.reset();
		}
	}
	
	public void setDifficulty(int newDifficulty)
	{
		myDifficulty = newDifficulty;
	}
	
	private void setOriginatingBoard(ChessBoard board)
	{
		myOriginatingBoard = board;
	}
	
	/**
	 * Makes a copy of the board without copying the listeners
	 * @return the new board or null if the copy failed
	 */
	@Override
	public ChessBoard clone() throws CloneNotSupportedException
	{
		ChessBoard newBoard = new ChessBoard(false);
		newBoard.setOriginatingBoard(this);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
		try
		{
			writePersistanceData(baos);
			newBoard.readPersistanceData(new ByteArrayInputStream(baos.toByteArray()));
			newBoard.setPossibleMoves();
			newBoard.updateGameState();
			newBoard.copyMoveCounters(this);
		}
		catch (IOException e)
		{
			newBoard = null;
		}
		catch (InvalidPosition e)
		{
			newBoard = null;
		}
		catch (DuplicatePieceException e)
		{
			LOGGER.warning("Got a duplicate piece: " + e.getDuplicatePiece() + ", conflicted with: " + e.getExistingPiece());
			newBoard = null;
		}
		return newBoard;
	}
	
	/**
	 * Copies the number of times the moves has been made from the given board
	 * @param chessBoard the board to copy the move counters from
	 */
	private void copyMoveCounters(ChessBoard chessBoard)
	{
		for(Piece p : getPieces())
		{
			List<Move> toMoves = p.getPossibleMoves();
			Piece originalPiece = chessBoard.getPiece(p.getCurrentPosition());
			List<Move> fromMoves = originalPiece.getPossibleMoves();
			
			//Note that if the arrays differ only the common moves will be copied, the rest is ignored
			int endIndex = Math.min(fromMoves.size(), toMoves.size());
			for(int i = 0;i<endIndex;i++)
			{
				toMoves.get(i).copyMoveCounter(fromMoves.get(i));
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

	public ChessBoard getOriginatingBoard()
	{
		return myOriginatingBoard;
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
	public final void reset()
	{
		clear();
		setupWhitePieces();
		setupBlackPieces();
		
		//Set lists of all the possible moves for all of the pieces
		setPossibleMoves();
		myCurrentPlayer = Piece.WHITE;
		
		for(ChessBoardListener listener : myBoardListeners)
		{
			listener.loadingOfBoardDone();	
		}
		
		updateGameState();
	}
	
	public void setMovesThatStopsKingFromBeingChecked(Set<Move> moves)
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
	public boolean isMoveUnavailableDueToCheck(KingMove kingMove)
	{
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
					if(destinationForMove != null && destinationForMove.equals(kingMove.getCurrentPosition()))
					{
						if(getAvailableMoves(move.getDestination(),move.getAffinity()).contains(move))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	public boolean isMoveUnavailableDueToCheck(Move move)
	{
		//This should only limit the current player and when he is under a check
		if(getCurrentState() != ChessState.CHECK || move.getAffinity() != getCurrentPlayer())
		{
			return false;
		}
		
		return !getMovesThatStopsKingFromBeingChecked().contains(move);
	}
	
	public final void addChessBoardListener(ChessBoardListener listener)
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
		//TODO(jontejj): could this be cached?
		for(Move m : getKing(myCurrentPlayer).getPossibleMoves())
		{
			m.updateMove(this);
		}
		updateGameState();
		
		for(ChessBoardListener listener : myBoardListeners)
		{
			listener.nextPlayer();
		}
	}
	/**
	 * Performs a random move for the current player
	 */
	public void performRandomMove() throws NoMovesAvailableException
	{
		if(!ChessBoardEvaluator.inPlay(this))
		{
			throw new NoMovesAvailableException();
		}
		
		Multimap<Position, Move> availableMoves = getAvailableMoves(getCurrentPlayer());
		
		
		List<Move> shuffledMoves = Lists.newArrayList(availableMoves.values());
		Collections.shuffle(shuffledMoves);
		
		try
		{
			for(Move randomMove : shuffledMoves)
			{
				Piece piece = randomMove.getPiece();
				if(piece == null)
				{
					throw new NoMovesAvailableException();
				}
				if(randomMove.canBeMade(this))
				{
					piece.performMove(randomMove, this);
					break;
				}
			}
		}
		catch(UnavailableMoveException ume)
		{
			throw new NoMovesAvailableException(ume);
		}
	}
	
	public void addPiece(Piece p, boolean initializePossibleMoves, boolean loadingInProgress)
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
		getMapForAffinity(p.getAffinity()).put(p.getCurrentPosition(), p);
		myPieceToPositionAvailableMoves.put(p, new HashMap<Position, Move>());
		myPieceToPositionNonAvailableMoves.put(p, new HashMap<Position, Move>());
		if(initializePossibleMoves)
		{
			p.initilizePossibilityOfMoves(this);
		}
		
		if(loadingInProgress)
		{
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.piecePlacedLoadingInProgress(p);
			}
		}
		else
		{
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.piecePlaced(p);	
			}
		}
	}
	
	/**
	 * Construct and place the white pieces
	 */
	private void setupWhitePieces()
	{	
		try
		{
			for(int column = 1; column <= ChessBoard.BOARD_SIZE; column++)
			{
				addPiece(new WhitePawn(createPosition(WHITE_PAWN_ROW, column), this), false, true);
			}
			
			addPiece(new Knight(createPosition(WHITE_STARTING_ROW, B), WHITE, this), false, true);
			addPiece(new Knight(createPosition(WHITE_STARTING_ROW, G), WHITE, this), false, true);
			
			Rock leftRock = new Rock(createPosition(WHITE_STARTING_ROW, A), WHITE, this);
			addPiece(leftRock, false, true);
			Rock rightRock = new Rock(createPosition(WHITE_STARTING_ROW, H), WHITE, this);
			addPiece(rightRock, false, true);
			
			addPiece(new Queen(createPosition(WHITE_STARTING_ROW, D), WHITE, this), false, true);
			addPiece(new Bishop(createPosition(WHITE_STARTING_ROW, C), WHITE, this), false, true);
			addPiece(new Bishop(createPosition(WHITE_STARTING_ROW, F), WHITE, this), false, true);
			
			addPiece(new King(createPosition(WHITE_STARTING_ROW, E), WHITE, this), false, true);
			
			myWhiteKing.getKingSideCastlingMove().setRock(rightRock);
			myWhiteKing.getQueenSideCastlingMove().setRock(leftRock);
		}
		catch(InvalidPosition ip)
		{
			throw new UnsupportedOperationException("Wrong with board setup", ip);
		}
	}
	
	/**
	 * Construct and place the black pieces
	 */
	private void setupBlackPieces()
	{
		try
		{
			for(int column = 1; column <= ChessBoard.BOARD_SIZE; column++)
			{
				addPiece(new BlackPawn(createPosition(BLACK_PAWN_ROW, column), this), false, true);
			}
			
			addPiece(new Knight(createPosition(BLACK_STARTING_ROW, B), BLACK, this), false, true);
			addPiece(new Knight(createPosition(BLACK_STARTING_ROW, G), BLACK, this), false, true);
			
			Rock leftRock = new Rock(createPosition(BLACK_STARTING_ROW, A), BLACK, this);
			addPiece(leftRock, false, true);
			
			Rock rightRock = new Rock(createPosition(BLACK_STARTING_ROW, H), BLACK, this);
			addPiece(rightRock, false, true);
			
			addPiece(new Queen(createPosition(BLACK_STARTING_ROW, D), BLACK, this), false, true);
			addPiece(new Bishop(createPosition(BLACK_STARTING_ROW, C), BLACK, this), false, true);
			addPiece(new Bishop(createPosition(BLACK_STARTING_ROW, F), BLACK, this), false, true);
			
			addPiece(new King(createPosition(BLACK_STARTING_ROW, E), BLACK, this), false, true);
			
			myBlackKing.getKingSideCastlingMove().setRock(rightRock);
			myBlackKing.getQueenSideCastlingMove().setRock(leftRock);
		}
		catch(InvalidPosition ip)
		{
			throw new UnsupportedOperationException("Wrong with board setup", ip);
		}
	}
	
	private void setupCastlingMoves()
	{
		try
		{
			if(myBlackKing.isAtStartingPosition())
			{
				Piece blackLeftRock = getPiece(createPosition(BLACK_STARTING_ROW, A));
				Piece blackRightRock = getPiece(createPosition(BLACK_STARTING_ROW, H));
				myBlackKing.getQueenSideCastlingMove().setRock(blackLeftRock);
				myBlackKing.getKingSideCastlingMove().setRock(blackRightRock);
			}
			if(myWhiteKing.isAtStartingPosition())
			{
				Piece whiteLeftRock = getPiece(createPosition(WHITE_STARTING_ROW, A));
				Piece whiteRightRock = getPiece(createPosition(WHITE_STARTING_ROW, H));
				myWhiteKing.getQueenSideCastlingMove().setRock(whiteLeftRock);
				myWhiteKing.getKingSideCastlingMove().setRock(whiteRightRock);
			}
		}
		catch (InvalidPosition e)
		{
			LOGGER.warning("Something wrong with board setup, got " + e);
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
	
	public void removePiece(Piece p)
	{
		getMapForAffinity(p.getAffinity()).remove(p.getCurrentPosition());
		myMoveLogger.pieceRemoved(p);
	}
	
	/**
	 * Removes the pawn from the board and replaces him with a new piece
	 * @param pawn the pawn to replace
	 * @return the piece that replaced the pawn
	 */
	public Piece replacePawn(Piece pawn)
	{
		//Remove pawn
		pawn.removeFromBoard(this);
		
		Piece newPiece = null;
		
		//Asks the GUI for a replacement piece
		for(ChessBoardListener cbl : myBoardListeners)
		{
			if(cbl.supportsPawnReplacementDialog())
			{
				newPiece = cbl.getPawnReplacementFromDialog();
				//TODO(jontejj): set position
				break;
			}
		}
		if(newPiece == null)
		{
			//Replace him with a Queen
			newPiece = new Queen(pawn.getCurrentPosition(), pawn.getAffinity(), this);
		}
		newPiece.setIsPawnReplacementPiece();
		
		addPiece(newPiece, true, false);
		
		//Update moves that can reach the queen
		this.updatePossibilityOfMovesForPosition(newPiece.getCurrentPosition());
		
		return newPiece;
	}
	
	public void removeAvailableMove(Position pos, Piece piece, Move move)
	{
		Multimap<Position, Move> availableMoves = getAvailableMoves(piece.getAffinity());
		availableMoves.remove(pos, move);
		myPieceToPositionAvailableMoves.get(piece).remove(pos);
	}

	public void removeNonAvailableMove(Position pos, Piece piece, Move move)
	{
		Multimap<Position, Move> nonAvailableMoves = getNonAvailableMoves(piece.getAffinity());
		nonAvailableMoves.remove(pos, move);
		myPieceToPositionNonAvailableMoves.get(piece).remove(pos);
	}
	
	public void addNonAvailableMove(Position pos, Piece piece, Move move)
	{
		//Out of bounds moves aren't handled here
		if(pos != null)
		{	
			Multimap<Position, Move> nonAvailableMoves = getNonAvailableMoves(piece.getAffinity());
			nonAvailableMoves.put(pos, move);
			myPieceToPositionNonAvailableMoves.get(piece).put(pos, move);
			
			//Check if the opposite king previously couldn't move into this position, if so then maybe he can now?
			if(!(piece instanceof King))
			{
				King oppositeKing = getOppositeKing(piece.getAffinity());
				Move kingMove = getNonAvailableMove(oppositeKing, pos);
				if(kingMove != null)
				{
					kingMove.updateMove(this);
				}
			}
		}
	}
	
	public void addAvailableMove(Position pos, Piece piece, Move move)
	{	
		Multimap<Position, Move> availableMoves = getAvailableMoves(piece.getAffinity());
		availableMoves.put(pos, move);
		myPieceToPositionAvailableMoves.get(piece).put(pos, move);
		
		//Check if the opposite king previously could move into this position, if so remove that move because now he can't
		King oppositeKing = getOppositeKing(piece.getAffinity());
		Move kingMove = getAvailableMove(oppositeKing, pos);
		if(kingMove != null)
		{
			kingMove.updateMove(this);
		}
	}
	
	/**
	 * Note this also runs canBeMade on the move before returning it
	 * @param p
	 * @param pos
	 * @return a move that the given piece can make to the given position
	 */
	public Move getAvailableMove(Piece p, Position pos)
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
	public Move getNonAvailableMove(Piece p, Position pos)
	{
		Move nonAvailableMove = myPieceToPositionNonAvailableMoves.get(p).get(pos);
		if(nonAvailableMove != null && !nonAvailableMove.canBeMade(this))
		{
			return nonAvailableMove;
		}
		return null;
	}
	
	public King getOppositeKing(boolean affinity)
	{
		if(affinity == Piece.BLACK)
		{
			return myWhiteKing;
		}
		
		return myBlackKing;
	}
	

	public King getKing(boolean affinity)
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
	
	private Map<Position, Piece> getMapForAffinity(boolean affinity)
	{
		if(affinity == Piece.BLACK)
		{
			return myBlackPieces;
		}
		
		return myWhitePieces;	
	}
	
	public Collection<Piece> getPiecesForAffinity(boolean affinity)
	{
		if(affinity == Piece.BLACK)
		{
			return myBlackPieces.values();
		}
		
		return myWhitePieces.values();	
	}
	
	/**
	 * Note that this may contain false positives as the game may be in check.
	 * @param affinity the affinity of the player's moves that should be returned
	 * @return the available moves for the given affinity
	 */
	public Multimap<Position, Move> getAvailableMoves(boolean affinity)
	{
		if(affinity == WHITE)
		{
			return myWhiteAvailableMoves;
		}
		
		return myBlackAvailableMoves;	
	}
	
	/**
	 * Note that this may contain false positives as the game may be in check.
	 * @param position the wanted position
	 * @param affinity the affinity of the player that should be able to move into the position
	 */
	public Collection<Move> getAvailableMoves(Position position, boolean affinity)
	{
		Collection<Move> moves = null;
		if(affinity == WHITE)
		{
			moves = myWhiteAvailableMoves.get(position);
		}
		else
		{
			moves = myBlackAvailableMoves.get(position);
		}
		return moves;	
	}
	
	/**
	 * Note that this may return a false positive as the game may be in check.
	 * @param position the wanted position
	 * @param affinity the affinity of the player that should be able to move into the position
	 * @return the first available move for the given position and the given affinity
	 * @throws NoSuchElementException if no move is available
	 */
	public Move getAvailableMove(Position position, boolean affinity)
	{
		return getAvailableMoves(position, affinity).iterator().next();	
	}
	
	public Multimap<Position, Move> getNonAvailableMoves(boolean affinity)
	{
		Multimap<Position, Move> moves = null;
		if(affinity == WHITE)
		{
			moves = myWhiteNonAvailableMoves;
		}
		else
		{
			moves = myBlackNonAvailableMoves;
		}
		
		return moves;	
	}

	/**
	 * Note because of performance issues this returns a modifiable map that you really shouldn't modify :)
	 * @param position
	 * @param affinity
	 */
	public Collection<Move> getNonAvailableMoves(Position position, boolean affinity)
	{
		Collection<Move> moves = null;
		if(affinity == BLACK)
		{
			moves  = myBlackNonAvailableMoves.get(position);
		}
		else
		{
			moves = myWhiteNonAvailableMoves.get(position);
		}
		return moves;	
	}
	
	/**
	 * This method checks for a move that could reach the given position by the other player in one move
	 * @param position the position to check
	 * @param affinity the affinity of the threatening player
	 * @param pieceThatWonders
	 * @param passThroughKing true if it's possible to pass through the king(i.e false if it's a castling move as the rock is going to protect the king)
	 * @return a move if the player with the given affinity could move into position in one move, otherwise null
	 */
	public Move moveThreateningPosition(Position position, boolean affinity, Piece pieceThatWonders, boolean passThroughKing)
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
	public int getNumberOfMovesThreateningPosition(Position position, boolean affinity, Piece pieceThatWonders)
	{
		int numberOfMoves = 0;
		Collection<Move> moves = getAvailableMoves(position, affinity);
		
		//Pieces may be able to move into this position if a piece moves there so 
		//we check all pieces that has a currently non possible move that leads to this position
		Collection<Move> possibleTakeOverMoves = getNonAvailableMoves(position, affinity);
		for(Move m : possibleTakeOverMoves)
		{
			//No threatening move
			if(!(m instanceof PawnMove))
			{
				if(m instanceof PawnTakeOverMove)
				{
					numberOfMoves++;
				}
				else if(!m.isPieceBlockingMe(position, pieceThatWonders.getCurrentPosition()))
				{
					numberOfMoves++;
				}
			}
		}
		
		for(Move m : moves)
		{
			//A pawn move can't be made if there is something standing in this square and thus is it not threatening this square
			if(!(m instanceof PawnMove))
			{
				numberOfMoves++;
			}
		}
		
		return numberOfMoves;
	}

	/**
	 * @param atPosition
	 * @return the Piece that is at the position provided, returns null if the position is free
	 */
	public Piece getPiece(Position atPosition)
	{
		Piece whitePiece = myWhitePieces.get(atPosition);
		if(whitePiece != null)
		{
			return whitePiece;
		}
		
		return myBlackPieces.get(atPosition);
	}
	
	/**
	 * Looks at the given move's properties and retrieves a move from this board that does the same
	 * @param from
	 * @return a move on this board that looks like the one given
	 * @throws UnavailableMoveException if no such move could be found on this board
	 */
	public Move getMove(Move from) throws UnavailableMoveException
	{
		Piece piece = getPiece(from.getCurrentPosition());
		if(piece != null)
		{
			return piece.getMove(from);
		}
		throw new UnavailableMoveException(from);
	}
	
	public List<Piece> getPieces()
	{
		List<Piece> list = Lists.newArrayList();
		list.addAll(myBlackPieces.values());
		list.addAll(myWhitePieces.values());
		return list;
	}
	
	/**
	 * 
	 * @return the number of pieces on this board
	 */
	public int getTotalPieceCount()
	{
		return myBlackPieces.size() + myWhitePieces.size();
	}
	
	public void movePiece(Piece pieceToMove, Move moveToPerform)
	{
		Position newPosition = moveToPerform.getDestination();
		Position oldPosition = pieceToMove.getCurrentPosition();
		Map<Position, Piece> map = getMapForAffinity(pieceToMove.getAffinity());
		map.remove(oldPosition);
		map.put(newPosition, pieceToMove);
		
		myMoveLogger.movePerformed(moveToPerform);
	}

	/**
	 * This method should be called every time a position has been taken over by a piece or when a piece has been removed from this position
	 * @param position the position that has been changed
	 */
	public void updatePossibilityOfMovesForPosition(Position position)
	{
		ImmutableSet<Move> updatedMoves = ImmutableSet.of();
		updatedMoves = updatePossibiltyForMapOfMoves(myWhiteAvailableMoves, position, updatedMoves);
		updatePossibiltyForMapOfMoves(myWhiteNonAvailableMoves, position, updatedMoves);
		updatedMoves = ImmutableSet.of();
		updatedMoves = updatePossibiltyForMapOfMoves(myBlackAvailableMoves, position, updatedMoves);
		updatePossibiltyForMapOfMoves(myBlackNonAvailableMoves, position, updatedMoves);
	}
	
	/**
	 * 
	 * @param moves
	 * @param pos
	 * @param exclusionMap a map of moves that won't need an update
	 * @return a map of the moves that was updated
	 */
	private ImmutableSet<Move> updatePossibiltyForMapOfMoves(Multimap<Position, Move> moves, Position pos, ImmutableSet<Move> exclusionMap)
	{
		//As the move may be removed during this iteration will need a shallow copy of the move map
		ImmutableSet<Move> copy = ImmutableSet.copyOf(moves.get(pos));
		for(Move m : copy)
		{
			if(!exclusionMap.contains(m))
			{
				m.updateMove(this);
			}
		}
		return copy;
	}

	public void updateGameState()
	{
		ChessState oldState = myCurrentGameState;
		ChessState newState = ChessBoardEvaluator.getState(this);
		if(newState != oldState)
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
	public final void clear()
	{
		myWhitePieces.clear();
		myWhiteAvailableMoves.clear();
		myWhiteNonAvailableMoves.clear();
		myBlackPieces.clear();
		myBlackAvailableMoves.clear();
		myBlackNonAvailableMoves.clear();
		myMoveLogger.clear();
		myWhiteKing = null;
		myBlackKing = null;
	}

	/**
	 * First this writes the state of the game to the given stream and then 
	 * it writes the position, affinity and type of each piece
	 * @param stream the stream to write to
	 * @throws IOException
	 */
	public void writePersistanceData(OutputStream stream) throws IOException
	{
		stream.write(getGameStatePersistanceData());
		for(Piece p : getPieces())
		{
			stream.write(Shorts.toByteArray(p.getPersistanceData()));
		}
	}
	
	public void readPersistanceData(InputStream stream) throws IOException, InvalidPosition, DuplicatePieceException
	{
		//Read the state of the game
		readGameStatePersistanceData(stream);
		//Read each piece from the file
		byte[] pieceBytes = new byte[2];
		int readBytes = 0;
		while(readBytes != -1)
		{
			readBytes = stream.read(pieceBytes);
			if(readBytes == 2)
			{
				short persistanceData = Shorts.fromByteArray(pieceBytes);
				Piece piece = Piece.getPieceFromPersistanceData(persistanceData, this);
				if(piece != null)
				{
					Piece existingPiece = this.getPiece(piece.getCurrentPosition());
					if(existingPiece != null && existingPiece.getPersistanceData() != persistanceData)
					{
						//Don't place a piece where one is already placed
						throw new DuplicatePieceException(existingPiece, piece);
					}
					addPiece(piece, false, true);
				}
			}
		}
		setupCastlingMoves();
	}
	
	private byte[] getGameStatePersistanceData()
	{
		byte[] settings = new byte[1];
		
		//Left most bit tell us the current player
		if(myCurrentPlayer == BLACK)
		{
			settings[0] = (byte) (1 << 7);
		}
		
		return settings;
	}
	
	private void readGameStatePersistanceData(InputStream stream) throws IOException
	{
		byte[] settings = new byte[1];
		int readBytes = stream.read(settings);
		if(readBytes == 1)
		{
			//Left most bit tell us the current player
			if((settings[0] & 0x80) == 0x80)
			{
				myCurrentPlayer = BLACK;
			}
			else
			{
				myCurrentPlayer = WHITE;
			}
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
	public int undoMoves(int movesToUndo)
	{
		return undoMoves(movesToUndo, true);
	}
	/**
	 * Undo the given number of moves
	 * @param movesToUndo the number of moves to undo
	 * @return moves that could be reverted
	 */
	public int undoMoves(int movesToUndo, boolean printOuts)
	{
		myAllowsMoves = false;
		int movesReverted = 0;
		while(movesReverted < movesToUndo)
		{
			try
			{
				Move lastMove = myMoveLogger.getLastMove();
				lastMove.getPiece().performMove(lastMove.getRevertingMove(), this, printOuts);
				if(!lastMove.isPartOfAnotherMove())
				{
					movesReverted++;
				}
			}
			catch (Exception e)
			{
				break;
			}		
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
	 */
	public boolean undoMove(Move moveToUndo, boolean printOuts)
	{
		boolean wasUndone = true;
		boolean wasPartOfAnotherMove = false;
		
		myAllowsMoves = false;
		try
		{
			RevertingMove revertingMove = moveToUndo.getRevertingMove();
			moveToUndo.getPiece().performMove(revertingMove, this, printOuts);
			if(revertingMove.isPartOfAnotherMove())
			{
				wasPartOfAnotherMove = true;
				wasUndone = (undoMoves(1, false) == 1);
			}
		}
		catch (Exception e)
		{
			wasUndone = false;
		}		
		myAllowsMoves = true;
		
		if(wasUndone && !wasPartOfAnotherMove)
		{
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
	
	public void newMoveEvaluationHasBeenDone(ImmutableMap<Position, String> positionScores)
	{
		for(ChessBoardListener cbl : myBoardListeners)
		{
			cbl.squareScores(positionScores);
		}
	}

	public Move popLastMoveIfEqual(Move moveToPop)
	{
		Move lastMove = getLastMove();
		if(lastMove == moveToPop)
		{
			return myMoveLogger.popMove();
		}
		
		return null;
	}

	public void decreaseProtectedPiecesCounter(boolean affinity, int decrementValue)
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

	public void increaseProtectedPiecesCounter(boolean affinity, int incrementValue)
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
	
	public long getProtectedPiecesCount(boolean affinity)
	{
		if(affinity == BLACK)
		{
			return myBlackProtectedPiecesCount;
		}
		
		return myWhiteProtectedPiecesCount;
	}
	
	public void decreaseTakeOverPiecesCounter(boolean affinity, int decrementValue)
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

	public void increaseTakeOverPiecesCounter(boolean affinity, int incrementValue)
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
	
	public long getTakeOverPiecesCount(boolean affinity)
	{
		if(affinity == BLACK)
		{
			return myBlackTakeOverPiecesCount;
		}
		
		return myWhiteTakeOverPiecesCount;
	}

	public long getMeasuredStatusForPlayer(boolean affinity)
	{
		int playerNrOfAvailableMoves = getAvailableMoves(affinity).size();
		//int playerNrOfNonAvailableMoves = getNonAvailableMoves(affinity).size();
		long playerProtectiveMoves = getProtectedPiecesCount(affinity);
		long playerTakeOverCount = getTakeOverPiecesCount(affinity);
		long totalPieceValue = 0;
		for(Piece p : getPiecesForAffinity(affinity))
		{
			totalPieceValue += p.getValue();
		}
		//Counts the available moves for the king
		int kingMobility = this.getKing(affinity).getAvailableMoves(NO_SORT, this).size() * KING_MOBILITY_FACTOR;
		return playerNrOfAvailableMoves + playerProtectiveMoves + playerTakeOverCount + totalPieceValue + kingMobility;
	}

	public int getDifficulty()
	{
		return myDifficulty;
	}
}
