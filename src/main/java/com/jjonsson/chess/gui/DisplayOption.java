package com.jjonsson.chess.gui;

public enum DisplayOption
{
	/**
	 * Indicates that the component should be visible and ready to be accessed
	 */
	DISPLAY,
	/**
	 * Indicates that the component shouldn't be visible
	 */
	DONT_DISPLAY;

	public boolean shouldDisplay()
	{
		return this == DISPLAY;
	}
}
