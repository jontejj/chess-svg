package com.jjonsson.chess.evaluators;

import static com.jjonsson.utilities.Logger.LOGGER;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.annotations.VisibleForTesting;
import com.jjonsson.chess.listeners.StatusListener;

public final class ProgressTracker
{
	private static long movesMade;
	private static long movesMadeAtLastSync;
	private static long myLastSync;
	private static StatusListener myStatusTracker;
	private static long startTime;

	private ProgressTracker()
	{

	}

	@VisibleForTesting
	public static synchronized void setStatusListener(final StatusListener listener)
	{
		myStatusTracker = listener;
		movesMade = 0;
		movesMadeAtLastSync = 0;
		startTime = System.nanoTime();
		myLastSync = System.nanoTime();
	}

	public static synchronized void moveHasBeenMade()
	{
		movesMade++;
		if(myStatusTracker != null && System.nanoTime() > myLastSync + SECONDS.toNanos(1))
		{
			long movesPerSecond = movesMade - movesMadeAtLastSync;
			myStatusTracker.setProgressInformation("Moves evaluated: " + movesMade + ", Moves/Second: " + movesPerSecond);
			movesMadeAtLastSync = movesMade;
			myLastSync = System.nanoTime();
		}
	}

	public static synchronized void done()
	{
		if(myStatusTracker != null)
		{
			double passedSeconds = ((double)(System.nanoTime() - startTime) / SECONDS.toNanos(1));
			long movesPerSecond;
			if(passedSeconds == 0.0)
			{
				movesPerSecond = movesMade;
			}
			else
			{
				movesPerSecond = (long) (movesMade / passedSeconds);
			}
			LOGGER.finest("Moves evaluated: " + movesMade + ", Moves/Second: " + movesPerSecond);
		}
	}
}
