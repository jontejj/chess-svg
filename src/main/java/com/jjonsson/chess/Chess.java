package com.jjonsson.chess;

import static com.jjonsson.utilities.Loggers.STDOUT;
import static org.apache.log4j.Level.DEBUG;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.DisplayOption;
import com.jjonsson.chess.gui.WindowUtilities;

public final class Chess {
	private Chess(){}

	/**
	 * @param args not used
	 */
	public static void main(final String[] args)
	{
		//TODO: remove
		//FileSystem.deleteDuplicateFiles("faulty_boards/");
		//BoardLoader.cleanUnloadableBoards("faulty_boards/");
		/*if(!DEBUG)
		{
			LOGGER.setLevel(Level.WARNING);
		}*/
		STDOUT.setLevel(DEBUG);

		WindowUtilities.setNativeLookAndFeel();

		ChessBoard board = new ChessBoard();
		board.setDifficulty(2);

		@SuppressWarnings("unused") //used by EDT
		ChessWindow window = new ChessWindow(board, DisplayOption.DISPLAY);
	}
}
