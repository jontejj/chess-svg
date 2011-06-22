package com.jjonsson.chess.gui; 

import java.awt.RenderingHints;

import javax.swing.*;

public class WindowUtilities {

	private static RenderingHints renderHints;
	static
	{
		  renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		  renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	}
	
	/**
	 * Tell system to use native look and feel, as in previous
     * releases. Metal (Java) LAF is the default otherwise.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void setNativeLookAndFeel() throws UnsupportedLookAndFeelException, ClassNotFoundException, InstantiationException, IllegalAccessException 
	{
		//For Mac's we want to use the native menu bar
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", ChessWindow.NAME);
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
	}
	  
	public static RenderingHints getRenderingHints()
	{
		return renderHints;
	}
}
