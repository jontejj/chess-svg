package com.jjonsson.chess.board;

/**
 * Two value boolean enum used as parameter when constructing a board
 */
public enum PiecePlacement
{
	/**
	 * Tells {@link ChessBoard#ChessBoard(PiecePlacement)} that it should place pieces on the board
	 */
	PLACE_PIECES,
	/**
	 * Tells {@link ChessBoard#ChessBoard(PiecePlacement)} that it shouldn't place pieces on the board
	 */
	DONT_PLACE_PIECES;

	public boolean shouldPlacePieces()
	{
		return this == PLACE_PIECES;
	}
}