package com.jjonsson.chess.evaluators;

import static com.jjonsson.utilities.Logger.LOGGER;
import static com.jjonsson.utilities.TimeConstants.ONE_SECOND_IN_NANOS;

import com.jjonsson.chess.gui.StatusListener;

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
	
	static synchronized void setStatusListener(StatusListener listener)
	{
		myStatusTracker = listener;
		movesMade = 0;
		movesMadeAtLastSync = 0;
		startTime = System.nanoTime();
	}
	
	static synchronized void moveHasBeenMade()
	{
		movesMade++;
		if(myStatusTracker != null && System.nanoTime() > myLastSync + ONE_SECOND_IN_NANOS)
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
			double passedSeconds = Math.max(((System.nanoTime() - startTime) / ONE_SECOND_IN_NANOS), 1.0);
			long movesPerSecond = (long) (movesMade / passedSeconds);
			LOGGER.info("Moves evaluated: " + movesMade + ", Moves/Second: " + movesPerSecond);
		}
	}
}
