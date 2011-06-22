package com.jjonsson.chess;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

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
		//Pawn should not be taken by the bishop at 7G
		makeSureMoveWasNotMade(board, Position.createPosition(2, Position.B));
	}
	
	@Test
	public void testADirectChechMateShouldBePrioritizedOverAFutureOne() throws NoMovesAvailableException
	{
		ChessBoard board = TestScenarios.loadBoard("pawn_moves_that_reach_their_destinations_should_be_worth_as_much_as_their_replacement_value");
		ChessMoveEvaluator.performBestMove(board);
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
	}
	
	@Test
	public void testMakingAMoveWhileInCheckMateState()
	{
		ChessBoard board = TestScenarios.loadBoard("should_be_checkmate");
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
		try
		{
			ChessMoveEvaluator.performBestMove(board);
		}
		catch (NoMovesAvailableException e)
		{
			assertFalse(ChessBoardEvaluator.inPlay(board));
		}
		assertNull(board.getLastMove());
	}
	
	/**
	 * Test if the AI avoids making risky moves that haven't been searched deeper than one level and thus may look good but
	 * when in fact they aren't
	 * @throws UnavailableMoveException 
	 * @throws NoMovesAvailableException 
	 * @throws InvalidPosition 
	 */
	@Test
	public void testOnlyMakeSureMoves() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("white_queen_should_not_move_to_5H");
		makeSureMoveWasNotMade(board, Position.createPosition(5, Position.H));
	}
	
	@Test
	public void testDontMoveIntoThreatenedSquare() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("white_queen_should_not_move_to_4C");
		makeSureMoveWasNotMade(board, Position.createPosition(4, Position.C));
	}
	
	@Test
	public void testWhiteQueenShouldAvoidBeingTaken() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("white_pawn_should_protect_queen_by_moving_to_4C");
		//This should move the queen out of harms way
		ChessMoveEvaluator.performBestMove(board);
		//Verify that the black pawn can't take over the queen
		assertTrue(board.getAvailableMoves(Position.createPosition(3, Position.B), Piece.BLACK).isEmpty());
	}
	
	private void makeSureMoveWasNotMade(ChessBoard board, Position badPosition) throws NoMovesAvailableException
	{
		ChessMoveEvaluator.performBestMove(board);
		Move lastMove = board.getLastMove();
		assertFalse("Last move should not be to: " + badPosition, lastMove.getCurrentPosition().equals(badPosition));
	}
}
