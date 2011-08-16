package com.jjonsson.chess.exceptions;

import com.jjonsson.chess.moves.Move;

public class UnavailableMoveException extends Exception
{
	private static final long	serialVersionUID	= -8863506321936559669L;

	public static volatile int exceptionsThrown = 0;
	private Move myMove;
	public UnavailableMoveException(final Move unavailableMove)
	{
		super();
		myMove = unavailableMove;
		exceptionsThrown++;
	}

	public Move getUnavailableMove()
	{
		return myMove;
	}

	@Override
	public String toString()
	{
		return "Move: " + myMove + " is not available";
	}
}
