package com.jjonsson.chess.exceptions;

public class UnremovablePieceError extends Error
{
	private static final long	serialVersionUID	= 5642812465261723590L;

	public UnremovablePieceError(final String message)
	{
		super(message);
	}
}
