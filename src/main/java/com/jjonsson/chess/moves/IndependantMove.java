package com.jjonsson.chess.moves;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.pieces.Piece;

/**
 * A move that isn't dependant on another move of the piece that it belongs to
 * @author jonatanjoensson
 *
 */
public abstract class IndependantMove extends Move
{

	public IndependantMove(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
	}

	@Override
	public void updatePossibility(final ChessBoard board, final boolean updatePieceAtDestination)
	{
		boolean oldCanBeMade = myCanBeMadeCache;

		if(updatePieceAtDestination)
		{
			setOldPieceAtDestination(getPieceAtDestination());
			if(getDestination() != null)
			{
				setPieceAtDestination(board.getPiece(getDestination()));
			}
			else
			{
				setPieceAtDestination(null);
			}
		}

		myCanBeMadeCache = canBeMadeInternal(board);
		if(oldCanBeMade != myCanBeMadeCache || getDestination() != getPreviousDestination() || resyncNeeded())
		{
			if(oldCanBeMade)
			{
				board.removeAvailableMove(getPreviousDestination(), getPiece(), this);
			}
			else
			{

				board.removeNonAvailableMove(getPreviousDestination(), getPiece(), this);
			}
			if(myCanBeMadeCache)
			{
				//The move is now possible
				board.addAvailableMove(getDestination(), getPiece(), this);
			}
			else
			{
				//The move isn't possible anymore
				board.addNonAvailableMove(getDestination(), getPiece(), this);
			}
			//Make sure that the previous destination is saved
			updatePreviousDestination();
			resyncWasMade();
		}
	}

	@Override
	public boolean isPieceBlockingMe(final Position ignoreIfPositionIsBlocked, final Position ignoreIfPositionIsBlocked2)
	{
		return false;
	}
}
