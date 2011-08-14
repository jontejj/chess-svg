package com.jjonsson.chess;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.WindowUtilities;

public final class Chess {

	private Chess()
	{

	}

	/**
	 * @param args not used
	 * @throws Throwable
	 */
	public static void main(final String[] args)
	{
		/*if(!DEBUG)
		{
			LOGGER.setLevel(Level.WARNING);
		}*/
		WindowUtilities.setNativeLookAndFeel();

		ChessBoard board = new ChessBoard(true, true);
		board.setDifficulty(2);
		ChessWindow window = new ChessWindow(board);
		window.displayGame();
	}

}
