package com.jjonsson.chess.moves;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
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
	
	public DependantMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
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
	public boolean furtherMovesInChainMayBePossible(ChessBoard board)
	{
		//Check if we have gone out of the board
		if(getPositionIfPerformed() == null)
			return false;
		
		//This move relies on the possibility of the previous move in the chain
		if(myMoveThatIDependUpon != null)
		{
			//Some previous move may have been either a take over or if a piece standing in the way, this means this move won't be possible 
			if(isAMoveThatIDependOnBlocked())
				return false;
			
			if(board.getCurrentState() != ChessState.CHECK && !myMoveThatIDependUpon.canBeMade(board))
				return false;
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
				return true;
			
			move = move.getMoveThatIDependUpon();
		}
		return false;
	}
	
	public boolean isPieceBlockingMe(Position ignoreIfPositionIsBlocked, Position ignoreIfPositionIsBlocked2)
	{
		DependantMove move = this;
		while(move != null)
		{
			//No jumping over pieces
			if(move.getPieceAtDestination() != null 
					&& !move.getPieceAtDestination().getCurrentPosition().equals(ignoreIfPositionIsBlocked)
					 && !move.getPieceAtDestination().getCurrentPosition().equals(ignoreIfPositionIsBlocked2))
				return true;
			
			move = move.getMoveThatIDependUpon();
		}
		return false;
	}

	private boolean canBeMadeDependantInternal(ChessBoard board)
	{
		if(furtherMovesInChainMayBePossible(board))
			return canBeMadeInternal(board);
		
		return false;	
	}
	
	@Override
	public void updateDestination(ChessBoard board)
	{
		//First update destination of moves that this move is dependent on
		updateDestinationUpwards(board);
		//Update myself
		updateDestinationInternal(board);
		//Update destination of moves that is dependent on this move
		updateDestinationDownwards(board);
	}
	
	protected void updateDestinationInternal(ChessBoard board)
	{
		super.updateDestination(board);
	}
	
	private void updateDestinationUpwards(ChessBoard board)
	{
		if(myMoveThatIDependUpon != null)
		{	
			myMoveThatIDependUpon.updateDestinationUpwards(board);
			myMoveThatIDependUpon.updateDestinationInternal(board);
		}
	}
	
	private void updateDestinationDownwards(ChessBoard board)
	{
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.updateDestinationInternal(board);
			myMoveDependingOnMe.updateDestinationDownwards(board);
		}
	}

	@Override
	public void updatePossibility(ChessBoard board)
	{
		//First update moves that this move is dependent on
		updatePossibilityUpwards(board);
		//Update myself
		updatePossiblityInternal(board);
		//Update moves that is dependent on this move
		updatePossibilityDownwards(board);
	}
	
	private void updatePossibilityUpwards(ChessBoard board)
	{
		if(myMoveThatIDependUpon != null)
		{	
			myMoveThatIDependUpon.updatePossibilityUpwards(board);
			myMoveThatIDependUpon.updatePossiblityInternal(board);
		}
	}
	
	private void updatePossiblityInternal(ChessBoard board)
	{
		myCanBeMadeCache = canBeMadeDependantInternal(board);
		if(myCanBeMadeCache)
		{
			//The move is now possible
			board.addAvailableMove(myDestination, myPiece, this);
			myMovesThatHasChangedMyPossibility.remove(board.getLastMove());
		}
		else
		{
			board.addNonAvailableMove(myDestination, myPiece, this);
			myMovesThatHasChangedMyPossibility.put(board.getLastMove(), board.getMoveLogger().getLatestMoves(5));
		}
	}

	private void updatePossibilityDownwards(ChessBoard board)
	{
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.updatePossiblityInternal(board);
			myMoveDependingOnMe.updatePossibilityDownwards(board);
		}
	}
	
	@Override
	protected void syncCountersWithBoard(ChessBoard board)
	{
		//First sync moves that this move is dependent on
		syncCountersWithBoardUpwards(board);
		//Sync myself
		syncCountersWithBoardInternal(board);
		//Sync moves that is dependent on this move
		syncCountersWithBoardDownwards(board);
	}
	
	private void syncCountersWithBoardUpwards(ChessBoard board)
	{
		if(myMoveThatIDependUpon != null)
		{	
			myMoveThatIDependUpon.syncCountersWithBoardUpwards(board);
			myMoveThatIDependUpon.syncCountersWithBoardInternal(board);
		}
	}

	private void syncCountersWithBoardInternal(ChessBoard board)
	{
		super.syncCountersWithBoard(board);
	}

	private void syncCountersWithBoardDownwards(ChessBoard board)
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
	public void copyMoveCounter(Move moveToCopyFrom)
	{
		super.copyMoveCounter(moveToCopyFrom);
		DependantMove moveDependingOnMe = getMoveDependingOnMe();
		if(moveDependingOnMe != null)
		{
			moveDependingOnMe.copyMoveCounter(DependantMove.class.cast(moveToCopyFrom).getMoveDependingOnMe());
		}
	}
	
	/**
	 * Resets the counters recursively
	 */
	public void resetMoveCounter()
	{
		super.resetMoveCounter();
		if(getMoveDependingOnMe() != null)
		{
			getMoveDependingOnMe().resetMoveCounter();
		}
	}
	
	public List<Move> getPossibleMovesThatIsDependantOnMe(ChessBoard board)
	{
		List<Move> dependantMoves = null;
		//Check if we have gone out of the board
		if(myDestination != null)
		{
			//Performs a dive into the dependant moves and stops when it finds either null or when it's not possible to perform a move further down
			DependantMove move = myMoveDependingOnMe;
			while(move != null)
			{
				if(move.canBeMade(board))
				{
					if(dependantMoves == null)
						dependantMoves = Lists.newArrayList();
					
					dependantMoves.add(move);
				}
				if(!move.furtherMovesInChainMayBePossible(board))
					break;
				
				move = move.getMoveDependingOnMe();
			}
		}
		if(dependantMoves == null)
			return Collections.emptyList();
		
		return dependantMoves;
	}
	

	public List<Move> getNonPossibleMovesThatIsDependantOnMe(ChessBoard board)
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
	
	public DependantMove getMoveDependingOnMe()
	{
		return myMoveDependingOnMe;
	}
	
	public DependantMove getMoveThatIDependUpon()
	{
		return myMoveThatIDependUpon;
	}
	
	public void setMoveThatIDependUpon(DependantMove move)
	{
		myMoveThatIDependUpon = move;
	}
	
	public void setMoveThatDependsOnMe(DependantMove move)
	{
		myMoveDependingOnMe = move;
	}
	
	
	/**
	 * Moves depending on this one will need to be removed as well
	 */
	@Override
	public void removeFromBoard(ChessBoard chessBoard)
	{
		super.removeFromBoard(chessBoard);
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.removeFromBoard(chessBoard);
		}
	}
	
	@Override
	public void reEnable()
	{
		myIsRemoved = false;
		if(myMoveDependingOnMe != null)
		{
			myMoveDependingOnMe.reEnable();
		}
	}
}
