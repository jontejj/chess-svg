package com.jjonsson.chess.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class WindowListener extends WindowAdapter
{	
	@Override
	public void windowClosing(WindowEvent event) 
	{
		System.exit(0);
	}
}
