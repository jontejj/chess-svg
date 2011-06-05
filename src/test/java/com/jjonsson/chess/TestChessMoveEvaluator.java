package com.jjonsson.chess;

import static junit.framework.Assert.assertEquals;
import org.junit.Test;

import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.scenarios.TestScenarios;


public class TestChessMoveEvaluator
{
	
	@Test
	public void testAI() throws NoMovesAvailableException, UnavailableMoveException
	{
		ChessBoard board = TestScenarios.loadBoard("tower_evade_take_over");
		long startNanos = System.nanoTime();
		while(ChessBoardEvaluator.inPlay(board))
		{
			if(board.getCurrentPlayer() == Piece.BLACK)
			{
				ChessMoveEvaluator.performBestMove(board);
			}
			else
				//Simulate that the white is a bad player that doesn't know what he's doing
				board.performRandomMove();
			
			if(System.nanoTime() > startNanos + 15000000000L)
			{
				break;
			}
		}
		
		if(board.getCurrentState() == ChessState.CHECKMATE)
		{
			//If someone won it should really be black :)
			assertEquals("A random player won against the AI within 15 seconds, that's some crazy shit", Piece.BLACK, !board.getCurrentPlayer());
			System.out.println("AI won within 15 seconds, that's good!");
		}
		else
		{
			//Well this is embarissing black should have played better/faster
		}
	}
}
