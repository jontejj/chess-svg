package com.jjonsson.chess;

import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Piece;

public interface ChessBoardListener
{
	public void piecePlaced(Piece p);

	public void gameStateChanged(ChessState newState);

	/**
	 * Called if the UI shouldn't refresh itself as this will be done later on instead when the loading is done
	 * @param p
	 */
	public void piecePlacedLoadingInProgress(Piece p);
	
	public void loadingOfBoardDone();
	
	/**
	 * Called when the current player has moved and it's the next players turn
	 */
	public void nextPlayer();
}
