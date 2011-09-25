package com.jjonsson.utilities;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * A class that holds some jobs and that can interrupt them at your discretion
 * @author jonatanjoensson
 *
 */
public class ThreadTracker
{

	private Map<Long, Thread> myCurrentJobs;

	public ThreadTracker()
	{
		myCurrentJobs = Maps.newHashMap();
	}

	public synchronized void addJob(final Thread job)
	{
		myCurrentJobs.put(Long.valueOf(job.getId()), job);
	}

	public synchronized void removeJob(final Thread job)
	{
		myCurrentJobs.remove(Long.valueOf(job.getId()));
	}

	public synchronized void interruptCurrentJobs()
	{
		for(Thread job : myCurrentJobs.values())
		{
			job.interrupt();
		}
		myCurrentJobs.clear();
	}

	public boolean isWorking()
	{
		return myCurrentJobs.size() > 0;
	}

	public void joinAllJobs() throws InterruptedException
	{
		for(Thread t : myCurrentJobs.values())
		{
			t.join();
		}
	}
}
