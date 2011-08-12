package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.moves.Move.DOWN;
import static com.jjonsson.chess.moves.Move.LEFT;
import static com.jjonsson.chess.moves.Move.RIGHT;
import static com.jjonsson.chess.moves.Move.UP;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.KnightMove;
import com.jjonsson.chess.moves.MutablePosition;

public class Knight extends Piece
{

	public Knight(final MutablePosition startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	@Override
	public int getValue()
	{
		return Piece.KNIGHT_VALUE;
	}

	@Override
	public void addPossibleMoves()
	{
		addPossibleMove(new KnightMove(1 * UP, 2 * LEFT, this)); //Up 1 left 2
		addPossibleMove(new KnightMove(2 * UP, 1 * LEFT, this)); //Up 2 left 1
		addPossibleMove(new KnightMove(2 * UP, 1 * RIGHT, this)); //Up 2 right 1
		addPossibleMove(new KnightMove(1 * UP, 2 * RIGHT, this)); //Up 1 right 2
		addPossibleMove(new KnightMove(1 * DOWN, 2 * RIGHT, this)); //Down 1 right 2
		addPossibleMove(new KnightMove(2 * DOWN, 1 * RIGHT, this)); //Down 2 right 1
		addPossibleMove(new KnightMove(2 * DOWN, 1 * LEFT, this)); //Down 2 left 1
		addPossibleMove(new KnightMove(1 * DOWN, 2 * LEFT, this)); //Down 1 left 2
	}

	@Override
	public String getPieceName()
	{
		return "Knight";
	}

	@Override
	protected byte getPersistenceIdentifierType()
	{
		return Piece.KNIGHT;
	}

	@Override
	public int getFirstDimensionMaxIndex()
	{
		return 0;
	}
}
