package com.jjonsson.chess.gui.components;

import static com.jjonsson.chess.gui.ChessWindow.ACTIONS_MENU_NAME;
import static com.jjonsson.chess.gui.ChessWindow.SHOW_STATISTICS_MENU_ITEM;
import static com.jjonsson.chess.moves.ImmutablePosition.from;
import static java.util.concurrent.TimeUnit.SECONDS;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import java.awt.Point;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.DisplayOption;
import com.jjonsson.chess.gui.Settings;
import com.jjonsson.chess.gui.WindowUtilities;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Position.Column;
import com.jjonsson.chess.pieces.Piece;
public class TestChessBoardComponent
{
	private static final String LAF_PROPERTY = "swing.systemlaf";
	static
	{
		//Exercises the exception handling
		String oldLaf = System.getProperty(LAF_PROPERTY);
		System.setProperty(LAF_PROPERTY, "classThatDoesNotExistTest");
		WindowUtilities.setNativeLookAndFeel();
		if(oldLaf != null)
		{
			System.setProperty(LAF_PROPERTY, oldLaf);
		}
		else
		{
			System.clearProperty(LAF_PROPERTY);
		}
		//Sets it for real
		WindowUtilities.setNativeLookAndFeel();
	}

	/**
	 * Represents 2 seconds
	 */
	private static final long SLEEP_TIME = SECONDS.toMillis(2);

	private static long benchmarkedPlaytime;
	private static long benchmarkedPlaytimeInSeconds;

	//We can turn this down as the AI gets more efficient
	private static final long BENCHMARKING_FACTOR = 1200;

	@BeforeClass
	public static void bencmarkComputer()
	{
		long startNanos = System.nanoTime();

		//Just do some random computations and measure the time it takes to perform them
		long junkValue = 0;
		long junkValue2 = 132;
		for(long i = 0;i<10000000;i++)
		{
			junkValue += junkValue2 * 3.4;
			junkValue2 = junkValue + 673232;
		}

		long endNanos = System.nanoTime();

		benchmarkedPlaytime = (endNanos - startNanos) * BENCHMARKING_FACTOR;
		benchmarkedPlaytimeInSeconds = benchmarkedPlaytime / SECONDS.toNanos(1);
	}


	@Test
	public void testPerformHintedMove()
	{
		ChessBoard board = new ChessBoard();
		ChessWindow window = new ChessWindow(board, DisplayOption.DISPLAY);
		window.setTitle("Testing and performing a hinted move");

		ChessBoardComponent component = window.getBoardComponent();
		component.showHint();
		//Show hint should select a piece to move
		assertNotNull(component.getSelectedPiece());
		//TODO(jontejj) validate that it was a good move
		sleep();
		component.positionClicked(component.getHintMove().getDestination());
		sleep();
		window.dispose();
	}

	/**
	 * Simulating a match between a white random player and a black AI player
	 * Note that on a MacBook Pro with a core i7 the AI usually wins within 60 seconds
	 * As I've discovered that this is a good stability test this serves several purposes :)
	 * @throws NoMovesAvailableException
	 * @throws InterruptedException
	 */
	@Test
	public void testSimulateMatchBetweenRandomAndAI() throws NoMovesAvailableException
	{
		//TODO: revert changes in this function
		//while(true)
		{
			ChessBoard board = new ChessBoard();
			ChessWindow window = new ChessWindow(board, DisplayOption.DISPLAY);

			window.setTitle("Expecting black to win within " + benchmarkedPlaytimeInSeconds + " secs");

			TestChessWindow.clickMenuItem(window, ACTIONS_MENU_NAME, SHOW_STATISTICS_MENU_ITEM);

			ChessBoardComponent component = window.getBoardComponent();
			component.setAIEnabled(false);

			long startNanos = System.nanoTime();
			while(ChessBoardEvaluator.inPlay(board) && System.nanoTime() < startNanos + benchmarkedPlaytime)
			{
				if(board.getCurrentPlayer() == Piece.BLACK)
				{
					ChessMoveEvaluator.performBestMove(board);
				}
				else
				{
					//Simulate that the white is a bad player that doesn't know what he's doing
					board.performRandomMove();
					//ChessMoveEvaluator.performBestMove(board);
				}
				long consumedSeconds = (System.nanoTime() - startNanos) / SECONDS.toNanos(1);
				window.setTitle("Expecting black to win within " + (benchmarkedPlaytimeInSeconds - consumedSeconds) + " secs");
			}
			//If the game ended in time the AI should win (i.e white (random) should lose)
			//assertTrue(ChessBoardEvaluator.inPlay(board) || (ChessState.CHECKMATE == board.getCurrentState() && board.getLastMove().getAffinity() == Piece.BLACK));
			//Black should have more pieces in all cases
			//TODO(jontejj) the takeOverPiecesCount seems to be off needs more testing
			//assertTrue(board.getMeasuredStatusForPlayer(Piece.BLACK) >= board.getMeasuredStatusForPlayer(Piece.WHITE));
			sleep();
			window.dispose();
		}
	}

	@Test
	public void testConversionBetweenPositionAndPoint() throws InvalidPosition
	{
		ChessBoardComponent component = new ChessWindow(new ChessBoard(), DisplayOption.DONT_DISPLAY).getBoardComponent();

		for(int r = 0; r < ChessBoard.BOARD_SIZE; r++)
		{
			for(Column c : Column.values())
			{
				ImmutablePosition pos = from(r, c.getValue());
				Point point = component.getPointForPosition(pos);

				assertEquals(pos, component.getPositionForPoint(point));
			}
		}
	}

	/**
	 * Sleeps {@link TestChessBoardComponent#SLEEP_TIME} seconds to show the user what the test cases are doing, sleeps only if {@link Settings#DEBUG} is true
	 */
	public static void sleep()
	{
		try
		{
			if(Settings.DEBUG)
			{
				Thread.sleep(SLEEP_TIME);
			}
		}
		catch(InterruptedException ie)
		{
			throw new IllegalStateException("Got interrupted during my sleep", ie);
		}
	}
}
