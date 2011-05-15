package com.jjonsson.chess.moves.ordering;

import com.google.common.collect.Ordering;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;

/**
 * Orders moves by how near the center of the board they are (Often considered a good heuristic in chess)
 * @author jonatanjoensson
 *
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
		if(rightDestination == null)
			return -1;
		Position leftDestination = right.getPositionIfPerformed();
		if(leftDestination == null)
			return 1;
		
		return Math.abs(leftDestination.getRow() - center) - Math.abs(rightDestination.getRow() - center);
	}

}
