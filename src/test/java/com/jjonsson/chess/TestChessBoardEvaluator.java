package com.jjonsson.chess;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.scenarios.TestScenarios;

public class TestChessBoardEvaluator
{

	@Test
	public void testCheck()
	{
		ChessBoard board = TestScenarios.loadBoard("king_should_not_be_able_to_move");
		ChessState currentState = board.getCurrentState();
		assertEquals("Game state should be " + ChessState.CHECK + ", was: " + currentState, currentState, ChessState.CHECK);
	}
	
	@Test
	public void testCheckMate()
	{
		ChessBoard board = TestScenarios.loadBoard("should_be_checkmate");
		ChessState currentState = board.getCurrentState();
		assertEquals("Game state should be " + ChessState.CHECKMATE + ", was: " + currentState, currentState, ChessState.CHECKMATE);
	}

	@Test
	public void testNotCheck()
	{
		ChessBoard board = TestScenarios.loadBoard("should_not_be_check");
		ChessState currentState = board.getCurrentState();
		assertEquals("Game state should be " + ChessState.PLAYING + ", was: " + currentState, currentState, ChessState.PLAYING);
	}
	
	@Test
	public void testStaleMate()
	{
		ChessBoard board = TestScenarios.loadBoard("should_be_stalemate");
		ChessState currentState = board.getCurrentState();
		assertEquals("Game state should be " + ChessState.STALEMATE + ", was: " + currentState, currentState, ChessState.STALEMATE);
	}
}
