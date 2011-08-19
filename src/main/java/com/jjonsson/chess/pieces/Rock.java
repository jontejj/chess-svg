package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.board.ChessBoard.MOVES_IN_ONE_DIRECTION;
import static com.jjonsson.chess.moves.Move.DOWN;
import static com.jjonsson.chess.moves.Move.LEFT;
import static com.jjonsson.chess.moves.Move.NO_CHANGE;
import static com.jjonsson.chess.moves.Move.RIGHT;
import static com.jjonsson.chess.moves.Move.UP;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.moves.CastlingMove;
import com.jjonsson.chess.moves.MutablePosition;

public class Rock extends Piece
{

	private CastlingMove myCastlingMove;

	public Rock(final MutablePosition startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
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

	public void setCastlingMove(final CastlingMove move)
	{
		myCastlingMove = move;
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
	protected byte getPersistenceIdentifierType()
	{
		if(getMovesMade() > 0)
		{
			return Piece.MOVED_ROCK;
		}
		return Piece.ROCK;
	}

	@Override
	public int expectedNumberOfPossibleMoves()
	{
		return 4;
	}

	@Override
	protected void removeMovesFromBoard(final ChessBoard chessBoard)
	{
		//TODO: add the move back to the kings possible moves when this piece is revived
		super.removeMovesFromBoard(chessBoard);
		if(myCastlingMove != null)
		{
			myCastlingMove.disable(chessBoard);
		}
	}
}
