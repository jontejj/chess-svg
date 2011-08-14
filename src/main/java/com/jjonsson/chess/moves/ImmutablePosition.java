package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;

public final class ImmutablePosition extends Position
{
	private final byte myPersistenceByte;
	private final int myHashcode;

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

	/**
	 * @return a byte where the four leftmost bits is the row (0-7) and the rightmost four bits is the column (0-7)
	 */
	private byte getPersistenceByte()
	{
		byte positionData = (byte) (getRow() << ROW_OFFSET);
		positionData |= getColumn();
		return positionData;
	}
	/**
	 * @return a cached byte where the four leftmost bits is the row (0-7) and the rightmost four bits is the column (0-7)
	 */
	public byte getPersistence()
	{
		return myPersistenceByte;
	}

	/**
	 * @param pos a byte where the four leftmost bits is the row (0-7) and the rightmost four bits is the column (0-7)
	 * @return a cached position
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public static ImmutablePosition from(final byte pos)
	{
		int row = pos >> ROW_OFFSET;
		int column = pos & COLUMN_MASK;
		return POSITIONS[row][column];
	}

	/**
	 * @param position a string like "1A" or "8H"
	 * @return a cached position
	 * @throws IndexOutOfBoundsException if the given string is less than 2 chars
	 * @throws NumberFormatException if the first digit in the string isn't a number
	 * @throws NullPointerException if the given string is null
	 */
	public static ImmutablePosition position(final String position)
	{
		return POSITIONS[Integer.parseInt(position.substring(0, 1)) - 1][position.charAt(1) - 'A'];
	}

	/**
	 * This is the preferred way of creating positions that isn't going to change and where you know that the input row/column are valid
	 * <br><b>Note:</b> No input validation is made, if that is wanted you should use {@link ImmutablePosition#of(int, int)} instead
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7 (A-H)
	 * @return a cached position that won't change during the lifetime of the application
	 * @throws ArrayIndexOutOfBoundsException if one or both of the given parameters are out of range
	 */
	public static ImmutablePosition from(final int row, final int column)
	{
		return POSITIONS[row][column];
	}

	/**
	 * <br><b>Note:</b> No input validation is made, if that is wanted you should use {@link ImmutablePosition#of(int, int)} instead
	 * @param row the row for this position, valid numbers are 1-8
	 * @param column the column for this position, valid numbers are 0-7 (A-H) (Use: {@link Position#A} etc)
	 * @return a cached position that won't change during the lifetime of the application
	 * @throws ArrayIndexOutOfBoundsException if one or both of the given parameters are out of range
	 */
	public static ImmutablePosition position(final int row, final int column)
	{
		return POSITIONS[row - 1][column];
	}

	/**
	 * This is like {@link ImmutablePosition#from(int, int)} but with added error checking
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7 (A-H)
	 * @return a cached position that won't change during the lifetime of the application
	 * @throws InvalidPosition if one or both of the given parameters are out of range
	 */
	public static ImmutablePosition of(final int row, final int column) throws InvalidPosition
	{
		if(isInvalidPosition(row, column))
		{
			throw new InvalidPosition(row, column);
		}
		return from(row, column);
	}

	/**
	 * 
	 * @return a newly created mutable version of this position
	 */
	public MutablePosition asMutable()
	{
		return MutablePosition.from(getRow(), getColumn());
	}

	/**
	 * 
	 * @param pos the position to return a "copy" for
	 * @return a cached position that won't change during the lifetime of the application
	 * @throws ArrayIndexOutOfBoundsException if the given position contains a row/column that isn't within the 0-7 range
	 * @throws NullPointerException if the given position is null
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

	public ImmutablePosition up()
	{
		return from(getRow() + 1, getColumn());
	}

	public ImmutablePosition down()
	{
		return from(getRow() - 1, getColumn());
	}

	public ImmutablePosition right()
	{
		return from(getRow(), getColumn() + 1);
	}

	public ImmutablePosition left()
	{
		return from(getRow(), getColumn() - 1);
	}
}
