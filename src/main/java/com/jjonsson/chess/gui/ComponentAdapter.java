package com.jjonsson.chess.gui;

import java.awt.event.ComponentEvent;

public class ComponentAdapter extends java.awt.event.ComponentAdapter
{
	private ChessWindow myWindow;
	
	public ComponentAdapter(ChessWindow window)
	{
		myWindow = window;
	}
	
	@Override
	public void componentResized(ComponentEvent e) 
	{
		myWindow.resizeWindow();
	}
}
