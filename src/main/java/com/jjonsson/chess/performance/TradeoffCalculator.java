package com.jjonsson.chess.performance;

import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static com.jjonsson.chess.pieces.Piece.WHITE;
import static com.jjonsson.utilities.Loggers.STDOUT;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.board.PiecePlacement;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.evaluators.SearchLimiter;
import com.jjonsson.chess.evaluators.statistics.StatisticsAction;
import com.jjonsson.chess.evaluators.statistics.StatisticsSnapshot;
import com.jjonsson.chess.gui.StatisticsWindow;
import com.jjonsson.chess.listeners.StatisticsListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.persistence.PersistanceLogging;

public final class TradeoffCalculator
{
	private TradeoffCalculator(){}

	private static final int BENCHMARK_AMOUNT = 100000;
	public static void main(final String[] args)
	{
		StatisticsListener listener = new StatisticsWindow();

		ChessBoard board = new ChessBoard(PiecePlacement.PLACE_PIECES);
		board.setStatisticsListener(listener);

		long startTime = System.nanoTime();
		for(int i = BENCHMARK_AMOUNT; i >0; i--)
		{
			board.copy(PersistanceLogging.SKIP_PERSISTANCE_LOGGING);
		}
		long nanosPerClone = (System.nanoTime() - startTime) / BENCHMARK_AMOUNT;
		double duration = (double)(nanosPerClone * BENCHMARK_AMOUNT) / SECONDS.toNanos(1);
		STDOUT.warn("cloning took " + duration + " secs, nanos per clone: " + nanosPerClone);

		Move move = board.getAvailableMove(position("4E"), WHITE);
		SearchLimiter limiter = new SearchLimiter(0);

		board.performStatisticsAction(StatisticsAction.RESET);
		startTime = System.nanoTime();
		for(int i = BENCHMARK_AMOUNT; i >0; i--)
		{
			ChessMoveEvaluator.performMoveWithMeasurements(move, board, limiter);
			board.undoMove(move, false);
			board.performStatisticsAction(StatisticsAction.MOVE_EVALUATED);
			//ChessMoveEvaluator.evaluateMove(move, board, limiter, new SearchResult(), 0);
		}
		board.performStatisticsAction(StatisticsAction.MOVE_EVALUATION_STOPPED);

		StatisticsSnapshot snapshot = board.getStatisticsTracker().createSnapshotForCurrentStatistics();
		long nanosPerMove = snapshot.getAverageNanosPerMove();
		duration = snapshot.getTotalTimeInSeconds();

		STDOUT.warn("eval took " + duration + " secs, nanos per move eval: " + nanosPerMove);

		long moveEvalsPerCloning = nanosPerClone / nanosPerMove;
		STDOUT.warn("Move evals per cloning: " +  moveEvalsPerCloning);
		board.performStatisticsAction(StatisticsAction.INTERRUPT_TRACKING);
	}
}
