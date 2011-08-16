package com.jjonsson.chess.evaluators.orderings;

import java.io.Serializable;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.moves.Move;

/**
 * Compares move by how progressive they are. (e.g a pawn move is very progressive as it leads to pawn replacements)
 * @author jonatanjoensson
 *
 */
public class ProgressivenessOrdering extends Ordering<Move> implements Serializable
{
	private static final long	serialVersionUID	= -1413656721452412905L;

	@Override
	public int compare(final Move left, final Move right)
	{
		return left.getProgressiveValue() - right.getProgressiveValue();
	}

}
