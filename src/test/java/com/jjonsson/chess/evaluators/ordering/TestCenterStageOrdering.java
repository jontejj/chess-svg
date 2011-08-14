package com.jjonsson.chess.evaluators.ordering;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.orderings.CenterStageOrdering;
import com.jjonsson.chess.moves.Move;


public class TestCenterStageOrdering
{

	@Test
	public void testCenterStageOrdering()
	{
		ChessBoard board = new ChessBoard(true);
		Move pawnTwoStepMove = board.getAvailableMove(position("4C"), WHITE);
		Move knightMove = board.getAvailableMove(position("3A"), WHITE);

		CenterStageOrdering ordering = new CenterStageOrdering();

		assertEquals(3, ordering.compare(pawnTwoStepMove, knightMove));

	}
}
