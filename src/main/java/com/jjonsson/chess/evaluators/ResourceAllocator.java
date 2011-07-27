package com.jjonsson.chess.evaluators;

public final class ResourceAllocator
{
	private static long availableThreads = Runtime.getRuntime().availableProcessors() - 1;
	
	private ResourceAllocator()
	{
		
	}
	
	/**
	 * 
	 * @return true if a new thread should be spawned. The spawned thread is responsible for calling freeThread() when it's done.
	 */
	public static synchronized boolean claimThread()
	{
		if(availableThreads > 0)
		{
			availableThreads--;
			return true;
		}
		return false;
	}
	
	public static synchronized void freeThread()
	{
		availableThreads++;
	}
}
