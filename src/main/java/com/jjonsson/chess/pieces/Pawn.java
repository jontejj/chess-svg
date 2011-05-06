package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnTwoStepMove;
import com.jjonsson.chess.moves.Position;

public abstract class Pawn extends Piece
{

	private PawnTwoStepMove myTwoStepMove;
	
	public Pawn(Position startingPosition, boolean affinity)
	{
		super(startingPosition, affinity);
	}
	
	public void setTwoStepMove(PawnTwoStepMove move)
	{
		myTwoStepMove = move;
	}
	
	public PawnTwoStepMove getTwoStepMove()
	{
		return myTwoStepMove;
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
		for(Move m : getPossibleMoves())
		{
			if(m instanceof PawnMove)
			{
				PawnMove pm = (PawnMove)m;
				if(pm.getMoveDependingOnMe() != null)
				{
					pm.getMoveDependingOnMe().removeMove(board);
					pm.setMoveThatDependsOnMe(null);
				}
			}
		}
	}
	
	protected abstract boolean isAtStartingRow();
	
	@Override
	public void revertedAMove(ChessBoard board)
	{
		if(isAtStartingRow())
			getTwoStepMove().possibleAgain(board);
	}
}
