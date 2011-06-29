package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Rock;

/**
 * Two instances of this move can represent the King's move and the Rock's move during a castling move
 * @author jonatanjoensson
 *
 */
public class CastlingMovePart extends IndependantMove
{

	/**
	 * The move that this Castling Move Part is a part of
	 */
	private CastlingMove myCastlingMove;
	/**
	 * Note this move doesn't handle bad positions
	 * @param rowChange
	 * @param columnChange
	 * @param pieceThatTheMoveWillBeMadeWith a Rock or a King
	 */
	public CastlingMovePart(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, CastlingMove castlingMove)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
		myCastlingMove = castlingMove;
		Position currentPosition = myPiece.getCurrentPosition();
		byte newRow = (byte)(currentPosition.getRow()+rowChange);
		byte newColumn = (byte)(currentPosition.getColumn()+columnChange);
		myDestination = new Position(newRow, newColumn);
		setPieceAtDestination(getPiece().getBoard().getPiece(myDestination));
	}
	
	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
	{
		//A castling move depends on unmoved pieces
		if(getPiece().getMovesMade() > 0)
			return false;
		
		//The king may never move into a threatened position
		if(getPiece() instanceof King)
		{
			//The Rock is going to protect the King if the threatening piece is standing on the same row
			Move threateningMove = board.moveThreateningPosition(this.getPositionIfPerformed(), !myPiece.getAffinity(), myPiece, false);
			
			if(threateningMove != null)
			{
				//If this is true for all the king's moves then the game is over
				return false;
			}
		}
		
		if(getPieceAtDestination() == null)
			return true; //The space is free
		
		return false;
	}
	
	@Override
	public boolean isPartOfAnotherMove()
	{
		if(getPiece() instanceof Rock)
		{
			return true;
		}
		return false;
	}
	
	@Override
	public void updateDestination(ChessBoard board)
	{
		setPieceAtDestination(board.getPiece(myDestination));
	}
	
	@Override
	public void updatePossibility(ChessBoard board)
	{
		myCanBeMadeCache = canBeMadeInternal(board);
	}	
	
	/**
	 * Overridden to not accumulate takeover/protecting values as this move can't either protect nor take over a piece
	 */
	@Override 
	public void syncCountersWithBoard(ChessBoard board)
	{
		
	}
	
}