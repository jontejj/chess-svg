package com.jjonsson.chess.pieces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.ChainMove;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.ordering.MoveOrdering;
import com.jjonsson.chess.pieces.ordering.PieceValueOrdering;

/**
 * @author jonatanjoensson
 *
 */
public abstract class Piece
{	
	//Used to figure out the take over value of a move
	public static final int TOWER_VALUE = 400;
	public static final int KNIGHT_VALUE = 300;
	public static final int BISHOP_VALUE = 300;
	public static final int PAWN_VALUE = 100;
	public static final int KING_VALUE = 800;
	public static final int QUEEN_VALUE = 800;
	
	//Used to save/load a piece
	protected static final byte BISHOP = 0;
	protected static final byte PAWN = 1;
	protected static final byte KING = 2;
	protected static final byte KNIGHT = 3;
	protected static final byte QUEEN = 4;
	protected static final byte ROCK = 5;
	
	//The affinity (color) of a piece
	public static boolean WHITE = false;
	public static boolean BLACK = true;
	
	protected Position myCurrentPosition;
	private boolean myAffinity;
	
	private HashSet<MoveListener> myListeners;
	
	/**
	 * List of "in theory" possible moves that this piece can make 
	 */
	private ArrayList<Move> myPossibleMoves;
	
	//TODO: keep this map updated and use it in the getMoveForPosition functions
	private HashMap<Position, Move> myMoveMap;
	
	private Set<Piece> myPiecesThatTakesMyPieceOver;
	private Piece myCheapestPieceThatTakesMeOver;
	
