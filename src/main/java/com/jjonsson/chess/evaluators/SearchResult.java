package com.jjonsson.chess.evaluators;

import com.jjonsson.chess.moves.Move;

class SearchResult
{
	Move bestMove = null;
	long bestMoveValue = Long.MIN_VALUE;
}
