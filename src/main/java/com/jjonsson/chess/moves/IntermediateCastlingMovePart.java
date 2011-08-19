package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

/**
 * This is not really a move, but it exists so that the castling possibility can be updated transparently with the other moves
 */
public class IntermediateCastlingMovePart extends CastlingMovePart
{

	public IntermediateCastlingMovePart(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith, final CastlingMove castlingMove)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, castlingMove);
	}

	@Override
	public boolean makeMove(final ChessBoard board)
	{
		//This makes the error handling handle this move as it couldn't be made and that is exactly how it should be
		return false;
	}

	@Override
	protected boolean canBeMadeInternal(final ChessBoard board)
	{
		if(board.getPiece(getDestination()) != null)
		{
			//For Queen Side castling moves there should be a free square over as there are three squares between the Rock and the King
			return false;
		}
		return true;
	}

}
