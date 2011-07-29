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

import static com.jjonsson.chess.moves.Position.*;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Rock;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static com.jjonsson.chess.pieces.Piece.*;

public class TestPersistance
{

	@Test
	public void testGetPieceFromPersistanceData() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(false);
		
		Position kingPos = createPosition(5, D);
		King k = new King(kingPos, BLACK, board);
		short p = k.getPersistanceData();
		Piece k2 = Piece.getPieceFromPersistanceData(p, board);
		assertTrue("Saved piece doesn't match the read one", k.same(k2));
	
		Position rockPos = createPosition(8, H);
		Rock r = new Rock(rockPos, WHITE, board);
		short p1 = r.getPersistanceData();
		Piece r2 = Piece.getPieceFromPersistanceData(p1, board);
		assertTrue("Saved piece doesn't match the read one", r.same(r2));
	}
	
	@Test
	public void testThatMovedKingCantCastleAfterBoardSaveAndLoad() throws InvalidPosition, UnavailableMoveException, FileNotFoundException
	{
		ChessBoard board = loadBoard("castling_move");
		Position kingOriginalPos = createPosition(1, E);
		Position castlingDestinationPos = createPosition(1, G);
		Position kingTempPos = createPosition(1, F);
		King whiteKing = board.getKing(WHITE);
		
		//First verify that it's possible to castle before any moves have been made
		Move castlingMoveBeforeSave = board.getAvailableMove(whiteKing, castlingDestinationPos);
		assertTrue(castlingMoveBeforeSave.canBeMade(board));
		
		Move kingRightMove = board.getAvailableMove(whiteKing, kingTempPos);
		whiteKing.performMove(kingRightMove, board);
		
		//This removes the possiblity to castle
		Move kingLeftMove = board.getAvailableMove(whiteKing, kingOriginalPos);
		whiteKing.performMove(kingLeftMove, board);
		
		assertFalse(castlingMoveBeforeSave.canBeMade(board));
		assertTrue(BoardLoader.saveBoard(board, "temp_save_test_1"));
		
		//Verify that the king can't castle after the save
		ChessBoard savedBoard = new ChessBoard(false);
		FileInputStream fis = new FileInputStream(new File("temp_save_test_1"));
		assertTrue(BoardLoader.loadStreamIntoBoard(fis, savedBoard));
		King savedKing = savedBoard.getKing(WHITE);
		Move castlingMoveAfterSave = savedKing.getMove(castlingMoveBeforeSave);
		assertFalse(castlingMoveAfterSave.canBeMade(savedBoard));
	}
	
	@Test
	public void testSaveBoard() throws InvalidPosition, UnavailableMoveException, FileNotFoundException
	{
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_should_not_be_able_to_move");
		Piece blackRock = board.getPiece(createPosition(8, H));
		Move rockMove = board.getAvailableMove(createPosition(8, F), BLACK);
		rockMove.getPiece().performMove(rockMove, board);
		
		assertTrue(BoardLoader.saveBoard(board, "temp_save_test"));
		
		//Verify that the changes could be read
		ChessBoard savedBoard = new ChessBoard(false);
		FileInputStream fis = new FileInputStream(new File("temp_save_test"));
		assertTrue(BoardLoader.loadStreamIntoBoard(fis, savedBoard));
		Piece savedRock = savedBoard.getPiece(createPosition(8, F));
		assertTrue("Saved piece doesn't match the read one", blackRock.same(savedRock));
	}
	

	@Test
	public void testLoadBoard() throws InvalidPosition
	{
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_to_3D_should_not_be_possible");
		King whiteKing = King.class.cast(board.getPiece(createPosition(2, E)));
		
		assertNull(board.getAvailableMove(whiteKing, createPosition(3, D)));
	}
}
