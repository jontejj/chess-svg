package com.jjonsson.chess.moves.ordering;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;

/**
 * Compares move by how progressive they are. (e.g a pawn move is very progressive as it leads to pawn replacements)
 * @author jonatanjoensson
 *
 */
public class ProgressivenessOrdering extends Ordering<Move>
{

	@Override
	public int compare(Move left, Move right)
	{
		return left.getProgressiveValue() - right.getProgressiveValue();
	}

}
