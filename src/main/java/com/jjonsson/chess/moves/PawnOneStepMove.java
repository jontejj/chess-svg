package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Piece;

public class PawnOneStepMove extends PawnMove
{

	public PawnOneStepMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
	}
	
	
	/**
	 * This is the 1-step/take over move for a pawn, this means that the 2-step move shouldn't be available anymore
	 * @see com.jjonsson.chess.moves.PawnMove#makeMove(com.jjonsson.chess.ChessBoard)
	 */
	@Override
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		super.makeMove(board);
			
		//Remove the possibility of the 2 step move
		if(getMoveDependingOnMe() != null)
			getMoveDependingOnMe().removeMove(board);
	}
}
