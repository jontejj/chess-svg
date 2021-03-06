package com.jjonsson.chess.pieces;

import static com.jjonsson.utilities.Bits.containBits;
import static com.jjonsson.utilities.Loggers.STDOUT;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Shorts;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.listeners.MoveListener;
import com.jjonsson.chess.moves.ChainMove;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.ordering.PieceValueOrdering;

/**
 * @author jonatanjoensson
 *
 */
public abstract class Piece
{
	//Used to figure out the take over value of a move
	public static final int ROCK_VALUE = 500;
	public static final int KNIGHT_VALUE = 300;
	public static final int BISHOP_VALUE = 320;
	public static final int PAWN_VALUE = 100;
	public static final int QUEEN_VALUE = 900;

	//This is handled by the ChessState's the king can't be taken over and therefore it shouldn't have a value
	public static final int KING_VALUE = 30;

	/**
	 * Defines how important is to have many take over alternatives (based on empirical tests)
	 */
	protected static final double TAKE_OVER_ACCUMULATOR_IMPORTANCE_FACTOR = 0.6;

	/**
	 * Defines how important it is to protect your own pieces
	 */
	protected static final double PROTECTIVE_MOVE_ACCUMULATOR_IMPORTANCE_FACTOR = 0.01;

	private static final int EXPECTED_TAKEOVERS_PER_PIECE = 4;

	//Used to save/load a piece
	protected static final byte BISHOP = 0;
	protected static final byte PAWN = 1;
	protected static final byte KING = 2;
	protected static final byte KNIGHT = 3;
	protected static final byte QUEEN = 4;
	protected static final byte ROCK = 5;
	protected static final byte MOVED_KING = 6;
	protected static final byte MOVED_ROCK = 7;

	//The affinity (color) of a piece
	public static final boolean WHITE = false;
	public static final boolean BLACK = true;

	/**
	 * The fourth bit is the color identifier
	 */
	private static final byte BLACK_BIT = (byte) 0x8;

	private static final byte TYPE_MASK = (byte) 0x07;

	public static final int BYTES_PER_PIECE = 2;

	private ImmutablePosition myCurrentPosition;
	private MutablePosition myPosition;
	private boolean myAffinity;

	private Set<MoveListener> myListeners;

	/**
	 * List of "in theory" possible moves that this piece can make
	 */
	private Collection<Move> myPossibleMoves;

	private Move[][] myMoves;

	private Set<Piece> myPiecesThatTakesMyPieceOver;
	private Piece myCheapestPieceThatTakesMeOver;

	private boolean myIsRemoved;

	/**
	 * The board that this piece is placed on
	 */
	private ChessBoard myBoard;

	private long myMovesMade;

	/**
	 * True for pawns that have reached their bottom/top destination row
	 */
	private boolean myHasBeenPromoted;


