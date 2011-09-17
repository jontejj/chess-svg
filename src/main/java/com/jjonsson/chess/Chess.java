package com.jjonsson.chess;

import static com.jjonsson.chess.board.PiecePlacement.PLACE_PIECES;
import static com.jjonsson.chess.persistence.PersistanceLogging.USE_PERSISTANCE_LOGGING;
import static com.jjonsson.utilities.Loggers.STDOUT;
import static org.apache.log4j.Level.DEBUG;

import java.lang.reflect.InvocationTargetException;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.WindowUtilities;

public final class Chess {

	private Chess(){}

	/**
	 * @param args not used
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	public static void main(final String[] args) throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
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

		ChessBoard board = new ChessBoard(PLACE_PIECES, USE_PERSISTANCE_LOGGING);
		board.setDifficulty(2);
		ChessWindow window = new ChessWindow(board);
		window.displayGame();
	}
}
