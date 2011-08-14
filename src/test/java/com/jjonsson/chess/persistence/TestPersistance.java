package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.jjonsson.chess.ChessBoard;
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
	public void testGetPieceFromPersistanceData()
	{
		ChessBoard board = new ChessBoard(false);

		Set<Short> persistanceUniquenessTest = Sets.newHashSet();
		ByteBuffer buffer = ByteBuffer.allocate(2);
		for(int row = 0; row < ChessBoard.BOARD_SIZE; row++)
		{
			for(int column = 0; column < ChessBoard.BOARD_SIZE; column++)
			{
				King k = new King(MutablePosition.from(row, column), BLACK, board);
				short persistanceData = k.getPersistenceData();
				buffer.putShort(persistanceData);
				buffer.flip();
				Piece k2 = Piece.getPieceFromPersistenceData(buffer, board);
				assertTrue("Saved piece doesn't match the read one", k.same(k2));
				assertTrue(persistanceUniquenessTest.add(persistanceData));
				buffer.rewind();
			}
		}
		Rock r = new Rock(MutablePosition.position(8, H), WHITE, board);
		buffer.putShort(r.getPersistenceData());
		buffer.flip();
		Piece r2 = Piece.getPieceFromPersistenceData(buffer, board);
		assertTrue("Saved piece doesn't match the read one", r.same(r2));
	}

	@Test
	public void testThatMovedKingCantCastleAfterBoardSaveAndLoad() throws UnavailableMoveException
	{
		String tempFilename = "temp_save_test_1";
		ChessBoard board = loadBoard("castling_move");
		Position kingOriginalPos = position(1, E);
		Position castlingDestinationPos = position(1, G);
		Position kingTempPos = position(1, F);
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
	public void testSaveBoard() throws UnavailableMoveException
	{
		String tempFilename = "temp_save_test";
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_should_not_be_able_to_move");
		Piece blackRock = board.getPiece(position(8, H));
		Move rockMove = board.getAvailableMove(position(8, F), BLACK);
		rockMove.getPiece().performMove(rockMove, board);

		assertTrue(BoardLoader.saveBoard(board, tempFilename));

		//Verify that the changes could be read
		ChessBoard savedBoard = new ChessBoard(false, true);
		assertTrue(BoardLoader.loadFileIntoBoard(new File(tempFilename), savedBoard));
		Piece savedRock = savedBoard.getPiece(position(8, F));
		assertTrue("Saved piece doesn't match the read one", blackRock.same(savedRock));
		assertEquals(1, savedBoard.undoMoves(1, false));
		assertNotNull(savedBoard.getPiece(position(8, H)));
	}


	@Test
	public void testLoadBoard()
	{
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_to_3D_should_not_be_possible");
		King whiteKing = King.class.cast(board.getPiece(position(2, E)));

		assertNull(board.getAvailableMove(whiteKing, position(3, D)));
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
