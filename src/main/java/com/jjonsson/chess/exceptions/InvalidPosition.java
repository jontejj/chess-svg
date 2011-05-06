package com.jjonsson.chess.exceptions;

public class InvalidPosition extends Exception
{

	private static final long	serialVersionUID	= -7230926466258265135L;
	
	int myRow;
	int myColumn;
	
	public InvalidPosition(int row, int column)
	{
		myRow = row;
		myColumn = column;
	}
	
	@Override
	public String toString()
	{
		return "Invalid Position: (" + myRow + ", " + myColumn +")";
	}
}
