package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;

public class Rock extends Piece
{

	public Rock(Position startingPosition, boolean affinity)
	{
		super(startingPosition, affinity);
	}

	@Override
	public void addPossibleMoves()
	{
		addMoveChain(Move.NO_CHANGE, Move.LEFT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.UP, Move.NO_CHANGE, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.NO_CHANGE, Move.RIGHT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.DOWN, Move.NO_CHANGE, ChessBoard.BOARD_SIZE);
	}

	@Override
	public int getValue()
	{
		return Piece.TOWER_VALUE;
	}

	@Override
	public String getPieceName()
	{
		return "Rock";
	}

	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.ROCK;
	}
}
