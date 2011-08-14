package com.jjonsson.chess.exceptions;

import com.jjonsson.chess.moves.Position;

public class UnavailableMoveItem extends Exception
{
	private static final long	serialVersionUID	= 3680952002291193598L;

	private Position myFromPosition;
	private Position myToPosition;
	public UnavailableMoveItem(final String message, final Position from, final Position to)
	{
		super(message);
		myFromPosition = from;
		myToPosition = to;
	}

	@Override
	public String toString()
	{
		return myFromPosition + " -> " + myToPosition + " is not available (" + getMessage() + ")";
	}
}
