package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.PawnOneStepMove;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.Position;

public abstract class Pawn extends Piece
{

	private PawnTwoStepMove myTwoStepMove;
	private PawnOneStepMove myOneStepMove;
	
	public Pawn(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	public void setOneStepMove(PawnOneStepMove oneStepMove)
	{
		myOneStepMove = oneStepMove;
	}
	
	public void setTwoStepMove(PawnTwoStepMove move)
	{
		myTwoStepMove = move;
	}

	@Override
	public int getValue()
	{
		return Piece.PAWN_VALUE;
	}

	@Override
	public String getPieceName()
	{
		return "Pawn";
	}
	
	@Override
	protected byte getPersistanceIdentifierType()
	{
		return Piece.PAWN;
	}
	
	public void removeTwoStepMove(ChessBoard board)
	{
		if(!myTwoStepMove.isRemoved())
		{
			myOneStepMove.setMoveThatDependsOnMe(null);
			myTwoStepMove.removeFromBoard(board);
		}
	}
	
	protected abstract boolean isAtStartingRow();
	
	@Override
	public void revertedAMove(ChessBoard board)
	{
		if(isAtStartingRow())
		{
			if(myTwoStepMove.isRemoved())
			{
				//The two step move can now be re-enabled
				myTwoStepMove.reEnable();
				myOneStepMove.setMoveThatDependsOnMe(myTwoStepMove);
				myTwoStepMove.updateMove(board);
			}
		}
	}
}
