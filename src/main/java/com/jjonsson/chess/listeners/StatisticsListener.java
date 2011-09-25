package com.jjonsson.chess.listeners;

import com.jjonsson.chess.evaluators.statistics.StatisticsSnapshot;

public interface StatisticsListener
{
	public void newStatistics(StatisticsSnapshot snapshot);

	public void wasInterrupted(InterruptedException ie);

	public long notificationIntervalInNanos();
}
