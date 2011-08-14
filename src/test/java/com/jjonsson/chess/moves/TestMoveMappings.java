package com.jjonsson.chess.moves;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertSame;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.pieces.Piece;

public class TestMoveMappings
{
	/**
	 * Verifies that no different moves for a piece aren't stored to the same index
	 * @throws InvalidPosition
	 */
	@Test
	public void testTwoDimMapping() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(true);
		for(Piece piece : board.getPieces())
		{
			//We keep our own copy to check for duplicates
			Move[][] moveMappings = piece.createMoveTable();
			for(Move move : piece.getMoves())
			{
				int row = move.getFirstDimensionIndex();
				int column = move.getSecondDimensionIndex();
				Move existingMove = moveMappings[row][column];
				String duplicateText = String.format("Duplicate for: (%d, %d) index: [%d, %d] for piece: %s, " +
						"Existing Move: %s, Conflicting Move: %s",
						move.getRowChange(),
						move.getColumnChange(),
						row,
						column, piece, existingMove, move);

				assertNull(duplicateText, existingMove);
				assertSame(move, piece.getMove(move));
				moveMappings[row][column] = move;
			}
		}
	}
}
