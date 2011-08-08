package com.jjonsson.chess.persistance;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.listeners.ChessBoardListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.pieces.Piece;

public class MoveLogger implements MoveListener, ChessBoardListener
{
	private Deque<Move> myMoveHistory;
	private Map<Integer, Piece> myRemovalHistory;
	
	/**
	 * Defines how many moves that are remembered so that the game doesn't get stuck in a repetitive loop
	 */
	@VisibleForTesting
	public static final int REPITION_HISTORY_RESET_INTERVAL = 30;
	public MoveLogger()
	{
		myMoveHistory = new ArrayDeque<Move>();
		myRemovalHistory = Maps.newHashMap();
	}
	
	public void clear()
	{
		myMoveHistory.clear();
		myRemovalHistory.clear();
	}
	
	public void addMove(Move move)
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

	@Override
	public void movePerformed(Move performedMove)
	{
		Move lastMove = myMoveHistory.peekFirst();
		if(lastMove instanceof PawnTwoStepMove)
		{
			((PawnTwoStepMove)lastMove).removeEnpassantMoves(performedMove.getPiece().getBoard());
		}
		if(!(performedMove instanceof RevertingMove))
		{
			addMove(performedMove);
			//We need to reset the move counters periodically to avoid the measurements from getting off the charts
			if(myMoveHistory.size() % REPITION_HISTORY_RESET_INTERVAL == 0)
			{
				performedMove.getPiece().getBoard().resetMoveCounters();
			}
		}
	}
	
	public Piece getRemovedPieceForLastMove()
	{
		return myRemovalHistory.get(myMoveHistory.size() - 1);
	}

	@Override
	public void pieceRemoved(Piece removedPiece)
	{
		if(!removedPiece.isPawnReplacementPiece())
		{
			myRemovalHistory.put(myMoveHistory.size(), removedPiece);
		}
	}

	@Override
	public void piecePlaced(Piece p)
	{
	}
	
	

	@Override
	public void gameStateChanged(ChessState newState)
	{
	}

	@Override
	public void piecePlacedLoadingInProgress(Piece p)
	{
	}

	@Override
	public void loadingOfBoardDone()
	{
	}

	@Override
	public void nextPlayer()
	{
	}

	@Override
	public boolean supportsPawnReplacementDialog() 
	{
		return false;
	}

	@Override
	public Piece getPawnReplacementFromDialog() 
	{
		return null;
	}

	@Override
	public void undoDone()
	{
	}

	@Override
	public void squareScores(ImmutableMap<Position, String> positionScores)
	{
	}
}
