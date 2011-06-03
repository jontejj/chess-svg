package com.jjonsson.chess.moves.ordering;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Piece;

/**
 * Compares the need for two moves to be made just to take their respective pieces to safety (or at least a new place)
 * @author jonatanjoensson
 *
 */
public class EvadeOrdering extends Ordering<Move>
{

	@Override
	public int compare(Move left, Move right)
	{
		Piece pieceThreateningLeftPiece = left.getPiece().getCheapestPieceThatTakesMeOver();
		Piece pieceThreateningRightPiece = left.getPiece().getCheapestPieceThatTakesMeOver();
		if(pieceThreateningLeftPiece != null && pieceThreateningRightPiece != null)
		{
			return pieceThreateningRightPiece.getValue() - pieceThreateningLeftPiece.getValue();
		}
		else if(pieceThreateningLeftPiece != null)
		{
			return pieceThreateningLeftPiece.getValue();
		}
		else if(pieceThreateningRightPiece != null)
		{
			return -pieceThreateningRightPiece.getValue();
		}
		//No moves is threatening either of the pieces, return equal
		return 0;
	}

}
