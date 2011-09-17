package com.jjonsson.chess.gui;

import static com.jjonsson.utilities.Loggers.STDOUT;

import java.awt.RenderingHints;

import javax.swing.UIManager;

public final class WindowUtilities {

	private WindowUtilities(){}

	private static RenderingHints renderHints;
	static
	{
		renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}

	/**
	 * Tell system to use native look and feel, as in previous
	 * releases. Metal (Java) LAF is the default otherwise.
	 */
	public static void setNativeLookAndFeel()
	{
		//For Mac's we want to use the native menu bar
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", ChessWindow.NAME);
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			STDOUT.fatal("Failed to set look and feel due to: " + e);
		}
	}

	public static RenderingHints getRenderingHints()
	{
		return renderHints;
	}
}
