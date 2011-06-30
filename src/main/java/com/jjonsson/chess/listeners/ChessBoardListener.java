package com.jjonsson.chess.listeners;

import com.google.common.collect.ImmutableMap;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;

public interface ChessBoardListener
{
	void piecePlaced(Piece p);

	void gameStateChanged(ChessState newState);

	/**
	 * Called if the UI shouldn't refresh itself as this will be done later on instead when the loading is done
	 * @param p
	 */
	void piecePlacedLoadingInProgress(Piece p);
	
	void loadingOfBoardDone();
	
	/**
	 * Called when the current player has moved and it's the next players turn
	 */
	void nextPlayer();
	
	/**
	 * Called when the requested undo command has been performed
	 */
	void undoDone();
	
	/**
	 * 
	 * @return true if the listener wishes to decide which piece a pawn should be replaced with
	 */
	boolean supportsPawnReplacementDialog();
	
	/**
	 * 
	 * @return null if you don't support pawn replacement dialogs
	 */
	Piece getPawnReplacementFromDialog();
	
	void squareScores(ImmutableMap<Position, String> positionScores);
}
