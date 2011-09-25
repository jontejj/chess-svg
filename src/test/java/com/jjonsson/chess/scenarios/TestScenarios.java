package com.jjonsson.chess.scenarios;

import static com.jjonsson.chess.board.PiecePlacement.DONT_PLACE_PIECES;
import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.persistence.PersistanceLogging.USE_PERSISTANCE_LOGGING;
import static com.jjonsson.utilities.Loggers.STDOUT;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collection;
import java.util.Set;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.persistence.BoardLoader;
import com.jjonsson.chess.persistence.ChessFileFilter;
import com.jjonsson.chess.pieces.BlackPawn;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.WhitePawn;
import com.jjonsson.chess.utilities.VersionControlHelper;
import com.jjonsson.chess.utilities.VersionControlHelper.ExceptionHandling;

public class TestScenarios
{

	public static ChessBoard loadBoard(final String testName)
	{
		return loadBoard(testName, true);
	}

	private static Set<String> usedTestFiles = Sets.newHashSet();

	public static ChessBoard loadBoard(final String testName, final boolean expectValidBoard)
	{
		String scenarioFile = "/scenarios/" + testName + ChessFileFilter.FILE_ENDING;
		STDOUT.info("Loading: " + scenarioFile);

		usedTestFiles.add(testName + ChessFileFilter.FILE_ENDING);

		ChessBoard board = new ChessBoard(DONT_PLACE_PIECES, USE_PERSISTANCE_LOGGING);

		VersionControlHelper.assertThatResourceIsVersionControlled(scenarioFile, ExceptionHandling.IGNORE_EXCEPTION);

		assertEquals("Could not load:" + scenarioFile, expectValidBoard, BoardLoader.loadStreamIntoBoard(BoardLoader.class.getResourceAsStream(scenarioFile), board));
		return board;
	}

	@Test
	public void testPawnTakeOverMoveShouldRemoveAvailabilityOfTwoStepMove()
	{
		ChessBoard board = loadBoard("pawn_take_over_move_should_remove_two_step_move");
		WhitePawn wp = (WhitePawn)board.getPiece(position("2F"));
		Move takeOverMove = board.getAvailableMove(wp, position("3G"));
		Assert.assertNotNull(takeOverMove);
		assertTrue(wp.performMove(takeOverMove, board, false));
		Move twoStepMove = board.getAvailableMove(wp, position("5G"));
		assertNull(twoStepMove);
	}

	@Test
	public void testMoveAvailabilityUnderFutureCheck()
	{
		ChessBoard board = loadBoard("new_move_is_also_on_stopping_path_take_over_should_also_be_possible");

		Queen q = (Queen)board.getPiece(position("2F"));

		Move stoppingMove = board.getAvailableMove(q, position("3G"));
		Assert.assertNotNull(stoppingMove);

		Move takeOverMove = board.getAvailableMove(q, position("4H"));
		Position oldQueenPosition = takeOverMove.getCurrentPosition().copy();
		Assert.assertNotNull(takeOverMove);

		assertEquals(ChessState.PLAYING, board.getCurrentState());
		assertTrue(q.performMove(takeOverMove, board, false));
		assertEquals(ChessState.PLAYING, board.getCurrentState());

		//When the move has been made the current position for the move should have changed
		assertFalse(oldQueenPosition.equals(takeOverMove.getCurrentPosition()));

		Piece pieceAtNewLocation = board.getPiece(takeOverMove.getCurrentPosition());
		assertEquals(pieceAtNewLocation, q);
	}

	/**
	 * Tests if available moves is removed when a piece is taken and that a pawn is replaced when it reaches it's destination
	 * @throws UnavailableMoveException
	 * @throws NoMovesAvailableException
	 * @throws UnavailableMoveItem
	 */
	@Test
	public void testPawnReplacementAndGameStateChanges() throws NoMovesAvailableException, UnavailableMoveItem
	{
		ChessBoard board = loadBoard("next_pawn_time_for_replacement_move_should_check_king_horse_take_queen_then_no_more_check");

		//White pawn takes over the rock, replacing the pawn with a queen, checking the black king
		board.move("7A", "8B");
		assertEquals(ChessState.CHECK, board.getCurrentState());

		//Take over the queen with the knight
		board.move("6C", "8B");

		assertEquals(ChessState.PLAYING, board.getCurrentState());
		board.performRandomMove();
		assertEquals(ChessState.PLAYING, board.getCurrentState());
	}

