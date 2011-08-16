package com.jjonsson.chess.evaluators.orderings;

import java.io.Serializable;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;

public class TakeOverValueOrdering extends Ordering<Move> implements Serializable
{
	private static final long	serialVersionUID	= 8835399325232597263L;

	@Override
	public int compare(final Move left, final Move right)
	{
		return left.getTakeOverValue() - right.getTakeOverValue();
	}

}
