package com.jjonsson.chess;

import static com.jjonsson.utilities.Logger.LOGGER;

import java.util.logging.Level;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.WindowUtilities;

public final class Chess {

	private Chess()
	{

	}

	/**
	 * @param args not used
	 */
	public static void main(final String[] args)
	{
		/*if(!DEBUG)
		{
			LOGGER.setLevel(Level.WARNING);
		}*/
		LOGGER.setLevel(Level.FINER);

		WindowUtilities.setNativeLookAndFeel();

		ChessBoard board = new ChessBoard(true, true);
		board.setDifficulty(2);
		ChessWindow window = new ChessWindow(board);
		window.displayGame();
	}

}
