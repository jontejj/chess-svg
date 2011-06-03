package com.jjonsson.chess;

import junit.framework.Assert;

import org.junit.Test;

import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.BlackPawn;
import com.jjonsson.chess.scenarios.TestScenarios;


public class TestChessMoveEvaluator
{
	
	@Test
	public void testAI() throws NoMovesAvailableException, UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("black_knight_should_move_to_4D");
		ChessMoveEvaluator.performBestMove(board);
		Move lastMove = board.getLastMove();
		
		BlackPawn blackPawn = BlackPawn.class.cast(board.getPiece(Position.createPosition(4, Position.D)));
		
		//Only here to enable better print outs
		board.undoMove(lastMove, false);
		
		Assert.assertNotNull(blackPawn);
		Assert.assertTrue("Best move should be a pawn take over move, was: " + lastMove, lastMove instanceof PawnTakeOverMove);
	}
}
