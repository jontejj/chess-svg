package com.jjonsson.chess.evaluators;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;

public class TestChessBoardEvaluator
{

	private static final String GAME_STATE = "Game state";
	@Test
	public void testCheck()
	{
		ChessBoard board = loadBoard("king_should_not_be_able_to_move");
		ChessState currentState = board.getCurrentState();
		assertEquals(GAME_STATE, currentState, ChessState.CHECK);
	}
	
	@Test
	public void testCheckMate()
	{
		ChessBoard board = loadBoard("should_be_checkmate");
		ChessState currentState = board.getCurrentState();
		assertEquals(GAME_STATE, currentState, ChessState.CHECKMATE);
	}

	@Test
	public void testNotCheck()
	{
		ChessBoard board = loadBoard("should_not_be_check");
		ChessState currentState = board.getCurrentState();
		assertEquals(GAME_STATE, currentState, ChessState.PLAYING);
	}
	
	@Test
	public void testStaleMate()
	{
		ChessBoard board = loadBoard("should_be_stalemate");
		ChessState currentState = board.getCurrentState();
		assertEquals(GAME_STATE, currentState, ChessState.STALEMATE);
	}
}
