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
}
