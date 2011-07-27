package com.jjonsson.chess.moves;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Piece;

import static com.jjonsson.chess.pieces.Piece.*;

import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TestCastlingMove
{

	@Test
	public void testThatCastlingMoveCanBeMade() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("castling_move");
		performAndVerifyWhiteKingsideCastlingMove(board);
		
	}
	
	private void performAndVerifyWhiteKingsideCastlingMove(ChessBoard board) throws InvalidPosition, UnavailableMoveException
	{
		Position castlingKingDestination = Position.createPosition(1, Position.G);
		Piece whiteRock = board.getPiece(Position.createPosition(1, Position.H));
		
		assertNotNull(whiteRock);
		
		Piece whiteKing = board.getKing(WHITE);
		
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
		
		Piece blackKing = board.getKing(BLACK);
		
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
		
		Piece blackKing = board.getKing(BLACK);
		
		assertNull(board.getAvailableMove(blackKing, castlingKingDestination));
	}
	
}
