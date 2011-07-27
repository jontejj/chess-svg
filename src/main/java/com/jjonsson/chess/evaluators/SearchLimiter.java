package com.jjonsson.chess.evaluators;

class SearchLimiter
{
	static final long MAX_DEPTH = 2;
	static final long MAX_BRANCH_MOVES = 30;
	private int myDifficulty;
	/**
	 * Used to limit the amount of moves to evaluate
	 */
	private long myMovesLeft;
	/**
	 * Used to limit the depth of the most interesting moves
	 */
	private long myDepth;
	
	/**
	 * either 1 if the current player is the AI or -1 if the current player is the opponent
	 * this makes it possible to simulate that the player would have made the best move according to the same algorithm
	 */
	private long myScoreFactor;
	
	SearchLimiter(int difficulty)
	{
		myMovesLeft = MAX_BRANCH_MOVES;
		myDepth = MAX_DEPTH;
		myScoreFactor = 1;
		myDifficulty = difficulty;
	}
	
	public SearchLimiter copy()
	{
		SearchLimiter copy = new SearchLimiter(myDifficulty);
		copy.myScoreFactor = this.myScoreFactor;
		copy.myMovesLeft = this.myMovesLeft;
		copy.myDepth = this.myDepth;
		return copy;
	}
	
	public void resetMovesLeft()
	{
		myMovesLeft = MAX_BRANCH_MOVES;
	}
	
	long getCurrentDepth()
	{
		return MAX_DEPTH-myDepth + 1;
	}
	
	int getMinimumDepthToSearch()
	{
		return myDifficulty;
	}
	
	long getDepth()
	{
		return myDepth;
	}
	
	long getMovesLeft()
	{
		return myMovesLeft;
	}
	
	long getScoreFactor()
	{
		return myScoreFactor;
	}
	
	void goDown()
	{
		myDepth--;
		myMovesLeft--;
		myScoreFactor *= -1;
	}
	
	void goUp()
	{		
		myDepth++;
	}
}