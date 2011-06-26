package com.jjonsson.chess.listeners;

import com.google.common.collect.ImmutableMap;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.moves.Position;
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
	
	/**
	 * Called when the requested undo command has been performed
	 */
	public void undoDone();
	
	/**
	 * 
	 * @return true if the listener wishes to decide which piece a pawn should be replaced with
	 */
	public boolean supportsPawnReplacementDialog();
	
	/**
	 * 
	 * @return null if you don't support pawn replacement dialogs
	 */
	public Piece getPawnReplacementFromDialog();
	
	public void squareScores(ImmutableMap<Position, String> positionScores);
}
