package com.jjonsson.chess.board;

import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.chess.pieces.Piece.WHITE;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Sets;
import com.jjonsson.chess.moves.CastlingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Piece;

/**
 * The purpose of this container is to know everything there is to know about a specific position on a board
 * @author jonatanjoensson
 *
 */
public class PositionContainer
{
	private ChessBoard myBoard;

	private Piece myCurrentPiece;
	/**
	 * A map that keeps track of every position reachable by a move by the black player
	 * To each possible position there is a sorted map (sorted by the pieces value) of pieces
	 * 	and the move that would reach this position
	 */
	private Set<Move> myBlackAvailableMoves;
	/**
	 * A map that keeps track of every position reachable by a move by the white player
	 * To each possible position there is a sorted map (sorted by the pieces value) of pieces
	 * 	and the move that would reach this position
	 */
	private Set<Move> myWhiteAvailableMoves;
	/**
	 * A map that keeps track of every move by the black player that isn't available right now
	 * To each possible position there is a sorted map (by the pieces value) of pieces
	 * 	and the move that would reach this position
	 */
	private Set<Move> myBlackNonAvailableMoves;
	private Set<Move> myWhiteNonAvailableMoves;

	private CastlingMove myCastlingMove;

	public PositionContainer(final ChessBoard board)
	{
		myBoard = board;
		createMoveMaps();
	}

	private void createMoveMaps()
	{
		myWhiteAvailableMoves = Sets.newHashSet();
		myWhiteNonAvailableMoves = Sets.newHashSet();
		myBlackAvailableMoves = Sets.newHashSet();
		myBlackNonAvailableMoves = Sets.newHashSet();
	}

	public void clear()
	{
		myWhiteAvailableMoves.clear();
		myWhiteNonAvailableMoves.clear();
		myBlackAvailableMoves.clear();
		myBlackNonAvailableMoves.clear();
		myCastlingMove = null;
		myCurrentPiece = null;
	}

	/**
	 * @param move the move to add
	 * @return true if the move was added
	 */
	public boolean addAvailableMove(final Move move)
	{
		Set<Move> availableMoves = getAvailableMoves(move.getAffinity());
		if(availableMoves.add(move))
		{
			return true;
		}
		return false;
	}

	public boolean removeAvailableMove(final Move move)
	{
		Set<Move> availableMoves = getAvailableMoves(move.getAffinity());
		if(availableMoves.remove(move))
		{
			return true;
		}
		return false;
	}

	public boolean addNonAvailableMove(final Move move)
	{
		Set<Move> nonAvailableMoves = getNonAvailableMoves(move.getAffinity());
		if(nonAvailableMoves.add(move))
		{
			return true;
		}
		return false;
	}

	public boolean removeNonAvailableMove(final Move move)
	{
		Set<Move> nonAvailableMoves = getNonAvailableMoves(move.getAffinity());
		if(nonAvailableMoves.remove(move))
		{
			return true;
		}
		return false;
	}
	/**
	 * Set the castling move that reaches this position. This container will update this move when the availability for this position changes.
	 * @param move the move
	 */
	public void setCastlingMove(final CastlingMove move)
	{
		myCastlingMove = move;
	}

	/**
	 * 
	 * @param newPiece the new piece to put at this position
	 * @return the old piece at this position
	 */
	public Piece setCurrentPiece(final Piece newPiece)
	{
		Piece oldPiece = myCurrentPiece;
		myCurrentPiece = newPiece;
		return oldPiece;
	}

	/**
	 * 
	 * @return the current piece at this position or null if there is no piece here
	 */
	public Piece getCurrentPiece()
	{
		return myCurrentPiece;
	}

	/**
	 * Note that this may contain false positives as the game may be in check.
	 * @param affinity the affinity of the player's moves that should be returned
	 * @return the available moves for the given affinity
	 */
	public Set<Move> getAvailableMoves(final boolean affinity)
	{
		if(affinity == WHITE)
		{
			return myWhiteAvailableMoves;
		}

		return myBlackAvailableMoves;
	}


	/**
	 * Note because of performance issues this returns a modifiable map that you really shouldn't modify :)
	 * @param affinity
	 */
	public Set<Move> getNonAvailableMoves(final boolean affinity)
	{
		if(affinity == BLACK)
		{
			return myBlackNonAvailableMoves;
		}
		return myWhiteNonAvailableMoves;
	}

	public void updatePossibiltyForSetOfMoves()
	{
		//TODO: can this be done more efficiently?
		//Update destinations
		//TODO: if this would place the king's moves last it may be possible to not update all the king's moves between each turn
		ImmutableSet<Move> movesToUpdate = new Builder<Move>().addAll(myWhiteAvailableMoves).
		addAll(myWhiteNonAvailableMoves).
		addAll(myBlackAvailableMoves).
		addAll(myBlackNonAvailableMoves).build();

		//Update possibilities
		updatePossibiltyForSetOfMoves(movesToUpdate);

		//Because all the destinations for a castling move can't be stored in the above maps this needs special handling here
		if(myCastlingMove != null)
		{
			myCastlingMove.updateDestination(myBoard);
			myCastlingMove.updatePossibility(myBoard, false);
		}
	}

	private void updatePossibiltyForSetOfMoves(final Set<Move> moves)
	{
		for(Move move : moves)
		{
			//TODO: this shouldn't be needed
			move.updateDestination(myBoard);

			move.updatePossibility(myBoard, true);
			move.syncCountersWithBoard(myBoard);
		}
	}
}
