package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;

public class ImmutablePosition extends Position
{
	private byte myPersistenceByte;
	private int myHashcode;

	private static final ImmutablePosition[][] POSITIONS = createPositions();

	private static ImmutablePosition[][] createPositions()
	{
		ImmutablePosition[][] positions = new ImmutablePosition[ChessBoard.BOARD_SIZE][ChessBoard.BOARD_SIZE];
		for(byte r = ChessBoard.BOARD_SIZE - 1; r >= 0; r--)
		{
			positions[r] = new ImmutablePosition[ChessBoard.BOARD_SIZE];
			for(byte c = ChessBoard.BOARD_SIZE - 1; c >= 0; c--)
			{
				positions[r][c] = new ImmutablePosition(r, c);
			}
		}
		return positions;
	}

	private ImmutablePosition(final byte row, final byte column)
	{
		super(row, column);
		myPersistenceByte = getPersistenceByte();
		//Well, this isn't going to change, lets cache it!
		myHashcode = super.hashCode();
	}

	@Override
	public int hashCode()
	{
		return myHashcode;
	}

	private byte getPersistenceByte()
	{
		//From left, first 4 bits row then 4 bits column
		byte positionData = (byte) (myRow << 4);
		positionData |= myColumn;
		return positionData;
	}

	public byte getPersistence()
	{
		return myPersistenceByte;
	}

	public static ImmutablePosition fromByte(final byte pos)
	{
		int row = pos >> 4;
		int column = pos & 0x0F;
		return POSITIONS[row][column];
	}

	/**
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7 (A-H)
	 * @return a cached position that won't change during the lifetime of the application
	 * @throws ArrayIndexOutOfBoundsException if one or both of the given parameters are out of range
	 */
	public static ImmutablePosition getPosition(final int row, final int column) throws ArrayIndexOutOfBoundsException
	{
		return POSITIONS[row][column];
	}

	/**
	 * 
	 * @param pos the position to return a "copy" for
	 * @return a cached position that won't change during the lifetime of the application
	 */
	public static ImmutablePosition getPosition(final Position pos)
	{
		return POSITIONS[pos.getRow()][pos.getColumn()];
	}

	/**
	 * As this won't change we need not create a copy of it
	 */
	@Override
	public ImmutablePosition copy()
	{
		return this;
	}
}
