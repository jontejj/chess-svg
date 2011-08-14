package com.jjonsson.chess.moves;

import static com.jjonsson.chess.moves.ImmutablePosition.of;
import static com.jjonsson.chess.moves.ImmutablePosition.position;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;


public class TestPosition
{

	/**
	 * Make sure that two different positions gives us different hashCodes (and that we avoid hash collisions)
	 */
	@Test
	public void testHashCode()
	{
		Set<Integer> test = Sets.newHashSet();
		for(byte r = 0;r<=ChessBoard.BOARD_SIZE + 1; r++)
		{
			for(byte c = 0;c<=ChessBoard.BOARD_SIZE + 1; c++)
			{

				Position p;
				try
				{
					p = of(r, c);
					assertTrue(test.add(p.hashCode()));
				}
				catch (InvalidPosition e)
				{
					assertTrue(Position.isInvalidPosition(r, c));
					assertNotNull(e.toString());
				}
			}
		}
	}

	@Test
	public void testPositionTraversal()
	{
		ImmutablePosition whiteRightTower = position("1A");
		ImmutablePosition whiteRightKnight = position("1B");
		assertEquals(whiteRightKnight, whiteRightTower.up().right().right().down().left());
	}

	private static final byte FOUR_D = (byte) 0x33;

	@Test
	public void testPersistence()
	{
		ImmutablePosition position = ImmutablePosition.from(FOUR_D);
		assertEquals(FOUR_D, position.getPersistence());
		assertTrue(position.equals(position("4D")));
	}
}