	/**
	 * 
	 * @param startingPosition where this piece should be placed
	 * @param affinity true if this piece belongs to the black player false otherwise
	 */
	public Piece(final MutablePosition startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
	{
		myMoves = createMoveTable();
		myListeners = Sets.newHashSetWithExpectedSize(2);
		myPossibleMoves = Sets.newHashSetWithExpectedSize(expectedNumberOfPossibleMoves());

		myPiecesThatTakesMyPieceOver = Sets.newHashSetWithExpectedSize(EXPECTED_TAKEOVERS_PER_PIECE);

		myPosition = startingPosition;
		myCurrentPosition = ImmutablePosition.getPosition(startingPosition);
		myAffinity = affinity;
		myBoard = boardPieceIsToBePlacedOn;
		addPossibleMoves();
	}

	/**
	 * Called when pawns have reached their bottom/top destination row
	 */
	public void promote()
	{
		myHasBeenPromoted = true;
	}

	/**
	 * Called when a promotion have been undone
	 */
	public void dePromote()
	{
		myHasBeenPromoted = false;
	}

	/**
	 * @return true for pawns that have reached their bottom/top destination row
	 */
	public boolean isPromoted()
	{
		return myHasBeenPromoted;
	}

	@VisibleForTesting
	public int getFirstDimensionMaxIndex()
	{
		return ChessBoard.BOARD_SIZE - 1;
	}

	@VisibleForTesting
	public int getSecondDimensionMaxIndex()
	{
		return ChessBoard.BOARD_SIZE - 1;
	}

	@VisibleForTesting
	public final Move[][] createMoveTable()
	{
		Move[][] moves = new Move[getFirstDimensionMaxIndex()+1][getSecondDimensionMaxIndex()+1];
		for(int index = 0;index<moves.length;index++)
		{
			moves[index] = new Move[getSecondDimensionMaxIndex()+1];
		}
		return moves;
	}

	public ChessBoard getBoard()
	{
		return myBoard;
	}

	public void addPieceThatTakesMeOver(final Piece p)
	{
		if(myPiecesThatTakesMyPieceOver.add(p))
		{
			//The piece was added, we need to recalculate the cheapest piece that takes myPiece over
			myCheapestPieceThatTakesMeOver = new PieceValueOrdering().min(myPiecesThatTakesMyPieceOver);
		}
	}

	public void removePieceThatTakesMeOver(final Piece p)
	{
		if(myPiecesThatTakesMyPieceOver.remove(p))
		{
			if(myPiecesThatTakesMyPieceOver.size() > 0)
			{
				//The piece was removed, we need to recalculate the cheapest piece that takes myPiece over
				myCheapestPieceThatTakesMeOver = new PieceValueOrdering().min(myPiecesThatTakesMyPieceOver);
			}
			else
			{
				myCheapestPieceThatTakesMeOver = null;
			}
		}
	}

	public Piece getCheapestPieceThatTakesMeOver()
	{
		return myCheapestPieceThatTakesMeOver;
	}

	@Override
	public String toString()
	{
		return getIdentifier() + " at: " + getCurrentPosition();
	}

	public String getIdentifier()
	{
		return (isBlack() ? "Black_" : "White_") + getPieceName();
	}

	public String getDisplayName()
	{
		return (isBlack() ? "Black " : "White ") + getPieceName();
	}

	public abstract String getPieceName();

	/**
	 * 
	 * @return a PersistenceIdentifier identifying the type of this piece
	 */
	protected abstract byte getPersistenceIdentifierType();

	private byte getPersistenceIdentifier()
	{
		byte type = getPersistenceIdentifierType();
		if(isBlack())
		{
			type |= BLACK_BIT;
		}
		return type;
	}

	/**
	 * @return a short containing one byte of position data and one byte of affinity and type data
	 */
	public short getPersistenceData()
	{
		return Shorts.fromBytes(myCurrentPosition.getPersistence(), getPersistenceIdentifier());
	}
	/**
	 * 
	 * @param buffer the buffer to read piece information from
	 * <br>From left, first 4 bits row, 4 bits column and then 8 bits type (where only the four rightmost is used)
	 * @param board the board the piece is going to be placed on
	 * @return a newly constructed piece
	 * @throws ArrayIndexOutOfBoundsException if the persistanceData contains an invalid position in the first byte
	 */
	public static Piece getPieceFromPersistenceData(final ByteBuffer buffer, final ChessBoard board)
	{
		Piece piece = null;
		//From left, first 4 bits row, 4 bits column and then 8 bits type (where only the four rightmost is used)
		MutablePosition position = MutablePosition.from(buffer.get());
		byte pieceInfo = buffer.get();
		boolean affinity = containBits(pieceInfo, BLACK_BIT);
		switch(pieceInfo & TYPE_MASK)
		{
			case BISHOP:
				piece = new Bishop(position, affinity, board);
				break;
			case QUEEN:
				piece = new Queen(position, affinity, board);
				break;
			case ROCK:
				piece = new Rock(position, affinity, board);
				break;
			case KING:
				piece = new King(position, affinity, board);
				break;
			case MOVED_KING:
				piece = new King(position, affinity, board);
				piece.increaseMovesMade();
				break;
			case MOVED_ROCK:
				piece = new Rock(position, affinity, board);
				piece.increaseMovesMade();
				break;
			case KNIGHT:
				piece = new Knight(position, affinity, board);
				break;
			case PAWN:
				if(affinity == BLACK)
				{
					piece = new BlackPawn(position, board);
				}
				else
				{
					piece = new WhitePawn(position, board);
				}
				break;
		}

		return piece;
	}

	/**
	 * Adds a possible move to this piece
	 * @param moveToAdd the move to add to
	 */
	protected void addPossibleMove(final Move moveToAdd)
	{
		myPossibleMoves.add(moveToAdd);
	}

	public void addMoveListener(final MoveListener listener)
	{
		myListeners.add(listener);
	}

	/**
	 * Utility function that adds a chain of moves that this piece can do
	 * @param rowChange the increment(positive)/decrement(negative) for each step
	 * @param columnChange the increment(right)/decrement(left) for each step
	 * @param movesToGenerate number of moves to generate
	 */
	protected void addMoveChain(final int rowChange, final int columnChange, final byte movesToGenerate)
	{
		ChainMove lastMove = null;
		ChainMove newMove = null;
		for(int i = 1; i <= movesToGenerate; i++)
		{
			newMove = new ChainMove(rowChange * i, columnChange * i, this, null, lastMove);

			//This makes sure the move list is double linked
			if(lastMove != null)
			{
				lastMove.setMoveThatDependsOnMe(newMove);
			}

			//Only add the link to the first move in the chain to the possible moves as this move will keep track of it's followers
			if(lastMove == null)
			{
				addPossibleMove(newMove);
			}

			lastMove = newMove;
		}
	}

	/**
	 * Add the theoretically possible moves for this piece to the internal move list for this piece
	 * (Automatically called from the constructor)
	 */
	public abstract void addPossibleMoves();

	/**
	 * 
	 * @return the amount of moves that is expected to be put into possibleMoves
	 */
	public int expectedNumberOfPossibleMoves()
	{
		return 8;
	}

	/**
	 * @return a list of "in theory" possible moves that this piece can make
	 */
	public Collection<Move> getPossibleMoves()
	{
		return myPossibleMoves;
	}

	/**
	 * Returns the cached evaluation of all the possible moves for this piece and excludes the ones that aren't possible at the moment
	 * Override this method if you want to optimize it for a specific piece
	 * @param sort true if the returned map should be ordered by the moves take over value
	 * @param board
	 * @return the available moves for this piece on the supplied board and within it's bounds ordered by their take over value
	 * Note: the returned list may be mutable because of performance issues
	 */

	public List<Move> getAvailableMoves()
	{
		List<Move> availableMoves = Lists.newArrayList();

		for(Move m : getPossibleMoves())
		{
			if(m.canBeMade(myBoard))
			{
				availableMoves.add(m);
			}
			if(m instanceof DependantMove)
			{
				availableMoves.addAll(((DependantMove)m).getPossibleMovesThatIsDependantOnMe(myBoard));
			}
		}
		return availableMoves;
	}

	public List<Move> getMoves()
	{
		List<Move> moves = Lists.newArrayList();

		for(Move m : getPossibleMoves())
		{
			moves.add(m);
			if(m instanceof DependantMove)
			{
				moves.addAll(((DependantMove)m).getMovesThatIsDependantOnMe());
			}
		}
		return moves;
	}

	public boolean canMakeAMove()
	{
		return getAvailableMoves().size() > 0;
	}

	/**
	 * @param from
	 * @return a move that this piece can do that matches the given move or null if no such move exists
	 */
	public Move getMove(final Move from)
	{
		return myMoves[from.getFirstDimensionIndex()][from.getSecondDimensionIndex()];
	}

	/**
	 * 
	 * @return where this Piece is currently located
	 */
	public ImmutablePosition getCurrentPosition()
	{
		return myCurrentPosition;
	}

	public MutablePosition getPosition()
	{
		return myPosition;
	}

	/**
	 * @param moveToPerform
	 * @throws ArrayIndexOutOfBoundsException if the move results in an invalid position,
	 * as this shouldn't happen there should be no need for catching this
	 */
	public void updateCurrentPosition(final Move moveToPerform)
	{
		int oldValue = getValue();
		myPosition.applyMove(moveToPerform);
		myCurrentPosition = ImmutablePosition.getPosition(myPosition);
		myBoard.pieceValueChanged(oldValue - getValue(), getAffinity());
	}

	/**
	 * @return the value for this piece, note that this may change depending on where the piece stands and etc.
	 */
	public abstract int getValue();

	public int getTakeOverImportanceValue()
	{
		return (int) (getValue() * TAKE_OVER_ACCUMULATOR_IMPORTANCE_FACTOR);
	}

	public int getProtectImportanceValue()
	{
		return (int) (getValue() * PROTECTIVE_MOVE_ACCUMULATOR_IMPORTANCE_FACTOR);
	}

	public long getAccumulatedTakeOverImportanceValue()
	{
		return myPiecesThatTakesMyPieceOver.size() * getTakeOverImportanceValue();
	}

	//TODO: these should be refactored to their own class
	/**
	 * 
	 * @return how many moves this piece has made
	 */
	public long getMovesMade()
	{
		return myMovesMade;
	}

	public void setMovesMade(final long movesMade)
	{
		myMovesMade = movesMade;
	}

	/**
	 * 
	 * @return true if this piece is black, false if it's white
	 */
	public boolean getAffinity()
	{
		return myAffinity;
	}

	/**
	 * Moves this Piece with the supplied move.
	 * @param move the move to apply to this piece
	 * @return false if this move isn't available right now
	 */
	public boolean performMove(final Move move, final ChessBoard board)
	{
		return performMove(move, board, true);
	}

	/**
	 * Moves this Piece with the supplied move.
	 * @param move the move to apply to this piece
	 * @param printOut true if there should be print outs about the move to the standard out stream
	 * @return false if this move isn't available right now
	 */
	public boolean performMove(final Move move, final ChessBoard board, final boolean printOut)
	{
		if(printOut)
		{
			STDOUT.debug("Performing: " + move);
		}

		if(!move.makeMove(board))
		{
			return false;
		}

		if(!isRemoved())
		{
			updateMoves(board);
		}

		//Update old position
		board.updatePossibilityOfMovesForPosition(move.getOldPosition());
		//Update new position
		board.updatePossibilityOfMovesForPosition(getCurrentPosition());

		for(MoveListener m : myListeners)
		{
			m.movePerformed(move);
		}
		if(!move.isPartOfAnotherMove())
		{
			board.nextPlayer();
		}
		return true;
	}

	/**
	 * This can be used when loading a saved game in order to remember the possibility of castling moves
	 */
	public void increaseMovesMade()
	{
		myMovesMade++;
	}

	/**
	 * TODO(jontejj): convert this into equals together with a good hashCode
	 * @param p
	 * @return if the given piece has the same location, affinity and type
	 */
	public boolean same(final Piece p)
	{
		if(p == null)
		{
			return false;
		}
		if(!this.hasSameAffinityAs(p))
		{
			return false;
		}
		if(!this.getCurrentPosition().equals(p.getCurrentPosition()))
		{
			return false;
		}
		if(this.getPersistenceIdentifierType() != p.getPersistenceIdentifierType())
		{
			return false;
		}

		return true;
	}

	public boolean hasSameAffinityAs(final Piece thePieceToCompareWith)
	{
		return myAffinity == thePieceToCompareWith.getAffinity();
	}

	public boolean hasSameAffinityAs(final boolean otherAffinity)
	{
		return myAffinity == otherAffinity;
	}
	/**
	 * Should be called when all the pieces have been placed on the board
	 * @param chessBoard
	 */
	public void initilizePossibilityOfMoves(final ChessBoard chessBoard)
	{
		updateMoves(chessBoard);
	}

	/**
	 * Updates the destination and the possibilities for moves that this piece can make
	 * @param board
	 */
	private void updateMoves(final ChessBoard board)
	{
		for(Move m : myPossibleMoves)
		{
			m.updateMove(board);
		}
	}

	protected void removeMovesFromBoard(final ChessBoard chessBoard)
	{
		for(Move m : myPossibleMoves)
		{
			m.disable(chessBoard);
			m.syncCountersWithBoard(chessBoard);
		}
	}

	/**
	 * 
	 * @param board
	 * @return the removed piece
	 */
	public Piece removeFromBoard(final ChessBoard board)
	{
		Piece returnPiece = this;
		if(!isRemoved())
		{
			this.removeMovesFromBoard(board);
			board.removePiece(this);
			myIsRemoved = true;

			for(MoveListener listener : myListeners)
			{
				listener.pieceRemoved(returnPiece);
			}
			myListeners.clear();
		}

		return returnPiece;
	}

	public boolean isRemoved()
	{
		return myIsRemoved;
	}

	public boolean isWhite()
	{
		return myAffinity == WHITE;
	}

	public boolean isBlack()
	{
		return myAffinity == BLACK;
	}

	/**
	 * Called when a move that this piece previously made has been reverted
	 */
	@SuppressWarnings("unused") //Used by subclasses, perhaps this should be solved nicer?
	public void revertedAMove(final ChessBoard board, final Position oldPosition)
	{
	}

	/**
	 * This causes the piece to be able to move again but only if it was removed previously
	 */
	public void reEnablePossibleMoves()
	{
		if(isRemoved())
		{
			getBoard().addPieceToPositionMaps(this);
			for(Move m : myPossibleMoves)
			{
				m.reEnable();
			}
			myIsRemoved = false;
		}
	}

	/**
	 * Use this if the given move wants to be returned by {@link Piece#getMove(Move)}
	 * @param move
	 */
	public void addToMoveTable(final Move move)
	{
		if(move.shouldBeIncludedInMoveTable())
		{
			myMoves[move.getFirstDimensionIndex()][move.getSecondDimensionIndex()] = move;
		}
	}
}
