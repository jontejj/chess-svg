package com.jjonsson.chess.persistance;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.scenarios.TestScenarios;

public class TestPersistance
{

	@Test
	public void testGetPieceFromPersistanceData() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(false);
		
		Position kingPos = Position.createPosition(5, Position.D);
		King k = new King(kingPos, Piece.BLACK, board);
		short p = k.getPersistanceData();
		Piece k2 = Piece.getPieceFromPersistanceData(p, board);
		assertTrue("Saved piece doesn't match the read one", k.same(k2));
	
		Position rockPos = Position.createPosition(8, Position.H);
		Rock r = new Rock(rockPos, Piece.WHITE, board);
		short p1 = r.getPersistanceData();
		Piece r2 = Piece.getPieceFromPersistanceData(p1, board);
		assertTrue("Saved piece doesn't match the read one", r.same(r2));
	}
	
	@Test
	public void testSaveBoard() throws InvalidPosition, UnavailableMoveException, FileNotFoundException
	{
		//Load a board and make changes to it
		ChessBoard board = TestScenarios.loadBoard("king_should_not_be_able_to_move");
		Piece blackRock = board.getPiece(Position.createPosition(8, Position.H));
		Move rockMove = board.getAvailableMove(Position.createPosition(8, Position.F), Piece.BLACK);
		rockMove.getPiece().performMove(rockMove, board);
		
		assertTrue(BoardLoader.saveBoard(board, "temp_save_test"));
		
		//Verify that the changes could be read
		ChessBoard savedBoard = new ChessBoard(false);
		FileInputStream fis = new FileInputStream(new File("temp_save_test"));
		assertTrue(BoardLoader.loadStreamIntoBoard(fis, savedBoard));
		Piece savedRock = savedBoard.getPiece(Position.createPosition(8, Position.F));
		assertTrue("Saved piece doesn't match the read one", blackRock.same(savedRock));
	}
	

	@Test
	public void testLoadBoard() throws InvalidPosition
	{
		//Load a board and make changes to it
		ChessBoard board = TestScenarios.loadBoard("king_to_3D_should_not_be_possible");
		King whiteKing = King.class.cast(board.getPiece(Position.createPosition(2, Position.E)));
		
		assertNull(whiteKing.getAvailableMoveForPosition(Position.createPosition(3, Position.D), board));
	}
}
