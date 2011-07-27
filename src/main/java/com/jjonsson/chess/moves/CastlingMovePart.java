package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

/**
 * Two instances of this move can represent the King's move and the Rock's move during a castling move
 * @author jonatanjoensson
 *
 */
public abstract class CastlingMovePart extends IndependantMove
{
	/**
	 * Note this move doesn't handle bad positions
	 * @param rowChange
	 * @param columnChange
	 * @param pieceThatTheMoveWillBeMadeWith a Rock or a King
	 */
	public CastlingMovePart(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
		Position currentPosition = getCurrentPosition();
		byte newRow = (byte)(currentPosition.getRow()+rowChange);
		byte newColumn = (byte)(currentPosition.getColumn()+columnChange);
		setDestination(new Position(newRow, newColumn));
		setPieceAtDestination(getPiece().getBoard().getPiece(getDestination()));
	}
	
	@Override
	public void updateDestination(ChessBoard board)
	{
		setPieceAtDestination(board.getPiece(getDestination()));
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
	
	//Only the main "King" castling move should be included in the move table
	@Override
	public boolean shouldBeIncludedInMoveTable()
	{
		return false;
	}
	
}