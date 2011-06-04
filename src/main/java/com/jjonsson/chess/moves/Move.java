package com.jjonsson.chess.moves;

import com.google.common.collect.ImmutableSet;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Piece;

public abstract class Move
{
	public static final int UP = 1;
	public static final int DOWN = -1;
	public static final int LEFT = -1;
	public static final int RIGHT = 1;
	public static final int	NO_CHANGE = 0;
	/**
	 * A positive change means that the piece is moving upwards the board
	 */
	private byte myRowChange;
	/**
	 * A positive change means that the piece is moving towards the right
	 */
	private byte myColumnChange;
	/**
	 * The piece that this move will be made with
	 */
	protected Piece myPiece;
	/**
	 * The piece that would be defeated if this move was performed
	 */
	protected Piece myPieceAtDestination;
	
	/**
	 * The cached position of this move's destination 
	 */
	protected Position myDestination;

	/**
	 * Cached decision of whether or not this move can be made
	 */
	protected boolean myCanBeMadeCache;
	
	protected RevertingMove myRevertingMove;
	
	protected boolean myIsRemoved;
	
	/**
	 * 
	 * @param rowChange the row change for this move, valid numbers are (-7) to (+7)
	 * @param columnChange the column change for this move, valid numbers are (-7) to (+7)
	 */
	public Move(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith)
	{
		myRowChange = (byte)rowChange;
		myColumnChange = (byte)columnChange;
		myPiece = pieceThatTheMoveWillBeMadeWith;
		setRevertingMove();
	}
	
	protected void setRevertingMove()
	{
		myRevertingMove = new RevertingMove(this);
	}
	
	/**
	 * 
	 * @return a move that will revert this move
	 */
	public RevertingMove getRevertingMove()
	{
		return myRevertingMove;
	}
	
	/**
	 * 
	 * @param p the piece that is at the destination of this move
	 */
	public void setPieceAtDestination(Piece p, ChessBoard board)
	{
		if(p != myPieceAtDestination)
		{
			//Clear old
			if(myPieceAtDestination != null)
			{
				if(!myPieceAtDestination.hasSameAffinityAs(myPiece))
				{
					myPieceAtDestination.removePieceThatTakesMeOver(myPiece);
				}
				else
				{
					board.decreaseProtectedPiecesCounter(getAffinity(), myPieceAtDestination.getValue() / 100);
				}
			}
			//Update new
			if(p != null)
			{
				if(!p.hasSameAffinityAs(myPiece))
				{
					p.addPieceThatTakesMeOver(myPiece);
				}
				else
				{
					board.increaseProtectedPiecesCounter(getAffinity(), p.getValue() / 100);
				}
			}
			myPieceAtDestination = p;
		}
	}
	
	protected boolean canBeMadeDefault(ChessBoard board)
	{
		Position newPosition = this.getPositionIfPerformed();
		if(newPosition == null)
			return false; //The move was out of bounds
		
		return canBeMadeEnding(board.getPiece(newPosition), board);
	}
	
	protected boolean canBeMadeEnding(Piece pieceAtDestination, ChessBoard board)
	{
		setPieceAtDestination(pieceAtDestination, board);
		if(pieceAtDestination == null)
			return true; //The space is free
		else if(pieceAtDestination.hasSameAffinityAs(myPiece))
		{
			//For DependantMoves this also means that moves further a long this move chain won't be possible either
			return false; //You can't take over your own pieces
		}
		else
		{
			//Take over is available
			return true;
		}
	}
	
	public Piece getPieceBlockingMe()
	{
		if(myPieceAtDestination != null && myPieceAtDestination.hasSameAffinityAs(myPiece))
			return myPieceAtDestination;
		
		return null;
	}
	
	public Piece getPiece()
	{
		return myPiece;
	}
	
	protected Piece getPieceAtDestination()
	{
		return myPieceAtDestination;
	}
	
	public boolean isPieceBlockingMe()
	{
		return getPieceBlockingMe() != null;
	}
	
