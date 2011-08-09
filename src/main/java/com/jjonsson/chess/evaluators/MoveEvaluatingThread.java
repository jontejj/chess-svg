package com.jjonsson.chess.evaluators;

import static com.jjonsson.utilities.Logger.LOGGER;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.CountDownLatch;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.utilities.Logger;

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
	}

	@Override
	public void run()
	{
		ChessMoveEvaluator.evaluateMove(myMoveToEvaluate, myBoard, myLimiter, myResult, myMovesLeftOnBranch);
		freeResources();
	}

	private void freeResources()
	{
		ResourceAllocator.freeThread();
		mySignal.countDown();
	}

	public static volatile long threadsCreated = 0;
	/**
	 * This may run in the caller thread instead of running in a new thread if it's more efficient not to allocate a new thread (
	 * which involves creating a clone of the chessboard etc.)
	 * TODO: find a better solution for when to create new threads (too many are created now)
	 */
	public void advancedRun()
	{
		boolean shouldContinueInNewThread = ChessMoveEvaluator.shouldContinueInNewThread(myBoard, myLimiter, myMovesLeftOnBranch, myMoveToEvaluate);
		if(shouldContinueInNewThread && ResourceAllocator.claimThread())
		{
			//Only copy the board/limiter if we aren't running in the "main" thread
			if(makeThreadSafe())
			{
				threadsCreated++;
				myThread = new Thread(this);
				myThread.setUncaughtExceptionHandler(this);
				myThread.start();
			}
			else
			{
				ResourceAllocator.freeThread();
				runInCurrentThread();
			}
		}
		else
		{
			runInCurrentThread();
		}
	}

	private void runInCurrentThread()
	{
		//TODO: this seems to fix a concurrency bug which it shouldn't have to (:
		if(myLimiter.getCurrentDepth() == 1)
		{
			if(!makeThreadSafe())
			{
				//This means that we can't continue and thus will the moves down this path not be evaluated (BAD)
				mySignal.countDown();
				LOGGER.warning("Failed to evaluate some moves due to a bug in the chessboard cloning process");
				return;
			}
		}
		ChessMoveEvaluator.evaluateMove(myMoveToEvaluate, myBoard, myLimiter, myResult, myMovesLeftOnBranch);
		mySignal.countDown();
	}

	/**
	 * 
	 * @return true if it was possible to make the board thread safe
	 */
	private boolean makeThreadSafe()
	{
		try
		{
			myBoard = myBoard.clone();
			myLimiter = myLimiter.copy();
			myMoveToEvaluate = myBoard.getMove(myMoveToEvaluate);
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnsupportedOperationException("Cloning of chessboard not possible", e);
		}
		return myMoveToEvaluate != null && myBoard != null;
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
		LOGGER.warning("Uncaught exception received: " + e + ", for thread: " + t);
		LOGGER.info("Exception trace: " + Logger.stackTraceToString(e));
		//Let's handle crashes gracefully
		freeResources();
	}
}
