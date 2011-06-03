package com.jjonsson.chess.moves.ordering;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;

/**
 * Orders moves first by their need to evade a hostile take over, then by their own take over value and lastly how near the center
 * they take their pieces
 * @author jonatanjoensson
 *
 */
public class MoveOrdering
{
	public static final Ordering<Move> instance = Ordering.compound(ImmutableList.of(
																	new TakeOverValueOrdering(), 
																	new EvadeOrdering(), 
																	new CenterStageOrdering()));
}
