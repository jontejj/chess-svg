package com.jjonsson.chess.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;

public class WindowListener extends WindowAdapter
{	
	@Override
	public void windowClosing(WindowEvent event) 
	{
		//This will trigger AWT to shutdown when the event queue is emptied (Nice shutdown)
		JFrame.class.cast(event.getSource()).dispose();
	}
}
