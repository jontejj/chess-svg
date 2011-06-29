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

public class TestCastlingMove
{

	@Test
	public void testThatCastlingMoveCanBeMade() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("castling_move");
		performAndVerifyWhiteKingsideCastlingMove(board);
		
	}
	
	@Test
	public void testThatCastlingMoveCanBeMadeDuringCheck() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("king_should_be_able_to_move_to_1G_with_a_castling_move");
		performAndVerifyWhiteKingsideCastlingMove(board);
		
	}
	
	private void performAndVerifyWhiteKingsideCastlingMove(ChessBoard board) throws InvalidPosition, UnavailableMoveException
	{
		Position castlingKingDestination = Position.createPosition(1, Position.G);
		Piece whiteRock = board.getPiece(Position.createPosition(1, Position.H));
		
		assertNotNull(whiteRock);
		
		Piece whiteKing = board.getKing(WHITE);
		
		Move castlingMove = whiteKing.getAvailableMoveForPosition(castlingKingDestination, board);
		
		assertNotNull("Castling move unavailable", castlingMove);
		whiteKing.performMove(castlingMove, board);
		
		assertEquals(whiteKing, board.getPiece(castlingKingDestination));
		assertEquals(whiteRock, board.getPiece(Position.createPosition(1, Position.F)));
	}
	
}
