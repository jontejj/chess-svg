package com.jjonsson.chess.evaluators;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;

public class TestChessBoardEvaluator
{

	private static final String GAME_STATE = "Game state";
	@Test
	public void testCheck()
	{
		ChessBoard board = loadBoard("king_should_not_be_able_to_move");
		ChessState currentState = board.getCurrentState();
		assertEquals(GAME_STATE, ChessState.CHECK, currentState);
	}

	@Test
	public void testCheckMate()
	{
		ChessBoard board = loadBoard("should_be_checkmate");
		ChessState currentState = board.getCurrentState();
		assertEquals(GAME_STATE, ChessState.CHECKMATE, currentState);

		try
		{
			//Make sure no more moves can be made on the board
			ChessMoveEvaluator.performBestMove(board);
			fail();
		}
		catch (NoMovesAvailableException e)
		{
		}
	}

	@Test
	public void testNotCheck()
	{
		ChessBoard board = loadBoard("should_not_be_check");
		ChessState currentState = board.getCurrentState();
		assertEquals(GAME_STATE, ChessState.PLAYING, currentState);
	}

	@Test
	public void testStaleMate()
	{
		ChessBoard board = loadBoard("should_be_stalemate");
		assertTrue(GAME_STATE, board.getStatusString().startsWith("Stalemate"));
	}

	@Test
	public void testShouldBeCheckMate() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("should_be_checkmate_4");
		board.move(position("6B"), position("6A"));
		board.move(position("8E"), position("4A"));
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
	}

	@Test
	public void testShouldBeCheckMate2() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("move_order_which_created_check_when_it_should_be_checkmate");
		board.move("7G", "6G");
		board.move("3H", "4H");
		board.move("7D", "6D");
		board.move("3G", "4G");
		board.move("6E", "5E");
		board.move("1D", "1E");

		board.move("8C", "4G");
		board.move("2E", "3G");
		board.move("5E", "4F");
		board.move("1E", "1A");
		board.move("6A", "6B");
		board.move("2D", "4D");

		board.move("6B", "4D");
		board.move("2F", "1E");
		board.move("4D", "1G");
		board.move("1E", "2D");
		board.move("1G", "3E");

		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
	}

	@Test
	public void testShouldBeCheckMate3()
	{
		ChessBoard board = loadBoard("should_be_checkmate_6");
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
	}

	@Test
	public void testShouldBeCheckMate4()
	{
		ChessBoard board = loadBoard("should_be_checkmate_7");
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
	}

	@Test
	public void testShouldBeCheck()
	{
		ChessBoard board = loadBoard("should_be_check");
		assertEquals(ChessState.CHECK, board.getCurrentState());
	}
}
