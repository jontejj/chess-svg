package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.moves.Move.LEFT;
import static com.jjonsson.chess.moves.Move.RIGHT;
import static com.jjonsson.chess.moves.Move.UP;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnOneStepMove;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.PawnTwoStepMove;

public class WhitePawn extends Pawn
{
	private static final byte WHITE_PAWN_STARTING_ROW = 1;
	public WhitePawn(final MutablePosition startingPosition, final ChessBoard boardPieceIsToBePlacedOn)
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
