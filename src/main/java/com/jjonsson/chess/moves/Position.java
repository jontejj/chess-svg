package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.utilities.HashCodes;

public abstract class Position
{

	private byte myRow;
	private byte myColumn;

	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	public static final int D = 3;
	public static final int E = 4;
	public static final int F = 5;
	public static final int G = 6;
	public static final int H = 7;

	public static final int BLACK_STARTING_ROW = ChessBoard.BOARD_SIZE - 1;
	public static final int BLACK_PAWN_ROW = ChessBoard.BOARD_SIZE - 2;
	public static final int WHITE_STARTING_ROW = 0;
	public static final int WHITE_PAWN_ROW = 1;


	/**
	 * A row is stored in the four leftmost bits
	 */
	protected static final byte ROW_OFFSET = 4;
	/**
	 * A column is stored in the four rightmost bits
	 */
	protected static final byte COLUMN_MASK = 0x0F;

	private static final char[] COLUMNS = {'A','B','C','D','E','F','G','H'};

	/**
	 * 
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7
	 */
	protected Position(final byte row, final byte column)
	{
		myRow = row;
		myColumn = column;
	}

	protected void addRowChange(final byte rowChange)
	{
		myRow += rowChange;
	}

	protected void addColumnChange(final byte columnChange)
	{
		myColumn += columnChange;
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


	/**
	 * Validates that row and column are NOT between 0-7 (inclusive)
	 * @param row
	 * @param column
	 * @return true if the given arguments results in a position that is'nt found on a chess board
	 */
	public static boolean isInvalidPosition(final byte row, final byte column)
	{
		return row < 0 || column < 0 || row >= ChessBoard.BOARD_SIZE || column >= ChessBoard.BOARD_SIZE;
	}

	/**
	 * Validates that row and column are NOT between 0-7 (inclusive)
	 * @param row
	 * @param column
	 * @return true if the given row/column isn't a valid position
	 */
	public static boolean isInvalidPosition(final int row, final int column)
	{
		return row < 0 || column < 0 || row >= ChessBoard.BOARD_SIZE || column >= ChessBoard.BOARD_SIZE;
	}

	@Override
	public int hashCode()
	{
		return myRow * HashCodes.PRIME_NUMBER + myColumn;
	}

	@Override
	public boolean equals(final Object o)
	{
		Position p = (Position)o;
		return myRow == p.getRow() && myColumn == p.getColumn();
	}

	/**
	 * Returns a string like "1A"
	 */
	@Override
	public String toString()
	{
		return (myRow+1) + "" + COLUMNS[myColumn];
	}

	/**
	 * Same as clone() but without the CloneNotSupportedException
	 * @return a new instance that represents this position, or the same instance if it's a {@link ImmutablePosition}
	 */
	public abstract Position copy();
}
