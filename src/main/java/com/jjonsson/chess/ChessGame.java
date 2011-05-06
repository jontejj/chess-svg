package com.jjonsson.chess;

import com.jjonsson.chess.gui.ChessWindow;

public class ChessGame 
{
	public static final String VERSION = "0.2";
	public static final String NAME = "Chess " + ChessGame.VERSION;
	ChessBoard myBoard;
	ChessWindow myWindow;
	
	public ChessGame()
	{
		myBoard = new ChessBoard();
		myBoard.reset();
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
