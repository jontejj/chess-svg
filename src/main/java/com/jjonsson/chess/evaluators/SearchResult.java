package com.jjonsson.chess.evaluators;

import com.jjonsson.chess.moves.Move;

public class SearchResult
{
	private volatile Move myBestMove = null;
	private volatile long myBestMoveValue = Long.MIN_VALUE;

	//TODO: sync the results of all threads after all threads have finished
	synchronized void setBestMoveIfBetter(Move newBestMove, long newMoveValue)
	{
		if(newMoveValue > myBestMoveValue || myBestMove == null)
		{
			myBestMove = newBestMove;
			myBestMoveValue = newMoveValue;
		}
	}
	
	synchronized long getBestMoveValue()
	{
		return myBestMoveValue;
	}
	
	synchronized Move getBestMove()
	{
		return myBestMove;
	}
	
	synchronized void applyPlayerAffinityFactor(long factor)
	{
		myBestMoveValue *= factor;
	}
}
