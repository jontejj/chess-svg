package com.jjonsson.chess.evaluators;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.moves.Position.B;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;

public class TestChessMoveEvaluator
{
	/**
	 * Test if the AI is to aggressive and doesn't recognize that the best move may be to move to cover
	 * instead of taking a less valuable piece as a trade for a more valuable one
	 * @throws NoMovesAvailableException
	 */
	@Test
	public void testStupidTakeOverShouldNotBeMade() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("bishop_should_move_rational");
		//Pawn should not be taken by the bishop at 7G
		makeSureMoveWasNotMade(board, position(2, B));
	}

	@Test
	public void testADirectChechMateShouldBePrioritizedOverAFutureOne() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("pawn_moves_that_reach_their_destinations_should_be_worth_as_much_as_their_replacement_value");
		ChessMoveEvaluator.performBestMove(board);
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
	}

	@Test
	public void testMakingAMoveWhileInCheckMateState()
	{
		ChessBoard board = loadBoard("should_be_checkmate");
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
		try
		{
			ChessMoveEvaluator.performBestMove(board);
		}
		catch (NoMovesAvailableException e)
		{
			assertFalse(ChessBoardEvaluator.inPlay(board));
		}
		assertNull(board.getLastMove());
	}

	@Test
	public void testCheckmateThreatShouldBeNeutralizedByAResonableMove() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("chechmate_threat_must_be_neatrulized");
		assertEquals(ChessState.PLAYING, board.getCurrentState());
		makeSureMoveWasNotMade(board, position("8G"));
		Piece blackQueen = board.getPiece(position("7G"));
		Move possibleCheckMateMove = board.getAvailableMove(blackQueen, position("3G"));
		assertTrue(blackQueen.performMove(possibleCheckMateMove, board));
		//The King should be able to flee
		assertFalse(ChessState.CHECKMATE == board.getCurrentState());
	}

	/**
	 * Test if the AI avoids making risky moves that haven't been searched deeper than one level and thus may look good but
	 * when in fact they aren't
	 * @throws NoMovesAvailableException
	 */
	@Test
	public void testOnlyMakeSureMoves() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("white_queen_should_not_move_to_5H");
		makeSureMoveWasNotMade(board, position("5H"));
	}

	@Test
	public void testDontMoveIntoThreatenedSquare() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("white_queen_should_not_move_to_4C");
		makeSureMoveWasNotMade(board, position("4C"));
	}

	@Test
	public void testWhiteQueenShouldAvoidBeingTaken() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("white_pawn_should_protect_queen_by_moving_to_4C");
		//This should move the queen out of harms way
		ChessMoveEvaluator.performBestMove(board);
		//Verify that the black pawn can't take over the queen
		assertTrue(board.getAvailableMoves(position("3B"), Piece.BLACK).isEmpty());
	}

	@Test
	public void testQueenShouldEvadeBeingTaken() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("queen_should_evade");
		Piece blackQueen = board.getPiece(position("5D"));
		Piece knight = blackQueen.getCheapestPieceThatTakesMeOver();
		assertNotNull(knight);
		//This should move the queen out of harms way (offering the rock)
		ChessMoveEvaluator.performBestMove(board);
		assertEquals(blackQueen, board.getLastMove().getPiece());
		//Verify that the knight can't take over the queen
		assertNull(blackQueen.getCheapestPieceThatTakesMeOver());
	}

	@Test
	public void testKnightShouldEvadeBeingTaken() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("knight_should_not_be_left_unprotected");
		ImmutablePosition knightPosition = position("5A");
		King blackDefendingKing = King.class.cast(board.getPiece(position("6B")));
		Piece knight = board.getPiece(knightPosition);
		Piece bishop = knight.getCheapestPieceThatTakesMeOver();
		assertNotNull(bishop);
		//The king should still be able to protect the knight after this move
		ChessMoveEvaluator.performBestMove(board);

		Move bishopMove = board.getAvailableMove(bishop, knightPosition);
		assertTrue(bishop.performMove(bishopMove, board));

		//The king should now be able to take over the bishop
		Move bishopTakeOverMove = board.getAvailableMove(blackDefendingKing, knightPosition);
		blackDefendingKing.performMove(bishopTakeOverMove, board);

		assertTrue(bishop.isRemoved());
	}

	@Test
	public void testShouldFinishRatherQuick() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("should_finish_rather_quick");
		//Without searching deep enough the game goes into a loop
		board.setDifficulty(2);
		ChessMoveEvaluator.performBestMove(board);

		//Moves the king to the only available slot
		board.performRandomMove();

		ChessMoveEvaluator.performBestMove(board);
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
	}

	@Test
	public void testBishopShouldAvoidBeingTaken() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("bishop_should_escape_from_6E");
		ChessMoveEvaluator.performBestMove(board);
		assertTrue(board.getAvailableMoves(position("6E"), WHITE).isEmpty());
	}

	@Test
	public void testThatStalemateIsAvoidedWhileHavingAdvantage() throws NoMovesAvailableException
	{
		ChessBoard board = loadBoard("should_not_make_move_that_stalemates");
		ChessMoveEvaluator.performBestMove(board);
		assertFalse(ChessState.STALEMATE.equals(board.getCurrentState()));
	}

	@Test
	public void testThatBestMoveDontReturnNull() throws UnavailableMoveItem, NoMovesAvailableException
	{
		ChessBoard board = loadBoard("best_move_should_not_be_null");
		board.move("4C", "5D");
		assertNotNull(ChessMoveEvaluator.getBestMove(board));
	}



	private void makeSureMoveWasNotMade(final ChessBoard board, final Position badPosition) throws NoMovesAvailableException
	{
		ChessMoveEvaluator.performBestMove(board);
		Move lastMove = board.getLastMove();
		assertFalse("Last move should not be to: " + badPosition, lastMove.getCurrentPosition().equals(badPosition));
	}
}
