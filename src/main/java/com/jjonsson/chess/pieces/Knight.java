package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.KnightMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;

public class Knight extends Piece 
{

	public Knight(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn) 
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
		addPossibleMove(new KnightMove(1 * Move.UP, 2 * Move.LEFT, this)); //Up 1 left 2
		addPossibleMove(new KnightMove(2 * Move.UP, 1 * Move.LEFT, this)); //Up 2 left 1
		addPossibleMove(new KnightMove(2 * Move.UP, 1 * Move.RIGHT, this)); //Up 2 right 1
		addPossibleMove(new KnightMove(1 * Move.UP, 2 * Move.RIGHT, this)); //Up 1 right 2
		addPossibleMove(new KnightMove(1 * Move.DOWN, 2 * Move.RIGHT, this)); //Down 1 right 2
		addPossibleMove(new KnightMove(2 * Move.DOWN, 1 * Move.RIGHT, this)); //Down 2 right 1
		addPossibleMove(new KnightMove(2 * Move.DOWN, 1 * Move.LEFT, this)); //Down 2 left 1
		addPossibleMove(new KnightMove(1 * Move.DOWN, 2 * Move.LEFT, this)); //Down 1 left 2
	}

	@Override
	public String getPieceName()
	{
		return "Knight";
	}
	
	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.KNIGHT;
	}

}
