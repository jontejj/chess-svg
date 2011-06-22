package com.jjonsson.chess.persistance;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.ChessBoardListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.pieces.Piece;

public class MoveLogger implements MoveListener, ChessBoardListener
{
	private Deque<Move> myMoveHistory;
	private Map<Integer, Piece> myRemovalHistory;
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
	
	public Move popMove() throws NoSuchElementException
	{
		return myMoveHistory.pop();
	}
	
	/**
	 * This also sets the correct piece to restore if this move took over a piece for the returned move's RevertingMove
	 * @return
	 */
	public Move getLastMove()
	{
		Move lastMove = myMoveHistory.peekFirst();
		if(lastMove != null)
		{
			Piece removedPiece = myRemovalHistory.get(myMoveHistory.size() - 1);
			lastMove.getRevertingMove().setPieceToPlaceAtOldPosition(removedPiece);
		}
		return myMoveHistory.peekFirst();
	}
	
	public void writeMoves(OutputSupplier<OutputStream> out)
	{
		//out.getOutput().write(b);
	}
	
	public void performMoves(InputSupplier<InputStream> in)
	{
		//in.getInput().
	}

	@Override
	public void movePerformed(Move performedMove)
	{
		if(!(performedMove instanceof RevertingMove))
		{
			addMove(performedMove);
			//We need to reset the move counters periodically to avoid the measurements from getting off the charts
			if(myMoveHistory.size() % 30 == 0)
			{
				performedMove.getPiece().getBoard().resetMoveCounters();
			}
		}
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
	
	/**
	 * 
	 * @param numberOfMoves the maximum amount of moves to return
	 * @return a sorted list of the moves that has been made (the first element in the list is the last move that was made)
	 */
	public ImmutableList<Move> getLatestMoves(int numberOfMoves)
	{
		List<Move> moves = Lists.newArrayList();
		for(Move m : myMoveHistory)
		{
			if(moves.size() >= numberOfMoves)
				break;
			moves.add(m);
		}
		return ImmutableList.copyOf(moves);
	}
}
