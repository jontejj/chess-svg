package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import static com.jjonsson.chess.ChessBoard.MOVES_IN_ONE_DIRECTION;
import static com.jjonsson.chess.moves.Move.*;
import com.jjonsson.chess.moves.Position;

public class Rock extends Piece
{

	public Rock(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
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
			return Piece.MOVED_ROCK;
		return Piece.ROCK;
	}
}
