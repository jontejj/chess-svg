package com.jjonsson.chess.moves;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;
import com.jjonsson.chess.board.ChessBoard;
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
	public boolean makeMove(final ChessBoard board)
	{
		if(super.makeMove(board))
		{
			performEnpassantPossibilitySync(board, getPiece(), true);
			return true;
		}
		return false;
	}

	/**
	 * 
	 * @param board
	 * @param pieceAtEnPassantDestination
	 * @param enable true if this sync is supposed to add possible en-passant moves, false if they should be removed
	 */
	private void performEnpassantPossibilitySync(final ChessBoard board, final Piece pieceAtEnPassantDestination, final boolean enable)
	{
		Collection<Move> moves = null;
		if(enable)
		{
			moves = board.getNonAvailableMoves(getEnpassantPosition(), !getAffinity());
		}
		else
		{
			moves = board.getAvailableMoves(getEnpassantPosition(), !getAffinity());
		}
		ImmutableSet<Move> otherTakeOverMoves = ImmutableSet.copyOf(moves);
		for(Move possiblePawnTakeOverMove : otherTakeOverMoves)
		{
			if(possiblePawnTakeOverMove instanceof PawnTakeOverMove)
			{
				possiblePawnTakeOverMove.setEnPassant(enable);
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
		performEnpassantPossibilitySync(board, board.getPiece(getEnpassantPosition()), false);
	}

	@Override
	public void onceAgainLastMoveThatWasMade(final ChessBoard board)
	{
		performEnpassantPossibilitySync(board, getPiece(), true);
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
