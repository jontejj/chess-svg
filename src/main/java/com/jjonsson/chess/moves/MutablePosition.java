package com.jjonsson.chess.moves;

import com.jjonsson.chess.exceptions.InvalidPosition;

public final class MutablePosition extends Position
{

	private MutablePosition(final byte row, final byte column)
	{
		super(row, column);
	}


	/**
	 * Uses the row/column change of the given move and applies it to this position
	 * <br><b>Note</b>: after this operation this position may be invalid
	 * @param move
	 */
	public void applyMove(final Move move)
	{
		addRowChange(move.getRowChange());
		addColumnChange(move.getColumnChange());
	}

	@Override
	public MutablePosition copy()
	{
		return new MutablePosition(getRow(), getColumn());
	}

	/**
	 * This is the preferred way of creating positions where you know that the input row/column are valid
	 * <br><b>Note:</b> No input validation is made, if that is wanted you should use {@link MutablePosition#of(int, int)} instead
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7 (A-H)
	 * @return a newly created mutable position
	 */
	public static MutablePosition from(final int row, final int column)
	{
		return new MutablePosition((byte)row, (byte)column);
	}

	/**
	 * <br><b>Note:</b> No input validation is made, if that is wanted you should use {@link MutablePosition#of(int, int)} instead
	 * @param row the row for this position, valid numbers are 1-8
	 * @param column the column for this position, valid numbers are 0-7 (A-H) (Use: {@link Position#A} etc)
	 * @return a newly created mutable position
	 */
	public static MutablePosition position(final int row, final int column)
	{
		return from(row - 1, column);
	}

	/**
	 * This is like {@link MutablePosition#from(int, int)} but with added error checking
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7 (A-H)
	 * @return a newly created mutable position
	 * @throws InvalidPosition if one or both of the given parameters are out of range
	 */
	public static MutablePosition of(final int row, final int column) throws InvalidPosition
	{
		if(isInvalidPosition(row, column))
		{
			throw new InvalidPosition(row, column);
		}
		return from(row, column);
	}
	/**
	 * @param persistenceData a byte where the four leftmost bits is the row (0-7) and the rightmost four bits is the column (0-7)
	 * @return a Mutable position
	 * @throws ArrayIndexOutOfBoundsException if the column or row is out of range
	 */
	public static MutablePosition from(final byte persistenceData)
	{
		ImmutablePosition pos = ImmutablePosition.from(persistenceData);
		return new MutablePosition(pos.getRow(), pos.getColumn());
	}
}
