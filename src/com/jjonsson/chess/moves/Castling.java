package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

public class Castling extends IndependantMove
{

	/**
	 * 
	 * @param rowChange
	 * @param columnChange
	 * @param pieceThatTheMoveWillBeMadeWith the king for the player that want's to do the move
	 */
	public Castling(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}

	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
	{
		// TODO Auto-generated method stub
		return false;
	}

}
