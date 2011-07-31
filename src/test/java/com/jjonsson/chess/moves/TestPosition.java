package com.jjonsson.chess.moves;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.jjonsson.chess.ChessBoard;
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
		for(byte r = 1;r<=ChessBoard.BOARD_SIZE + 1; r++)
		{
			for(byte c = 1;c<=ChessBoard.BOARD_SIZE + 1; c++)
			{
				
				Position p;
				try
				{
					p = Position.createPosition(r, c);
					assertTrue(test.add(p.hashCode()));
				}
				catch (InvalidPosition e)
				{
					assertTrue(Position.isInvalidPosition((byte)(r-1), (byte)(c-1)));
					assertNotNull(e.toString());
				}
			}
		}
	}
	
	@Test
	public void testPositionTraversal() throws InvalidPosition
	{
		Position whiteRightTower = Position.createPosition(1, Position.A);
		Position whiteRightKnight = Position.createPosition(1, Position.B);
		assertEquals(whiteRightKnight, whiteRightTower.up().right().right().down().left());
	}
}
