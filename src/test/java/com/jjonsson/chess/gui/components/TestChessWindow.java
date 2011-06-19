package com.jjonsson.chess.gui.components;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

import java.awt.event.ActionEvent;

import javax.swing.JMenuItem;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.pieces.WhitePawn;
import com.jjonsson.chess.scenarios.TestScenarios;

public class TestChessWindow
{
	static JMenuItem fakeMenuItem;
	
	@BeforeClass
	public static void setup()
	{
		fakeMenuItem = new JMenuItem();
	}
	
	private void undoOneMove(ChessWindow window)
	{
		//Undo the move
		ActionEvent undoEvent = new ActionEvent(fakeMenuItem, ActionEvent.ACTION_PERFORMED, ChessWindow.UNDO_BLACK_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(undoEvent);
	}
	
	private void disableAI(ChessWindow window)
	{
		//Disable AI
		ActionEvent disableAIEvent = new ActionEvent(fakeMenuItem, ActionEvent.ACTION_PERFORMED, ChessWindow.DISABLE_AI_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(disableAIEvent);
	}
	
	private void showClicks(ChessWindow window)
	{
		ActionEvent showClicksEvent = new ActionEvent(fakeMenuItem, ActionEvent.ACTION_PERFORMED, ChessWindow.SHOW_AVAILABLE_CLICKS_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(showClicksEvent);
	}
	
	private void newGame(ChessWindow window)
	{
		ActionEvent newGameEvent = new ActionEvent(fakeMenuItem, ActionEvent.ACTION_PERFORMED, ChessWindow.NEW_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(newGameEvent);
	}
	
	private void exit(ChessWindow window)
	{
		ActionEvent exitEvent = new ActionEvent(fakeMenuItem, ActionEvent.ACTION_PERFORMED, ChessWindow.EXIT_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(exitEvent);
	}
	
	/**
	 * Tests that it's possible to undo moves
	 * @throws InvalidPosition
	 */
	@Test
	public void testUndoMove() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);
		
		window.displayGame();
		disableAI(window);
		showClicks(window);
		
		ChessBoardComponent component = window.getBoardComponent();
		Position toPosition = Position.createPosition(4, Position.E);
		Position fromPosition = Position.createPosition(2, Position.E);
		component.positionClicked(fromPosition);
		TestChessBoardComponent.sleep();
		component.positionClicked(toPosition);
		TestChessBoardComponent.sleep();
		
		//Make sure the move was made
		assertNotNull(board.getPiece(toPosition));
		assertNull(board.getPiece(fromPosition));
		
		undoOneMove(window);
		
		//Make sure the move was undone
		assertNull(board.getPiece(toPosition));
		assertNotNull(board.getPiece(fromPosition));
		
		exit(window);
	}
	
	
	@Test
	public void testRevertOfPawnReplacementMove() throws InvalidPosition
	{
		ChessBoard board = TestScenarios.loadBoard("next_pawn_time_for_replacement_move_should_check_king_horse_take_queen_then_no_more_check");
		ChessWindow window = new ChessWindow(board);
		
		window.displayGame();
		disableAI(window);
		showClicks(window);
		
		ChessBoardComponent component = window.getBoardComponent();
		Position pawnPosition = Position.createPosition(7, Position.A);
		Position rockPosition = Position.createPosition(8, Position.B);
		component.positionClicked(pawnPosition);
		TestChessBoardComponent.sleep();
		//Take over rock
		component.positionClicked(rockPosition);
		TestChessBoardComponent.sleep();
		
		//The pawn should have been moved and replaced with a queen
		assertNull(board.getPiece(pawnPosition));
		assertTrue(board.getPiece(rockPosition) instanceof Queen);
		
		undoOneMove(window);
		
		//The rock should have been restored
		assertTrue(board.getPiece(rockPosition) instanceof Rock);
		
		//The pawn should have been moved back
		assertTrue(board.getPiece(pawnPosition) instanceof WhitePawn);
		TestChessBoardComponent.sleep();
		
		exit(window);
	}
	
	@Test
	public void testNewGame() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);
		
		window.displayGame();
		disableAI(window);
		
		ChessBoardComponent component = window.getBoardComponent();
		Position fromPosition = Position.createPosition(2, Position.A);
		Position toPosition = Position.createPosition(4, Position.A);
		component.positionClicked(fromPosition);
		TestChessBoardComponent.sleep();
		component.positionClicked(toPosition);
		TestChessBoardComponent.sleep();
		
		assertNotNull(board.getPiece(toPosition));
		newGame(window);
		assertNull(board.getPiece(toPosition));
	}

}
