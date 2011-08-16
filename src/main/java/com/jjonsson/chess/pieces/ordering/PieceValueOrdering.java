package com.jjonsson.chess.pieces.ordering;

import java.io.Serializable;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.pieces.Piece;

public class PieceValueOrdering extends Ordering<Piece> implements Serializable
{
	private static final long	serialVersionUID	= 4244194659150651368L;

	@Override
	public int compare(final Piece left, final Piece right)
	{
		return left.getValue() - right.getValue();
	}

}
