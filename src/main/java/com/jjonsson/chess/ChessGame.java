package com.jjonsson.chess;

import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.scenarios.TestScenarios;

public class ChessGame 
{
	public static final String VERSION = "0.2";
	public static final String NAME = "Chess";
	public static final String APP_TITLE = NAME + " " + VERSION;
	ChessBoard myBoard;
	ChessWindow myWindow;
	
	public ChessGame()
	{
		myBoard = new ChessBoard(true);
		myWindow = new ChessWindow(this);
	}
	
	/**
	 * 
	 * @return the current board that this game uses
	 */
	public ChessBoard getBoard()
	{
		return myBoard;
	}
	
	/**
	 * Assigns this game a new board
	 * @param board the new board to play on
	 */
	public void setBoard(ChessBoard board)
	{
		myBoard = board;
	}

	public void launch()
	{
		myWindow.displayGame();	
	}

}
