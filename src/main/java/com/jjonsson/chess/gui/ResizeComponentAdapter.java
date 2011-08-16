package com.jjonsson.chess.gui;

import java.awt.event.ComponentEvent;

public class ResizeComponentAdapter extends java.awt.event.ComponentAdapter
{
	private ChessWindow myWindow;
	
	public ResizeComponentAdapter(ChessWindow window)
	{
		myWindow = window;
	}
	
	@Override
	public void componentResized(ComponentEvent e) 
	{
		myWindow.resizeWindow();
	}
}
