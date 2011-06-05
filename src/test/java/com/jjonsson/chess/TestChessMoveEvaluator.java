package com.jjonsson.chess;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import org.junit.Test;

import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.scenarios.TestScenarios;


public class TestChessMoveEvaluator
{
	
	@Test
	public void testAI() throws NoMovesAvailableException
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
			//If someone wins it should really be black :)
			assertEquals("A random player won against the AI within 15 seconds, that's some crazy shit", Piece.BLACK, !board.getCurrentPlayer());
			System.out.println("AI won within 15 seconds, that's good!");
		}
		else
		{
			//Well this is embarrassing black should have played better/faster
		}
	}
	
	/**
	 * Test if the AI is to aggressive and doesn't recognize that the best move may be to move to cover 
	 * instead of taking a less valuable piece as a trade for a more valuable one
	 * @throws UnavailableMoveException 
	 * @throws NoMovesAvailableException 
	 * @throws InvalidPosition 
	 */
	@Test
	public void testStupidTakeOverShouldNotBeMade() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("bishop_should_move_rational");
		
		ChessMoveEvaluator.performBestMove(board);
		
		Move lastMove = board.getLastMove();
		
		Position badPosition = Position.createPosition(2, Position.B);
		
		assertFalse("Pawn should not be taken by the bishop at 7G", lastMove.getCurrentPosition().equals(badPosition));
	}
}
