package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.persistence.MoveLogger;
import com.jjonsson.chess.pieces.Piece;


public class TestMoveLogger
{

	@Test
	public void testMovesMadeShouldBeResetPeriodically() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("repetitive_test");

		Position whiteRockStartingPosition = Position.createPosition(1, Position.H);
		Position whiteRockMoveDestination = Position.createPosition(3, Position.H);
		Position blackRockStartingPosition = Position.createPosition(8, Position.H);
		Position blackRockMoveDestination = Position.createPosition(6, Position.H);


		Piece whiteRock = board.getPiece(whiteRockStartingPosition);
		Piece blackRock = board.getPiece(blackRockStartingPosition);

		int movesDone = 0;
		while(movesDone < MoveLogger.REPITION_HISTORY_RESET_INTERVAL)
		{
			//Moves the rocks back and forth until we reach the reset point
			Move whiteRockMove = board.getAvailableMove(whiteRock, whiteRockMoveDestination);
			whiteRock.performMove(whiteRockMove, board, false);
			Move blackRockMove = board.getAvailableMove(blackRock, blackRockMoveDestination);
			blackRock.performMove(blackRockMove, board, false);
			whiteRockMove = board.getAvailableMove(whiteRock, whiteRockStartingPosition);
			whiteRock.performMove(whiteRockMove, board, false);
			blackRockMove = board.getAvailableMove(blackRock, blackRockStartingPosition);
			blackRock.performMove(blackRockMove, board, false);
			movesDone +=4;
		}
		//It may be made one time depending on what interval we use but it shouldn't be more
		assertFalse(board.getAvailableMove(whiteRock, whiteRockMoveDestination).getMovesMade() > 1);
	}
}
