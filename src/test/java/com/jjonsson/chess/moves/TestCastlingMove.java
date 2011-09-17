package com.jjonsson.chess.moves;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;

public class TestCastlingMove
{

	@Test
	public void testThatCastlingMoveCanBeMade()
	{
		ChessBoard board = loadBoard("castling_move");
		performAndVerifyWhiteKingsideCastlingMove(board);

	}

	private void performAndVerifyWhiteKingsideCastlingMove(final ChessBoard board)
	{
		ImmutablePosition castlingKingDestination = position("1G");
		Piece whiteRock = board.getPiece(position("1H"));

		assertNotNull(whiteRock);

		King whiteKing = board.getKing(WHITE);

		Move castlingMove = board.getAvailableMove(whiteKing, castlingKingDestination);

		assertNotNull("Castling move unavailable", castlingMove);
		assertTrue(whiteKing.performMove(castlingMove, board));

		assertEquals(whiteKing, board.getPiece(castlingKingDestination));
		assertEquals(whiteRock, board.getPiece(position("1F")));
	}

	@Test
	public void testBlackQueenSideCastling()
	{
		ChessBoard board = loadBoard("queenside_castling");
		ImmutablePosition castlingKingDestination = position("8C");
		Piece blackRock = board.getPiece(position("8A"));

		assertNotNull(blackRock);

		King blackKing = board.getKing(BLACK);

		Move castlingMove = board.getAvailableMove(blackKing, castlingKingDestination);

		assertNotNull("Castling move unavailable", castlingMove);
		assertTrue(blackKing.performMove(castlingMove, board));

		assertEquals(blackKing, board.getPiece(castlingKingDestination));
		assertEquals(blackRock, board.getPiece(position("8D")));
	}

	@Test
	public void testCastlingMoveUnavailableDuringCheck()
	{
		ChessBoard board = loadBoard("queenside_castling_not_available_during_check");
		Position castlingKingDestination = position("8C");

		King blackKing = board.getKing(BLACK);

		assertNull(board.getAvailableMove(blackKing, castlingKingDestination));
	}

	@Test
	public void testCastlingMoveShouldBeUnavailableWhenDestinationForKingIsThreatened()
	{
		ChessBoard board = loadBoard("castling_move_should_not_be_possible_when_kings_destination_is_threatened");
		King blackKing = board.getKing(BLACK);
		assertNull(board.getAvailableMove(blackKing, position("8C")));
	}

	@Test
	public void testThatCastlingMoveUpdatesTheThreePositionsThatChange() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("queenside_castling_should_update_moves_for_all_three_positions_that_it_affects");
		King whiteKing = board.getCurrentKing();
		Move castlingMove = board.getAvailableMove(whiteKing, position("1C"));
		assertTrue(whiteKing.performMove(castlingMove, board));
		board.move(position("5A"), position("4A"));
		board.move(position("1H"), position("1E"));
	}

	@Test
	public void testThatIntermediatePositionTriggerCastlingMoveUpdate() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("intermediate_position_should_trigger_castling_move_update");
		board.move(position("1E"), position("1G"));
		assertEquals(1, board.undoMoves(1));
		board.move(position("1E"), position("1G"));
	}

	@Test
	public void testThatUndomoveReenablesPossiblitityOfCastling() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("castling_should_be_possible_after_pawn_move_has_been_undone");
		board.move(position("3G"), position("2H"));
		assertEquals(1, board.undoMoves(1));
		board.move(position("7E"), position("5E"));
		board.move(position("1E"), position("1G"));
	}
}
