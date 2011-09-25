package com.jjonsson.chess.moves;


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
	 * <br><b>Note:</b> No input validation is made.
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column for this position, valid numbers are 0-7 (A-H)
	 * @return a newly created mutable position
	 */
	public static MutablePosition from(final int row, final int column)
	{
		return new MutablePosition((byte)row, (byte)column);
	}

	/**
	 * This is the preferred way of creating positions where you know that the input row/column are valid
	 * <br><b>Note:</b> No input validation is made.
	 * @param row the row for this position, valid numbers are 0-7
	 * @param column the column
	 * @return a newly created mutable position
	 */
	public static MutablePosition from(final int row, final Column column)
	{
		return new MutablePosition((byte)row, column.getValue());
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
