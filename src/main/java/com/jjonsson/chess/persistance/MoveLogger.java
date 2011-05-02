package com.jjonsson.chess.persistance;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import com.google.common.io.InputSupplier;
import com.google.common.io.OutputSupplier;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.ChessBoardListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.pieces.Piece;

public class MoveLogger implements MoveListener, ChessBoardListener
{
	private List<Move> myMadeMoves;
	public MoveLogger()
	{
		myMadeMoves = new LinkedList<Move>();
	}
	
	public void addMove(Move move)
	{
		myMadeMoves.add(move);
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
}
