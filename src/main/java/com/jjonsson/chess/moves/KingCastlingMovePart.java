package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

public class KingCastlingMovePart extends CastlingMovePart
{

	public KingCastlingMovePart(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}
	
	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
	{
		//A castling move depends on unmoved pieces
		if(getPiece().getMovesMade() > 0)
		{
			return false;
		}
		
		if(board.getAvailableMoves(getCurrentPosition(), !getAffinity()).size() > 0)
		{
			//Castling moves should be unavailable when in check
			return false;
		}
		
		//The Rock is going to protect the King if the threatening piece is standing on the same row
		Move threateningMove = board.moveThreateningPosition(this.getDestination(), !getAffinity(), getPiece(), false);
		
		if(threateningMove != null)
		{
			//If this is true for all the king's moves then the game is over
			return false;
		}
		
		if(getPieceAtDestination() == null)
		{
			//The space is free
			return true; 
		}
		
		return false;
	}

}
