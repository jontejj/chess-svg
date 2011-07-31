package com.jjonsson.chess.performance;

import static com.jjonsson.chess.moves.Position.E;
import static com.jjonsson.chess.moves.Position.createPosition;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.utilities.Logger.LOGGER;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.evaluators.ProgressTracker;
import com.jjonsson.chess.evaluators.SearchLimiter;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.gui.StatusListener;
import com.jjonsson.chess.moves.Move;

public class TradeoffCalculator
{
	private static int BENCHMARK_AMOUNT = 100000;
	public static void main(String[] args) throws CloneNotSupportedException, InvalidPosition
	{
		StatusListener listener = new StatusListener(){
			@Override public void statusHasBeenUpdated(){}
			@Override public void setResultOfInteraction(String resultText){}
			@Override public void setProgressInformation(String progressText){}
		};
		ProgressTracker.setStatusListener(listener);
		
		ChessBoard board = new ChessBoard(true);

		long startTime = System.nanoTime();
		long i2 = Integer.MAX_VALUE;
		for(int i = BENCHMARK_AMOUNT; i >0; i--)
		{
			board.clone();
			//for(int y = 0;y<10000;y++)
			//	i2 /= 1.00001;
		}
		System.out.println(i2);
		long nanosPerClone = (System.nanoTime() - startTime) / BENCHMARK_AMOUNT;
		double duration = (double)(nanosPerClone * BENCHMARK_AMOUNT) / 1000000000;
		LOGGER.warning("cloning took " + duration + " secs, nanos per clone: " + nanosPerClone);
		
		Move move = board.getAvailableMove(createPosition(4, E), WHITE);
		SearchLimiter limiter = new SearchLimiter(0);
		
		startTime = System.nanoTime();
		for(int i = BENCHMARK_AMOUNT; i >0; i--)
		{
			ChessMoveEvaluator.performMoveWithMeasurements(move, board, limiter);
			board.undoMove(move, false);
			//ChessMoveEvaluator.evaluateMove(move, board, limiter, new SearchResult(), 0);
		}
		ProgressTracker.done();
		long nanosPerMove = (System.nanoTime() - startTime) / BENCHMARK_AMOUNT;
		duration = (double)(nanosPerMove * BENCHMARK_AMOUNT) / 1000000000;
		
		LOGGER.warning("eval took " + duration + " secs, nanos per move eval: " + nanosPerMove);
		
		long moveEvalsPerCloning = nanosPerClone / nanosPerMove;
		LOGGER.warning("Move evals per cloning: " +  moveEvalsPerCloning);
	}
}
