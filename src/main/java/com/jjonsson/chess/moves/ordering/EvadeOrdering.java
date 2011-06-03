package com.jjonsson.chess.moves.ordering;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Piece;

/**
 * Compares the need for two moves to be made just to take their respective pieces to safety (or at least a new place)
 * It also compares the safety of the destination of the moves
 * @author jonatanjoensson
 *
 */
public class EvadeOrdering extends Ordering<Move>
{

	@Override
	public int compare(Move left, Move right)
	{
		Piece pieceThreateningLeftPiece = left.getPiece().getCheapestPieceThatTakesMeOver();
		Piece pieceThreateningRightPiece = right.getPiece().getCheapestPieceThatTakesMeOver();
		if(pieceThreateningLeftPiece != null && pieceThreateningRightPiece != null)
		{
			//TODO: if this is expensive it needs to be cached in a better way, but how?
			int movesThreateningLeftDestination = left.getPiece().getBoard().getNumberOfMovesThreateningPosition(left.getPositionIfPerformed(), !left.getAffinity(), left.getPiece());
			int movesThreateningRightDestination = right.getPiece().getBoard().getNumberOfMovesThreateningPosition(right.getPositionIfPerformed(), !right.getAffinity(), right.getPiece());
			
			return left.getPiece().getValue() - right.getPiece().getValue() + (movesThreateningRightDestination - movesThreateningLeftDestination);
			//return pieceThreateningRightPiece.getValue() - pieceThreateningLeftPiece.getValue();
		}
		else if(pieceThreateningLeftPiece != null)
		{
			int movesThreateningLeftDestination = left.getPiece().getBoard().getNumberOfMovesThreateningPosition(left.getPositionIfPerformed(), !left.getAffinity(), left.getPiece());
			return left.getPiece().getValue() - movesThreateningLeftDestination;
			//return pieceThreateningLeftPiece.getValue();
		}
		else if(pieceThreateningRightPiece != null)
		{
			int movesThreateningRightDestination = right.getPiece().getBoard().getNumberOfMovesThreateningPosition(right.getPositionIfPerformed(), !right.getAffinity(), right.getPiece());
			return -(right.getPiece().getValue() - movesThreateningRightDestination);
			//return -pieceThreateningRightPiece.getValue();
		}
		//No moves is threatening either of the pieces, return equal
		return 0;
	}

}
