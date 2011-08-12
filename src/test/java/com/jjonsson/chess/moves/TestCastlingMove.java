package com.jjonsson.chess.moves;

import static com.jjonsson.chess.moves.Position.A;
import static com.jjonsson.chess.moves.Position.C;
import static com.jjonsson.chess.moves.Position.E;
import static com.jjonsson.chess.moves.Position.H;
import static com.jjonsson.chess.moves.Position.createPosition;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;

public class TestCastlingMove
{

	@Test
	public void testThatCastlingMoveCanBeMade() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("castling_move");
		performAndVerifyWhiteKingsideCastlingMove(board);

	}

	private void performAndVerifyWhiteKingsideCastlingMove(final ChessBoard board) throws InvalidPosition, UnavailableMoveException
	{
		Position castlingKingDestination = Position.createPosition(1, Position.G);
		Piece whiteRock = board.getPiece(Position.createPosition(1, Position.H));

		assertNotNull(whiteRock);

		King whiteKing = board.getKing(WHITE);

		Move castlingMove = board.getAvailableMove(whiteKing, castlingKingDestination);

		assertNotNull("Castling move unavailable", castlingMove);
		whiteKing.performMove(castlingMove, board);

		assertEquals(whiteKing, board.getPiece(castlingKingDestination));
		assertEquals(whiteRock, board.getPiece(Position.createPosition(1, Position.F)));
	}

	@Test
	public void testBlackQueenSideCastling() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("queenside_castling");
		Position castlingKingDestination = Position.createPosition(8, Position.C);
		Piece blackRock = board.getPiece(Position.createPosition(8, Position.A));

		assertNotNull(blackRock);

		King blackKing = board.getKing(BLACK);

		Move castlingMove = board.getAvailableMove(blackKing, castlingKingDestination);

		assertNotNull("Castling move unavailable", castlingMove);
		blackKing.performMove(castlingMove, board);

		assertEquals(blackKing, board.getPiece(castlingKingDestination));
		assertEquals(blackRock, board.getPiece(Position.createPosition(8, Position.D)));
	}

	@Test
	public void testCastlingMoveUnavailableDuringCheck() throws InvalidPosition
	{
		ChessBoard board = loadBoard("queenside_castling_not_available_during_check");
		Position castlingKingDestination = Position.createPosition(8, Position.C);

		King blackKing = board.getKing(BLACK);

		assertNull(board.getAvailableMove(blackKing, castlingKingDestination));
	}

	@Test
	public void testThatCastlingMoveUpdatesTheThreePositionsThatChange() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("queenside_castling_should_update_moves_for_all_three_positions_that_it_affects");
		King whiteKing = board.getCurrentKing();
		Move castlingMove = board.getAvailableMove(whiteKing, createPosition(1, C));
		whiteKing.performMove(castlingMove, board);
		board.move(createPosition(5, A), createPosition(4, A));
		board.move(createPosition(1, H), createPosition(1, E));
	}

}
