package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

public class RockCastlingMovePart extends CastlingMovePart
{

	public RockCastlingMovePart(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith, final CastlingMove castlingMove)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, castlingMove);
	}

	@Override
	protected boolean canBeMadeInternal(final ChessBoard board)
	{
		//A castling move depends on unmoved pieces
		if(getPiece().getMovesMade() > 0)
		{
			return false;
		}

		if(getPieceAtDestination() == null)
		{
			//The space is free
			return true;
		}

		return false;
	}

	@Override
	public  boolean isPartOfAnotherMove()
	{
		return true;
	}
}
