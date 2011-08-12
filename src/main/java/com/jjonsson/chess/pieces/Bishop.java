package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.ChessBoard.MOVES_IN_ONE_DIRECTION;
import static com.jjonsson.chess.moves.Move.DOWN;
import static com.jjonsson.chess.moves.Move.LEFT;
import static com.jjonsson.chess.moves.Move.RIGHT;
import static com.jjonsson.chess.moves.Move.UP;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.MutablePosition;

public class Bishop extends Piece
{

	public Bishop(final MutablePosition startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	@Override
	public void addPossibleMoves()
	{
		addMoveChain(UP, LEFT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(UP, RIGHT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(DOWN, RIGHT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(DOWN, LEFT, MOVES_IN_ONE_DIRECTION);
	}

	@Override
	public int getValue()
	{
		return Piece.BISHOP_VALUE;
	}

	@Override
	public String getPieceName()
	{
		return "Bishop";
	}

	@Override
	protected byte getPersistenceIdentifierType()
	{
		return Piece.BISHOP;
	}
}
