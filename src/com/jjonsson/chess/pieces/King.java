package com.jjonsson.chess.pieces;

import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Position;

public class King extends Piece 
{
	
	/**
	 * 
	 * @param startingPosition where this king should be placed
	 * @param affinity true if this piece belongs to the black player false otherwise
	 */
	public King(Position startingPosition, boolean affinity)
	{
		super(startingPosition, affinity);
	}

	@Override
	public int getValue() 
	{
		return Piece.KING_VALUE;
	}

	void addPossibleMoves()
	{
		addPossibleMove(new KingMove(1, -1, this)); //Up left
		addPossibleMove(new KingMove(1, 0, this)); //Up
		addPossibleMove(new KingMove(1, 1, this)); //Up Right
		addPossibleMove(new KingMove(0, 1, this)); //Right
		addPossibleMove(new KingMove(-1, 1, this)); //Down Right
		addPossibleMove(new KingMove(-1, 0, this)); //Down
		addPossibleMove(new KingMove(-1, -1, this)); //Down left
		addPossibleMove(new KingMove(0, -1, this)); //Left
	}

	@Override
	protected String getPieceName()
	{
		return "King";
	}
	
	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.KING;
	}
}
