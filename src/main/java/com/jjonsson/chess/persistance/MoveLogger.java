package com.jjonsson.chess.persistance;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.NoSuchElementException;

import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.ChessBoardListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.pieces.Piece;

public class MoveLogger implements MoveListener, ChessBoardListener
{
	private Deque<Move> myMoveHistory;
	public MoveLogger()
	{
		myMoveHistory = new ArrayDeque<Move>();
	}
	
	public void clear()
	{
		myMoveHistory.clear();
	}
	
	public void addMove(Move move)
	{
		myMoveHistory.push(move);
	}
	
	public Move popMove() throws NoSuchElementException
	{
		return myMoveHistory.pop();
	}
	
	public Move getLastMove()
	{
		return myMoveHistory.peek();
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
			addMove(performedMove);
	}

	@Override
	public void pieceRemoved(Piece removedPiece)
	{
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
}
