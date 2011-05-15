package com.jjonsson.chess.gui; 

import java.awt.RenderingHints;

import javax.swing.*;

import com.jjonsson.chess.ChessGame;

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
	 */
	public static void setNativeLookAndFeel() 
	{
		//For Mac's we want to use the native menu bar
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", ChessGame.NAME);
		
		try 
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch(Exception e) 
		{
			System.out.println("Error setting native look and feel: " + e.getLocalizedMessage());
		}
	}
	  
	public static RenderingHints getRenderingHints()
	{
		return renderHints;
	}
}