	/**
	 * The board that this piece is placed on
	 */
	private ChessBoard myBoard;
	
	
	/**
	 * 
	 * @param startingPosition where this piece should be placed
	 * @param affinity true if this piece belongs to the black player false otherwise
	 */
	public Piece(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
	{
		myMoveMap = Maps.newHashMap();
		myListeners = Sets.newHashSet();
		myPossibleMoves = Lists.newArrayList();
		
		myPiecesThatTakesMyPieceOver = Sets.newHashSet();
		
		myCurrentPosition = startingPosition;
		myAffinity = affinity;
		myBoard = boardPieceIsToBePlacedOn;
		addPossibleMoves();
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
				myCheapestPieceThatTakesMeOver = null;
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
		return ((getAffinity() == Piece.BLACK) ? "Black_" : "White_") + getPieceName();
	}
	
	public String getDisplayName()
	{
		return ((getAffinity() == Piece.BLACK) ? "Black " : "White ") + getPieceName();
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
		if(getAffinity() == Piece.BLACK)
		{
			//The fourth bit is the color identifier
			type = (byte) (0x8 | type);
		}
		return type;
	}
	
	public short getPersistanceData()
	{
		//From left, first 4 bits row, 4 bits column and then 8 bits type (where only the four rightmost is used)
		byte positionData = (byte) (getCurrentPosition().getRow() << 4);
		positionData |= getCurrentPosition().getColumn();
		
		short persistanceData = (short) (positionData << 8);
		persistanceData |= getPersistanceIdentifier();
		return persistanceData;
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
		byte column = (byte) (persistanceData >> 8 & 0xF);
		
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
			case KNIGHT:
				piece = new Knight(position, affinity, board);
				break;
			case PAWN:
				if(affinity == BLACK)
					piece = new BlackPawn(position, board);
				else
					piece = new WhitePawn(position, board);
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
				lastMove.setMoveThatDependsOnMe(newMove);
			
			//Only add the link to the first move in the chain to the possible moves as this move will keep track of it's followers
			if(lastMove == null)
				addPossibleMove(newMove);
			
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
	public ArrayList<Move> getPossibleMoves()
	{
		return myPossibleMoves;
	}
	
	/**
	 * Returns the cached evaluation of all the possible moves for this piece and excludes the ones that aren't possible at the moment
	 * Override this method if you want to optimize it for a specific piece
	 * @param sort true if the returned map should be ordered by the moves take over value
	 * @param board 
	 * @return the available moves for this piece on the supplied board and within it's bounds ordered by their take over value
	 */

	public ImmutableList<Move> getAvailableMoves(boolean sort, ChessBoard board) 
	{
		List<Move> availableMoves = Lists.newArrayList();
		ArrayList<Move> possibleMoves = getPossibleMoves();
		
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
			return MoveOrdering.instance.immutableSortedCopy(availableMoves);
		}
		
		return ImmutableList.copyOf(availableMoves);
	}
	
	/**
	 * Returns the cached evaluation of all the possible moves for this piece and excludes the ones that are possible at the moment
	 * Override this method if you want to optimize it for a specific piece
	 * @param board 
	 * @return the non available moves for this piece on the supplied board and within it's bounds ordered by their take over value
	 */

	public List<Move> getNonAvailableMoves(ChessBoard board) 
	{
		List<Move> nonAvailableMoves = Lists.newArrayList();
		ArrayList<Move> possibleMoves = getPossibleMoves();
		
		for(Move m : possibleMoves)
		{
			if(!m.canBeMade(board))
			{
				nonAvailableMoves.add(m);
				if(m instanceof DependantMove)
				{
					nonAvailableMoves.addAll(((DependantMove)m).getNonPossibleMovesThatIsDependantOnMe(board));
				}
			}
		}
		
		return nonAvailableMoves;
	}
	
	public boolean canMakeAMove(ChessBoard board)
	{
		return getAvailableMoves(false, board).size() > 0;
	}
	/**
	 * 
	 * @param pos
	 * @param board 
	 * @return an available move if one was found, otherwise null meaning that this piece can't move into the given position
	 */
	public Move getAvailableMoveForPosition(Position pos, ChessBoard board)
	{
		//A quick check, you can't move into your own square
		if(getCurrentPosition().equals(pos))
			return null;
		
		for(Move m : getAvailableMoves(false, board))
		{
			//TODO: faster iteration (ie a map)
			if(m.getPositionIfPerformed().equals(pos))
			{
				return m;
			}
		}
		return null;
	}
	
	/**
	 * 
	 * @param pos
	 * @param board 
	 * @return a non available move if one was found, otherwise null meaning that this piece can move into the given position
	 */
	public Move getNonAvailableMoveForPosition(Position pos, ChessBoard board)
	{
		for(Move m : getNonAvailableMoves(board))
		{
			//Note that pos can never be null, but positionIfPerformed can so the check needs to be in this order to avoid unnessecary null checks
			if(pos.equals(m.getPositionIfPerformed()))
			{
				return m;
			}
		}
		return null;
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
	 * @return the estimated value of the move performed 
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
	 * @return the estimated value of the move performed 
	 * (Note that this will be misleading if there are ChessBoardListener's that performs another move when nextPlayer is called)
	 */
	public void performMove(Move move, ChessBoard board, boolean printOut) throws UnavailableMoveException
	{		
		if(printOut)
			System.out.println("Performing: " + move);
		
		move.makeMove(board);
		for(Move m : getPossibleMoves())
		{
			m.updateMove(board);
		}
		for(MoveListener m : myListeners)
		{
			m.movePerformed(move);
		}
		board.nextPlayer();
	}
	
	/**
	 * TODO: convert this into equals together with a code hashCode
	 * @param p
	 * @return if the given piece has the same location, affinity and type
	 */
	public boolean same(Piece p)
	{
		if(p == null)
			return false;
		if(!this.hasSameAffinityAs(p))
			return false;
		if(!this.getCurrentPosition().equals(p.getCurrentPosition()))
			return false;
		if(this.getPersistanceIdentifierType() != p.getPersistanceIdentifierType())
			return false;
		
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
		}
	}
	
	public void removeFromBoard(ChessBoard board)
	{	
		board.removePiece(this);
		this.removeMovesFromBoard(board);
	
		Piece currentPieceAtMyPosition = board.getPiece(getCurrentPosition());
		//This could happen if the previous piece was taken and there is a new piece that could be taken
		if(currentPieceAtMyPosition != null && currentPieceAtMyPosition != this)
		{
			currentPieceAtMyPosition.removeFromBoard(board);
		}
		
		Object clonedSet = myListeners.clone();
		if(clonedSet instanceof HashSet<?>)
		{
			for(Object listener : (HashSet<?>)clonedSet)
			{
				if(listener instanceof MoveListener)
					((MoveListener)listener).pieceRemoved(this);
			}
		}
	}

	public boolean isWhite()
	{
		return myAffinity == Piece.WHITE;
	}
	
	public boolean isBlack()
	{
		return myAffinity == Piece.BLACK;
	}

	/**
	 * Called when a move that this piece previously made has been reverted
	 */
	@SuppressWarnings("unused")
	public void revertedAMove(ChessBoard board)
	{
	}

	public void reEnablePossibleMoves()
	{
		for(Move m : myPossibleMoves)
		{
			m.reEnable();
		}
	}
}
