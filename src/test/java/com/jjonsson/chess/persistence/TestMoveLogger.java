package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Piece;


public class TestMoveLogger
{

	@Test
	public void testMovesMadeShouldBeResetPeriodically()
	{
		ChessBoard board = loadBoard("repetitive_test");

		ImmutablePosition whiteRockStartingPosition = position("1H");
		ImmutablePosition whiteRockMoveDestination = position("3H");
		ImmutablePosition blackRockStartingPosition = position("8H");
		ImmutablePosition blackRockMoveDestination = position("6H");


		Piece whiteRock = board.getPiece(whiteRockStartingPosition);
		Piece blackRock = board.getPiece(blackRockStartingPosition);

		int movesDone = 0;
		while(movesDone < MoveLogger.REPITION_HISTORY_RESET_INTERVAL)
		{
			//Moves the rocks back and forth until we reach the reset point
			Move whiteRockMove = board.getAvailableMove(whiteRock, whiteRockMoveDestination);
			assertTrue(whiteRock.performMove(whiteRockMove, board, false));
			Move blackRockMove = board.getAvailableMove(blackRock, blackRockMoveDestination);
			assertTrue(blackRock.performMove(blackRockMove, board, false));
			whiteRockMove = board.getAvailableMove(whiteRock, whiteRockStartingPosition);
			assertTrue(whiteRock.performMove(whiteRockMove, board, false));
			blackRockMove = board.getAvailableMove(blackRock, blackRockStartingPosition);
			assertTrue(blackRock.performMove(blackRockMove, board, false));
			movesDone +=4;
		}
		//It may be made one time depending on what interval we use but it shouldn't be more
		assertFalse(board.getAvailableMove(whiteRock, whiteRockMoveDestination).getMovesMade() > 1);
	}
}
