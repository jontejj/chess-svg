package com.jjonsson.chess.pieces;

import static com.jjonsson.chess.moves.Move.DOWN;
import static com.jjonsson.chess.moves.Move.LEFT;
import static com.jjonsson.chess.moves.Move.NO_CHANGE;
import static com.jjonsson.chess.moves.Move.RIGHT;
import static com.jjonsson.chess.moves.Move.UP;
import static com.jjonsson.utilities.Loggers.STDERR;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.UnremovablePieceError;
import com.jjonsson.chess.moves.CastlingMove;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MutablePosition;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.persistence.BoardLoader;
import com.jjonsson.chess.persistence.ChessFileFilter;

public class King extends Piece
{
	private static final ImmutablePosition WHITE_KING_START_POSITION = ImmutablePosition.from(0, 4);
	private static final ImmutablePosition BLACK_KING_START_POSITION = ImmutablePosition.from(ChessBoard.BOARD_SIZE - 1, 4);

	private CastlingMove myKingSideCastlingMove;
	private CastlingMove myQueenSideCastlingMove;

	/**
	 * 
	 * @param startingPosition where this king should be placed
	 * @param affinity true if this piece belongs to the black player false otherwise
	 */
	public King(final MutablePosition startingPosition, final boolean affinity, final ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	@Override
	public int getValue()
	{
		return Piece.KING_VALUE;
	}

	@Override
	public void addPossibleMoves()
	{
		addPossibleMove(new KingMove(UP, LEFT, this));
		addPossibleMove(new KingMove(UP, NO_CHANGE, this));
		addPossibleMove(new KingMove(UP, RIGHT, this));
		addPossibleMove(new KingMove(NO_CHANGE, 1, this));
		addPossibleMove(new KingMove(DOWN, RIGHT, this));
		addPossibleMove(new KingMove(DOWN, NO_CHANGE, this));
		addPossibleMove(new KingMove(DOWN, LEFT, this));
		addPossibleMove(new KingMove(NO_CHANGE, LEFT, this));
	}

	/**
	 * 
	 * @param rightRock the Rock that is connected to this King's king side castling move
	 */
	public void setRightRock(final Piece rightRock)
	{
		if(isRock(rightRock) && isAtStartingPosition())
		{
			myKingSideCastlingMove = new CastlingMove(0, 2, this);
			myKingSideCastlingMove.setRock((Rock) rightRock);
			addPossibleMove(myKingSideCastlingMove);
		}
	}

	private boolean isRock(final Piece aPiece)
	{
		return aPiece instanceof Rock && hasSameAffinityAs(aPiece);
	}

	/**
	 * 
	 * @param leftRock the Rock that is connected to this King's queen side castling move
	 */
	public void setLeftRock(final Piece leftRock)
	{
		if(isRock(leftRock) && isAtStartingPosition())
		{
			myQueenSideCastlingMove = new CastlingMove(0, -2, this);
			myQueenSideCastlingMove.setRock((Rock) leftRock);
			addPossibleMove(myQueenSideCastlingMove);
		}
	}

	public boolean isAtStartingPosition()
	{
		if(isBlack())
		{
			return BLACK_KING_START_POSITION == getCurrentPosition();
		}

		return WHITE_KING_START_POSITION == getCurrentPosition();
	}

	@Override
	public String getPieceName()
	{
		return "King";
	}

	@Override
	protected byte getPersistenceIdentifierType()
	{
		if(getMovesMade() > 0)
		{
			return Piece.MOVED_KING;
		}

		return Piece.KING;
	}

	@Override
	public boolean performMove(final Move move, final ChessBoard board, final boolean printOut)
	{
		if(getMovesMade() == 0)
		{
			if(myKingSideCastlingMove != null)
			{
				myKingSideCastlingMove.removeFromBoard(board);
			}
			if(myQueenSideCastlingMove != null)
			{
				myQueenSideCastlingMove.removeFromBoard(board);
			}
		}
		if(!super.performMove(move, board, printOut))
		{
			revertedAMove(board, getCurrentPosition());
			return false;
		}
		return true;
	}

	@Override
	public Piece removeFromBoard(final ChessBoard board)
	{
		BoardLoader.saveBoard(board, "faulty_boards/board_with_move_that_takes_king_over_" + System.currentTimeMillis() + ChessFileFilter.FILE_ENDING);
		Error error = new UnremovablePieceError("Kings may not be removed from the board");
		STDERR.fatal("", error);
		throw error;
	}

	/**
	 * Called when a move that this piece previously made has been reverted
	 */
	@Override
	public void revertedAMove(final ChessBoard board, final Position oldPosition)
	{
		if(getMovesMade() == 0)
		{
			if(myKingSideCastlingMove != null)
			{
				addPossibleMove(myKingSideCastlingMove);
				myKingSideCastlingMove.updateMove(board);
			}

			if(myQueenSideCastlingMove != null)
			{
				addPossibleMove(myQueenSideCastlingMove);
				myQueenSideCastlingMove.updateMove(board);
			}
		}
	}

	@Override
	public int getFirstDimensionMaxIndex()
	{
		return 0;
	}

	@Override
	public int getSecondDimensionMaxIndex()
	{
		return 9; //8 one step moves in each direction plus two castling moves
	}

	@Override
	public int expectedNumberOfPossibleMoves()
	{
		return 10;
	}
}
