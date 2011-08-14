package com.jjonsson.chess.gui.components;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.scenarios.TestScenarios.loadBoard;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;

import javax.swing.JMenuItem;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.WindowUtilities;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.pieces.WhitePawn;
import com.jjonsson.chess.scenarios.TestScenarios;

public class TestChessWindow
{
	static
	{
		WindowUtilities.setNativeLookAndFeel();
	}

	private static final JMenuItem FAKE_MENU_ITEM = new JMenuItem();

	private void undoOneMove(final ChessWindow window)
	{
		//Undo the move
		ActionEvent undoEvent = new ActionEvent(FAKE_MENU_ITEM, ActionEvent.ACTION_PERFORMED, ChessWindow.UNDO_BLACK_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(undoEvent);
	}

	private void disableAI(final ChessWindow window)
	{
		//Disable AI
		ActionEvent disableAIEvent = new ActionEvent(FAKE_MENU_ITEM, ActionEvent.ACTION_PERFORMED, ChessWindow.DISABLE_AI_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(disableAIEvent);
	}

	private void showClicks(final ChessWindow window)
	{
		ActionEvent showClicksEvent = new ActionEvent(FAKE_MENU_ITEM, ActionEvent.ACTION_PERFORMED, ChessWindow.SHOW_AVAILABLE_CLICKS_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(showClicksEvent);
	}

	private void newGame(final ChessWindow window)
	{
		ActionEvent newGameEvent = new ActionEvent(FAKE_MENU_ITEM, ActionEvent.ACTION_PERFORMED, ChessWindow.NEW_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(newGameEvent);
	}

	private void exit(final ChessWindow window)
	{
		ActionEvent exitEvent = new ActionEvent(FAKE_MENU_ITEM, ActionEvent.ACTION_PERFORMED, ChessWindow.EXIT_MENU_ITEM, System.nanoTime(), 0);
		window.actionPerformed(exitEvent);
	}

	/**
	 * Tests that it's possible to undo moves
	 * @throws InvalidPosition
	 * @throws InterruptedException
	 */
	@Test
	public void testUndoMove() throws InvalidPosition
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);

		window.displayGame();
		window.setTitle("Testing undo of a move");
		disableAI(window);
		showClicks(window);

		ChessBoardComponent component = window.getBoardComponent();
		ImmutablePosition toPosition = position("4E");
		ImmutablePosition fromPosition = position("2E");
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
	public void testInterruptingAI()
	{
		ChessBoard board = loadBoard("bishop_should_move_rational");
		//Make sure we think a long time :)
		board.setDifficulty(5);
		ChessWindow window = new ChessWindow(board);
		ChessBoardComponent component = window.getBoardComponent();

		window.displayGame();
		window.setTitle("Testing interruption of an AI move");

		//Triggers the AI
		component.loadingOfBoardDone();

		//Verify that the AI is thinking
		assertTrue(component.isWorking());

		//Interrupt the AI
		disableAI(window);

		//The AI thread should have been aborted
		assertFalse(component.isWorking());
	}


	@Test
	public void testRevertOfPawnReplacementMove()
	{
		ChessBoard board = TestScenarios.loadBoard("next_pawn_time_for_replacement_move_should_check_king_horse_take_queen_then_no_more_check");
		ChessWindow window = new ChessWindow(board);

		window.displayGame();
		window.setTitle("Testing undo of a pawn replacement move");
		disableAI(window);
		showClicks(window);

		ChessBoardComponent component = window.getBoardComponent();
		ImmutablePosition pawnPosition = position("7A");
		ImmutablePosition rockPosition = position("8B");
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
	public void testNewGame()
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);

		window.displayGame();
		window.setTitle("Testing that new game resets the board");
		disableAI(window);

		ChessBoardComponent component = window.getBoardComponent();
		ImmutablePosition fromPosition = position("2A");
		ImmutablePosition toPosition = position("4A");
		component.positionClicked(fromPosition);
		TestChessBoardComponent.sleep();
		component.positionClicked(toPosition);

		assertNotNull(board.getPiece(toPosition));
		newGame(window);
		assertNull(board.getPiece(toPosition));
	}

	/**
	 * Doesn't really test anything more than that the resize code doesn't throw anything that it shouldn't
	 * (Useful for testing resize events)
	 */
	@Test
	public void testWindowResize()
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);

		window.displayGame();
		window.setTitle("Testing resize of window");


		window.getComponentListeners()[0].componentResized(new WindowEvent(window, WindowEvent.WINDOW_STATE_CHANGED));
		assertTrue(window.isEnabled());
	}

}
