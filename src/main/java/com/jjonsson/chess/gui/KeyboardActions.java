package com.jjonsson.chess.gui;

import static org.fest.swing.util.Platform.controlOrCommandMask;
import static org.fest.swing.util.Platform.isMacintosh;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public final class KeyboardActions
{
	private KeyboardActions(){};

	public enum Action
	{
		EXIT,
		SAVE_AS,
		SAVE,
		NEW,
		LOAD,
		UNDO,
		UNDO_TWICE,
		SHOW_HINT,
		RELOAD,
		SHOW_STATISTICS
	}

	public static KeyStroke getKeyStrokeForAction(final Action action)
	{
		int keyCode = 0;
		int keyModifiers = controlOrCommandMask();

		switch(action)
		{
			case SAVE:
				keyCode = KeyEvent.VK_S;
				break;
			case SAVE_AS:
				keyCode = KeyEvent.VK_S;
				keyModifiers |= java.awt.event.InputEvent.SHIFT_MASK;
				break;
			case NEW:
				keyCode = KeyEvent.VK_N;
				break;
			case LOAD:
				keyCode = KeyEvent.VK_O;
				break;
			case UNDO_TWICE:
				keyCode = KeyEvent.VK_Z;
				break;
			case UNDO:
				keyCode = KeyEvent.VK_Z;
				keyModifiers |= java.awt.event.InputEvent.SHIFT_MASK;
				break;
			case SHOW_HINT:
				keyCode = KeyEvent.VK_I;
				break;
			case RELOAD:
				keyCode = KeyEvent.VK_R;
				break;
			case SHOW_STATISTICS:
				keyCode = KeyEvent.VK_D;
				break;
			case EXIT:
				if(isMacintosh())
				{
					keyCode = KeyEvent.VK_Q;
				}
				else
				{
					keyCode = KeyEvent.VK_F4;
					keyModifiers = java.awt.event.InputEvent.ALT_MASK;
				}
				break;
		}

		return KeyStroke.getKeyStroke(keyCode, keyModifiers);
	}
}
