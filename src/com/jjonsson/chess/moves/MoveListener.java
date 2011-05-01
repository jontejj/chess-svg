package com.jjonsson.chess.moves;

import com.jjonsson.chess.pieces.Piece;

public interface MoveListener
{
	/**
	 * Called whenever a piece has been moved
	 */
	public void movePerformed(Move performedMove);

	public void pieceRemoved(Piece removedPiece);
}
