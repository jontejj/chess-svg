package com.jjonsson.chess.pieces;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.CastlingMove;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Position;

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
		addPossibleMove(new KingMove(1, -1, this)); //Up left
		addPossibleMove(new KingMove(1, 0, this)); //Up
		addPossibleMove(new KingMove(1, 1, this)); //Up Right
		addPossibleMove(new KingMove(0, 1, this)); //Right
		addPossibleMove(new KingMove(-1, 1, this)); //Down Right
		addPossibleMove(new KingMove(-1, 0, this)); //Down
		addPossibleMove(new KingMove(-1, -1, this)); //Down left
		addPossibleMove(new KingMove(0, -1, this)); //Left
		if(isAtStartingPosition())
		{
			myKingSideCastlingMove = new CastlingMove(0, 2, this);
			addPossibleMove(myKingSideCastlingMove);
			myQueenSideCastlingMove = new CastlingMove(0, -3, this);
			addPossibleMove(myQueenSideCastlingMove);
		}
	}
	
	public boolean isAtStartingPosition()
	{
		if(getAffinity() == Piece.BLACK)
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
}
