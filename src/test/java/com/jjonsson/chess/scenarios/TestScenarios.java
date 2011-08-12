package com.jjonsson.chess.scenarios;

import static com.jjonsson.chess.moves.Position.A;
import static com.jjonsson.chess.moves.Position.B;
import static com.jjonsson.chess.moves.Position.C;
import static com.jjonsson.chess.moves.Position.D;
import static com.jjonsson.chess.moves.Position.E;
import static com.jjonsson.chess.moves.Position.F;
import static com.jjonsson.chess.moves.Position.G;
import static com.jjonsson.chess.moves.Position.H;
import static com.jjonsson.chess.moves.Position.createPosition;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import junit.framework.Assert;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.persistence.BoardLoader;
import com.jjonsson.chess.persistence.ChessFileFilter;
import com.jjonsson.chess.pieces.BlackPawn;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.WhitePawn;

public class TestScenarios
{

	public static ChessBoard loadBoard(final String testName)
	{
		return loadBoard(testName, true);
	}

	public static ChessBoard loadBoard(final String testName, final boolean expectValidBoard)
	{
		String scenarioFile = "/scenarios/" + testName + ChessFileFilter.FILE_ENDING;
		ChessBoard board = new ChessBoard(false, true);
		assertEquals("Could not load:" + scenarioFile, expectValidBoard, BoardLoader.loadStreamIntoBoard(BoardLoader.class.getResourceAsStream(scenarioFile), board));
		return board;
	}

