package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

public class KnightMove extends IndependantMove 
{

	public KnightMove(int rowChange, int columnChange,Piece pieceThatTheMoveWillBeMadeWith) 
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}

	public boolean canBeMadeInternal(ChessBoard board) 
	{
		return canBeMadeDefault(board);
	}

}
