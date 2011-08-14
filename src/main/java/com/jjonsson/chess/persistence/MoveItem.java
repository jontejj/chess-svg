package com.jjonsson.chess.persistence;

import java.nio.ByteBuffer;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Move;

public final class MoveItem
{
	private ImmutablePosition myFromPosition;
	private ImmutablePosition myToPosition;

	private MoveItem(final ImmutablePosition fromPosition, final ImmutablePosition toPosition)
	{
		myFromPosition = fromPosition;
		myToPosition = toPosition;
	}
	/**
	 * Uses the old position and the current position of the given move
	 * @param moveToConstructFrom the last move that was made on a board
	 */
	public static MoveItem from(final Move moveToConstructFrom)
	{
		return new MoveItem(moveToConstructFrom.getCurrentPosition(), moveToConstructFrom.getDestination());
	}
	public static MoveItem from(final ImmutablePosition fromPosition, final ImmutablePosition toPosition)
	{
		return new MoveItem(fromPosition, toPosition);
	}

	public void put(final ByteBuffer buffer)
	{
		buffer.put(myFromPosition.getPersistence());
		buffer.put(myToPosition.getPersistence());
	}

	public void perform(final ChessBoard board) throws UnavailableMoveException, UnavailableMoveItem
	{
		board.move(myFromPosition, myToPosition);
	}

	@Override
	public String toString()
	{
		return myFromPosition + " -> " + myToPosition;
	}
}
