package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.ChessBoard.MOVES_IN_ONE_DIRECTION;
import static com.jjonsson.chess.moves.Move.*;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Position;

public class Queen extends Piece
{

	public Queen(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	@Override
	public void addPossibleMoves()
	{
		addMoveChain(NO_CHANGE, LEFT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(UP, LEFT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(UP, NO_CHANGE, MOVES_IN_ONE_DIRECTION);
		addMoveChain(UP, RIGHT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(NO_CHANGE, RIGHT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(DOWN, RIGHT, MOVES_IN_ONE_DIRECTION);
		addMoveChain(DOWN, NO_CHANGE, MOVES_IN_ONE_DIRECTION);
		addMoveChain(DOWN, LEFT, MOVES_IN_ONE_DIRECTION);
	}

	@Override
	public int getValue()
	{
		return Piece.QUEEN_VALUE;
	}

	@Override
	public String getPieceName()
	{
		return "Queen";
	}
	
	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.QUEEN;
	}

}
