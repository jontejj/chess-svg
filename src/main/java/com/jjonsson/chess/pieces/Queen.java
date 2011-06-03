package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Move;
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
		addMoveChain(Move.NO_CHANGE, Move.LEFT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.UP, Move.LEFT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.UP, Move.NO_CHANGE, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.UP, Move.RIGHT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.NO_CHANGE, Move.RIGHT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.DOWN, Move.RIGHT, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.DOWN, Move.NO_CHANGE, ChessBoard.BOARD_SIZE);
		addMoveChain(Move.DOWN, Move.LEFT, ChessBoard.BOARD_SIZE);
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
