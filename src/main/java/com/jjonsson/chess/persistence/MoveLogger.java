package com.jjonsson.chess.persistence;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.jjonsson.chess.listeners.MoveListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.pieces.Piece;

public class MoveLogger implements MoveListener
{
	private Deque<Move> myMoveHistory;
	private Map<Integer, Piece> myRemovalHistory;

	/**
	 * Defines how many moves (for each move) that are remembered so that the game doesn't get stuck in a repetitive loop
	 */
	@VisibleForTesting
	public static final int REPITION_HISTORY_RESET_INTERVAL = 30;

	private int myMovesMadeOffset;

	MoveLogger()
	{
		myMoveHistory = new ArrayDeque<Move>();
		myRemovalHistory = Maps.newHashMap();
	}

	@Override
	public void reset()
	{
		myMoveHistory.clear();
		myRemovalHistory.clear();
	}

	public void setMovesMadeOffset(final int movesMade)
	{
		myMovesMadeOffset = movesMade;
	}

	private void addMove(final Move move)
	{
		myMoveHistory.push(move);
	}

	public Move popMove()
	{
		return myMoveHistory.pop();
	}

	/**
	 * This also sets the correct piece to restore if this move took over a piece for the returned move's RevertingMove
	 * @return the last move that was made on the connected board or null if no moves has been made
	 */
	public Move getLastMove()
	{
		Move lastMove = myMoveHistory.peekFirst();
		if(lastMove != null)
		{
			Piece removedPiece = myRemovalHistory.get(myMoveHistory.size() - 1);
			lastMove.getRevertingMove().setPieceToPlaceAtOldPosition(removedPiece);
		}
		return lastMove;
	}

	public int getMovesMade()
	{
		return myMoveHistory.size() + myMovesMadeOffset;
	}

	private void removeLastEnPassant()
	{
		Move lastMove = myMoveHistory.peekFirst();
		if(lastMove instanceof PawnTwoStepMove)
		{
			((PawnTwoStepMove)lastMove).removeEnpassantMoves(lastMove.getPiece().getBoard());
		}
	}

	@Override
	public void movePerformed(final Move performedMove)
	{
		removeLastEnPassant();
		addMove(performedMove);
		//We need to reset the move counters periodically to avoid the measurements from getting off the charts
		if(getMovesMade() % REPITION_HISTORY_RESET_INTERVAL == 0)
		{
			performedMove.getPiece().getBoard().resetMoveCounters();
		}
	}

	@Override
	public void moveReverted(final RevertingMove move)
	{
		myRemovalHistory.remove(myMoveHistory.size());
		removeLastEnPassant();
	}

	public Piece getRemovedPieceForLastMove()
	{
		return myRemovalHistory.get(myMoveHistory.size() - 1);
	}

	@Override
	public void pieceRemoved(final Piece removedPiece)
	{
		if(!removedPiece.isPromoted())
		{
			myRemovalHistory.put(myMoveHistory.size(), removedPiece);
		}
	}
}
