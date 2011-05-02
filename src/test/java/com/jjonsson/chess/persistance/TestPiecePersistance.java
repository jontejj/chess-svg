package com.jjonsson.chess.persistance;

import static org.junit.Assert.*;

import org.junit.Test;

import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Rock;

public class TestPiecePersistance
{

	@Test
	public void testGetPieceFromPersistanceData()
	{
		Position kingPos = Position.createPosition(5, Position.D);
		King k = new King(kingPos, Piece.BLACK);
		short p = k.getPersistanceData();
		Piece k2 = Piece.getPieceFromPersistanceData(p);
		assertTrue("Saved piece doesn't match the read one", k.same(k2));
	
		Position rockPos = Position.createPosition(8, Position.H);
		Rock r = new Rock(rockPos, Piece.WHITE);
		short p1 = r.getPersistanceData();
		Piece r2 = Piece.getPieceFromPersistanceData(p1);
		assertTrue("Saved piece doesn't match the read one", r.same(r2));
	}

}
