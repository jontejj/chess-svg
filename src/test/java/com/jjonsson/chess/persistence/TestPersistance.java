package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.board.PiecePlacement.DONT_PLACE_PIECES;
import static com.jjonsson.chess.moves.ImmutablePosition.from;
import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.moves.Position.Column.E;
import static com.jjonsson.chess.moves.Position.Column.F;
import static com.jjonsson.chess.moves.Position.Column.G;
import static com.jjonsson.chess.persistence.PersistanceLogging.SKIP_PERSISTANCE_LOGGING;
import static com.jjonsson.chess.persistence.PersistanceLogging.USE_PERSISTANCE_LOGGING;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Rock;

public class TestPersistance
{

	public class ErrorenousInputStream extends InputStream
	{
		@Override
		public int read() throws IOException
		{
			throw new IOException("Faked exception");
		}
	}

	@Test
	public void testGetPieceFromPersistanceData()
	{
		ChessBoard board = new ChessBoard(DONT_PLACE_PIECES);

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
		Rock r = new Rock(position("8H").asMutable(), WHITE, board);
		buffer.putShort(r.getPersistenceData());
		buffer.flip();
		Piece r2 = Piece.getPieceFromPersistenceData(buffer, board);
		assertTrue("Saved piece doesn't match the read one", r.same(r2));
	}

	@Test
	public void testThatMovedKingCantCastleAfterBoardSaveAndLoad() throws NoMovesAvailableException
	{
		String tempFilename = "temp_save_test_1";
		ChessBoard board = loadBoard("castling_move");
		Position kingOriginalPos = from(1, E);
		Position castlingDestinationPos = from(1, G);
		Position kingTempPos = from(1, F);
		King whiteKing = board.getKing(WHITE);

		//First verify that it's possible to castle before any moves have been made
		Move castlingMoveBeforeSave = board.getAvailableMove(whiteKing, castlingDestinationPos);
		assertTrue(castlingMoveBeforeSave.canBeMade(board));

		Move kingRightMove = board.getAvailableMove(whiteKing, kingTempPos);
		assertTrue(whiteKing.performMove(kingRightMove, board));

		board.performRandomMove();
		//This removes the possibility to castle
		Move kingLeftMove = board.getAvailableMove(whiteKing, kingOriginalPos);
		assertTrue(whiteKing.performMove(kingLeftMove, board));

		castlingMoveBeforeSave = board.getAvailableMove(whiteKing, castlingDestinationPos);
		assertNull(castlingMoveBeforeSave);
		assertTrue(BoardLoader.saveBoard(board, tempFilename));

		//Verify that the king can't castle after the save
		ChessBoard savedBoard = new ChessBoard(DONT_PLACE_PIECES, USE_PERSISTANCE_LOGGING);
		assertTrue(BoardLoader.loadFileIntoBoard(new File(tempFilename), savedBoard));
		King savedKing = savedBoard.getKing(WHITE);
		assertNull(savedBoard.getAvailableMove(savedKing, castlingDestinationPos));
	}

	@Test
	public void testSaveBoard()
	{
		String tempFilename = "temp_save_test";
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_should_not_be_able_to_move");
		Piece blackRock = board.getPiece(position("8H"));
		Move rockMove = board.getAvailableMove(position("8F"), BLACK);
		assertTrue(rockMove.getPiece().performMove(rockMove, board));

		assertTrue(BoardLoader.saveBoard(board, tempFilename));

		//Verify that the changes could be read
		ChessBoard savedBoard = new ChessBoard(DONT_PLACE_PIECES, USE_PERSISTANCE_LOGGING);
		assertTrue(BoardLoader.loadFileIntoBoard(new File(tempFilename), savedBoard));
		Piece savedRock = savedBoard.getPiece(position("8F"));
		assertTrue("Saved piece doesn't match the read one", blackRock.same(savedRock));
		assertEquals(1, savedBoard.undoMoves(1, false));
		assertNotNull(savedBoard.getPiece(position("8H")));
	}


	@Test
	public void testLoadBoard()
	{
		//Load a board and make changes to it
		ChessBoard board = loadBoard("king_to_3D_should_not_be_possible");
		King whiteKing = King.class.cast(board.getPiece(position("2E")));

		assertNull(board.getAvailableMove(whiteKing, position("3D")));

		//Test the exception handling
		assertFalse(BoardLoader.loadStreamIntoBoard(new ErrorenousInputStream(), board));
		assertFalse(BoardLoader.loadFileIntoBoard(new File("no_file"), board));
		assertFalse(BoardLoader.saveBoard(board, "."));
	}

	@Test
	public void testCopyBoard()
	{
		ChessBoard board = loadBoard("board_cloning_test");
		Collection<Piece> pieces = board.getPieces();
		ChessBoard copy = board.copy(SKIP_PERSISTANCE_LOGGING);
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
