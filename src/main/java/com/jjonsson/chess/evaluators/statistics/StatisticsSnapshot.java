package com.jjonsson.chess.evaluators.statistics;

import static java.util.concurrent.TimeUnit.SECONDS;

public class StatisticsSnapshot
{
	private long myMovesMade;
	private long myStartTime;
	private long myEndTime;

	public StatisticsSnapshot(final StatisticsTracker statisticsTracker)
	{
		myMovesMade = statisticsTracker.myMovesMade;
		myStartTime = statisticsTracker.myStartTime;
		myEndTime = statisticsTracker.myTemporaryEndTime;
	}

	public long getMovesMade()
	{
		return myMovesMade;
	}

	public long getAverageNanosPerMove()
	{
		return (myEndTime - myStartTime) / Math.max(1, myMovesMade);
	}

	public long getMovesEvaluatedPerSecond()
	{
		if(myEndTime == myStartTime || myMovesMade == 0)
		{
			//This is the best value we can give for such fast runs
			return myMovesMade;
		}
		return SECONDS.toNanos(1) / getAverageNanosPerMove();
	}

	public double getTotalTimeInSeconds()
	{
		return (double)(getAverageNanosPerMove() * getMovesMade()) / SECONDS.toNanos(1);
	}
}
