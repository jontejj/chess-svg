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
		Position newPosition = this.getPositionIfPerformed();
		if(newPosition == null)
			return false; //The move was out of bounds
		
		Piece pieceAtDestination = board.getPiece(newPosition);
		setPieceAtDestination(pieceAtDestination);
		if(pieceAtDestination == null)
			return true; //The space is free
		else if(pieceAtDestination.hasSameAffinityAs(myPiece))
			return false; //You can't take over your own pieces
		else
		{
			//Take over is available
			return true;
		}
	}

}
