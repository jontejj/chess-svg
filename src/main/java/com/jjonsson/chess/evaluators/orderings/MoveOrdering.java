package com.jjonsson.chess.evaluators.orderings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;

/**
 * Orders moves first by their need to evade a hostile take over, then by their own take over value and lastly how near the center
 * they take their pieces
 * @author jonatanjoensson
 *
 */
public final class MoveOrdering
{
	private MoveOrdering()
	{

	}

	private static final Ordering<Move> INSTANCE = Ordering.compound(ImmutableList.of(
			new TakeOverValueOrdering(),
			new EvadeOrdering(),
			new ProgressivenessOrdering(),
			new CenterStageOrdering()));

	public static Ordering<Move> getInstance()
	{
		return INSTANCE;
	}
}
