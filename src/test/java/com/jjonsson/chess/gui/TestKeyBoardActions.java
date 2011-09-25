package com.jjonsson.chess.gui;

import static com.jjonsson.chess.gui.KeyboardActions.getKeyStrokeForAction;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Set;

import javax.swing.KeyStroke;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.jjonsson.chess.gui.KeyboardActions.Action;

public class TestKeyBoardActions
{
	/**
	 * Tests that all {@link Action}s is implemented correctly
	 */
	@Test
	public void testThatAllActionsReturnDifferentKeyStrokes()
	{
		Set<KeyStroke> keyStrokes = Sets.newIdentityHashSet();
		for(Action a : Action.values())
		{
			KeyStroke ks = getKeyStrokeForAction(a);
			assertFalse("Failed to get key for action: " + a, ks.getKeyCode() == 0);
			assertTrue(keyStrokes.add(ks));
		}
	}
}
