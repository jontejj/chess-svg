package com.jjonsson.chess.persistance;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;

import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static org.junit.Assert.assertFalse;


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
			Move whiteRockMove = whiteRock.getAvailableMoveForPosition(whiteRockMoveDestination, board);
			whiteRock.performMove(whiteRockMove, board, false);
			Move blackRockMove = blackRock.getAvailableMoveForPosition(blackRockMoveDestination, board);
			blackRock.performMove(blackRockMove, board, false);
			whiteRockMove = whiteRock.getAvailableMoveForPosition(whiteRockStartingPosition, board);
			whiteRock.performMove(whiteRockMove, board, false);
			blackRockMove = blackRock.getAvailableMoveForPosition(blackRockStartingPosition, board);
			blackRock.performMove(blackRockMove, board, false);
			movesDone +=4;
		}
		//It may be made one time depending on what interval we use but it shouldn't be more
		assertFalse(whiteRock.getAvailableMoveForPosition(whiteRockMoveDestination, board).getMovesMade() > 1);
	}
}
