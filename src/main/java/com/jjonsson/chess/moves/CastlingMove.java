package com.jjonsson.chess.moves;

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
	public CastlingMove(int rowChange, int columnChange, Piece pieceThatTheMoveWillBeMadeWith)
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
	public void setRock(Piece aRock)
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
	public void updateMove(ChessBoard board)
	{
		myKingMove.updateMove(board);
		if(myRockMove != null)
		{
			myRockMove.updateMove(board);
		}
		super.updateMove(board);
	}

	@Override
	protected boolean canBeMadeInternal(ChessBoard board)
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
	public void makeMove(ChessBoard board) throws UnavailableMoveException
	{
		if(!canBeMade(board))
		{
			throw new UnavailableMoveException(this);
		}
		
		board.movePiece(getPiece(), myKingMove);
		getCurrentPosition().applyMove(myKingMove);
		
		myRock.performMove(myRockMove, board, false);
		
		setMovesMade(getMovesMade()+1);
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
