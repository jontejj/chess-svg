package com.jjonsson.chess.evaluators.ordering;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.orderings.TakeOverValueOrdering;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.scenarios.TestScenarios;


public class TestTakeOverValueOrdering
{
	@Test
	public void testTakeOverValueOrdering()
	{
		ChessBoard board = TestScenarios.loadBoard("king_right_move_should_not_be_possible");
		Move queenTakeOverMove = board.getAvailableMove(position("7H"), Piece.BLACK);

		//A move worth nothing
		Move kingEvadeMove = board.getAvailableMove(position("6D"), Piece.BLACK);

		TakeOverValueOrdering ordering = new TakeOverValueOrdering();
		assertEquals(ordering.compare(kingEvadeMove, queenTakeOverMove), -Piece.QUEEN_VALUE);

	}
}
