package com.jjonsson.chess.exceptions;

public class NoMovesAvailableException extends Exception
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2276866519775930204L;

	public NoMovesAvailableException(final Throwable cause)
	{
		super(cause);
	}

	public NoMovesAvailableException()
	{
		super();
	}

}
