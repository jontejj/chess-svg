package com.jjonsson.chess.pieces;

import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnOneStepMove;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.Position;

public class BlackPawn extends Pawn
{

	private static final byte	BLACK_PAWN_STARTING_ROW	= 6;

	public BlackPawn(Position startingPosition)
	{
		super(startingPosition, Piece.BLACK);
	}

	@Override
	public void addPossibleMoves()
	{
		PawnMove step1 = new PawnOneStepMove(1 * Move.DOWN, 0, this, null, null);
		PawnMove step2 = new PawnTwoStepMove(2 * Move.DOWN, 0, this, null, step1);
		//If this isn't true then the pawn already has moved from it's initial position and the 2-step move shouln't be available then
		if(isAtStartingRow())
		{
			step1.setMoveThatDependsOnMe(step2);
		}
		
		addPossibleMove(step1);
		addPossibleMove(new PawnTakeOverMove(1 * Move.DOWN, 1 * Move.LEFT, this, null, null));
		addPossibleMove(new PawnTakeOverMove(1 * Move.DOWN, 1 * Move.RIGHT, this, null, null));
	}
	
	@Override
	protected boolean isAtStartingRow()
	{
		return getCurrentPosition().getRow() == BLACK_PAWN_STARTING_ROW;
	}
}
