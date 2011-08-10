package com.jjonsson.chess.evaluators;

import com.jjonsson.chess.moves.Move;

public class SearchResult implements Comparable<SearchResult>
{
	private Move myBestMove = null;
	private Long myBestMoveValue = Long.MIN_VALUE;

	void setBestMoveIfBetter(final Move newBestMove, final long newMoveValue)
	{
		if(newMoveValue > myBestMoveValue || myBestMove == null)
		{
			myBestMove = newBestMove;
			myBestMoveValue = newMoveValue;
		}
	}

	long getBestMoveValue()
	{
		return myBestMoveValue;
	}

	Move getBestMove()
	{
		return myBestMove;
	}

	void applyPlayerAffinityFactor(final long factor)
	{
		myBestMoveValue *= factor;
	}

	@Override
	public int compareTo(final SearchResult o)
	{
		return myBestMoveValue.compareTo(o.myBestMoveValue);
	}
}
