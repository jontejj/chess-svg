package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.King;

public class KingMove extends IndependantMove 
{
	
	public KingMove(int rowChange, int columnChange, King pieceThatTheMoveWillBeMadeWith) 
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}
	
	@Override
	public boolean canBeMade(ChessBoard board)
	{
		if(board.isMoveUnavailableDueToCheck(this))
			return false;
		
		return myCanBeMadeCache;
	}
	
	public boolean canBeMadeInternal(ChessBoard board) 
	{
		Position newPosition = this.getPositionIfPerformed();
		if(newPosition == null)
			return false; //The move was out of bounds
		
		Move threateningMove = board.moveThreateningPosition(newPosition, !myPiece.getAffinity(), myPiece, true);
		
		if(threateningMove != null)
		{
			//If this is true for all the king's moves then the game is over
			return false;
		}
		
		return canBeMadeEnding();
	}
}
