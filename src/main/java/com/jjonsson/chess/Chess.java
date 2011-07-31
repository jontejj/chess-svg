package com.jjonsson.chess;

import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.WindowUtilities;

public final class Chess {
	
	private Chess()
	{
		
	}
	
	/**
	 * @param args not used
	 */
	public static void main(String[] args)
	{	
		//LOGGER.setLevel(Level.WARNING);
		WindowUtilities.setNativeLookAndFeel();
		
		ChessBoard board = new ChessBoard(true);
		board.setDifficulty(2);
		ChessWindow window = new ChessWindow(board);
		window.displayGame();
	}

}
