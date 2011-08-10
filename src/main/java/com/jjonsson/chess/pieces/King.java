package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.CastlingMove;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Position;
import static com.jjonsson.chess.moves.Move.*;

public class King extends Piece 
{
	private static final Position WHITE_KING_START_POSITION = new Position((byte)0, (byte)4);
	private static final Position BLACK_KING_START_POSITION = new Position((byte)7, (byte)4);
	
	private CastlingMove myKingSideCastlingMove;
	private CastlingMove myQueenSideCastlingMove;
	
	/**
	 * 
	 * @param startingPosition where this king should be placed
	 * @param affinity true if this piece belongs to the black player false otherwise
	 */
	public King(Position startingPosition, boolean affinity, ChessBoard boardPieceIsToBePlacedOn)
	{
		super(startingPosition, affinity, boardPieceIsToBePlacedOn);
	}

	@Override
	public int getValue() 
	{
		return Piece.KING_VALUE;
	}

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
		if(isAtStartingPosition())
		{
			myKingSideCastlingMove = new CastlingMove(0, 2, this);
			addPossibleMove(myKingSideCastlingMove);
			myQueenSideCastlingMove = new CastlingMove(0, -2, this);
			addPossibleMove(myQueenSideCastlingMove);
		}
	}
	
	public boolean isAtStartingPosition()
	{
		if(isBlack())
		{
			return BLACK_KING_START_POSITION.equals(getCurrentPosition());
		}
		
		return WHITE_KING_START_POSITION.equals(getCurrentPosition());	
	}

	@Override
	public String getPieceName()
	{
		return "King";
	}
	
	@Override
	protected byte getPersistanceIdentifierType()
	{
		if(getMovesMade() > 0)
		{
			return Piece.MOVED_KING;
		}
		
		return Piece.KING;
	}
	
	/**
	 * 
	 * @return the move that takes the king two steps to the right
	 */
	public CastlingMove getKingSideCastlingMove()
	{
		return myKingSideCastlingMove;
	}
	
	/**
	 * 
	 * @return the move that takes the king three steps to the left
	 */
	public CastlingMove getQueenSideCastlingMove()
	{
		return myQueenSideCastlingMove;
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
}
