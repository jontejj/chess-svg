package com.jjonsson.chess.scenarios;

import static junit.framework.Assert.assertNull;

import org.junit.Test;

import junit.framework.Assert;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
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
		String scenarioFile = "/scenarios/" + testName + ChessFileFilter.fileEnding;
		ChessBoard board = new ChessBoard(false);
		
		if(!BoardLoader.loadStreamIntoBoard(BoardLoader.class.getResourceAsStream(scenarioFile), board))
		{
			Assert.fail("Could not load:" + scenarioFile);
		}
		return board;
	}
	
	@Test
	public void testPawnTakeOverMoveShouldRemoveAvailabilityOfTwoStepMove() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("pawn_take_over_move_should_remove_two_step_move");
		Piece p = board.getPiece(Position.createPosition(2, Position.F));
		if(p instanceof WhitePawn)
		{
			WhitePawn wp = (WhitePawn) p;
			Move takeOverMove = wp.getAvailableMoveForPosition(Position.createPosition(3, Position.G), board);
			Assert.assertNotNull(takeOverMove);
			wp.performMove(takeOverMove, board, false);
			Move twoStepMove = wp.getAvailableMoveForPosition(Position.createPosition(5, Position.G), board);
			if(twoStepMove != null)
				Assert.assertTrue("Pawn should not able to make: " + twoStepMove, !twoStepMove.canBeMade(board));
			
		}
		else
			Assert.fail("Piece under test should be a white pawn, was: " + p);
	}
	
	@Test
	public void testMoveAvailabilityUnderFutureCheck() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("new_move_is also_on_stopping_path_take_over_should_also_be_possible");
		
		Piece p = board.getPiece(Position.createPosition(2, Position.F));
		if(p instanceof Queen)
		{
			Queen q = (Queen)p;
			Move stoppingMove = q.getAvailableMoveForPosition(Position.createPosition(3, Position.G), board);
			Assert.assertNotNull(stoppingMove);
			Move takeOverMove = q.getAvailableMoveForPosition(Position.createPosition(4, Position.H), board);
			Position oldQueenPosition = takeOverMove.getCurrentPosition().clone();
			Assert.assertNotNull(takeOverMove);
			Assert.assertNotNull(stoppingMove);
			ChessState currentState = board.getCurrentState();
			Assert.assertTrue("Game state", currentState == ChessState.PLAYING);
			q.performMove(takeOverMove, board, false);
			currentState = board.getCurrentState();
			Assert.assertTrue("Game state", currentState == ChessState.PLAYING);
			Assert.assertTrue("When move has been made the current position for the move should have changed", !oldQueenPosition.equals(takeOverMove.getCurrentPosition()));
			Piece pieceAtNewLocation = board.getPiece(takeOverMove.getCurrentPosition());
			Assert.assertEquals("The board's piece at: " + takeOverMove.getCurrentPosition() + "was not right" , pieceAtNewLocation, q);
		}
		else
			Assert.fail("Piece under test should be a queen was: " + p);
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
		Piece p = board.getPiece(Position.createPosition(7, Position.A));
		if(p instanceof WhitePawn)
		{
			WhitePawn pawn = (WhitePawn)p;
			Move replacementMove = pawn.getAvailableMoveForPosition(Position.createPosition(8, Position.B), board);
			Assert.assertNotNull(replacementMove);
			
			//Takes over the rock, replacing the pawn with a queen, checking the black king
			pawn.performMove(replacementMove, board, false);
			
			ChessState currentState = board.getCurrentState();
			Assert.assertTrue("Game state", currentState == ChessState.CHECK);
			
			Piece defendingKnight = board.getPiece(Position.createPosition(6, Position.C));
			Assert.assertNotNull(defendingKnight);
			Move defendingMove = defendingKnight.getAvailableMoveForPosition(Position.createPosition(8, Position.B), board);
			Assert.assertNotNull(defendingMove);
			
			//Take over the queen
			defendingKnight.performMove(defendingMove, board, false);
			
			currentState = board.getCurrentState();
			Assert.assertTrue("Game state", currentState == ChessState.PLAYING);
			board.performRandomMove();
			currentState = board.getCurrentState();
			Assert.assertTrue("Game state", currentState == ChessState.PLAYING);
		}
		else
			Assert.fail("Piece under test should be a white pawn was: " + p);
	}
	
	@Test
	public void testKingPossibleMovesUnderCheck() throws UnavailableMoveException, InvalidPosition
	{
		ChessBoard board = loadBoard("queen_left_move_check_king_not_possible_to_move_down");		
		Piece p = board.getPiece(Position.createPosition(4, Position.H));
		if(p instanceof Queen)
		{
			Queen q = (Queen)p;
			Move takeOverMove = q.getAvailableMoveForPosition(Position.createPosition(4, Position.E), board);
			Assert.assertNotNull(takeOverMove);
			q.performMove(takeOverMove, board, false);

			ChessState currentState = board.getCurrentState();
			Assert.assertTrue("Game state", currentState == ChessState.CHECK);
			Piece p2 = board.getPiece(Position.createPosition(2, Position.E));
			if(p2 instanceof King)
			{
				King k = (King)p2;
				Move downMove = k.getAvailableMoveForPosition(Position.createPosition(1, Position.E), board);
				if(downMove != null)
					Assert.assertTrue("Queen should be threatening this position: " + downMove, !downMove.canBeMade(board));
			}
			else
				Assert.fail("Piece under test should be a king was: " + p);
		}
		else
			Assert.fail("Piece under test should be a queen was: " + p);
	}
	
	@Test
	public void testTakeOverMoveShouldNotBePossible() throws InvalidPosition
	{
		ChessBoard board = loadBoard("knight_should_not_be_possible_to_take");		
		Piece p = board.getPiece(Position.createPosition(8, Position.F));
		if(p instanceof King)
		{
			King k = (King)p;
			Move takeOverMove = k.getAvailableMoveForPosition(Position.createPosition(8, Position.E), board);
			if(takeOverMove != null)
				Assert.assertTrue("Knight should not be able to make this move: " + takeOverMove, !takeOverMove.canBeMade(board));
		}
		else
			Assert.fail("Piece under test should be a king was: " + p);
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
		
		ChessState checkState = board.getCurrentState();
		Assert.assertTrue("Game state", checkState == ChessState.CHECK);
		
		King k = King.class.cast(board.getPiece(Position.createPosition(2, Position.E)));
		Move takeQueenMove = k.getAvailableMoveForPosition(Position.createPosition(3, Position.D), board);
		k.performMove(takeQueenMove, board, false);
		
		ChessState playingState = board.getCurrentState();
		Assert.assertTrue("Game state", playingState == ChessState.PLAYING);
		
	}
}