package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;

public class ChainMove extends DependantMove
{

	public ChainMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
	}

	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
	{	
		Position newPosition = this.getPositionIfPerformed();
		if(newPosition == null)
			return false; //The move was out of bounds
		
		Piece pieceAtDestination = board.getPiece(newPosition);
		setPieceAtDestination(pieceAtDestination);
		if(pieceAtDestination == null)
			return true; //The space is free
		else if(pieceAtDestination.hasSameAffinityAs(myPiece))
		{
			//This also means that moves further a long this move chain won't be possible either
			return false; //You can't take over your own pieces
		}
		else
		{
			//Take over is available
			return true;
		}
	}

}
