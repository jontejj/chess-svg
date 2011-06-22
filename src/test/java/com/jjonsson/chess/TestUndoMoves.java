package com.jjonsson.chess;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.*;

import java.util.NoSuchElementException;

import org.junit.Test;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.scenarios.TestScenarios;

public class TestUndoMoves
{

	@Test
	public void testPawnTwoStepMoveUndo() throws NoSuchElementException, InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = new ChessBoard(true);
		Move pawnTwoStepMove = board.getAvailableMove(Position.createPosition(4, Position.C), Piece.WHITE);
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
	public void testUndoAndVerifyThatPieceRevivalOccured() throws NoSuchElementException, InvalidPosition, UnavailableMoveException
	{
		ChessBoard board2 = TestScenarios.loadBoard("next_pawn_time_for_replacement_move_should_check_king_horse_take_queen_then_no_more_check");
		
		Position takeOverSpot = Position.createPosition(8, Position.B);
		Move pawnTakeOverMove = board2.getAvailableMove(takeOverSpot, Piece.WHITE);
		
		//Take over the black rock
		pawnTakeOverMove.getPiece().performMove(pawnTakeOverMove, board2);
		
		Piece whiteQueen = board2.getPiece(takeOverSpot);
		assertEquals(Piece.WHITE, whiteQueen.getAffinity());
		assertTrue(whiteQueen instanceof Queen);
		
		board2.undoMoves(1);
		
		//The black rock should again be on the board
		Piece blackRock = board2.getPiece(takeOverSpot);
		assertEquals(Piece.BLACK, blackRock.getAffinity());
		assertTrue(blackRock instanceof Rock);
	}
	
	@Test
	public void testUndoTwoTakeOverMovesAndVerifyThatBothPiecesWereRevived() throws NoSuchElementException, InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = TestScenarios.loadBoard("undo_two_take_over_moves_in_a_row_for_the_same_piece");
		
		Position blackPawnPosition = Position.createPosition(4, Position.C);
		Position takeOverOneSpot = Position.createPosition(3, Position.D);
		Position takeOverTwoSpot = Position.createPosition(2, Position.E);
		Position rockMoveToSpot = Position.createPosition(7, Position.A);
		
		//Take over a white pawn
		Move pawnTakeOverMove = board.getAvailableMove(takeOverOneSpot, Piece.BLACK);
		pawnTakeOverMove.getPiece().performMove(pawnTakeOverMove, board);
		
		//Perform the white rock move
		Move rockMove = board.getAvailableMove(rockMoveToSpot, Piece.WHITE);
		rockMove.getPiece().performMove(rockMove, board);
		
		
		//Take over the second white pawn
		pawnTakeOverMove = board.getAvailableMove(takeOverTwoSpot, Piece.BLACK);
		pawnTakeOverMove.getPiece().performMove(pawnTakeOverMove, board);
		
		int undidMoves = board.undoMoves(3);
		assertEquals(3, undidMoves);
		
		//The white pawn that was taken over first should now be able to remove again
		Piece whitePawn = board.getPiece(takeOverOneSpot);
		//It should exist and it should be able to take over the black pawn that took it over
		assertNotNull(whitePawn.getAvailableMoveForPosition(blackPawnPosition, board));
		
		whitePawn = board.getPiece(takeOverTwoSpot);
		assertNotNull(whitePawn);
	}
}
