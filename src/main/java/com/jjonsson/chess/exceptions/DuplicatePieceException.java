package com.jjonsson.chess.exceptions;

import com.jjonsson.chess.pieces.Piece;

/**
 * Thrown when two pieces occupy the same position on the board
 * @author jonatanjoensson
 *
 */
public class DuplicatePieceException extends Exception
{
	private static final long	serialVersionUID	= -7501157625082377801L;
	
	private Piece myExistingPiece;
	private Piece myDuplicatePiece;
	public DuplicatePieceException(Piece existingPiece, Piece duplicatePiece)
	{
		myExistingPiece = existingPiece;
		myDuplicatePiece = duplicatePiece;
	}
	
	public Piece getExistingPiece()
	{
		return myExistingPiece;
	}
	
	public Piece getDuplicatePiece()
	{
		return myDuplicatePiece;
	}
	
	public String toString()
	{
		return "Duplicate Piece: " + myDuplicatePiece + " tried to replace exisiting piece: " + myExistingPiece;
	}
}
