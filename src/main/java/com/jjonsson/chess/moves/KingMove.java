package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.King;

public class KingMove extends IndependantMove 
{
	
	private boolean myIsCheckedByAnotherMove;
	
	public KingMove(int rowChange, int columnChange, King pieceThatTheMoveWillBeMadeWith) 
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}

	/**
	 * If this is true then the reason for this move not being available is that a move from the other player could reach the same destination
	 * @return
	 */
	public boolean isCheckedByAnotherMove()
	{
		return myIsCheckedByAnotherMove;
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
		myIsCheckedByAnotherMove = false;
		Position newPosition = this.getPositionIfPerformed();
		
		setPieceAtDestination(board.getPiece(newPosition), board);
		
		if(newPosition == null)
			return false; //The move was out of bounds
		
		Move threateningMove = board.moveThreateningPosition(newPosition, !myPiece.getAffinity(), myPiece);
		
		if(threateningMove != null)
		{
			myIsCheckedByAnotherMove = true;
			//If this is true for all the king's moves then the game is over
			return false;
		}
		
		return canBeMadeEnding(board.getPiece(newPosition), board);
	}
}
