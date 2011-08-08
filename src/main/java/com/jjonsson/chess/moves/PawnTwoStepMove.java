package com.jjonsson.chess.moves;

import java.util.Collection;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.Pawn;
import com.jjonsson.chess.pieces.Piece;

public class PawnTwoStepMove extends PawnMove
{

	public PawnTwoStepMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith, DependantMove moveDependingOnMe, DependantMove moveThatIDependUpon)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, moveDependingOnMe, moveThatIDependUpon);
		
		Pawn.class.cast(pieceThatTheMoveWillBeMadeWith).setTwoStepMove(this);
	}
	
	/**
	 * {@inheritDoc}
	 * This may also add the possibility for an en-passant move for the other player.
	 * @throws UnavailableMoveException  if this move isn't available right now
	 */
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		super.makeMove(board);
		performEnpassantPossibilitySync(board, getPiece());
	}
	
	private void performEnpassantPossibilitySync(ChessBoard board, Piece pieceAtEnPassantDestination)
	{
		Collection<Move> otherTakeOverMoves = board.getNonAvailableMoves(getEnpassantPosition(), !getAffinity());
		for(Move possiblePawnTakeOverMove : otherTakeOverMoves)
		{
			if(possiblePawnTakeOverMove instanceof PawnTakeOverMove)
			{
				possiblePawnTakeOverMove.setPieceAtDestination(pieceAtEnPassantDestination);
				possiblePawnTakeOverMove.updatePossibility(board);
			}
		}
	}
	
	/**
	 *
	 * @return the position that this two step move passes by
	 */
	private Position getEnpassantPosition()
	{
		try
		{
			if(getPiece().isBlack())
			{
				return getCurrentPosition().up();
			}
			return getCurrentPosition().down();
		}
		catch (InvalidPosition e)
		{
			throw new UnsupportedOperationException(this + " caused an invalid position. Enpassant moves will not be possible. This should never happen");
		}
	}
	
	/**
	 * Called when this two-step move isn't the last move made anymore
	 * @param board
	 */
	public void removeEnpassantMoves(ChessBoard board)
	{
		performEnpassantPossibilitySync(board, board.getPiece(getEnpassantPosition()));
	}
	
	@Override
	public void onceAgainLastMoveThatWasMade(ChessBoard board)
	{
		performEnpassantPossibilitySync(board, getPiece());
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
	public void syncCountersWithBoard(ChessBoard board)
	{
		
	}
	
	@Override
	protected int getSecondDimensionIndexInternal()
	{
		return 1;
	}
}
