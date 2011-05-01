package com.jjonsson.chess.pieces;

import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Position;

public class WhitePawn extends Piece
{
	private static final byte WHITE_PAWN_STARTING_ROW = 1;
	public WhitePawn(Position startingPosition)
	{
		super(startingPosition, Piece.WHITE);
	}

	@Override
	void addPossibleMoves()
	{
		PawnMove step1 = new PawnMove(1 * Move.UP, 0, this, null, null);
		//If this isn't true then the pawn already has moved from it's initial position and the 2-step move shouln't be available then
		if(getCurrentPosition().getRow() == WHITE_PAWN_STARTING_ROW)
		{
			PawnMove step2 = new PawnMove(2 * Move.UP, 0, this, null, step1);
			step1.setMoveThatDependsOnMe(step2);
		}
		
		addPossibleMove(step1);
		addPossibleMove(new PawnTakeOverMove(1 * Move.UP, 1 * Move.LEFT, this, null, null));
		addPossibleMove(new PawnTakeOverMove(1 * Move.UP, 1 * Move.RIGHT, this, null, null));
	}

	@Override
	public int getValue()
	{
		return Piece.PAWN_VALUE;
	}

	@Override
	protected String getPieceName()
	{
		return "Pawn";
	}
	
	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.PAWN;
	}

}
