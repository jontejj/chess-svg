package com.jjonsson.chess.gui.components;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.ChessBoardEvaluator;
import com.jjonsson.chess.ChessMoveEvaluator;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.pieces.Piece;

public class TestChessBoardComponent
{

	private static final int SLEEP_TIME = 2000;
	
	private static long BENCHMARKED_PLAY_TIME;
	private static long BENCHMARKED_PLAY_TIME_SECONDS;
	
	//We can turn this down as the AI gets more efficient
	private static long BENCHMARKING_FACTOR = 1200;
	
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
		
		BENCHMARKED_PLAY_TIME = (endNanos - startNanos) * BENCHMARKING_FACTOR;
		BENCHMARKED_PLAY_TIME_SECONDS = BENCHMARKED_PLAY_TIME / 1000000000;
	}
	
	
	@Test
	public void testPerformHintedMove() throws InterruptedException
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);
		window.setTitle("Testing and performing a hinted move");
		window.displayGame();
		
		ChessBoardComponent component = window.getBoardComponent();
		component.showHint();
		//Show hint should select a piece to move
		assertNotNull(component.getSelectedPiece());
		//TODO(jontejj) validate that it was a good move
		sleep();
		component.positionClicked(component.getHintMove().getPositionIfPerformed());
		sleep();
	}
	
	/**
	 * Simulating a match between a white random player and a black AI player
	 * Note that on a MacBook Pro with a core i7 the AI usually wins within 60 seconds
	 * @throws NoMovesAvailableException
	 * @throws InterruptedException
	 */
	@Test
	public void testSimulateMatchBetweenRandomAndAI() throws NoMovesAvailableException, InterruptedException
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);
		
		window.displayGame();
		window.setTitle("Expecting black to win within " + BENCHMARKED_PLAY_TIME_SECONDS + " secs");
		
		ChessBoardComponent component = window.getBoardComponent();
		component.setAIEnabled(false);
		
		long startNanos = System.nanoTime();
		while(ChessBoardEvaluator.inPlay(board) && System.nanoTime() < startNanos + BENCHMARKED_PLAY_TIME)
		{
			if(board.getCurrentPlayer() == Piece.BLACK)
			{
				ChessMoveEvaluator.performBestMove(board);
			}
			else
				//Simulate that the white is a bad player that doesn't know what he's doing
				board.performRandomMove();
			long consumedSeconds = (System.nanoTime() - startNanos) / 1000000000;
			window.setTitle("Expecting black to win within " + (BENCHMARKED_PLAY_TIME_SECONDS - consumedSeconds) + " secs");
		}
		//The AI should win (i.e white (random) should lose)
		assertEquals(ChessState.CHECKMATE, board.getCurrentState());
		assertEquals(Piece.WHITE, board.getCurrentPlayer());
		sleep();
	}
	
	public static void sleep() throws InterruptedException
	{
		Thread.sleep(SLEEP_TIME);
	}
}
