package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Move;
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
		addMoveChain(Move.UP, Move.LEFT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.UP, Move.RIGHT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.DOWN, Move.RIGHT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.DOWN, Move.LEFT, ChessBoard.BOARD_SIZE);
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
