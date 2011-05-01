package com.jjonsson.chess.gui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowListener extends WindowAdapter
{
	ChessWindow myWindow;
	Dimension myCurrentWindowSize;
	
	public WindowListener(ChessWindow window)
	{
		myWindow = window;
		myCurrentWindowSize = myWindow.getSize(myCurrentWindowSize);
	}

	@Override
	public void windowStateChanged(WindowEvent e) 
	{
		switch(e.getNewState())
		{
			case WindowEvent.COMPONENT_RESIZED:
				e.getComponent().getSize(myCurrentWindowSize);
				myWindow.resizeWindow(myCurrentWindowSize);
				break;
		}
	}
	
	@Override
	public void windowClosing(WindowEvent event) 
	{
		System.exit(0);
	}
}
