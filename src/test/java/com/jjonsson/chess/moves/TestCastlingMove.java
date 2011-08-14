package com.jjonsson.chess.moves;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.moves.Position.A;
import static com.jjonsson.chess.moves.Position.C;
import static com.jjonsson.chess.moves.Position.D;
import static com.jjonsson.chess.moves.Position.E;
import static com.jjonsson.chess.moves.Position.F;
import static com.jjonsson.chess.moves.Position.G;
import static com.jjonsson.chess.moves.Position.H;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;

public class TestCastlingMove
{

	@Test
	public void testThatCastlingMoveCanBeMade() throws UnavailableMoveException
	{
		ChessBoard board = loadBoard("castling_move");
		performAndVerifyWhiteKingsideCastlingMove(board);

	}

	private void performAndVerifyWhiteKingsideCastlingMove(final ChessBoard board) throws UnavailableMoveException
	{
		ImmutablePosition castlingKingDestination = position(1, G);
		Piece whiteRock = board.getPiece(position(1, H));

		assertNotNull(whiteRock);

		King whiteKing = board.getKing(WHITE);

		Move castlingMove = board.getAvailableMove(whiteKing, castlingKingDestination);

		assertNotNull("Castling move unavailable", castlingMove);
		whiteKing.performMove(castlingMove, board);

		assertEquals(whiteKing, board.getPiece(castlingKingDestination));
		assertEquals(whiteRock, board.getPiece(position(1, F)));
	}

	@Test
	public void testBlackQueenSideCastling() throws UnavailableMoveException
	{
		ChessBoard board = loadBoard("queenside_castling");
		ImmutablePosition castlingKingDestination = position(8, C);
		Piece blackRock = board.getPiece(position(8, A));

		assertNotNull(blackRock);

		King blackKing = board.getKing(BLACK);

		Move castlingMove = board.getAvailableMove(blackKing, castlingKingDestination);

		assertNotNull("Castling move unavailable", castlingMove);
		blackKing.performMove(castlingMove, board);

		assertEquals(blackKing, board.getPiece(castlingKingDestination));
		assertEquals(blackRock, board.getPiece(position(8, D)));
	}

	@Test
	public void testCastlingMoveUnavailableDuringCheck()
	{
		ChessBoard board = loadBoard("queenside_castling_not_available_during_check");
		Position castlingKingDestination = position(8, C);

		King blackKing = board.getKing(BLACK);

		assertNull(board.getAvailableMove(blackKing, castlingKingDestination));
	}

	@Test
	public void testThatCastlingMoveUpdatesTheThreePositionsThatChange() throws UnavailableMoveException, UnavailableMoveItem
	{
		ChessBoard board = loadBoard("queenside_castling_should_update_moves_for_all_three_positions_that_it_affects");
		King whiteKing = board.getCurrentKing();
		Move castlingMove = board.getAvailableMove(whiteKing, position(1, C));
		whiteKing.performMove(castlingMove, board);
		board.move(position(5, A), position(4, A));
		board.move(position(1, H), position(1, E));
	}

	@Test
	public void testThatIntermediatePositionTriggerCastlingMoveUpdate() throws UnavailableMoveException, UnavailableMoveItem
	{
		ChessBoard board = loadBoard("intermediate_position_should_trigger_castling_move_update");
		board.move(position(1, E), position(1, G));
	}

}
