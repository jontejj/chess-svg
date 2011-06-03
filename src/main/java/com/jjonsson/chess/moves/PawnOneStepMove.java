package com.jjonsson.chess.moves;

import com.jjonsson.chess.pieces.Pawn;
import com.jjonsson.chess.pieces.Piece;

public class PawnOneStepMove extends PawnMove
{

	public PawnOneStepMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
		
		Pawn.class.cast(pieceThatTheMoveWillBeMadeWith).setOneStepMove(this);
	}
}
