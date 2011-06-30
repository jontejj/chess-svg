package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;

public class Position implements Cloneable
{
	
	private byte myRow;
	private byte myColumn;
	
	public static final int A = 1;
	public static final int B = 2;
	public static final int C = 3;
	public static final int D = 4;
	public static final int E = 5;
	public static final int F = 6;
	public static final int G = 7;
	public static final int H = 8;
	
	private static final char[] COLUMNS = {'A','B','C','D','E','F','G','H'}; 
	
	/**
	 * 
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7
	 */
	public Position(byte row, byte column)
	{
		myRow = row;
		myColumn = column;
	}
	
	/**
	 * 
	 * @return 0-7
	 */
	public byte getRow()
	{
		return myRow;
	}
	
	/**
	 * 
	 * @return 0-7
	 */
	public byte getColumn()
	{
		return myColumn;
	}

	void applyMove(Move move) 
	{
		myRow += move.getRowChange();
		myColumn += move.getColumnChange();
	}
	
	/**
	 * 
	 * @param row the row for this position, valid numbers are 1-8
	 * @param column the column for this position, valid numbers are 1-8 (A-H)
	 * @throws InvalidPosition if one or both of the given parameters are out of range 
	 */
	public static Position createPosition(int row, int column) throws InvalidPosition
	{
		if(isInvalidPosition(row-1, column-1))
		{
			throw new InvalidPosition(row, column);
		}
		
		return new Position((byte)(row - 1), (byte)(column - 1));
	}
	
	/**
	 * Validates that row and column are NOT between 0-7 (inclusive)
	 * @param row
	 * @param column
	 * @return true if the given arguments results in a position that is'nt found on a chess board
	 */
	public static boolean isInvalidPosition(byte row, byte column)
	{
		return row < 0 || column < 0 || row >= ChessBoard.BOARD_SIZE || column >= ChessBoard.BOARD_SIZE;
	}
	
	/**
	 * Validates that row and column are NOT between 0-7 (inclusive)
	 * @param row
	 * @param column
	 * @return
	 */
	private static boolean isInvalidPosition(int row, int column)
	{
		return row < 0 || column < 0 || row >= ChessBoard.BOARD_SIZE || column >= ChessBoard.BOARD_SIZE;
	}
	
	public Position up() throws InvalidPosition
	{
		return Position.createPosition(myRow + 2, myColumn + 1);
	}
	
	public Position down() throws InvalidPosition
	{
		return Position.createPosition(myRow, myColumn + 1);
	}
	
	public Position right() throws InvalidPosition
	{
		return Position.createPosition(myRow + 1, myColumn + 2);
	}
	
	public Position left() throws InvalidPosition
	{
		return Position.createPosition(myRow + 1, myColumn);
	}
	
	/**
	 * This method can be used to bind this position to a specific piece
	 */
	@Override
	public int hashCode()
	{
		return myRow * 1237 + myColumn;
	}
	
	@Override
	public boolean equals(Object o)
	{
		if(o == null)
		{
			return false;
		}
		Position p = (Position)o;
		return myRow == p.getRow() && myColumn == p.getColumn();
	}
	
	@Override
	public String toString()
	{
		//if myColumn is an invalid index there is something that is seriously wrong
		return (myRow+1) + "" + COLUMNS[myColumn];
	}
	
	public Position copy()
	{
		Position p = null;
		try
		{
			p = clone();
		}
		catch (CloneNotSupportedException e)
		{
		}
		return p;
	}
	
	@Override
	public Position clone() throws CloneNotSupportedException
	{
		return (Position) super.clone();
		
	}
}
