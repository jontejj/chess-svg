package com.jjonsson.chess.evaluators;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import static com.jjonsson.chess.pieces.Piece.*;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;

public class TestChessMoveEvaluator
{	
	/**
	 * Test if the AI is to aggressive and doesn't recognize that the best move may be to move to cover 
	 * instead of taking a less valuable piece as a trade for a more valuable one
	 * @throws UnavailableMoveException 
	 * @throws NoMovesAvailableException 
	 * @throws InvalidPosition 
	 */
	@Test
	public void testStupidTakeOverShouldNotBeMade() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = loadBoard("bishop_should_move_rational");
		//Pawn should not be taken by the bishop at 7G
		makeSureMoveWasNotMade(board, Position.createPosition(2, Position.B));
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
	public void testCheckmateThreatShouldBeNeutralizedByAResonableMove() throws NoMovesAvailableException, InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("chechmate_threat_must_be_neatrulized");
		assertEquals(ChessState.PLAYING, board.getCurrentState());
		makeSureMoveWasNotMade(board, Position.createPosition(8, Position.G));
		Piece blackQueen = board.getPiece(Position.createPosition(7, Position.G));
		Move possibleCheckMateMove = blackQueen.getAvailableMoveForPosition(Position.createPosition(3, Position.G), board);
		blackQueen.performMove(possibleCheckMateMove, board);
		//The King should be able to flee
		assertFalse(ChessState.CHECKMATE == board.getCurrentState());
	}
	
	/**
	 * Test if the AI avoids making risky moves that haven't been searched deeper than one level and thus may look good but
	 * when in fact they aren't
	 * @throws UnavailableMoveException 
	 * @throws NoMovesAvailableException 
	 * @throws InvalidPosition 
	 */
	@Test
	public void testOnlyMakeSureMoves() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = loadBoard("white_queen_should_not_move_to_5H");
		makeSureMoveWasNotMade(board, Position.createPosition(5, Position.H));
	}
	
	@Test
	public void testDontMoveIntoThreatenedSquare() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = loadBoard("white_queen_should_not_move_to_4C");
		makeSureMoveWasNotMade(board, Position.createPosition(4, Position.C));
	}
	
	@Test
	public void testWhiteQueenShouldAvoidBeingTaken() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = loadBoard("white_pawn_should_protect_queen_by_moving_to_4C");
		//This should move the queen out of harms way
		ChessMoveEvaluator.performBestMove(board);
		//Verify that the black pawn can't take over the queen
		assertTrue(board.getAvailableMoves(Position.createPosition(3, Position.B), Piece.BLACK).isEmpty());
	}
	
	@Test
	public void testQueenShouldEvadeBeingTaken() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = loadBoard("queen_should_evade");
		Piece blackQueen = board.getPiece(Position.createPosition(5, Position.D));
		Piece knight = blackQueen.getCheapestPieceThatTakesMeOver();
		assertNotNull(knight);
		//This should move the queen out of harms way (offering the rock)
		ChessMoveEvaluator.performBestMove(board);
		assertEquals(blackQueen, board.getLastMove().getPiece());
		//Verify that the knight can't take over the queen
		assertNull(blackQueen.getCheapestPieceThatTakesMeOver());
	}
	
	@Test
	public void testKnightShouldEvadeBeingTaken() throws NoMovesAvailableException, InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("knight_should_not_be_left_unprotected");
		Position knightPosition = Position.createPosition(5, Position.A);
		King blackDefendingKing = King.class.cast(board.getPiece(Position.createPosition(6, Position.B)));
		Piece knight = board.getPiece(knightPosition);
		Piece bishop = knight.getCheapestPieceThatTakesMeOver();
		assertNotNull(bishop);
		//The king should still be able to protect the knight after this move
		ChessMoveEvaluator.performBestMove(board);
		
		Move bishopMove = bishop.getAvailableMoveForPosition(knightPosition, board);
		bishop.performMove(bishopMove, board);
		
		//The king should now be able to take over the bishop
		Move bishopTakeOverMove = blackDefendingKing.getAvailableMoveForPosition(knightPosition, board);
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
		
		assertTrue(ChessState.CHECKMATE == board.getCurrentState());
	}
	
	@Test
	public void testBishopShouldAvoidBeingTaken() throws NoMovesAvailableException, InvalidPosition
	{
		ChessBoard board = loadBoard("bishop_should_escape_from_6E");
		ChessMoveEvaluator.performBestMove(board);
		assertTrue(board.getAvailableMoves(Position.createPosition(6, Position.E), WHITE).isEmpty());
	}
	
	private void makeSureMoveWasNotMade(ChessBoard board, Position badPosition) throws NoMovesAvailableException
	{
		ChessMoveEvaluator.performBestMove(board);
		Move lastMove = board.getLastMove();
		assertFalse("Last move should not be to: " + badPosition, lastMove.getCurrentPosition().equals(badPosition));
	}
}
