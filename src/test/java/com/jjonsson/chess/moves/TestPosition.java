package com.jjonsson.chess.moves;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import java.util.HashMap;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.jjonsson.chess.exceptions.InvalidPosition;


public class TestPosition
{
	
	/**
	 * Make sure that two different positions gives us different hashCodes (and that we avoid hash collisions)
	 */
	@Test
	public void testHashCode()
	{
		HashMap<Position, Position> test = Maps.newHashMap();
		for(byte r = 1;r<=9; r++)
		{
			for(byte c = 1;c<=9; c++)
			{
				
				Position p;
				try
				{
					p = Position.createPosition(r, c);
					assertNull(test.put(p, p));
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
