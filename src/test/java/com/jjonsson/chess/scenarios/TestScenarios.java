package com.jjonsson.chess.scenarios;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import junit.framework.Assert;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.persistance.BoardLoader;
import com.jjonsson.chess.persistance.ChessFileFilter;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.WhitePawn;

public class TestScenarios
{
	
	public static ChessBoard loadBoard(String testName)
	{
		String scenarioFile = "/scenarios/" + testName + ChessFileFilter.FILE_ENDING;
		ChessBoard board = new ChessBoard(false);
		
		assertTrue("Could not load:" + scenarioFile, BoardLoader.loadStreamIntoBoard(BoardLoader.class.getResourceAsStream(scenarioFile), board));
		return board;
	}
	
	@Test
	public void testPawnTakeOverMoveShouldRemoveAvailabilityOfTwoStepMove() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("pawn_take_over_move_should_remove_two_step_move");
		WhitePawn wp = (WhitePawn)board.getPiece(Position.createPosition(2, Position.F));
		Move takeOverMove = wp.getAvailableMoveForPosition(Position.createPosition(3, Position.G), board);
		Assert.assertNotNull(takeOverMove);
		wp.performMove(takeOverMove, board, false);
		Move twoStepMove = wp.getAvailableMoveForPosition(Position.createPosition(5, Position.G), board);
		assertNull(twoStepMove);
	}
	
	@Test
	public void testMoveAvailabilityUnderFutureCheck() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("new_move_is also_on_stopping_path_take_over_should_also_be_possible");
		
		Queen q = (Queen)board.getPiece(Position.createPosition(2, Position.F));
		
		Move stoppingMove = q.getAvailableMoveForPosition(Position.createPosition(3, Position.G), board);
		Assert.assertNotNull(stoppingMove);
		
		Move takeOverMove = q.getAvailableMoveForPosition(Position.createPosition(4, Position.H), board);
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
		WhitePawn pawn = (WhitePawn)board.getPiece(Position.createPosition(7, Position.A));

		Move replacementMove = pawn.getAvailableMoveForPosition(Position.createPosition(8, Position.B), board);
		assertNotNull(replacementMove);
		
		//Takes over the rock, replacing the pawn with a queen, checking the black king
		pawn.performMove(replacementMove, board, false);
		
		assertEquals(ChessState.CHECK, board.getCurrentState());
		
		Piece defendingKnight = board.getPiece(Position.createPosition(6, Position.C));
		assertNotNull(defendingKnight);
		Move defendingMove = defendingKnight.getAvailableMoveForPosition(Position.createPosition(8, Position.B), board);
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
		Queen q = (Queen)board.getPiece(Position.createPosition(4, Position.H));

		Move takeOverMove = q.getAvailableMoveForPosition(Position.createPosition(4, Position.E), board);
		q.performMove(takeOverMove, board, false);

		assertEquals(ChessState.CHECK, board.getCurrentState());
		King k = (King)board.getPiece(Position.createPosition(2, Position.E));
		Move downMove = k.getAvailableMoveForPosition(Position.createPosition(1, Position.E), board);
		assertNull(downMove);
	}
	
	@Test
	public void testTakeOverMoveShouldNotBePossible() throws InvalidPosition
	{
		ChessBoard board = loadBoard("knight_should_not_be_possible_to_take");		
		King k = (King)board.getPiece(Position.createPosition(8, Position.F));
		Move takeOverMove = k.getAvailableMoveForPosition(Position.createPosition(8, Position.E), board);
		assertNull(takeOverMove);
	}
	
	@Test
	public void testPawnTwoStepMoveShouldNotBeAbleToPassThroughAPiece() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("pawn_two_step_move_should_not_be_able_to_pass_by_king_and_protect_him");		
		Queen q = Queen.class.cast(board.getPiece(Position.createPosition(6, Position.D)));

		Move checkMove = q.getAvailableMoveForPosition(Position.createPosition(5, Position.E), board);
		q.performMove(checkMove, board, false);
		
		WhitePawn wp = WhitePawn.class.cast(board.getPiece(Position.createPosition(2, Position.E)));

		Move unavailableMove = wp.getAvailableMoveForPosition(Position.createPosition(4, Position.E), board);
		
		assertNull("PawnTwoStepMove should not be able to pass through a piece", unavailableMove);
	}
	
	@Test
	public void testPawnForwardMoveShouldNotProtectPieces() throws InvalidPosition, UnavailableMoveException
	{
		ChessBoard board = loadBoard("queen_to_3D_should_not_checkmate");
		Queen q = Queen.class.cast(board.getPiece(Position.createPosition(3, Position.B)));
		Move checkingMove = q.getAvailableMoveForPosition(Position.createPosition(3, Position.D), board);
		q.performMove(checkingMove, board, false);
		
		assertEquals(ChessState.CHECK, board.getCurrentState());
		
		King k = King.class.cast(board.getPiece(Position.createPosition(2, Position.E)));
		Move takeQueenMove = k.getAvailableMoveForPosition(Position.createPosition(3, Position.D), board);
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
}