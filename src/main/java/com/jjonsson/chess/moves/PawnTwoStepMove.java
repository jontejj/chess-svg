package com.jjonsson.chess.moves;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Pawn;
import com.jjonsson.chess.pieces.Piece;

public class PawnTwoStepMove extends PawnMove
{

	public PawnTwoStepMove(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith, final DependantMove moveDependingOnMe, final DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);

		Pawn.class.cast(pieceThatTheMoveWillBeMadeWith).setTwoStepMove(this);
	}

	/**
	 * {@inheritDoc}
	 * This may also add the possibility for an en-passant move for the other player.
	 * @throws UnavailableMoveException  if this move isn't available right now
	 */
	@Override
	public void makeMove(final ChessBoard board) throws UnavailableMoveException
	{
		super.makeMove(board);
		performEnpassantPossibilitySync(board, getPiece(), false);
	}

	/**
	 * 
	 * @param board
	 * @param pieceAtEnPassantDestination
	 * @param remove true if this sync is supposed to remove possible en-passant moves, false if they should be added
	 */
	private void performEnpassantPossibilitySync(final ChessBoard board, final Piece pieceAtEnPassantDestination, final boolean remove)
	{
		Collection<Move> moves = null;
		if(remove)
		{
			moves = board.getAvailableMoves(getEnpassantPosition(), !getAffinity());
		}
		else
		{
			moves = board.getNonAvailableMoves(getEnpassantPosition(), !getAffinity());
		}
		ImmutableSet<Move> otherTakeOverMoves = ImmutableSet.copyOf(moves);
		for(Move possiblePawnTakeOverMove : otherTakeOverMoves)
		{
			if(possiblePawnTakeOverMove instanceof PawnTakeOverMove)
			{
				possiblePawnTakeOverMove.setPieceAtDestination(pieceAtEnPassantDestination);
				possiblePawnTakeOverMove.updatePossibility(board, false);
			}
		}
	}

	/**
	 *
	 * @return the position that this two step move passes by
	 */
	private ImmutablePosition getEnpassantPosition()
	{
		if(getPiece().isBlack())
		{
			return getCurrentPosition().up();
		}
		return getCurrentPosition().down();
	}

	/**
	 * Called when this two-step move isn't the last move made anymore
	 * @param board
	 */
	public void removeEnpassantMoves(final ChessBoard board)
	{
		performEnpassantPossibilitySync(board, board.getPiece(getEnpassantPosition()), true);
	}

	@Override
	public void onceAgainLastMoveThatWasMade(final ChessBoard board)
	{
		performEnpassantPossibilitySync(board, getPiece(), false);
	}

	@Override
	public boolean isTakeOverMove()
	{
		return false;
	}

	@Override
	public int getTakeOverValue()
	{
		return 0;
	}

	/**
	 * Overridden to not accumulate takeover/protecting values as this move can't either protect nor take over a piece
	 */
	@Override
	public void syncCountersWithBoard(final ChessBoard board)
	{

	}

	@Override
	protected int getSecondDimensionIndexInternal()
	{
		return 1;
	}
}
