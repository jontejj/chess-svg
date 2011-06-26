package com.jjonsson.chess.evaluators.orderings;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;

/**
 * Orders moves by how near the center of the board they are (Often considered a good heuristic in chess)
 * <br>Note: This throws NullPointerException if the destination of the moves to order is null
 * @author jonatanjoensson
 */
public class CenterStageOrdering extends Ordering<Move>
{

	static int center;
	static 
	{
		center = ChessBoard.BOARD_SIZE / 2 - 1;
	}
	
	@Override
	public int compare(Move left, Move right)
	{
		Position rightDestination = left.getPositionIfPerformed();
		Position leftDestination = right.getPositionIfPerformed();
		
		return Math.abs(leftDestination.getRow() - center) - Math.abs(rightDestination.getRow() - center) +
		(Math.abs(leftDestination.getColumn() - center) - Math.abs(rightDestination.getColumn() - center));
	}

}
