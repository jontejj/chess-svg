package com.jjonsson.chess.pieces;

import static com.jjonsson.utilities.Logger.LOGGER;

import java.util.List;
import java.util.Set;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Shorts;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.orderings.MoveOrdering;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.ChainMove;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.RevertingMove;
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
	
	public static final boolean NO_SORT = false;
	public static final boolean SORT = true;
	
	private Position myCurrentPosition;
	private boolean myAffinity;
	
	private Set<MoveListener> myListeners;
	
	/**
	 * List of "in theory" possible moves that this piece can make 
	 */
	private List<Move> myPossibleMoves;
	
	private Move[][] myMoves;
	
	private Set<Piece> myPiecesThatTakesMyPieceOver;
	private Piece myCheapestPieceThatTakesMeOver;
	
	private boolean myIsRemoved;
	
	/**
	 * True if a pawn has been exchanged for this piece
	 */
	private boolean myIsPawnReplacementPiece;
	
	/**
	 * The board that this piece is placed on
	 */
	private ChessBoard myBoard;
	
	private long myMovesMade;
	
	
	/**
	 * 
	 * @param startingPosition where this piece should be placed
	 * @param affinity true if this piece belongs to the black player false otherwise
	 */
	public Piece(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
	{
		myMoves = createMoveTable();
		myListeners = Sets.newHashSet();
		myPossibleMoves = Lists.newArrayList();
		
		myPiecesThatTakesMyPieceOver = Sets.newHashSet();
		
		myCurrentPosition = startingPosition;
		myAffinity = affinity;
		myBoard = boardPieceIsToBePlacedOn;
		addPossibleMoves();
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
	
	public void addPieceThatTakesMeOver(Piece p)
	{
		if(myPiecesThatTakesMyPieceOver.add(p))
		{
			//The piece was added, we need to recalculate the cheapest piece that takes myPiece over
			myCheapestPieceThatTakesMeOver = new PieceValueOrdering().min(myPiecesThatTakesMyPieceOver);
		}
	}
	
	public void removePieceThatTakesMeOver(Piece p)
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
	 * @return a PersistanceIdentifier identifying the type of this piece
	 */
	protected abstract byte getPersistanceIdentifierType();
	
	private byte getPersistanceIdentifier()
	{
		byte type = getPersistanceIdentifierType();
		if(isBlack())
		{
			//The fourth bit is the color identifier
			type = (byte) (0x8 | type);
		}
		return type;
	}
	
	/**
	 * @return a short containing one byte of position data and one byte of affinity and type data
	 */
	public short getPersistanceData()
	{
		//From left, first 4 bits row, 4 bits column and then 8 bits type (where only the four rightmost is used)
		byte positionData = (byte) (getCurrentPosition().getRow() << 4);
		positionData |= getCurrentPosition().getColumn();
		return Shorts.fromBytes(positionData, getPersistanceIdentifier());
	}
	/**
	 * 
	 * @param persistanceData the bits to read piece information from
	 * <br>From left, first 4 bits row, 4 bits column and then 8 bits type (where only the four rightmost is used)
	 * @param board the board the piece is going to be placed on
	 * @return a newly constructed piece
	 * @throws InvalidPosition
	 */
	public static Piece getPieceFromPersistanceData(short persistanceData, ChessBoard board) throws InvalidPosition
	{
		Piece piece = null;
		//From left, first 4 bits row, 4 bits column and then 8 bits type (where only the four rightmost is used)
		byte row = (byte) (persistanceData >> 12);
		byte column = (byte) (persistanceData >> Byte.SIZE & 0xF);
		
		Position position = Position.createPosition(row + 1, column + 1);
		
		boolean affinity = ((persistanceData & 0x0008) == 0x0008);
		int type = persistanceData & 0x0007;

		switch(type)
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
	protected void addPossibleMove(Move moveToAdd)
	{
		myPossibleMoves.add(moveToAdd);
	}
	
	public void addMoveListener(MoveListener listener)
	{
		myListeners.add(listener);
	}
	
	public void removeMoveListener(MoveListener listener)
	{
		myListeners.remove(listener);
	}
	
	/**
	 * Utility function that adds a chain of moves that this piece can do
	 * @param rowChange the increment(positive)/decrement(negative) for each step 
	 * @param columnChange the increment(right)/decrement(left) for each step 
	 * @param movesToGenerate number of moves to generate
	 */
	protected void addMoveChain(int rowChange, int columnChange, byte movesToGenerate)
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
	 * @return a list of "in theory" possible moves that this piece can make 
	 */
	public List<Move> getPossibleMoves()
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

	public List<Move> getAvailableMoves(boolean sort, ChessBoard board) 
	{
		List<Move> availableMoves = Lists.newArrayList();
		List<Move> possibleMoves = getPossibleMoves();
		
		for(Move m : possibleMoves)
		{
			if(m.canBeMade(board))
			{
				availableMoves.add(m);
			}
			if(m instanceof DependantMove)
			{
				availableMoves.addAll(((DependantMove)m).getPossibleMovesThatIsDependantOnMe(board));
			}
		}
		
		if(sort)
		{
			return MoveOrdering.getInstance().immutableSortedCopy(availableMoves);
		}
		return availableMoves;
	}
	
	public List<Move> getMoves() 
	{
		List<Move> moves = Lists.newArrayList();
		List<Move> possibleMoves = getPossibleMoves();
		
		for(Move m : possibleMoves)
		{
			moves.add(m);
			if(m instanceof DependantMove)
			{
				moves.addAll(((DependantMove)m).getMovesThatIsDependantOnMe());
			}
		}
		return moves;
	}
	
	public boolean canMakeAMove(ChessBoard board)
	{
		return getAvailableMoves(NO_SORT, board).size() > 0;
	}

	/**
	 * @param from
	 * @return a move that this piece can do that matches the given move or null if no such move exists
	 */
	public Move getMove(Move from)
	{
		return myMoves[from.getFirstDimensionIndex()][from.getSecondDimensionIndex()];
	}
	
	/**
	 * 
	 * @return where this Piece is currently located
	 */
	public Position getCurrentPosition()
	{
		return myCurrentPosition;
	}
	
	/**
	 * @return the value for this piece
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
	
	/**
	 * 
	 * @return how many moves this piece has made
	 */
	public long getMovesMade()
	{
		return myMovesMade;
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
	 * @throws UnavailableMoveException  if this move isn't available right now
	 * (Note that this will be misleading if there are ChessBoardListener's that performs another move when nextPlayer is called)
	 */
	public void performMove(Move move, ChessBoard board) throws UnavailableMoveException
	{
		performMove(move, board, true);
	}
	
	/**
	 * Moves this Piece with the supplied move.
	 * @param move the move to apply to this piece
	 * @param printOut true if there should be print outs about the move to the standard out stream
	 * @throws UnavailableMoveException  if this move isn't available right now 
	 * (Note that this will be misleading if there are ChessBoardListener's that performs another move when nextPlayer is called)
	 */
	public void performMove(Move move, ChessBoard board, boolean printOut) throws UnavailableMoveException
	{			
		if(printOut)
		{
			LOGGER.finest("Performing: " + move);
		}
		
		move.makeMove(board);
		
		if(move instanceof RevertingMove)
		{
			myMovesMade--;
		}
		else
		{
			myMovesMade++;
		}
		
		for(Move m : getPossibleMoves())
		{
			m.updateMove(board);
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
	public boolean same(Piece p)
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
		if(this.getPersistanceIdentifierType() != p.getPersistanceIdentifierType())
		{
			return false;
		}
		
		return true;
	}
	
	public boolean hasSameAffinityAs(Piece thePieceToCompareWith) 
	{
		return myAffinity == thePieceToCompareWith.getAffinity();
	}
	
	public boolean hasSameAffinityAs(boolean otherAffinity) 
	{
		return myAffinity == otherAffinity;
	}
	/**
	 * Should be called when all the pieces have been placed on the board
	 * @param chessBoard
	 */
	public void initilizePossibilityOfMoves(ChessBoard chessBoard)
	{
		for(Move m : myPossibleMoves)
		{
			m.updateMove(chessBoard);
		}
	}

	private void removeMovesFromBoard(ChessBoard chessBoard)
	{
		for(Move m : myPossibleMoves)
		{
			m.removeFromBoard(chessBoard);
			m.syncCountersWithBoard(chessBoard);
		}
	}
	
	/**
	 * 
	 * @param board
	 * @return the removed piece
	 */
	public Piece removeFromBoard(ChessBoard board)
	{	
		Piece returnPiece = this;
		board.removePiece(this);
		this.removeMovesFromBoard(board);
		myIsRemoved = true;
	
		Piece currentPieceAtMyPosition = board.getPiece(getCurrentPosition());
		//This could happen if the previous piece was taken and there is a new piece that could be taken
		if(currentPieceAtMyPosition != null && currentPieceAtMyPosition != this)
		{
			return currentPieceAtMyPosition.removeFromBoard(board);
		}
		
		for(MoveListener listener : myListeners)
		{
			listener.pieceRemoved(returnPiece);
		}
		myListeners.clear();
		
		return returnPiece;
	}
	
	public boolean isRemoved()
	{
		return myIsRemoved;
	}
	
	public boolean isPawnReplacementPiece()
	{
		return myIsPawnReplacementPiece;
	}
	
	public void setIsPawnReplacementPiece()
	{
		myIsPawnReplacementPiece = true;
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
	public void revertedAMove(ChessBoard board, Position oldPosition)
	{
	}

	/**
	 * This causes the piece to be able to move again but only if it was removed previously
	 */
	public void reEnablePossibleMoves()
	{
		if(isRemoved())
		{
			for(Move m : myPossibleMoves)
			{
				m.reEnable();
			}
			myIsRemoved = false;
		}
	}
	
	/**
	 * Use this if the given move wants to be returned by getMove(rowChange, columnChange)
	 * @param move
	 */
	public void addToMoveTable(Move move)
	{
		if(move.shouldBeIncludedInMoveTable())
		{
			myMoves[move.getFirstDimensionIndex()][move.getSecondDimensionIndex()] = move;
		}
	}
}
