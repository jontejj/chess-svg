package com.jjonsson.chess.moves;

import static com.jjonsson.utilities.Logger.LOGGER;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.pieces.Piece;

/**
 * A move that either cares about the possibility of another move or is a move that someone else cares about
 * @author jonatanjoensson
 *
 */
public abstract class DependantMove extends Move
{
	private DependantMove myMoveDependingOnMe;
	private DependantMove myMoveThatIDependUpon;

	public DependantMove(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith, final DependantMove moveDependingOnMe, final DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
		myMoveDependingOnMe = moveDependingOnMe;
		myMoveThatIDependUpon = moveThatIDependUpon;
	}

	/**
	 * Checks if it's necessary to look further down the move chain for possible moves
	 * @param board
	 * @return true if moves depending on this move may be made
	 */
	public boolean furtherMovesInChainMayBePossible(final ChessBoard board)
	{
		if(getDestination() == null)
		{
			//Check if we have gone out of the board
			return false;
		}

		//This move relies on the possibility of the previous move in the chain
		if(myMoveThatIDependUpon != null)
		{
			if(isAMoveThatIDependOnBlocked())
			{
				//Some previous move may have been either a take over or if a piece standing in the way, this means this move won't be possible
				return false;
			}

			if(board.getCurrentState() != ChessState.CHECK && !myMoveThatIDependUpon.canBeMade(board))
			{
				return false;
			}
		}
		return true;
	}

	private boolean isAMoveThatIDependOnBlocked()
	{
		//TODO(jontejj): Could this be cached?
		DependantMove move = this.getMoveThatIDependUpon();
		while(move != null)
		{
			//No jumping over pieces
			if(move.getPieceAtDestination() != null)
			{
				return true;
			}

			move = move.getMoveThatIDependUpon();
		}
		return false;
	}

	@Override
	public boolean isPieceBlockingMe(final Position ignoreIfPositionIsBlocked, final Position ignoreIfPositionIsBlocked2)
	{
		DependantMove move = this;
		while(move != null)
		{
			//No jumping over pieces
			if(move.getPieceAtDestination() != null
					&& !move.getPieceAtDestination().getCurrentPosition().equals(ignoreIfPositionIsBlocked)
					&& !move.getPieceAtDestination().getCurrentPosition().equals(ignoreIfPositionIsBlocked2))
			{
				return true;
			}

			move = move.getMoveThatIDependUpon();
		}
		return false;
	}

	private boolean canBeMadeDependantInternal(final ChessBoard board)
	{
		if(furtherMovesInChainMayBePossible(board))
		{
			return canBeMadeInternal(board);
		}

		return false;
	}

	@Override
	public void updateDestination(final ChessBoard board)
	{
		//Update myself
		updateDestinationInternal(board);
		//Update destination of moves that is dependent on this move
		updateDestinationDownwards(board);
	}

	protected void updateDestinationInternal(final ChessBoard board)
	{
		super.updateDestination(board);
	}

