package com.jjonsson.chess;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.Rock;

public class TestUndoMoves
{

	@Test
	public void testPawnTwoStepMoveUndo() throws UnavailableMoveException
	{
		ChessBoard board = new ChessBoard(true);
		Move pawnTwoStepMove = board.getAvailableMove(position("4C"), WHITE);
		pawnTwoStepMove.getPiece().performMove(pawnTwoStepMove, board);
		assertEquals(1, board.undoMoves(1));
		assertTrue(pawnTwoStepMove.canBeMade(board));


	}

	@Test
	public void testUndoNotAvailable()
	{
		ChessBoard board = new ChessBoard(true);
		assertEquals(0, board.undoMoves(1, false));
	}

	@Test
	public void testUndoAndVerifyThatPieceRevivalOccured() throws UnavailableMoveException
	{
		ChessBoard board2 = loadBoard("next_pawn_time_for_replacement_move_should_check_king_horse_take_queen_then_no_more_check");

		ImmutablePosition takeOverSpot = position("8B");
		Move pawnTakeOverMove = board2.getAvailableMove(takeOverSpot, WHITE);

		//Take over the black rock
		pawnTakeOverMove.getPiece().performMove(pawnTakeOverMove, board2);

		Piece whiteQueen = board2.getPiece(takeOverSpot);
		assertTrue(whiteQueen.isWhite());
		assertTrue(whiteQueen instanceof Queen);

		board2.undoMoves(1);

		//The black rock should again be on the board
		Piece blackRock = board2.getPiece(takeOverSpot);
		assertTrue(blackRock.isBlack());
		assertTrue(blackRock instanceof Rock);
	}

	@Test
	public void testUndoTwoTakeOverMovesAndVerifyThatBothPiecesWereRevived() throws UnavailableMoveException
	{
		ChessBoard board = loadBoard("undo_two_take_over_moves_in_a_row_for_the_same_piece");

		ImmutablePosition blackPawnPosition = position("4C");
		ImmutablePosition takeOverOneSpot = position("3D");
		ImmutablePosition takeOverTwoSpot = position("2E");
		ImmutablePosition rockMoveToSpot = position("7A");

		int movesMade = 0;
		//Take over a white pawn
		Move pawnTakeOverMove = board.getAvailableMove(takeOverOneSpot, BLACK);
		pawnTakeOverMove.getPiece().performMove(pawnTakeOverMove, board);
		movesMade++;

		//Perform the white rock move
		Move rockMove = board.getAvailableMove(rockMoveToSpot, WHITE);
		rockMove.getPiece().performMove(rockMove, board);
		movesMade++;

		//Take over the second white pawn
		pawnTakeOverMove = board.getAvailableMove(takeOverTwoSpot, BLACK);
		pawnTakeOverMove.getPiece().performMove(pawnTakeOverMove, board);
		movesMade++;

		int undidMoves = board.undoMoves(movesMade, false);
		assertEquals(movesMade, undidMoves);

		//The white pawn that was taken over first should now be able to remove again
		Piece whitePawn = board.getPiece(takeOverOneSpot);
		//It should exist and it should be able to take over the black pawn that took it over
		assertNotNull(board.getAvailableMove(whitePawn, blackPawnPosition));

		whitePawn = board.getPiece(takeOverTwoSpot);
		assertNotNull(whitePawn);
	}
}
