package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

/**
 * Two instances of this move can represent the King's move and the Rock's move during a castling move
 * @author jonatanjoensson
 *
 */
public abstract class CastlingMovePart extends IndependantMove
{
	/**
	 * The main castling move that this part belongs to
	 */
	private CastlingMove myCastlingMove;

	/**
	 * Note this move doesn't handle bad positions
	 * @param rowChange
	 * @param columnChange
	 * @param pieceThatTheMoveWillBeMadeWith a Rock or a King
	 */
	public CastlingMovePart(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith, final CastlingMove castlingMove)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
		super.updateDestination(getPiece().getBoard());
		myCastlingMove = castlingMove;
	}

	@Override
	public void updatePossibility(final ChessBoard board, final boolean updatePieceAtDestination)
	{
		super.updatePossibility(board, updatePieceAtDestination);
		//Now that the possibility of this part has changed maybe the main castling move can be made
		myCastlingMove.updatePossibility(board);
	}

	@Override
	public void updateDestination(final ChessBoard board)
	{
		//Nothing to do here since the destination never changes
	}

	/**
	 * Overridden to not accumulate takeover/protecting values as this move can't either protect nor take over a piece
	 */
	@Override
	public void syncCountersWithBoard(final ChessBoard board)
	{

	}

	//Only the main "King" castling move should be included in the move table
	@Override
	public boolean shouldBeIncludedInMoveTable()
	{
		return false;
	}

}