package com.jjonsson.chess.evaluators;

import static com.jjonsson.chess.gui.Settings.DEBUG;
import static com.jjonsson.chess.persistence.PersistanceLogging.SKIP_PERSISTANCE_LOGGING;
import static com.jjonsson.chess.persistence.PersistanceLogging.USE_PERSISTANCE_LOGGING;
import static com.jjonsson.utilities.Loggers.STDERR;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.CountDownLatch;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.SearchInterruptedError;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.persistence.BoardLoader;

public class MoveEvaluatingThread implements Runnable, UncaughtExceptionHandler
{
	private ChessBoard myBoard;
	private Move myMoveToEvaluate;
	private SearchResult myResult;
	private SearchLimiter	myLimiter;
	private long	myMovesLeftOnBranch;
	private CountDownLatch mySignal;
	private Thread myThread;

	/**
	 * Note that this constructor takes care of the copying of resources as it deems necessary
	 * @param board
	 * @param moveToEvaluate
	 * @param limiter
	 * @param result
	 * @param movesLeftOnBranch
	 * @param workersDoneSignal
	 */
	public MoveEvaluatingThread(final ChessBoard board, final Move moveToEvaluate, final SearchLimiter limiter, final SearchResult result, final long movesLeftOnBranch, final CountDownLatch workersDoneSignal)
	{
		super();
		myBoard = board;
		myLimiter = limiter;
		myMoveToEvaluate = moveToEvaluate;
		//All move evaluators on the same branch share the same result
		myResult = result;
		myMovesLeftOnBranch = movesLeftOnBranch;
		mySignal = workersDoneSignal;
		boolean shouldContinueInNewThread = ChessMoveEvaluator.shouldContinueInNewThread(myBoard, myLimiter, myMovesLeftOnBranch, myMoveToEvaluate);
		if(shouldContinueInNewThread && ResourceAllocator.claimThread())
		{
			//Only copy the board/limiter if we aren't running the next move eval in the same thread
			if(makeThreadSafe())
			{
				myThread = new Thread(this, MoveEvaluatingThread.class.getName());
				myThread.setUncaughtExceptionHandler(this);
			}
			else
			{
				ResourceAllocator.freeThread();
			}
		}
	}

	@Override
	public void run()
	{
		try
		{
			ChessMoveEvaluator.evaluateMove(myMoveToEvaluate, myBoard, myLimiter, myResult, myMovesLeftOnBranch);
		}
		catch(SearchInterruptedError error)
		{
			//Nothing to do here
		}
		freeResources();
	}

	private void freeResources()
	{
		ResourceAllocator.freeThread();
		mySignal.countDown();
	}
	/**
	 * This may run in the caller thread instead of running in a new thread if it's more efficient not to allocate a new thread (
	 * which involves creating a clone of the chessboard etc.)
	 * TODO: find a better solution for when to create new threads (too many are created now)
	 */
	public void advancedRun()
	{
		if(myThread != null)
		{
			myThread.start();
		}
		else
		{
			runInCurrentThread();
		}
	}

	private void runInCurrentThread()
	{
		//TODO: this seems to fix a concurrency bug which it shouldn't have to (:
		if(myLimiter.getCurrentDepth() == 1 && !makeThreadSafe())
		{
			//This means that we can't continue and thus will the moves down this path not be evaluated (BAD)
			mySignal.countDown();
			STDERR.fatal("Failed to evaluate some moves due to a bug in the chessboard cloning process");
			return;
		}
		ChessMoveEvaluator.evaluateMove(myMoveToEvaluate, myBoard, myLimiter, myResult, myMovesLeftOnBranch);
		mySignal.countDown();
	}

	/**
	 * TODO: this should always be possible
	 * @return true if it was possible to make the board thread safe
	 */
	private boolean makeThreadSafe()
	{
		ChessBoard copy = myBoard.copy(DEBUG ? USE_PERSISTANCE_LOGGING : SKIP_PERSISTANCE_LOGGING);
		if(copy == null)
		{
			STDERR.fatal("Failed to clone board.");
			BoardLoader.saveBoard(myBoard, "faulty_boards/temp_board_causing_clone_failure");
			return false;
		}
		Move move = copy.getMove(myMoveToEvaluate);
		if(move == null)
		{
			STDERR.fatal("Failed to find move for: " + myMoveToEvaluate);
			return false;
		}
		myLimiter = myLimiter.copy();
		myBoard = copy;
		myMoveToEvaluate = move;
		return true;
	}

	boolean isRunningInSeperateThread()
	{
		return myThread != null;
	}

	Thread getThread()
	{
		return myThread;
	}

	@Override
	public void uncaughtException(final Thread t, final Throwable e)
	{
		STDERR.fatal("Uncaught exception received: " + e + ", for thread: " + t, e);
		//Let's handle crashes gracefully
		freeResources();
	}
}