	private void updateDestinationDownwards(final ChessBoard board)
	{
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.updateDestinationInternal(board);
			myMoveDependingOnMe.updateDestinationDownwards(board);
		}
	}

	@Override
	public void updatePossibility(final ChessBoard board, final boolean updatePieceAtDestination)
	{
		//Update myself
		updatePossiblityInternal(board, updatePieceAtDestination);
		//Update moves that is dependent on this move
		updatePossibilityDownwards(board, updatePieceAtDestination);
	}

	private void updatePossiblityInternal(final ChessBoard board, final boolean updatePieceAtDestination)
	{
		boolean oldCanBeMade = myCanBeMadeCache;

		if(updatePieceAtDestination)
		{
			setOldPieceAtDestination(getPieceAtDestination());
			setPieceAtDestination(board.getPiece(getDestination()));
		}

		myCanBeMadeCache = canBeMadeDependantInternal(board);
		if(oldCanBeMade != myCanBeMadeCache || getDestination() != getPreviousDestination())
		{
			if(oldCanBeMade)
			{
				board.removeAvailableMove(getPreviousDestination(), getPiece(), this);
			}
			else
			{

				board.removeNonAvailableMove(getPreviousDestination(), getPiece(), this);
			}
			if(myCanBeMadeCache)
			{
				//The move is now possible
				board.addAvailableMove(getDestination(), getPiece(), this);
			}
			else
			{
				//The move isn't possible anymore
				board.addNonAvailableMove(getDestination(), getPiece(), this);
			}
			//Make sure that the previous destination is saved
			setDestination(getDestination());
		}
	}

	private void updatePossibilityDownwards(final ChessBoard board, final boolean updatePieceAtDestination)
	{
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.updatePossiblityInternal(board, updatePieceAtDestination);
			myMoveDependingOnMe.updatePossibilityDownwards(board, updatePieceAtDestination);
		}
	}

	@Override
	public void syncCountersWithBoard(final ChessBoard board)
	{
		//Sync myself
		syncCountersWithBoardInternal(board);
		//Sync moves that is dependent on this move
		syncCountersWithBoardDownwards(board);
	}

	private void syncCountersWithBoardInternal(final ChessBoard board)
	{
		super.syncCountersWithBoard(board);
	}

	private void syncCountersWithBoardDownwards(final ChessBoard board)
	{
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.syncCountersWithBoardInternal(board);
			myMoveDependingOnMe.syncCountersWithBoardDownwards(board);
		}
	}
	/**
	 * Copies the counters recursively
	 */
	@Override
	public void copyMoveCounter(final Move moveToCopyFrom)
	{
		super.copyMoveCounter(moveToCopyFrom);
		DependantMove moveDependingOnMe = getMoveDependingOnMe();
		DependantMove fromMove = null;
		try
		{
			fromMove = DependantMove.class.cast(moveToCopyFrom).getMoveDependingOnMe();
		}
		catch(ClassCastException cce)
		{
			LOGGER.warning("Could not cast: " + moveToCopyFrom + ", Move that tried to copy was: " + this);
			//It's okey to fail silently because a King or Rock is saved differently if they can't make a castling move
			return;
		}
		if(moveDependingOnMe != null && fromMove != null)
		{
			moveDependingOnMe.copyMoveCounter(fromMove);
		}
	}

	/**
	 * Resets the counters recursively
	 */
	@Override
	public void resetMoveCounter()
	{
		super.resetMoveCounter();
		if(getMoveDependingOnMe() != null)
		{
			getMoveDependingOnMe().resetMoveCounter();
		}
	}

	public List<Move> getPossibleMovesThatIsDependantOnMe(final ChessBoard board)
	{
		List<Move> dependantMoves = null;
		//Check if we have gone out of the board
		if(getDestination() != null)
		{
			//Performs a dive into the dependant moves and stops when it finds either null or when it's not possible to perform a move further down
			DependantMove move = myMoveDependingOnMe;
			while(move != null)
			{
				if(move.canBeMade(board))
				{
					if(dependantMoves == null)
					{
						dependantMoves = Lists.newArrayList();
					}

					dependantMoves.add(move);
				}
				if(!move.furtherMovesInChainMayBePossible(board))
				{
					break;
				}

				move = move.getMoveDependingOnMe();
			}
		}
		if(dependantMoves == null)
		{
			return Collections.emptyList();
		}

		return dependantMoves;
	}


	public List<Move> getNonPossibleMovesThatIsDependantOnMe(final ChessBoard board)
	{
		DependantMove move = myMoveDependingOnMe;
		if(move == null)
		{
			return Collections.emptyList();
		}

		List<Move> dependantMoves = Lists.newArrayList();
		while(move != null)
		{
			if(!move.canBeMade(board))
			{
				dependantMoves.add(move);
			}
			move = move.getMoveDependingOnMe();
		}
		return dependantMoves;
	}

	/**
	 * 
	 * @return regardless if the moves can be made or not
	 */
	public List<Move> getMovesThatIsDependantOnMe()
	{
		List<Move> dependantMoves = Lists.newArrayList();
		//Performs a dive into the dependant moves and stops when it finds either null or when it's not possible to perform a move further down
		DependantMove move = myMoveDependingOnMe;
		while(move != null)
		{
			dependantMoves.add(move);
			move = move.getMoveDependingOnMe();
		}
		return dependantMoves;
	}

	public DependantMove getMoveDependingOnMe()
	{
		return myMoveDependingOnMe;
	}

	public DependantMove getMoveThatIDependUpon()
	{
		return myMoveThatIDependUpon;
	}

	public void setMoveThatIDependUpon(final DependantMove move)
	{
		myMoveThatIDependUpon = move;
	}

	public void setMoveThatDependsOnMe(final DependantMove move)
	{
		myMoveDependingOnMe = move;
	}


	/**
	 * Moves depending on this one will need to be disabled as well
	 */
	@Override
	public void disable(final ChessBoard chessBoard)
	{
		super.disable(chessBoard);
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.disable(chessBoard);
		}
	}

	@Override
	public void reEnable()
	{
		super.reEnable();
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.reEnable();
		}
	}
}