	@Test
	public void testPawnTakeOverMoveShouldRemoveAvailabilityOfTwoStepMove() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("pawn_take_over_move_should_remove_two_step_move");
		WhitePawn wp = (WhitePawn)board.getPiece(createPosition(2, F));
		Move takeOverMove = board.getAvailableMove(wp, createPosition(3, G));
		Assert.assertNotNull(takeOverMove);
		wp.performMove(takeOverMove, board, false);
		Move twoStepMove = board.getAvailableMove(wp, createPosition(5, G));
		assertNull(twoStepMove);
	}

	@Test
	public void testMoveAvailabilityUnderFutureCheck() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("new_move_is also_on_stopping_path_take_over_should_also_be_possible");

		Queen q = (Queen)board.getPiece(createPosition(2, F));

		Move stoppingMove = board.getAvailableMove(q, createPosition(3, G));
		Assert.assertNotNull(stoppingMove);

		Move takeOverMove = board.getAvailableMove(q, createPosition(4, H));
		Position oldQueenPosition = takeOverMove.getCurrentPosition().copy();
		Assert.assertNotNull(takeOverMove);

		assertEquals(ChessState.PLAYING, board.getCurrentState());
		q.performMove(takeOverMove, board, false);
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
	 * @throws InvalidPosition
	 */
	@Test
	public void testPawnReplacementAndGameStateChanges() throws UnavailableMoveException, NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = loadBoard("next_pawn_time_for_replacement_move_should_check_king_horse_take_queen_then_no_more_check");
		WhitePawn pawn = (WhitePawn)board.getPiece(createPosition(7, A));

		Move replacementMove = board.getAvailableMove(pawn, createPosition(8, B));
		assertNotNull(replacementMove);

		//Takes over the rock, replacing the pawn with a queen, checking the black king
		pawn.performMove(replacementMove, board, false);

		assertEquals(ChessState.CHECK, board.getCurrentState());

		Piece defendingKnight = board.getPiece(createPosition(6, C));
		assertNotNull(defendingKnight);
		Move defendingMove = board.getAvailableMove(defendingKnight, createPosition(8, B));
		assertNotNull(defendingMove);

		//Take over the queen
		defendingKnight.performMove(defendingMove, board, false);

		assertEquals(ChessState.PLAYING, board.getCurrentState());
		board.performRandomMove();
		assertEquals(ChessState.PLAYING, board.getCurrentState());
	}

	@Test
	public void testKingPossibleMovesUnderCheck() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("queen_left_move_check_king_not_possible_to_move_down");
		Queen q = (Queen)board.getPiece(createPosition(4, H));

		Move takeOverMove = board.getAvailableMove(q, createPosition(4, E));
		q.performMove(takeOverMove, board, false);

		assertEquals(ChessState.CHECK, board.getCurrentState());
		King k = (King)board.getPiece(createPosition(2, E));
		Move downMove = board.getAvailableMove(k, createPosition(1, E));
		assertNull(downMove);
	}

	@Test
	public void testTakeOverMoveShouldNotBePossible() throws InvalidPosition
	{
		ChessBoard board = loadBoard("knight_should_not_be_possible_to_take");
		King k = (King)board.getPiece(createPosition(8, F));
		Move takeOverMove = board.getAvailableMove(k, createPosition(8, E));
		assertNull(takeOverMove);
	}

	@Test
	public void testPawnTwoStepMoveShouldNotBeAbleToPassThroughAPiece() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("pawn_two_step_move_should_not_be_able_to_pass_by_king_and_protect_him");
		Queen q = Queen.class.cast(board.getPiece(createPosition(6, D)));

		Move checkMove = board.getAvailableMove(q, createPosition(5, E));
		q.performMove(checkMove, board, false);

		WhitePawn wp = WhitePawn.class.cast(board.getPiece(createPosition(2, E)));

		Move unavailableMove = board.getAvailableMove(wp, createPosition(4, E));

		assertNull("PawnTwoStepMove should not be able to pass through a piece", unavailableMove);
	}

	@Test
	public void testPawnForwardMoveShouldNotProtectPieces() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("queen_to_3D_should_not_checkmate");
		Queen q = Queen.class.cast(board.getPiece(createPosition(3, B)));
		Move checkingMove = board.getAvailableMove(q, createPosition(3, D));
		q.performMove(checkingMove, board, false);

		assertEquals(ChessState.CHECK, board.getCurrentState());

		King k = King.class.cast(board.getPiece(createPosition(2, E)));
		Move takeQueenMove = board.getAvailableMove(k, createPosition(3, D));
		k.performMove(takeQueenMove, board, false);

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
	public void testEnPassantPossiblility() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("enpassant_possible");
		Position enPassantTriggeringDestination = createPosition(4, B);
		//First we test if black can make an en-passant move
		WhitePawn whiteTriggeringPiece = WhitePawn.class.cast(board.getPiece(createPosition(2, B)));
		Move enpassantTriggeringMove = board.getAvailableMove(whiteTriggeringPiece, enPassantTriggeringDestination);
		whiteTriggeringPiece.performMove(enpassantTriggeringMove, board);

		BlackPawn leftBlackPawn = BlackPawn.class.cast(board.getPiece(createPosition(4, A)));
		Move enpassantMoveFromLeft = board.getAvailableMove(leftBlackPawn, createPosition(3, B));
		assertNotNull(enpassantMoveFromLeft);
		leftBlackPawn.performMove(enpassantMoveFromLeft, board);
		//En-passant should take over the piece that triggered the possibility of en-passant
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMove(enpassantMoveFromLeft, false);

		BlackPawn rightBlackPawn = BlackPawn.class.cast(board.getPiece(createPosition(4, C)));
		Move enpassantMoveFromRight = board.getAvailableMove(rightBlackPawn, createPosition(3, B));
		assertNotNull(enpassantMoveFromRight);
		rightBlackPawn.performMove(enpassantMoveFromRight, board);
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMove(enpassantMoveFromRight, false);

		//Then white
		enPassantTriggeringDestination = createPosition(5, G);
		BlackPawn blackTriggeringPiece = BlackPawn.class.cast(board.getPiece(createPosition(7, G)));
		enpassantTriggeringMove = board.getAvailableMove(blackTriggeringPiece, enPassantTriggeringDestination);
		blackTriggeringPiece.performMove(enpassantTriggeringMove, board);

		WhitePawn leftWhitePawn = WhitePawn.class.cast(board.getPiece(createPosition(5, F)));
		enpassantMoveFromLeft = board.getAvailableMove(leftWhitePawn, createPosition(6, G));
		assertNotNull(enpassantMoveFromLeft);
		leftWhitePawn.performMove(enpassantMoveFromLeft, board);
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMove(enpassantMoveFromLeft, false);

		WhitePawn rightWhitePawn = WhitePawn.class.cast(board.getPiece(createPosition(5, H)));
		enpassantMoveFromRight = board.getAvailableMove(rightWhitePawn, createPosition(6, G));
		assertNotNull(enpassantMoveFromRight);
		rightWhitePawn.performMove(enpassantMoveFromRight, board);
		assertNull(board.getPiece(enPassantTriggeringDestination));
		board.undoMove(enpassantMoveFromRight, false);

		//Check that an intermediate move disables the possibility of an en-passant move
		Move intermediateWhiteMove = board.getAvailableMove(whiteTriggeringPiece, createPosition(5, B));
		whiteTriggeringPiece.performMove(intermediateWhiteMove, board);
		Move intermediateBlackMove = board.getAvailableMove(rightBlackPawn, createPosition(3, C));
		rightBlackPawn.performMove(intermediateBlackMove, board);

		//An en-passant move should only be possible right after the right pawn two-step move
		assertFalse(enpassantMoveFromRight.canBeMade(board));

		//TODO: verify that en-passant is possible even after board save/load operations
	}

	@Test
	public void testThatAPieceCanMoveWhenAnotherPieceProtectsTheKing() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("black_pawn_at_7D_should_be_able_to_move_to_6D");
		BlackPawn pawn = BlackPawn.class.cast(board.getPiece(createPosition(7, D)));
		Move move = board.getAvailableMove(pawn, createPosition(6, D));
		pawn.performMove(move, board);
	}

	@Test
	public void testCheckAndCheckmateOnAndOff() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("should_not_be_checkmate");
		assertEquals(ChessState.CHECK, board.getCurrentState());
		//This also tests that moves are updated somewhat properly
		board.move(createPosition(8, G), createPosition(8, H));
		board.move(createPosition(4, C), createPosition(4, D));
		board.move(createPosition(8, H), createPosition(8, G));
		board.move(createPosition(4, D), createPosition(5, D));
		assertEquals(ChessState.CHECK, board.getCurrentState());
	}

	@Test
	public void testTrickyPlayWithKing() throws InvalidPosition
	{
		ChessBoard board = loadBoard("king_should_not_able_to_take_bishop_at_2E");
		Piece king = board.getPiece(createPosition(1, D));
		Move notAvailableMove = board.getNonAvailableMove(king, createPosition(2, E));
		assertFalse(notAvailableMove.canBeMade(board));
	}

	@Test
	public void testThatAKingCantMoveIntoThreatenedPosition() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("white_king_should_not_be_able_to_take_over_bishop_after_take_over");
		board.move(createPosition(3, F), createPosition(2, E));
		Piece king = board.getPiece(createPosition(1, D));
		Move takeOverMove = board.getAvailableMove(king, createPosition(2, E));
		assertNull(takeOverMove);
	}
}
