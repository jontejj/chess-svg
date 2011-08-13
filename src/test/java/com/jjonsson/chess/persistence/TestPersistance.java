package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.moves.Position.D;
import static com.jjonsson.chess.moves.Position.E;
import static com.jjonsson.chess.moves.Position.F;
import static com.jjonsson.chess.moves.Position.G;
import static com.jjonsson.chess.moves.Position.H;
import static com.jjonsson.chess.moves.Position.createPosition;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Rock;

public class TestPersistance
{

	@Test
	public void testGetPieceFromPersistanceData() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(false);

		Set<Short> persistanceUniquenessTest = Sets.newHashSet();
		for(int row = 1; row <= ChessBoard.BOARD_SIZE; row++)
		{
			for(int column = 1; column <= ChessBoard.BOARD_SIZE; column++)
			{
				MutablePosition kingPos = createPosition(row, column);
				King k = new King(kingPos, BLACK, board);
				short p = k.getPersistenceData();
				Piece k2 = Piece.getPieceFromPersistenceData(p, board);
				assertTrue("Saved piece doesn't match the read one", k.same(k2));
				assertTrue(persistanceUniquenessTest.add(p));
			}
		}

		MutablePosition rockPos = createPosition(8, H);
		Rock r = new Rock(rockPos, WHITE, board);
		short p1 = r.getPersistenceData();
		Piece r2 = Piece.getPieceFromPersistenceData(p1, board);
		assertTrue("Saved piece doesn't match the read one", r.same(r2));
	}

	@Test
	public void testThatMovedKingCantCastleAfterBoardSaveAndLoad() throws InvalidPosition, UnavailableMoveException, FileNotFoundException
	{
		String tempFilename = "temp_save_test_1";
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

		//This removes the possibility to castle
		Move kingLeftMove = board.getAvailableMove(whiteKing, kingOriginalPos);
		whiteKing.performMove(kingLeftMove, board);

		assertFalse(castlingMoveBeforeSave.canBeMade(board));
		assertTrue(BoardLoader.saveBoard(board, tempFilename));

		//Verify that the king can't castle after the save
		ChessBoard savedBoard = new ChessBoard(false, true);
		assertTrue(BoardLoader.loadFileIntoBoard(new File(tempFilename), savedBoard));
		King savedKing = savedBoard.getKing(WHITE);
		Move castlingMoveAfterSave = savedKing.getMove(castlingMoveBeforeSave);
		assertFalse(castlingMoveAfterSave.canBeMade(savedBoard));
	}

	@Test
	public void testSaveBoard() throws InvalidPosition, UnavailableMoveException
	{
		String tempFilename = "temp_save_test";
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_should_not_be_able_to_move");
		Piece blackRock = board.getPiece(createPosition(8, H));
		Move rockMove = board.getAvailableMove(createPosition(8, F), BLACK);
		rockMove.getPiece().performMove(rockMove, board);

		assertTrue(BoardLoader.saveBoard(board, tempFilename));

		//Verify that the changes could be read
		ChessBoard savedBoard = new ChessBoard(false, true);
		assertTrue(BoardLoader.loadFileIntoBoard(new File(tempFilename), savedBoard));
		Piece savedRock = savedBoard.getPiece(createPosition(8, F));
		assertTrue("Saved piece doesn't match the read one", blackRock.same(savedRock));
		assertEquals(1, savedBoard.undoMoves(1, false));
		assertNotNull(savedBoard.getPiece(createPosition(8, H)));
	}


	@Test
	public void testLoadBoard() throws InvalidPosition
	{
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_to_3D_should_not_be_possible");
		King whiteKing = King.class.cast(board.getPiece(createPosition(2, E)));

		assertNull(board.getAvailableMove(whiteKing, createPosition(3, D)));
	}

	@Test
	public void testCopyBoard()
	{
		ChessBoard board = loadBoard("board_cloning_test");
		Collection<Piece> pieces = board.getPieces();
		ChessBoard copy = board.copy();
		for(Piece p : pieces)
		{
			assertTrue(p.same(copy.getPiece(p.getCurrentPosition())));
		}

	}

	@Test
	public void testHandlingOfBoardWithoutWhiteKing()
	{
		loadBoard("board_without_white_king", false);
	}
}
