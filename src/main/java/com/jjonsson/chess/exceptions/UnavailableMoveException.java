package com.jjonsson.chess.exceptions;

import com.jjonsson.chess.moves.Move;

public class UnavailableMoveException extends Exception
{
	private static final long	serialVersionUID	= -8863506321936559669L;

	private Move myMove;
	public UnavailableMoveException(Move unavailableMove)
	{
		super();
		myMove = unavailableMove;
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
