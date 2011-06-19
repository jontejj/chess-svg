package com.jjonsson.chess.gui;

/**
 * A listener for when the status of the chess game has been changed
 * @author jonatanjoensson
 *
 */
public interface StatusListener
{
	public void statusHasBeenUpdated();
	
	public void setResultOfInteraction(String resultText);
}
