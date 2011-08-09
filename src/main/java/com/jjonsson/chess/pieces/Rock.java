package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.ChessBoard.MOVES_IN_ONE_DIRECTION;
import static com.jjonsson.chess.moves.Move.DOWN;
import static com.jjonsson.chess.moves.Move.LEFT;
import static com.jjonsson.chess.moves.Move.NO_CHANGE;
import static com.jjonsson.chess.moves.Move.RIGHT;
import static com.jjonsson.chess.moves.Move.UP;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Position;

public class Rock extends Piece
{

	public Rock(final Position startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	@Override
	public void addPossibleMoves()
	{
		addMoveChain(NO_CHANGE, LEFT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(UP, NO_CHANGE, MOVES_IN_ONE_DIRECTION);
		addMoveChain(NO_CHANGE, RIGHT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(DOWN, NO_CHANGE, MOVES_IN_ONE_DIRECTION);
	}

	@Override
	public int getValue()
	{
		return Piece.ROCK_VALUE;
	}

	@Override
	public String getPieceName()
	{
		return "Rock";
	}

	@Override
	protected byte getPersistanceIdentifierType()
	{
		if(getMovesMade() > 0)
		{
			return Piece.MOVED_ROCK;
		}
		return Piece.ROCK;
	}
}
