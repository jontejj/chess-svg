package com.jjonsson.chess.moves;

import java.util.Collection;

import com.jjonsson.chess.board.ChessBoard;
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
	private Piece myPiece;
	/**
	 * The piece that would be defeated if this move was performed
	 */
	private Piece myPieceAtDestination;

	private Piece myOldPieceAtDestination;

	/**
	 * The cached position of this move's destination
	 */
	private ImmutablePosition myDestination;

	private ImmutablePosition myPreviousDestination;

	/**
	 * Cached decision of whether or not this move can be made
	 */
	protected boolean myCanBeMadeCache;

	/**
	 * True if the syncCountersWithBoard function changed the counters on the board
	 */
	private boolean myChangedCountersDuringLastSync;

	protected RevertingMove myRevertingMove;

	private boolean myIsRemoved;

	/**
	 * Holds the number of moves made with this move
	 */
	private long myMovesMade;

	private int myFirstDimensionIndex;
	private int mySecondDimensionIndex;

	/**
	 * 
	 * @param rowChange the row change for this move, valid numbers are (-7) to (+7)
	 * @param columnChange the column change for this move, valid numbers are (-7) to (+7)
	 */
	public Move(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith)
	{
		myRowChange = (byte)rowChange;
		myColumnChange = (byte)columnChange;
		myPiece = pieceThatTheMoveWillBeMadeWith;
		setRevertingMove();
		myFirstDimensionIndex = getFirstDimensionIndexInternal();
		mySecondDimensionIndex = getSecondDimensionIndexInternal();
		pieceThatTheMoveWillBeMadeWith.addToMoveTable(this);
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
	public void setPieceAtDestination(final Piece p)
	{
		myPieceAtDestination = p;
	}

	protected boolean canBeMadeDefault()
	{
		if(this.getDestination() == null)
		{
			//The move was out of bounds
			return false;
		}

		return canBeMadeEnding();
	}

	protected boolean canBeMadeEnding()
	{
		if(getPieceAtDestination() != null && getPieceAtDestination().hasSameAffinityAs(myPiece))
		{
			//For DependantMoves this also means that moves further a long this move chain won't be possible either
			return false; //You can't take over your own pieces
		}
		//The space is either free or a take over is available
		return true;
	}

	public Piece getPiece()
	{
		return myPiece;
	}

	public Piece getPieceAtDestination()
	{
		return myPieceAtDestination;
	}

	public void setOldPieceAtDestination(final Piece oldPiece)
	{
		myOldPieceAtDestination = oldPiece;
	}

	/**
	 * Override this for moves depending on other pieces not standing in the way
	 * @param ignoreIfPositionIsBlocked this position should not be considered when checking for blocking pieces (i.e simulating a pass-through piece)
	 * @param ignoreIfPositionIsBlocked2 this position should not be considered when checking for blocking pieces (i.e simulating that the piece asking is hovering)
	 * @return true if this move isn't possible to do because of piece standing in the way
	 */
	public abstract boolean isPieceBlockingMe(Position ignoreIfPositionIsBlocked, Position ignoreIfPositionIsBlocked2);

	/**
	 * 
	 * @return true if this move would take over one of the opponents pieces
	 */
	public boolean isTakeOverMove()
	{
		if(myPieceAtDestination != null)
		{
			return !myPiece.hasSameAffinityAs(myPieceAtDestination);
		}

		return false;
	}

	/**
	 * 
	 * @return true if this move can take over another piece when it's standing at this move's destination
	 */
	public boolean canBeTakeOverMove()
	{
		return true;
	}

	/**
	 * 
	 * @param newMovesMadeCount the new value for how many times this move has been made
	 */
	public void setMovesMade(final long newMovesMadeCount)
	{
		myMovesMade = newMovesMadeCount;
	}

	/**
	 * 
	 * @return how many times this move has been made
	 */
	public long getMovesMade()
	{
		return myMovesMade;
	}

	/**
	 * Copies the move counter from the given move
	 * @param moveToCopyFrom
	 */
	public void copyMoveCounter(final Move moveToCopyFrom)
	{
		myMovesMade = moveToCopyFrom.getMovesMade();
	}

	/**
	 * Could be called periodically to reset the repetitiveness protection
	 */
	public void resetMoveCounter()
	{
		myMovesMade = 0;
	}

	/**
	 * 
	 * @return the affinity of the piece making this move
	 */
	public boolean getAffinity()
	{
		return getPiece().getAffinity();
	}

	/**
	 * 
	 * @return the current position of the piece that this move belongs to
	 */
	public ImmutablePosition getCurrentPosition()
	{
		return getPiece().getCurrentPosition();
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
	 * 
	 * @return how progressive this move is (e.g a pawn move is very progressive as it leads to pawn replacements)
	 */
	public int getProgressiveValue()
	{
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
	public ImmutablePosition getDestination()
	{
		return myDestination;
	}

	public ImmutablePosition getPreviousDestination()
	{
		return myPreviousDestination;
	}

	public void updatePreviousDestination()
	{
		myPreviousDestination = myDestination;
	}

	public void setDestination(final ImmutablePosition newDestination)
	{
		myPreviousDestination = myDestination;
		myDestination = newDestination;
	}

	/**
	 * Updates the destination of this move due to another move by this piece
	 * @param board the board where the move took place
	 */
	public void updateDestination(final ChessBoard board)
	{
		Position currentPosition = myPiece.getCurrentPosition();
		byte newRow = (byte)(currentPosition.getRow()+myRowChange);
		byte newColumn = (byte)(currentPosition.getColumn()+myColumnChange);

		if(Position.isInvalidPosition(newRow, newColumn))
		{
			setDestination(null);
		}
		else
		{
			setDestination(ImmutablePosition.from(newRow, newColumn));
		}
	}

	/**
	 * Performs an update of the possibility for this move
	 * @param board
	 */
	public void updateMove(final ChessBoard board)
	{
		this.updateDestination(board);
		this.updatePossibility(board, true);
		this.syncCountersWithBoard(board);
	}

	public void syncCountersWithBoard(final ChessBoard board)
	{
		//Only update the counters if there has been a change
		if(myOldPieceAtDestination != myPieceAtDestination || myChangedCountersDuringLastSync)
		{
			//Clear old
			if(myOldPieceAtDestination != null && myChangedCountersDuringLastSync)
			{
				if(!myOldPieceAtDestination.hasSameAffinityAs(myPiece))
				{
					myOldPieceAtDestination.removePieceThatTakesMeOver(myPiece);
					board.decreaseTakeOverPiecesCounter(getAffinity(), myOldPieceAtDestination.getTakeOverImportanceValue());
				}
				else
				{
					board.decreaseProtectedPiecesCounter(getAffinity(), myOldPieceAtDestination.getProtectImportanceValue());
				}
			}
			myChangedCountersDuringLastSync = false;
			//Update new
			if(myPieceAtDestination != null)
			{
				if(!myPieceAtDestination.hasSameAffinityAs(myPiece))
				{
					if(myCanBeMadeCache)
					{
						myPieceAtDestination.addPieceThatTakesMeOver(myPiece);
						board.increaseTakeOverPiecesCounter(getAffinity(), myPieceAtDestination.getTakeOverImportanceValue());
						myChangedCountersDuringLastSync = true;
					}
				}
				else
				{
					//You are only protecting a piece if you can move there when the piece you protect doesn't stand there
					if(!this.isPieceBlockingMe(getCurrentPosition(), myPieceAtDestination.getCurrentPosition()))
					{
						board.increaseProtectedPiecesCounter(getAffinity(), myPieceAtDestination.getProtectImportanceValue());
						myChangedCountersDuringLastSync = true;
					}
				}
			}
		}
	}

	/**
	 * Due to changes on the board this move's possibility needs to be re-evaluated
	 * @param board
	 */
	public abstract void updatePossibility(ChessBoard board, boolean updatePieceAtDestination);

	public void disable(final ChessBoard chessBoard)
	{
		myIsRemoved = true;
		removeFromBoard(chessBoard);
		myCanBeMadeCache = false;
		myOldPieceAtDestination = myPieceAtDestination;
		myPieceAtDestination = null;
	}

	public void removeFromBoard(final ChessBoard chessBoard)
	{
		if(myCanBeMadeCache)
		{
			chessBoard.removeAvailableMove(myDestination, myPiece, this);
		}
		else
		{
			chessBoard.removeNonAvailableMove(myDestination, myPiece, this);
		}
		myPreviousDestination = null;
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
	 * Called when this move is the last one that was made on the board, i.e when a move has been reverted.
	 */
	public void onceAgainLastMoveThatWasMade(final ChessBoard board)
	{

	}

	/**
	 * Returns the cached possibility of this move (Also checks if the move is unavailable due to a check
	 * @return true if the move is allowed to do
	 */
	public boolean canBeMade(final ChessBoard board)
	{
		if(isRemoved())
		{
			return false;
		}

		if(myCanBeMadeCache && !isPartOfAnotherMove())
		{
			if(board.isMoveUnavailableDueToCheck(this))
			{
				return false;
			}

			//TODO(jontejj): could this be cached?
			return !isMoveUnavailableDueToCheckMate(board);

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
	 * This checks if the move would end in check mate, because no other piece stands in the path of a check-mating move that this piece stops right now
	 */
	public boolean isMoveUnavailableDueToCheckMate(final ChessBoard board)
	{
		Collection<Move> kingThreateningMoves = board.getNonAvailableMoves(board.getKing(myPiece.getAffinity()).getCurrentPosition(), !myPiece.getAffinity());
		for(Move threateningMove : kingThreateningMoves)
		{
			if(this.getDestination().equals(threateningMove.getCurrentPosition()))
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
				while(move != null)
				{
					destinationForMove = move.getDestination();
					if(destinationForMove == null)
					{
						break;
					}
					else if(destinationForMove.equals(myPiece.getCurrentPosition()))
					{
						myPieceIsStoppingCheckMate = true;
					}
					else if(move.getPieceAtDestination() != null)
					{
						anotherPieceIsStoppingCheckMate = true;
					}
					else if(destinationForMove.equals(this.getDestination()))
					{
						//this(move) is on the path for the checking move even if this move is performed (i.e still stopping check mate)
						anotherPieceIsStoppingCheckMate = true;
					}

					move = move.getMoveThatIDependUpon();
				}
				if(myPieceIsStoppingCheckMate && !anotherPieceIsStoppingCheckMate)
				{
					//This move would end in check mate, because no other piece stands in the path of the check-mating move
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Makes this move and updates all the moves of all the pieces that will need to be updated on the given board
	 * @throws UnavailableMoveException if this move isn't available right now
	 */
	public void makeMove(final ChessBoard board) throws UnavailableMoveException
	{
		if(!canBeMade(board))
		{
			//TODO: this throws too often, return boolean instead
			throw new UnavailableMoveException(this);
		}
		if(myPieceAtDestination != null)
		{
			//Take over is happening
			myPieceAtDestination = myPieceAtDestination.removeFromBoard(board);
		}

		board.movePiece(myPiece, this);
		myPiece.updateCurrentPosition(this);
		myMovesMade++;
		getPiece().setMovesMade(getPiece().getMovesMade() + 1);
	}

	@Override
	public String toString()
	{
		return myPiece + ": " + myPiece.getCurrentPosition() + " -> " + getDestination();
	}

	/**
	 * Used to know how many moves to undo during an undo moves action
	 * @return true if this move is part of another move (such as a {@link CastlingMove})
	 */
	public boolean isPartOfAnotherMove()
	{
		return false;
	}

	/**
	 * Used when initializing the move table
	 * @return true if this move should be returned by the getMove(rowChange, columnChange) function
	 */
	public boolean shouldBeIncludedInMoveTable()
	{
		return true;
	}

	/**
	 * 
	 * @return a message explaining what this move did
	 */
	public String logMessageForLastMove()
	{
		String log = myPiece.getDisplayName() + ": " + getRevertingMove().getDestination() + " -> " + myPiece.getCurrentPosition();
		Piece removedPiece = getPiece().getBoard().getMoveLogger().getRemovedPieceForLastMove();
		if(removedPiece != null)
		{
			log += " (Took over: " + removedPiece.getDisplayName() + ")";
		}
		return log;
	}

	/**
	 * @return the sum of getTakeOverImportanceValue for all the pieces that can take over the piece at my destination
	 */
	public long getAccumulatedTakeOverValuesForPieceAtDestination()
	{
		if(isTakeOverMove())
		{
			return getPieceAtDestination().getAccumulatedTakeOverImportanceValue();
		}
		return 0;
	}

	/**
	 * @return if this move was the last one to be made this will
	 * return the position where the piece previously was at, if it wasn't the returned position will be erroronous
	 */
	public ImmutablePosition getOldPosition()
	{
		return getRevertingMove().getDestination();
	}

	/**
	 * 
	 * @return a value between 0-7 (inclusive) that binds the row/column change of this move to a particular row index in a two
	 * dimensional array. -1 is returned if columnChange and rowChange are zero but that should never happen.
	 */
	protected int getFirstDimensionIndexInternal()
	{
		if(myRowChange > 0)
		{
			if(myColumnChange == 0)
			{
				return 0;
			}
			else if(myColumnChange > 0)
			{
				return 1;
			}
			else if(myColumnChange < 0)
			{
				return 2;
			}
		}
		else if(myRowChange == 0)
		{
			if(myColumnChange > 0)
			{
				return 3;
			}
			else if(myColumnChange < 0)
			{
				return 4;
			}
		}
		else
		{
			if(myColumnChange == 0)
			{
				return 5;
			}
			else if(myColumnChange > 0)
			{
				return 6;
			}
			else
			{
				return 7;
			}
		}
		//Detect faulty moves early
		return -1;
	}

	/**
	 * 
	 * @return a value between 0-7 (inclusive) that binds the row/column change of this move to a particular row index in a two
	 * dimensional array. -1 is returned if columnChange and rowChange is zero but that should never happen.
	 */
	public int getFirstDimensionIndex()
	{
		return myFirstDimensionIndex;
	}
	/**
	 * 
	 * @return a value between 0-7 (inclusive) that binds the row/column change of this move to a particular column index in a two
	 * dimensional array.
	 */
	protected int getSecondDimensionIndexInternal()
	{
		return Math.max(Math.abs(myColumnChange), Math.abs(myRowChange));
	}

	/**
	 * 
	 * @return a value between 0-7 (inclusive) that binds the row/column change of this move to a particular column index in a two
	 * dimensional array.
	 */
	public int getSecondDimensionIndex()
	{
		return mySecondDimensionIndex;
	}
}
