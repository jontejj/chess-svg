package com.jjonsson.chess.exceptions;

import com.jjonsson.chess.pieces.Piece;

/**
 * Thrown when two pieces occupy the same position on the board
 * @author jonatanjoensson
 *
 */
public class DuplicatePieceError extends Error
{
	private static final long	serialVersionUID	= -7501157625082377801L;

	private Piece myExistingPiece;
	private Piece myDuplicatePiece;
	public DuplicatePieceError(final Piece existingPiece, final Piece duplicatePiece)
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

	@Override
	public String toString()
	{
		return "Duplicate Piece: " + getDuplicatePiece() + " tried to replace exisiting piece: " + getExistingPiece();
	}
}
