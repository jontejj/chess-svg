package com.jjonsson.chess.board;

/**
 * Two value boolean enum used as parameter when constructing a board
 */
public enum PiecePlacement
{
	PLACE_PIECES,
	DONT_PLACE_PIECES;

	public boolean shouldPlacePieces()
	{
		return this == PLACE_PIECES;
	}
}