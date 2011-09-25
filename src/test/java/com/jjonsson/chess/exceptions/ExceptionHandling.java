package com.jjonsson.chess.exceptions;

import static com.jjonsson.chess.moves.Position.Column.A;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.pieces.King;

public class ExceptionHandling
{

	@Test
	public void testUnavailableMoveItem()
	{
		ChessBoard board = new ChessBoard();
		try
		{
			board.move("4A", "5D");
			fail();
		}
		catch(UnavailableMoveItem umi)
		{
			assertNotNull(umi.toString());
			try
			{
				board.move("2A", "5D");
				fail();
			}
			catch (UnavailableMoveItem e)
			{
			}
		}
	}

	@Test
	public void testDuplicatePieceDetection()
	{
		ChessBoard board = new ChessBoard();
		King king = new King(MutablePosition.from(1, A), BLACK, board);
		try
		{
			board.addPiece(king, true, false);
			fail();
		}
		catch(DuplicatePieceError dpe)
		{
			assertNotNull(dpe.toString());
		}
	}
}
