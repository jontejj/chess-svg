package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.moves.Position.A;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static junit.framework.Assert.assertFalse;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.board.PiecePlacement;
import com.jjonsson.chess.moves.MutablePosition;

public class TestPiece
{

	@Test
	public void testSamePiece()
	{
		ChessBoard board = new ChessBoard(PiecePlacement.DONT_PLACE_PIECES);
		Queen blackQueen = new Queen(MutablePosition.from(1, A), BLACK, board);
		King blackKing = new King(MutablePosition.from(1, A), BLACK, board);
		Knight whiteKnight = new Knight(MutablePosition.from(3, A), WHITE, board);
		Knight whiteKnightTwo = new Knight(MutablePosition.from(2, A), WHITE, board);

		assertFalse(blackKing.same(whiteKnight));
		assertFalse(blackKing.same(blackQueen));
		assertFalse(blackKing.same(null));
		assertFalse(whiteKnight.same(whiteKnightTwo));
	}
}