	/**
	 * Override this for moves depending on other pieces not standing in the way
	 * @param ignoreIfPositionIsBlocked this position should not be considered when checking for blocking pieces (i.e simulating a pass-through piece)
	 * @param ignoreIfPositionIsBlocked2 this position should not be considered when checking for blocking pieces (i.e simulating that the piece asking is hovering)
	 * @return
	 */
	public boolean isPieceBlockingMe(Position ignoreIfPositionIsBlocked, Position ignoreIfPositionIsBlocked2)
	{
		return false;
	}
	
	/**
	 * 
	 * @return true if this move would take over one of the opponents pieces
	 */
	public boolean isTakeOverMove()
	{
		if(myPieceAtDestination != null)
			return !myPiece.hasSameAffinityAs(myPieceAtDestination);
		
		return false;
	}
	

	/**
	 * 
	 * @return the affinity of the piece making this move
	 */
	public boolean getAffinity()
	{
		return myPiece.getAffinity();
	}
	
	/**
	 * 
	 * @return the current position of the piece that this move belongs to
	 */
	public Position getCurrentPosition()
	{
		return myPiece.getCurrentPosition();
	}
	
	/**
	 * 
	 * @return the value of the piece at this move's destination
	 */
	public int getTakeOverValue()
	{
		if(isTakeOverMove())
		{
			return myPieceAtDestination.getValue();
		}
		return 0;
	}

	/**
	 * @return A positive change means that the piece is moving upwards the board
	 */
	public byte getRowChange()
	{
		return myRowChange;
	}
	/**
	 * @return A positive change means that the piece is moving towards the right
	 */
	public byte getColumnChange()
	{
		return myColumnChange;
	}
	
	/**	 
	 * @return the new position or null if the move isn't valid
	 */
	public Position getPositionIfPerformed()
	{
		return myDestination;
	}
	
	/**
	 * Updates the destination of this move due to another move by this piece
	 * @param board the board where the move took place
	 */
	public void updateDestination(ChessBoard board)
	{
		myPieceAtDestination = null;
		if(myDestination != null)
		{
			//The old destination where this move previously took us, remove it
			if(myCanBeMadeCache)
				board.removeAvailableMove(myDestination, myPiece, this);
			else
				board.removeNonAvailableMove(myDestination, myPiece, this);
				
		}
		
		Position currentPosition = myPiece.getCurrentPosition();
		byte newRow = (byte)(currentPosition.getRow()+myRowChange);
		byte newColumn = (byte)(currentPosition.getColumn()+myColumnChange);
		
		if(newRow < 0 || newColumn < 0 || newRow >= ChessBoard.BOARD_SIZE || newColumn >= ChessBoard.BOARD_SIZE)
		{
			//Out of bounds
			myDestination =  null;
		}
		else
			myDestination = new Position(newRow, newColumn);
	}
	
	/**
	 * Performs a full update for this move
	 * @param board
	 */
	public void updateMove(ChessBoard board)
	{
		this.updateDestination(board);
		this.updatePossibility(board);
	}
	
	/**
	 * Due to changes on the board this move's possiblity needs to be re-evaluated
	 * @param board
	 */
	public abstract void updatePossibility(ChessBoard board);
	
	public void removeFromBoard(ChessBoard chessBoard)
	{
		myIsRemoved = true;
		chessBoard.removeAvailableMove(myDestination, myPiece, this);
		chessBoard.removeNonAvailableMove(myDestination, myPiece, this);
	}
	
	public boolean isRemoved()
	{
		return myIsRemoved;
	}
	
	/**
	 * Re-enables this move so that it can be made again
	 */
	public void reEnable()
	{
		myIsRemoved = false;
	}
	
