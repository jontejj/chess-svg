package com.jjonsson.chess.performance;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.utilities.Logger.LOGGER;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.evaluators.ProgressTracker;
import com.jjonsson.chess.evaluators.SearchLimiter;
import com.jjonsson.chess.listeners.StatusListener;
import com.jjonsson.chess.moves.Move;

public final class TradeoffCalculator
{
	private TradeoffCalculator(){}

	private static final int BENCHMARK_AMOUNT = 100000;
	public static void main(final String[] args)
	{
		StatusListener listener = new StatusListener(){
			@Override public void statusHasBeenUpdated(){}
			@Override public void setResultOfInteraction(final String resultText){}
			@Override public void setProgressInformation(final String progressText){}
		};
		ProgressTracker.setStatusListener(listener);

		ChessBoard board = new ChessBoard(true);

		long startTime = System.nanoTime();
		for(int i = BENCHMARK_AMOUNT; i >0; i--)
		{
			board.copy();
		}
		long nanosPerClone = (System.nanoTime() - startTime) / BENCHMARK_AMOUNT;
		double duration = (double)(nanosPerClone * BENCHMARK_AMOUNT) / SECONDS.toNanos(1);
		LOGGER.warning("cloning took " + duration + " secs, nanos per clone: " + nanosPerClone);

		Move move = board.getAvailableMove(position("4E"), WHITE);
		SearchLimiter limiter = new SearchLimiter(0);

		startTime = System.nanoTime();
		for(int i = BENCHMARK_AMOUNT; i >0; i--)
		{
			ChessMoveEvaluator.performMoveWithMeasurements(move, board, limiter);
			board.undoMove(move, false);
			ProgressTracker.moveHasBeenMade();
			//ChessMoveEvaluator.evaluateMove(move, board, limiter, new SearchResult(), 0);
		}
		ProgressTracker.done();
		long nanosPerMove = (System.nanoTime() - startTime) / BENCHMARK_AMOUNT;
		duration = (double)(nanosPerMove * BENCHMARK_AMOUNT) / SECONDS.toNanos(1);

		LOGGER.warning("eval took " + duration + " secs, nanos per move eval: " + nanosPerMove);

		long moveEvalsPerCloning = nanosPerClone / nanosPerMove;
		LOGGER.warning("Move evals per cloning: " +  moveEvalsPerCloning);
	}
}
