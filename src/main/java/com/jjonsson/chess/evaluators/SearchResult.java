package com.jjonsson.chess.evaluators;

import com.jjonsson.chess.moves.Move;

class SearchResult
{
	public Move bestMove = null;
	public long bestMoveValue = Long.MIN_VALUE;
}
