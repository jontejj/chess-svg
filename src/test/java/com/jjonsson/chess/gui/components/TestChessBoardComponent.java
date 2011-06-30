package com.jjonsson.chess.gui.components;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.pieces.Piece;

public class TestChessBoardComponent
{

	private static final int SLEEP_TIME = 2000;
	
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
		benchmarkedPlaytimeInSeconds = benchmarkedPlaytime / 1000000000;
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
		component.positionClicked(component.getHintMove().getDestination());
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
		window.setTitle("Expecting black to win within " + benchmarkedPlaytimeInSeconds + " secs");
		
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
			}
			long consumedSeconds = (System.nanoTime() - startNanos) / 1000000000;
			window.setTitle("Expecting black to win within " + (benchmarkedPlaytimeInSeconds - consumedSeconds) + " secs");
		}
		//If the game ended in time the AI should win (i.e white (random) should lose)
		assertTrue(ChessBoardEvaluator.inPlay(board) || (ChessState.CHECKMATE == board.getCurrentState() && board.getLastMove().getAffinity() == Piece.BLACK));
		//Black should have more pieces in all cases
		//TODO(jontejj) the takeOverPiecesCount seems to be off needs more testing
		//assertTrue(board.getMeasuredStatusForPlayer(Piece.BLACK) >= board.getMeasuredStatusForPlayer(Piece.WHITE));
		sleep();
	}
	
	public static void sleep() throws InterruptedException
	{
		Thread.sleep(SLEEP_TIME);
	}
}
