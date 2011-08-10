package com.jjonsson.chess.gui; 

import java.awt.RenderingHints;

import javax.swing.*;

public final class WindowUtilities {

	private WindowUtilities()
	{
		
	}
	
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
		catch (UnsupportedLookAndFeelException e){}
		catch (ClassNotFoundException e){}
		catch (InstantiationException e){}
		catch (IllegalAccessException e){}
	}
	  
	public static RenderingHints getRenderingHints()
	{
		return renderHints;
	}
}
