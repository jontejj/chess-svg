package com.jjonsson.chess.moves;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

/**
 * A move that isn't dependant on another move of the piece that it belongs to 
 * @author jonatanjoensson
 *
 */
public abstract class IndependantMove extends Move
{

	public IndependantMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}
	
	public void updatePossibility(ChessBoard board)
	{
		myOldCanBeMadeCache = myCanBeMadeCache;
		myCanBeMadeCache = canBeMadeInternal(board);
		
		if(myCanBeMadeCache)
		{
			if(myDestination == null)
			{
				System.out.println(this + "Shouldn't be possible to do");
			}
			//The move is now possible
			board.addAvailableMove(myDestination, myPiece, this);
		}
		else
		{
			board.addNonAvailableMove(myDestination, myPiece, this);
		}
	}
	
	public boolean isPieceBlockingMe(Position ignoreIfPositionIsBlocked, Position ignoreIfPositionIsBlocked2)
	{
		return false;
	}
}
