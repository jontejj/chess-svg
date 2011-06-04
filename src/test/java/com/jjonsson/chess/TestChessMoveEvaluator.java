package com.jjonsson.chess;

import junit.framework.Assert;

import org.junit.Test;

import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.ChainMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.BlackPawn;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.scenarios.TestScenarios;


public class TestChessMoveEvaluator
{
	
	@Test
	public void testAI() throws NoMovesAvailableException, UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("tower_evade_take_over");
		ChessMoveEvaluator.performBestMove(board);
		
		Rock rock = Rock.class.cast(board.getPiece(Position.createPosition(1, Position.A)));
		Assert.assertNotNull(rock);
	}
}
