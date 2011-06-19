package com.jjonsson.chess.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class WindowListener extends WindowAdapter
{	
	@Override
	public void windowClosing(WindowEvent event) 
	{
		JFrame.class.cast(event.getSource()).setVisible(false);
		//System.exit(0);
	}
}
