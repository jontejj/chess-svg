package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import static com.jjonsson.chess.moves.Move.*;
import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnOneStepMove;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.Position;

public class WhitePawn extends Pawn
{
	private static final byte WHITE_PAWN_STARTING_ROW = 1;
	public WhitePawn(Position startingPosition, ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, WHITE, boardPieceIsToBePlacedOn);
	}

	@Override
	public void addPossibleMoves()
	{
		PawnMove step1 = new PawnOneStepMove(1 * UP, 0, this, null, null);
		PawnMove step2 = new PawnTwoStepMove(2 * UP, 0, this, null, step1);
		//If this isn't true then the pawn already has moved from it's initial position and the 2-step move shouln't be available then
		if(isAtStartingRow())
		{
			step1.setMoveThatDependsOnMe(step2);
		}
		
		addPossibleMove(step1);
		addPossibleMove(new PawnTakeOverMove(1 * UP, 1 * LEFT, this, null, null));
		addPossibleMove(new PawnTakeOverMove(1 * UP, 1 * RIGHT, this, null, null));
	}

	@Override
	protected boolean isAtStartingRow()
	{
		return getCurrentPosition().getRow() == WHITE_PAWN_STARTING_ROW;
	}
	
	//The pawns are worth more as they approach their top/bottom goal
	@Override
	public int getValue()
	{
		return Piece.PAWN_VALUE + (getCurrentPosition().getRow() * PAWN_VALUE_INCREASE_PER_ROW);
	}
}
