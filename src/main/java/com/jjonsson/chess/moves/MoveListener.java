package com.jjonsson.chess.moves;

import com.jjonsson.chess.pieces.Piece;

public interface MoveListener
{
	/**
	 * Called whenever a piece has been moved
	 */
	void movePerformed(Move performedMove);

	void pieceRemoved(Piece removedPiece);
}
