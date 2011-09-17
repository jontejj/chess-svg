package com.jjonsson.chess.moves;

import static com.jjonsson.utilities.Loggers.STDERR;

import com.jjonsson.chess.board.ChessBoard;
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
	private CastlingMovePart myIntermediateStep;
	private ImmutablePosition myPreviousPosition;

	/**
	 * 
	 * @param rowChange
	 * @param columnChange
	 * @param pieceThatTheMoveWillBeMadeWith the king for the player that want's to do the move
	 */
	public CastlingMove(final int rowChange, final int columnChange, final Piece pieceThatTheMoveWillBeMadeWith)
	{
		super(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith);
		super.updateDestination(getPiece().getBoard());
		myKingMove = new KingCastlingMovePart(rowChange, columnChange, pieceThatTheMoveWillBeMadeWith, this);
	}

	private boolean isQueenSideCastlingMove()
	{
		return getColumnChange() < 0;
	}

	/**
	 * @param aRock The rock that this castling move also moves
	 * @throws NullPointerException if aRock is null
	 */
	public void setRock(final Rock aRock)
	{
		aRock.setCastlingMove(this);
		if(isQueenSideCastlingMove())
		{
			myRockMove = new RockCastlingMovePart(0, 3, aRock, this);
			myIntermediateStep = new IntermediateCastlingMovePart(0, -3, getPiece(), this);
		}
		else
		{
			myRockMove = new RockCastlingMovePart(0, -2, aRock, this);
		}
		myRock = aRock;
	}

	@Override
	public void updateDestination(final ChessBoard board)
	{
		//Nothing to do here since the destination never changes
	}

	@Override
	public void updatePossibility(final ChessBoard board, final boolean updatePieceAtDestination)
	{
		myKingMove.updatePossibility(board, updatePieceAtDestination);
		myRockMove.updatePossibility(board, updatePieceAtDestination);
		super.updatePossibility(board, updatePieceAtDestination);
	}

	public void updatePossibility(final ChessBoard board)
	{
		boolean oldCanBeMade = myCanBeMadeCache;
		super.updatePossibility(board, false);

		//Make sure all parts are updated and check once more if castling is possible
		if(myCanBeMadeCache && !oldCanBeMade)
		{
			updatePossibility(board, false);
		}
	}

	@Override
	protected boolean canBeMadeInternal(final ChessBoard board)
	{
		if(!myKingMove.canBeMade(board))
		{
			return false;
		}

		if(!myRockMove.canBeMade(board))
		{
			return false;
		}

		if(isQueenSideCastlingMove() && !myIntermediateStep.canBeMade(board))
		{
			//For Queen Side castling moves there should be a free square over as there are three squares between the Rock and the King
			return false;
		}

		return true;
	}

	@Override
	public boolean makeMove(final ChessBoard board)
	{
		if(!canBeMade(board))
		{
			return false;
		}

		if(!board.movePiece(getPiece(), myKingMove))
		{
			return false;
		}
		myPreviousPosition = getCurrentPosition();
		getPiece().updateCurrentPosition(myKingMove);

		setMovesMade(getMovesMade()+1);
		getPiece().setMovesMade(getPiece().getMovesMade() + 1);
		if(!myRock.performMove(myRockMove, board, false))
		{
			STDERR.warn("Castling move: " + this + " not available");
			board.undoMove(myKingMove, false);
			return false;
		}
		return true;
	}

	@Override
	public void removeFromBoard(final ChessBoard chessBoard)
	{
		getPiece().getPossibleMoves().remove(this);
		super.removeFromBoard(chessBoard);
		myKingMove.removeFromBoard(chessBoard);
		myRockMove.removeFromBoard(chessBoard);
		if(isQueenSideCastlingMove())
		{
			myIntermediateStep.removeFromBoard(chessBoard);
		}
	}

	@Override
	public void updateMove(final ChessBoard board)
	{
		myKingMove.updatePossibility(board, true);
		myRockMove.updatePossibility(board, true);
		if(isQueenSideCastlingMove())
		{
			myIntermediateStep.updatePossibility(board, true);
		}
		super.updateMove(board);
	}

	/**
	 * Overridden because castling moves makes two moves in one and thus
	 * would the oldPosition be faulty if it weren't handled here
	 */
	@Override
	public ImmutablePosition getOldPosition()
	{
		return myPreviousPosition;
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
