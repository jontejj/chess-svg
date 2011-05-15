package com.jjonsson.chess.ordering;

import static org.junit.Assert.assertEquals;

import java.util.NoSuchElementException;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.ordering.TakeOverValueOrdering;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.scenarios.TestScenarios;


public class TestTakeOverValueOrdering
{
	@Test
	public void testTakeOverValueOrdering() throws NoSuchElementException, InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("king_right_move_should_not_be_possible");
		
		Move queenTakeOverMove = board.getAvailableMove(Position.createPosition(7, Position.H), Piece.BLACK);
		
		//A move worth nothing
		Move kingEvadeMove = board.getAvailableMove(Position.createPosition(6, Position.D), Piece.BLACK);
		
		TakeOverValueOrdering ordering = new TakeOverValueOrdering();
		assertEquals(ordering.compare(kingEvadeMove, queenTakeOverMove), -Piece.QUEEN_VALUE);
		
	}
}
