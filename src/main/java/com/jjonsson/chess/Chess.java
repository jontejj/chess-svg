package com.jjonsson.chess;

import javax.swing.UnsupportedLookAndFeelException;

import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.WindowUtilities;

public class Chess {

	/**
	 * @param args not used
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws UnsupportedLookAndFeelException 
	 */
	public static void main(String[] args)
	{	
		try
		{
			WindowUtilities.setNativeLookAndFeel();
		}
		catch(Exception e)
		{
			/* The UI will look awful but what can we do? :)*/
		}
		
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);
		window.displayGame();
	}

}
