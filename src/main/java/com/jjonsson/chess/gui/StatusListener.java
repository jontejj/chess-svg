package com.jjonsson.chess.gui;

/**
 * A listener for when the status of the chess game has been changed
 * @author jonatanjoensson
 *
 */
public interface StatusListener
{
	void statusHasBeenUpdated();
	
	void setResultOfInteraction(String resultText);
}
