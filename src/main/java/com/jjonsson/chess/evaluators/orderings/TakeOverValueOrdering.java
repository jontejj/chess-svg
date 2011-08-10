package com.jjonsson.chess.evaluators.orderings;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;

public class TakeOverValueOrdering extends Ordering<Move>
{

	@Override
	public int compare(Move left, Move right)
	{
		return left.getTakeOverValue() - right.getTakeOverValue();
	}

}
