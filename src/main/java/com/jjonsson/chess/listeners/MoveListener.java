package com.jjonsson.chess.listeners;

import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.pieces.Piece;

public interface MoveListener
{
	/**
	 * Called whenever a piece has been moved
	 */
	void movePerformed(Move performedMove);

	void pieceRemoved(Piece removedPiece);

	void moveReverted(RevertingMove move);

	/**
	 * Called when the board have been reset
	 */
	void reset();
}
