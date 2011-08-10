package com.jjonsson.chess.moves;

import static com.jjonsson.utilities.Logger.LOGGER;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Rock;

public class CastlingMove extends IndependantMove
{
	/**
	 * The rock that this castling move also moves
	 */
	private Piece myRock;

	private CastlingMovePart myKingMove;
	private CastlingMovePart myRockMove;
	/**
	 * The position that the king needs to traverse over in a QueenSideCastling
	 */
	private Position myQueenSideCastlingKingStepPosition;

	/**
	 * 
	 * @param rowChange
	 * @param columnChange
	 * @param pieceThatTheMoveWillBeMadeWith the king for the player that want's to do the move
	 */
	public CastlingMove(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
		myKingMove = new KingCastlingMovePart(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
		if(isQueenSideCastlingMove())
		{
			myQueenSideCastlingKingStepPosition = new Position(getCurrentPosition().getRow(), (byte) (getCurrentPosition().getColumn() - 3));
		}
	}

	private boolean isQueenSideCastlingMove()
	{
		return getColumnChange() < 0;
	}

	/**
	 * This also sanity checks the input so that's a{@link Rock}
	 * @param aRock The rock that this castling move also moves
	 */
	public void setRock(final Piece aRock)
	{
		if(aRock == null)
		{
			return;
		}

		if(aRock instanceof Rock)
		{
			myRock = aRock;
			if(isQueenSideCastlingMove())
			{
				myRockMove = new RockCastlingMovePart(0, 3, myRock);
			}
			else
			{
				myRockMove = new RockCastlingMovePart(0, -2, myRock);
			}
		}
	}

	@Override
	public void updateMove(final ChessBoard board)
	{
		myKingMove.updateMove(board);
		if(myRockMove != null)
		{
			myRockMove.updateMove(board);
		}
		super.updateMove(board);
	}

	@Override
	public void updateDestination(final ChessBoard board)
	{
		myKingMove.updateDestination(board);
		if(myRockMove != null)
		{
			myRockMove.updateDestination(board);
		}
		super.updateDestination(board);
	}

	@Override
	public void updatePossibility(final ChessBoard board, final boolean updatePieceAtDestination)
	{
		myKingMove.updatePossibility(board, updatePieceAtDestination);
		if(myRockMove != null)
		{
			myRockMove.updatePossibility(board, updatePieceAtDestination);
		}
		super.updatePossibility(board, updatePieceAtDestination);
	}

	@Override
	protected boolean canBeMadeInternal(final ChessBoard board)
	{
		if(myRock == null)
		{
			return false;
		}
		//TODO: remove this when the test case files have been updated with the new piece identifiers
		if(!King.class.cast(getPiece()).isAtStartingPosition())
		{
			return false;
		}

		if(!myKingMove.canBeMadeInternal(board))
		{
			return false;
		}

		if(!myRockMove.canBeMadeInternal(board))
		{
			return false;
		}

		if(isQueenSideCastlingMove() && board.getPiece(myQueenSideCastlingKingStepPosition) != null)
		{
			//For Queen Side castling moves there should be a free square over as there are three squares between the Rock and the King
			return false;
		}

		return true;
	}

	@Override
	public void makeMove(final ChessBoard board) throws UnavailableMoveException
	{
		if(!canBeMade(board))
		{
			throw new UnavailableMoveException(this);
		}

		board.movePiece(getPiece(), myKingMove);
		getCurrentPosition().applyMove(myKingMove);
		setMovesMade(getMovesMade()+1);
		try
		{
			myRock.performMove(myRockMove, board, false);
		}
		catch(UnavailableMoveException e)
		{
			LOGGER.warning(e.toString() + ", during castling move");
			board.undoMove(myKingMove, false);
		}
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
	public boolean canBeTakeOverMove()
	{
		return false;
	}

	/**
	 * Overridden to trigger a chain of moves that reverts both the Rock's move and the King's move
	 */
	@Override
	public RevertingMove getRevertingMove()
	{
		return myRockMove.getRevertingMove();
	}

	@Override
	protected int getFirstDimensionIndexInternal()
	{
		return 0;
	}

	/**
	 * Part of the King's moves
	 */
	@Override
	protected int getSecondDimensionIndexInternal()
	{
		if(isQueenSideCastlingMove())
		{
			return 8;
		}
		return 9;
	}

}
