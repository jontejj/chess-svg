package com.jjonsson.chess.scenarios;

import java.io.File;

import org.junit.Test;

import junit.framework.Assert;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.persistance.BoardLoader;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.WhitePawn;

public class TestScenarios
{
	
	private ChessBoard loadBoard(String testName)
	{
		String scenarioFile = "/scenarios/" + testName + ".chess";
		ChessBoard board = new ChessBoard();
		if(!BoardLoader.loadFileIntoBoard(scenarioFile, board))
		{
			Assert.fail("Could not load:" + scenarioFile);
		}
		return board;
	}
	@Test
	public void testMoveAvailabilityUnderFutureCheck() throws UnavailableMoveException
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
			Assert.assertTrue("Game state should be " + ChessState.PLAYING + ", was: " + currentState, currentState == ChessState.PLAYING);
			q.performMove(takeOverMove, board);
			currentState = board.getCurrentState();
			Assert.assertTrue("Game state should be " + ChessState.PLAYING + ", was: " + currentState, currentState == ChessState.PLAYING);
			Assert.assertTrue("When move has been made the current position for the move should have changed", !oldQueenPosition.equals(takeOverMove.getCurrentPosition()));
			Piece pieceAtNewLocation = board.getPiece(takeOverMove.getCurrentPosition());
			Assert.assertEquals("The board's piece at: " + takeOverMove.getCurrentPosition() + "was not right" , pieceAtNewLocation, q);
		}
		else
			Assert.fail("Piece under test should be a queen was: " + p);
	}

	@Test
	public void testStaleMate()
	{
		ChessBoard board = loadBoard("should_be_stalemate");
		ChessState currentState = board.getCurrentState();
		Assert.assertTrue("Game state should be " + ChessState.STALEMATE + ", was: " + currentState, currentState == ChessState.STALEMATE);
	}
	
	/**
	 * Tests if available moves is removed when a piece is taken and that a pawn is replaced when it reaches it's destination
	 * @throws UnavailableMoveException 
	 * @throws NoMovesAvailableException 
	 */
	@Test 
	public void testPawnReplacementAndGameStateChanges() throws UnavailableMoveException, NoMovesAvailableException
	{
		ChessBoard board = loadBoard("next_pawn_time_for_replacement_move_should_check_king_horse_take_queen_then_no_more_check");		
		Piece p = board.getPiece(Position.createPosition(7, Position.A));
		if(p instanceof WhitePawn)
		{
			WhitePawn pawn = (WhitePawn)p;
			Move replacementMove = pawn.getAvailableMoveForPosition(Position.createPosition(8, Position.B), board);
			Assert.assertNotNull(replacementMove);
			//Takes over the rock, replacing the pawn with a queen, checking the black king
			pawn.performMove(replacementMove, board);
			
			ChessState currentState = board.getCurrentState();
			Assert.assertTrue("Game state should be " + ChessState.CHECK + ", was: " + currentState, currentState == ChessState.CHECK);
			
			Piece defendingKnight = board.getPiece(Position.createPosition(6, Position.C));
			Assert.assertNotNull(defendingKnight);
			Move defendingMove = defendingKnight.getAvailableMoveForPosition(Position.createPosition(8, Position.B), board);
			Assert.assertNotNull(defendingMove);
			defendingKnight.performMove(defendingMove, board);
			currentState = board.getCurrentState();
			Assert.assertTrue("Game state should be " + ChessState.PLAYING + ", was: " + currentState, currentState == ChessState.PLAYING);
			board.performRandomMove();
			currentState = board.getCurrentState();
			Assert.assertTrue("Game state should be " + ChessState.PLAYING + ", was: " + currentState, currentState == ChessState.PLAYING);
		}
		else
			Assert.fail("Piece under test should be a white pawn was: " + p);
	}
	
	@Test
	public void testNotCheck()
	{
		ChessBoard board = loadBoard("should_not_be_check");
		ChessState currentState = board.getCurrentState();
		Assert.assertTrue("Game state should be " + ChessState.PLAYING + ", was: " + currentState, currentState == ChessState.PLAYING);
	}
	
	
	@Test
	public void testKingPossibleMovesUnderCheck() throws UnavailableMoveException
	{
		ChessBoard board = loadBoard("queen_left_move_check_king_not_possible_to_move_down");		
		Piece p = board.getPiece(Position.createPosition(4, Position.H));
		if(p instanceof Queen)
		{
			Queen q = (Queen)p;
			Move takeOverMove = q.getAvailableMoveForPosition(Position.createPosition(4, Position.E), board);
			Assert.assertNotNull(takeOverMove);
			q.performMove(takeOverMove, board);

			ChessState currentState = board.getCurrentState();
			Assert.assertTrue("Game state should be " + ChessState.CHECK + ", was: " + currentState, currentState == ChessState.CHECK);
			Piece p2 = board.getPiece(Position.createPosition(2, Position.E));
			if(p2 instanceof King)
			{
				King k = (King)p2;
				Move downMove = k.getAvailableMoveForPosition(Position.createPosition(1, Position.E), board);
				Assert.assertNull("Queen should be threatening this position: " + downMove, downMove);
			}
			else
				Assert.fail("Piece under test should be a king was: " + p);
		}
		else
			Assert.fail("Piece under test should be a queen was: " + p);
	}
	
	@Test
	public void testTakeOverMoveShouldNotBePossible()
	{
		ChessBoard board = loadBoard("knight_should_not_be_possible_to_take");		
		Piece p = board.getPiece(Position.createPosition(8, Position.F));
		if(p instanceof King)
		{
			King k = (King)p;
			Move takeOverMove = k.getAvailableMoveForPosition(Position.createPosition(8, Position.E), board);
			Assert.assertNull(takeOverMove);
		}
		else
			Assert.fail("Piece under test should be a king was: " + p);
	}
}