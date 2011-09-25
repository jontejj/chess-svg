package com.jjonsson.chess.evaluators.statistics;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import com.jjonsson.chess.listeners.StatisticsListener;

/**
 * A tracker that notifies the {@link StatisticsListener} when statistics changes occur
 * @author jonatanjoensson
 *
 */
public class StatisticsTracker
{
	private StatisticsListener myListener;
	private long myLastNotificationNanoTime;
	private long myNotificationIntervaInNanos;
	private BlockingDeque<StatisticsAction> myWorkQueue;

	/**
	 * Measurements
	 */
	long myMovesMade;
	long myStartTime;

	long myTemporaryEndTime;

	public StatisticsTracker(final StatisticsListener listener)
	{
		myListener = listener;
		myNotificationIntervaInNanos = listener.notificationIntervalInNanos();

		//This makes the first action trigger a notification
		myLastNotificationNanoTime = -myNotificationIntervaInNanos;

		myWorkQueue = new LinkedBlockingDeque<StatisticsAction>();

		//Startup the daemon that listens for statistics changes
		new Thread(new QueueWorker(), getClass().getName()).start();
	}

	/**
	 * Performs the given {@link StatisticsAction} on this tracker
	 * @param action
	 */
	public void perform(final StatisticsAction action)
	{
		myWorkQueue.add(action);
	}

	public synchronized StatisticsSnapshot createSnapshotForCurrentStatistics()
	{
		//End time is used by the snapshot in order to figure out the speed etc.
		myTemporaryEndTime = System.nanoTime();
		return new StatisticsSnapshot(StatisticsTracker.this);
	}

	private final class QueueWorker implements Runnable
	{
		@Override public void run()
		{
			while(true)
			{
				try
				{
					StatisticsAction action = myWorkQueue.take();

					switch(action)
					{
						case RESET:
							myMovesMade = 0;
							myStartTime = System.nanoTime();
							break;
						case MOVE_EVALUATED:
							myMovesMade++;
							break;
						case MOVE_EVALUATION_STOPPED:
							//This Nothing to do here?
							break;
						case INTERRUPT_TRACKING:
							throw new InterruptedException();
					}
					if(timeToNotify(action))
					{
						myListener.newStatistics(createSnapshotForCurrentStatistics());
						myLastNotificationNanoTime = System.nanoTime();
					}
				}
				catch (InterruptedException e)
				{
					myListener.wasInterrupted(e);
					break;
				}

			}
		}
	}

	private boolean timeToNotify(final StatisticsAction action)
	{
		boolean enoughTimePassed = System.nanoTime() - myLastNotificationNanoTime > myNotificationIntervaInNanos;
		return enoughTimePassed || action != StatisticsAction.MOVE_EVALUATED;
	}
}
