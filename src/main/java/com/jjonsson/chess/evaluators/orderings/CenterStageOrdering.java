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

	private static final int CENTER = ChessBoard.BOARD_SIZE / 2 - 1;

	@Override
	public int compare(final Move left, final Move right)
	{
		Position rightDestination = left.getDestination();
		Position leftDestination = right.getDestination();
		//TODO: null checks shouldn't have to be here since only available moves (which has a destination) should be sorted
		if(rightDestination == null && leftDestination == null)
		{
			return 0;
		}
		else if(rightDestination == null)
		{
			return 1;
		}
		else if(leftDestination == null)
		{
			return -1;
		}
		return Math.abs(leftDestination.getRow() - CENTER) - Math.abs(rightDestination.getRow() - CENTER) +
		(Math.abs(leftDestination.getColumn() - CENTER) - Math.abs(rightDestination.getColumn() - CENTER));
	}

}
