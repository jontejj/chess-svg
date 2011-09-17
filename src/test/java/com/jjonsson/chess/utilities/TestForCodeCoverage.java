package com.jjonsson.chess.utilities;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.jjonsson.chess.Chess;
import com.jjonsson.chess.board.PiecePlacement;
import com.jjonsson.chess.board.PositionContainer;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.evaluators.ProgressTracker;
import com.jjonsson.chess.evaluators.orderings.MoveOrdering;
import com.jjonsson.chess.gui.PieceImageCache;
import com.jjonsson.chess.gui.Settings;
import com.jjonsson.chess.gui.WindowUtilities;
import com.jjonsson.chess.persistence.BoardLoader;
import com.jjonsson.chess.persistence.MoveItem;
import com.jjonsson.chess.persistence.MoveLoggerFactory;
import com.jjonsson.chess.persistence.PersistanceLogging;
import com.jjonsson.utilities.Bits;
import com.jjonsson.utilities.CrossPlatformUtilities;
import com.jjonsson.utilities.HashCodes;
import com.jjonsson.utilities.Loggers;

public class TestForCodeCoverage
{

	/**
		The reasoning behind testing code that doesn't do anything is to achieve 100% code coverage and to notice when the code
		coverage drops. Otherwise one could always think, hey I don't have 100% code coverage anymore but it's PROBABLY because
		of my private constructors (or any other code that's not used but needed). This makes it easy to spot untested methods without having to check that it just was a
		private constructor etc.

		IMO it's best to use reflection here since otherwise you would have to either get a better code coverage tool that
		ignores these constructors or somehow tell the code coverage tool to ignore the method
		(perhaps an Annotation or a configuration file) because then you would be stuck with a specific code coverage tool.

		In a perfect world all code coverage tools would ignore private constructors that belong to a final class
		because the constructor is there as a "security" measure nothing else:)
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws IllegalArgumentException
	 */
	@Test
	public void callPrivateConstructorsForCodeCoverage() throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		Class<?>[] classesToConstruct = {MoveOrdering.class, Bits.class, CrossPlatformUtilities.class, ChessMoveEvaluator.class,
				ChessBoardEvaluator.class, ProgressTracker.class, BoardLoader.class, MoveLoggerFactory.class, Settings.class,
				WindowUtilities.class, PieceImageCache.class, HashCodes.class, Loggers.class, Chess.class,
				VersionControlHelper.class};

		for(Class<?> clazz : classesToConstruct)
		{
			Constructor<?> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			assertNotNull(constructor.newInstance());
		}
	}

	/**
	 * Apparently the compiler injects methods into the byte code for enums that the code coverage tool detects
	 */
	@Test
	public void testEnumsForCodeCoverage()
	{
		PiecePlacement.valueOf(PiecePlacement.PLACE_PIECES.toString());
		PersistanceLogging.valueOf(PersistanceLogging.USE_PERSISTANCE_LOGGING.toString());
		ChessState.valueOf(ChessState.CHECK.toString());
		assertTrue(true);
	}

	/**
	 * Calls toString methods for objects that's used during debugging
	 */
	@Test
	public void testDebugCodeForCodeCoverage()
	{
		new PositionContainer(null).toString();
		MoveItem.from(null, null).toString();
		assertTrue(true);
	}

	@Test
	public void testMainForCodeCoverage() throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException
	{
		Chess.main(null);
		assertTrue(true);
	}
}
