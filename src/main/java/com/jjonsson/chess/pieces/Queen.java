package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.board.ChessBoard.MOVES_IN_ONE_DIRECTION;
import static com.jjonsson.chess.moves.Move.DOWN;
import static com.jjonsson.chess.moves.Move.LEFT;
import static com.jjonsson.chess.moves.Move.NO_CHANGE;
import static com.jjonsson.chess.moves.Move.RIGHT;
import static com.jjonsson.chess.moves.Move.UP;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.moves.MutablePosition;

public class Queen extends Piece
{

	public Queen(final MutablePosition startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
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
	protected byte getPersistenceIdentifierType()
	{
		return Piece.QUEEN;
	}

}
