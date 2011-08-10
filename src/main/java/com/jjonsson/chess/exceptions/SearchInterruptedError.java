package com.jjonsson.chess.exceptions;

/**
 * Thrown when something interrupts a search such as when you start a new game while the AI is thinking.
 * @author jonatanjoensson
 *
 */
public class SearchInterruptedError extends Error
{
	private static final long	serialVersionUID	= 6462136781600348640L;
	
	public SearchInterruptedError(Throwable cause)
	{
		super(cause);
	}
}
