package com.jjonsson.utilities;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

public class CrossPlatformUtilities
{
	private static final boolean isWindows;
	private static final boolean isMac; 
	private static final int shortcutModifier;
	
	public static final int NO_ACTION	= -1;
	public static final int	EXIT		= 0;
	public static final int	SAVE_AS		= 1;
	public static final int	SAVE		= 2;
	public static final int	NEW			= 3;
	public static final int	LOAD		= 4;
	public static final int	UNDO		= 5;
	public static final int	UNDO_TWICE	= 6;
	public static final int SHOW_HINT	= 7;
	
	static
	{
		String operatingSystem = System.getProperty("os.name").toLowerCase();
		isWindows = (operatingSystem.indexOf("windows") != -1);
		isMac = (operatingSystem.indexOf("mac") != -1);
		shortcutModifier = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	}
	
	public static boolean isWindows()
	{
		return isWindows;
	}
	
	public static boolean isMac()
	{
		return isMac;
	}
	
	public static KeyStroke getExitKeyStroke()
	{
		return getKeyStrokeForAction(EXIT);
	}
	
	public static KeyStroke getLoadKeyStroke()
	{
		return getKeyStrokeForAction(LOAD);
	}
	
	public static KeyStroke getSaveKeyStroke()
	{
		return getKeyStrokeForAction(SAVE);
	}
	
	public static KeyStroke getSaveAsKeyStroke()
	{
		return getKeyStrokeForAction(SAVE_AS);
	}
	
	public static KeyStroke getNewKeyStroke()
	{
		return getKeyStrokeForAction(NEW);
	}
	
	public static KeyStroke getUndoKeyStroke()
	{
		return getKeyStrokeForAction(UNDO);
	}
	
	public static KeyStroke getUndoTwiceKeyStroke()
	{
		return getKeyStrokeForAction(UNDO_TWICE);
	}
	
	private static KeyStroke getKeyStrokeForAction(int action)
	{
		int keyCode = 0;
		int keyModifiers = shortcutModifier;
		
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
			case EXIT:
				if(isMac)
					keyCode = KeyEvent.VK_Q;
				else
				{
					keyCode = KeyEvent.VK_F4;
					keyModifiers = java.awt.event.InputEvent.ALT_MASK;
				}
				break;
			default:
				break;
		}
		
		return KeyStroke.getKeyStroke(keyCode, keyModifiers);
	}
	
	/**
	 * Can be used if you wan't to implement KeyListener instead of ActionListener
	 * @param event
	 * @return
	 */
	public static int getActionFromKeyEvent(KeyEvent event)
	{
		int action = NO_ACTION;
		
		if((event.getModifiers() & shortcutModifier) > 0)
		{
			switch(event.getKeyCode())
			{
				case KeyEvent.VK_S:
					if(event.isShiftDown())
						action = SAVE_AS;
					else
						action = SAVE;
					break;
					
				case KeyEvent.VK_N:
					action = NEW;
					break;
					
				case KeyEvent.VK_O:
					action = LOAD;
					break;
					
				case KeyEvent.VK_I:
					action = SHOW_HINT;
					break;
					
				case KeyEvent.VK_Z:
					if(event.isShiftDown())
						action = UNDO;
					else
						action = UNDO_TWICE;
					break;
					
				//Usual exit
				case KeyEvent.VK_Q:
					if(event.getModifiers() == shortcutModifier)
						action = EXIT;
					break;
				default:
					break;
			}
		}
		//Windows Exit
		else if(event.getKeyCode() == KeyEvent.VK_F4 && event.isAltDown())
			action = EXIT;
		
		return action;
	}

	public static KeyStroke getShowHintKeyStroke()
	{
		return getKeyStrokeForAction(SHOW_HINT);
	}
}
