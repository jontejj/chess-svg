package com.jjonsson.chess.pieces.ordering;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.pieces.Piece;

public class PieceValueOrdering extends Ordering<Piece>
{

	@Override
	public int compare(Piece left, Piece right)
	{
		return left.getValue() - right.getValue();
	}

}
