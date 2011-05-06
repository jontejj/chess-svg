package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Pawn;
import com.jjonsson.chess.pieces.Piece;

public class PawnTwoStepMove extends PawnMove
{

	public PawnTwoStepMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
		
		if(pieceThatTheMoveWillBeMadeWith instanceof Pawn)
		{
			((Pawn)pieceThatTheMoveWillBeMadeWith).setTwoStepMove(this);
		}
		else
			throw new IllegalArgumentException("Piece to move must be a pawn, was a " + pieceThatTheMoveWillBeMadeWith);
	}
	
	/**
	 * This is the 2-step move for a pawn and when it has been made, it won't be possible again
	 * @see com.jjonsson.chess.moves.PawnMove#makeMove(com.jjonsson.chess.ChessBoard)
	 */
	@Override
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		super.makeMove(board);
			
		//Remove the possibility of this move
		this.removeMove(board);
	}

	public void possibleAgain(ChessBoard board)
	{
		getMoveThatIDependUpon().setMoveThatDependsOnMe(this);
		updateMove(board);
	}
}
