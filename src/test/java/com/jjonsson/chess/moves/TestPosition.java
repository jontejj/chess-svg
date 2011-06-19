package com.jjonsson.chess.moves;

import static junit.framework.Assert.fail;

import java.util.HashMap;

import org.junit.Test;

import com.google.common.collect.Maps;
import com.jjonsson.chess.exceptions.InvalidPosition;


public class TestPosition
{
	@Test
	public void testHashCode()
	{
		HashMap<Position, Position> test = Maps.newHashMap();
		for(byte r = 0;r<8; r++)
		{
			for(byte c = 0;c<8; c++)
			{
				Position p = new Position(r, c);
				Position before = test.put(p, p);
				if(before != null && !before.equals(p))
				{
					//We have two different positions that gives us the same hashCode even though equals doesn't return true
					fail();
				}
			}
		}
	}
	
	@Test (expected = InvalidPosition.class)
	public void testBadPosition() throws InvalidPosition
	{
		Position.createPosition(40, 2);
	}
}