	@Test
	public void testKingPossibleMovesUnderCheck() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("queen_left_move_check_king_not_possible_to_move_down");
		board.move("4H", "4E");
		assertEquals(ChessState.CHECK, board.getCurrentState());
		King k = (King)board.getPiece(position("2E"));
		Move downMove = board.getAvailableMove(k, position("1E"));
		assertNull(downMove);
	}

	@Test
	public void testTakeOverMoveShouldNotBePossible()
	{
		ChessBoard board = loadBoard("knight_should_not_be_possible_to_take");
		King k = (King)board.getPiece(position("8F"));
		Move takeOverMove = board.getAvailableMove(k, position("8E"));
		assertNull(takeOverMove);
	}

	@Test
	public void testPawnTwoStepMoveShouldNotBeAbleToPassThroughAPiece() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("pawn_two_step_move_should_not_be_able_to_pass_by_king_and_protect_him");
		board.move("6D", "5E");

		WhitePawn wp = WhitePawn.class.cast(board.getPiece(position("2E")));

		Move unavailableMove = board.getAvailableMove(wp, position("4E"));

		assertNull("PawnTwoStepMove should not be able to pass through a piece", unavailableMove);
	}

	@Test
	public void testPawnForwardMoveShouldNotProtectPieces() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("queen_to_3D_should_not_checkmate");
		board.move("3B", "3D");
		assertEquals(ChessState.CHECK, board.getCurrentState());
		board.move("2E", "3D");
		assertEquals(ChessState.PLAYING, board.getCurrentState());
	}

	@Test
	public void testStalemateSituations()
	{
		ChessBoard board = loadBoard("only_knight_should_be_stalemate");
		assertEquals(ChessState.STALEMATE, board.getCurrentState());
		board = loadBoard("only_bishop_should_be_stalemate");
		assertEquals(ChessState.STALEMATE, board.getCurrentState());
		board = loadBoard("only_kings_should_be_stalemate");
		assertEquals(ChessState.STALEMATE, board.getCurrentState());
		board = loadBoard("two_bishops_on_the_same_diagonal_should_be_stalemate");
		assertEquals(ChessState.STALEMATE, board.getCurrentState());
		board = loadBoard("bishops_on_different_diagonals_should_not_be_stalemate");
		assertEquals(ChessState.PLAYING, board.getCurrentState());
	}

	@Test
	public void testEnPassantPossiblility() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("enpassant_possible");
		ImmutablePosition enPassantTriggeringDestination = position("4B");
		//First we test if black can make an en-passant move
		board.move("2B", enPassantTriggeringDestination.toString()); //White opens for en-passant
		board.move("4A", "3B"); //Black makes en-passant from left
		//En-passant should take over the piece that triggered the possibility of en-passant
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMoves(1, false); //Undo should place the white pawn back on 4B
		assertNotNull(board.getPiece(enPassantTriggeringDestination));
		board.move("4C", "3B"); //Black makes en-passant from right
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMoves(1, false); //Undo should place the white pawn back on 4B

		//Then white
		enPassantTriggeringDestination = position("5G");
		board.move("7G", enPassantTriggeringDestination.toString());

		board.move("5F", "6G");
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMoves(1, false);

		board.move("5H", "6G");
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMoves(1, false);

		//Check that an intermediate move disables the possibility of an en-passant move
		board.move("4B", "5B");
		board.move("4C", "3C");

		//An en-passant move should only be possible right after the right pawn two-step move
		Piece blackPiece = board.getPiece(position("5H"));
		assertNull(board.getAvailableMove(blackPiece, position("6G")));

		//TODO: verify that en-passant is possible even after board save/load operations
	}

	@Test
	public void testEnpassantUpdateMovesForPositionOfTakeoverPawn() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("enpassant_should_update_moves_for_the_position_of_the_takeover_pawn");
		board.move("6B", "5C");
	}

	@Test
	public void testThatAPieceCanMoveWhenAnotherPieceProtectsTheKing()
	{
		ChessBoard board = loadBoard("black_pawn_at_7D_should_be_able_to_move_to_6D");
		BlackPawn pawn = BlackPawn.class.cast(board.getPiece(position("7D")));
		Move move = board.getAvailableMove(pawn, position("6D"));
		assertTrue(pawn.performMove(move, board));
	}

	@Test
	public void testCheckAndCheckmateOnAndOff() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("should_not_be_checkmate");
		assertEquals(ChessState.CHECK, board.getCurrentState());
		//This also tests that moves are updated somewhat properly
		board.move(position("8G"), position("8H"));
		board.move(position("4C"), position("4D"));
		board.move(position("8H"), position("8G"));
		board.move(position("4D"), position("5D"));
		assertEquals(ChessState.CHECK, board.getCurrentState());
	}

	@Test
	public void testTrickyPlayWithKing()
	{
		ChessBoard board = loadBoard("king_should_not_able_to_take_bishop_at_2E");
		Piece king = board.getPiece(position("1D"));
		Move notAvailableMove = board.getNonAvailableMove(king, position("2E"));
		assertFalse(notAvailableMove.canBeMade(board));
	}

	@Test
	public void testThatAKingCantMoveIntoThreatenedPosition() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("white_king_should_not_be_able_to_take_over_bishop_after_take_over");
		board.move(position("3F"), position("2E"));
		Piece king = board.getPiece(position("1D"));
		Move takeOverMove = board.getAvailableMove(king, position("2E"));
		assertNull(takeOverMove);
	}

	@Test
	public void testThatAProtectingPieceCanMoveIntoAPositionThatAlsoProtectsTheKing()
	{
		ChessBoard board = loadBoard("white_bishop_should_be_able_to_move_to_3C");
		Piece bishop = board.getPiece(position("2D"));
		Move bishopMove = board.getAvailableMove(bishop, position("3C"));
		assertTrue(bishop.performMove(bishopMove, board));
	}

	@Test
	public void testThatPiecesThatProtectsTheKingCanMoveWhenANewPieceProtectsTheKing() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("pieces_that_protects_the_king_should_be_able_to_move_when_a_new_piece_protects_the_king");
		board.move("2D", "4E");
	}

	@Test
	public void testHintedMoveWasNotPossible()
	{
		ChessBoard board = loadBoard("hinted_move_should_be_possible");
		Piece queen = board.getPiece(position("1D"));
		Move queenMove = board.getAvailableMove(queen, position("3F"));
		assertTrue(queen.performMove(queenMove, board));
	}

	@Test
	public void testOnlyKingShouldBeAllowedToMove()
	{
		Collection<String> files = Lists.newArrayList("only_the_king_should_be_allowed_to_move", "only_the_king_should_be_allowed_to_move_2");
		for(String file : files)
		{
			ChessBoard board = loadBoard(file);
			for(Move m : board.getAvailableMoves())
			{
				assertTrue(m + " was allowed to be made", m instanceof KingMove);
			}
		}
	}

	@Test
	public void testKingMoveUpdates() throws UnavailableMoveItem
	{
		ChessBoard board = loadBoard("king_should_not_be_able_to_move_into_thretenened_2");
		board.move("8C", "7B");
		Piece king = board.getPiece(position("5A"));
		assertNull(board.getAvailableMove(king, position("6A")));
	}

	@AfterClass
	public static void testMoveUnusedTestFiles()
	{
		File testFiles = new File("src/test/resources/scenarios/");
		for(File testFile : testFiles.listFiles())
		{
			if(!usedTestFiles.contains(testFile.getName()))
			{
				//testFile.renameTo(new File("src/test/resources/scenarios/unused/" + testFile.getName()));
				System.out.println("Possibly unused test file: " + testFile);
			}
		}
	}
}
