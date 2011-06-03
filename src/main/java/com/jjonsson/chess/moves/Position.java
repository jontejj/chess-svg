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
	
	private static final char[] columns = {'A','B','C','D','E','F','G','H'}; 
	
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
	
	public void setRow(byte newRow)
	{
		myRow = newRow;
	}
	
	public void setColumn(byte newColumn)
	{
		myColumn = newColumn;
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
		if(row < 1 || row > ChessBoard.BOARD_SIZE || column < 1 || column > ChessBoard.BOARD_SIZE)
			throw new InvalidPosition(row, column);
		
		return new Position((byte)(row - 1), (byte)(column - 1));
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
			return false;
		if(o.getClass() != getClass())
			return false;
		Position p = (Position)o;
		return myRow == p.getRow() && myColumn == p.getColumn();
	}
	
	@Override
	public String toString()
	{
		//if myColumn is an invalid index there is something that is seriously wrong
		return (myRow+1) + "" + columns[myColumn];
	}
	
	@Override
	public Position clone()
	{
		Position p = null;
		try
		{
			p = (Position) super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
		}
		return p;
		
	}
}
