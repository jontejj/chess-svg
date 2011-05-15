package com.jjonsson.chess.moves.ordering;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;

public class MoveOrdering
{
	public static final Ordering<Move> instance = Ordering.compound(ImmutableList.of(new TakeOverValueOrdering(), new CenterStageOrdering())); 
}
