package com.jjonsson.chess;

import com.jjonsson.chess.gui.WindowUtilities;

public class Chess {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{	
		WindowUtilities.setNativeLookAndFeel();
		
		ChessGame game = new ChessGame();		
		game.launch();
	}

}