	/**
	 * Returns the cached possibility of this move (Also checks if the move is unavailable due to a check
	 * @return true if the move is allowed to do
	 */
	public boolean canBeMade(ChessBoard board)
	{
		if(isRemoved())
			return false;
		
		if(board.isMoveUnavailableDueToCheck(this))
			return false;
		
		if(myCanBeMadeCache)
		{			
			//TODO: could this be cached?
			return !isMoveUnavailableDueToCheckMate(board) && myCanBeMadeCache;
			
			//Checks if this piece is protecting the king from being taken
			/*Move kingThreateningMove = board.moveThreateningPosition(board.getKing(myPiece.getAffinity()).getCurrentPosition(), !myPiece.getAffinity(), myPiece);
			if(kingThreateningMove != null)
				return false;*/
		}
		return myCanBeMadeCache;
	}
	/**
	 * Check the board and return the result of the evaluation
	 * This function should also use setPieceAtDestination if the move would result in piece being overtaken
	 * @param board
	 * @return true if the move is allowed to do
	 */
	protected abstract boolean canBeMadeInternal(ChessBoard board);
	
	/**
	 * This checks if the move would end in check mate, because no other piece stands in the path a check-mating move that this piece stops right now
	 */
	public boolean isMoveUnavailableDueToCheckMate(ChessBoard board)
	{
		ImmutableSet<Move> kingThreateningMoves = board.getNonAvailableMoves(board.getKing(myPiece.getAffinity()).getCurrentPosition(), !myPiece.getAffinity());
		if(kingThreateningMoves.size() > 0)
		{
			for(Move threateningMove : kingThreateningMoves)
			{
				if(this.getPositionIfPerformed().equals(threateningMove.getCurrentPosition()))
				{
					//This is a take over move that would remove the threatening piece
					continue;
				}
				if(threateningMove instanceof DependantMove)
				{
					DependantMove move = ((DependantMove) threateningMove).getMoveThatIDependUpon();
					boolean myPieceIsStoppingCheckMate = false;
					boolean anotherPieceIsStoppingCheckMate = false;
					Position destinationForMove = null;
					while(move != null && (destinationForMove = move.getPositionIfPerformed()) != null)
					{
						if(destinationForMove.equals(myPiece.getCurrentPosition()))
							myPieceIsStoppingCheckMate = true;
						else if(board.getPiece(destinationForMove) != null)
							anotherPieceIsStoppingCheckMate = true;
						else if(destinationForMove.equals(this.getPositionIfPerformed()))
						{
							//this(move) is on the path for the checking move even if this move is performed (i.e still stopping check mate)
							anotherPieceIsStoppingCheckMate = true;
						}
						
						move = move.getMoveThatIDependUpon();
					}
					if(myPieceIsStoppingCheckMate && !anotherPieceIsStoppingCheckMate)
						return true; //This move would end in check mate, because no other piece stands in the path of the check-mating move
				}
			}
		}
		return false;
	}
	/**
	 * Makes this move and updates all the moves of all the pieces that will need to be updated on the given board
	 * @throws UnavailableMoveException if this move isn't available right now
	 */
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		if(!canBeMade(board))
		{
			throw new UnavailableMoveException(this);
		}
		if(myPieceAtDestination != null)
		{
			//Take over is happening
			myPieceAtDestination.removeFromBoard(board);
			myRevertingMove.setPieceAtOldPosition(myPieceAtDestination);
		}
		Position oldPosition = myPiece.getCurrentPosition().clone();
		board.movePiece(myPiece, this);
		myPiece.getCurrentPosition().applyMove(this);
		board.updatePossibilityOfMovesForPosition(oldPosition);
		board.updatePossibilityOfMovesForPosition(myPiece.getCurrentPosition());
	}
	
	@Override
	public String toString()
	{
		return myPiece + ": " + myPiece.getCurrentPosition() + " -> " + getPositionIfPerformed();
	}

	/**
	 * 
	 * @return a message explaining what this move did
	 */
	public String logMessageForLastMove() 
	{
		String log = myPiece.getDisplayName() + ": " + getRevertingMove().getPositionIfPerformed() + " -> " + myPiece.getCurrentPosition();
		if(getRevertingMove().getPieceThatITookOver() != null)
			log += " (Took over: " + getRevertingMove().getPieceThatITookOver().getDisplayName() + ")";
		return log;
	}
}
