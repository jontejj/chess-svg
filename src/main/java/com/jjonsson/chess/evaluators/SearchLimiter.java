package com.jjonsson.chess.evaluators;

class SearchLimiter
{
	static final long MAX_DEPTH = 2;
	static final long MAX_BRANCH_MOVES = 30;
	private int myDifficulty;
	/**
	 * Used to limit the amount of moves to evaluate
	 */
	long movesLeft;
	/**
	 * Used to limit the depth of the most interesting moves
	 */
	long depth;
	
	/**
	 * either 1 if the current player is the AI or -1 if the current player is the opponent
	 * this makes it possible to simulate that the player would have made the best move according to the same algorithm
	 */
	long scoreFactor;
	
	SearchLimiter(int difficulty)
	{
		movesLeft = MAX_BRANCH_MOVES;
		depth = MAX_DEPTH;
		scoreFactor = 1;
		myDifficulty = difficulty;
	}
	
	long getCurrentDepth()
	{
		return MAX_DEPTH-depth + 1;
	}
	
	int getMinimumDepthToSearch()
	{
		return myDifficulty;
	}
}