package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import static com.jjonsson.chess.ChessBoard.MOVES_IN_ONE_DIRECTION;
import static com.jjonsson.chess.moves.Move.*;
import com.jjonsson.chess.moves.Position;

public class Bishop extends Piece
{

	public Bishop(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
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
	protected byte getPersistanceIdentifierType()
	{
		return Piece.BISHOP;
	}
}
