package com.jjonsson.chess.evaluators.ordering;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.orderings.CenterStageOrdering;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import static com.jjonsson.chess.pieces.Piece.*;


public class TestCenterStageOrdering
{

	@Test
	public void testCenterStageOrdering() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(true);
		Move pawnTwoStepMove = board.getAvailableMove(Position.createPosition(4, Position.C), WHITE);
		Move knightMove = board.getAvailableMove(Position.createPosition(3, Position.A), WHITE);
		
		CenterStageOrdering ordering = new CenterStageOrdering();
		
		assertEquals(3, ordering.compare(pawnTwoStepMove, knightMove));
		
	}
}
