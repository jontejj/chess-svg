package com.jjonsson.chess.pieces;

import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;

public class BlackPawn extends Piece
{

	private static final byte	BLACK_PAWN_STARTING_ROW	= 6;

	public BlackPawn(Position startingPosition)
	{
		super(startingPosition, Piece.BLACK);
	}

	@Override
	public void addPossibleMoves()
	{
		PawnMove step1 = new PawnMove(1 * Move.DOWN, 0, this, null, null);
		//If this isn't true then the pawn already has moved from it's initial position and the 2-step move shouln't be available then
		if(getCurrentPosition().getRow() == BLACK_PAWN_STARTING_ROW)
		{
			PawnMove step2 = new PawnMove(2 * Move.DOWN, 0, this, null, step1);
			step1.setMoveThatDependsOnMe(step2);
		}
		
		addPossibleMove(step1);
		addPossibleMove(new PawnTakeOverMove(1 * Move.DOWN, 1 * Move.LEFT, this, null, null));
		addPossibleMove(new PawnTakeOverMove(1 * Move.DOWN, 1 * Move.RIGHT, this, null, null));
	}

	@Override
	public int getValue()
	{
		return Piece.PAWN_VALUE;
	}

	@Override
	public String getPieceName()
	{
		return "Pawn";
	}

	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.PAWN;
	}

}
