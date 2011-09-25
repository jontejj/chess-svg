package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.board.PiecePlacement.DONT_PLACE_PIECES;
import static com.jjonsson.chess.gui.Settings.disableDebug;
import static com.jjonsson.chess.gui.Settings.enableDebug;
import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.persistence.PersistanceLogging.USE_PERSISTANCE_LOGGING;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
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

	@Test
	public void testCopyMoveHistory() throws UnavailableMoveItem, NoMovesAvailableException
	{
		String filename = "test_copy_move_history" + ChessFileFilter.FILE_ENDING;
		ChessBoard board = new ChessBoard();
		board.move("2A", "3A");
		ChessBoard copy = board.copy(USE_PERSISTANCE_LOGGING);
		copy.move("7A", "5A");
		BoardLoader.saveBoard(copy, filename);

		ChessBoard savedBoard = new ChessBoard(DONT_PLACE_PIECES, USE_PERSISTANCE_LOGGING);
		BoardLoader.loadFileIntoBoard(new File(filename), savedBoard);
		savedBoard.move("2B", "3B");
		assertEquals(3, savedBoard.undoMoves(3));

		enableDebug();
		Move bestMove = ChessMoveEvaluator.getBestMove(savedBoard);
		assertNotNull(bestMove);
		disableDebug();
	}
}
