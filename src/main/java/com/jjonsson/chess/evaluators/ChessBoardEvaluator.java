package com.jjonsson.chess.evaluators;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.Bishop;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Knight;
import com.jjonsson.chess.pieces.Piece;

public final class ChessBoardEvaluator
{
	private ChessBoardEvaluator(){}

	private static final long VALUE_OF_CHECKMATE = 50000;
	private static final long VALUE_OF_STALEMATE = -10000;

	public enum ChessState
	{
		CHECKMATE,
		CHECK,
		STALEMATE,
		PLAYING
	}

	public static long valueOfState(final ChessState state)
	{
		long result = 0;
		switch(state)
		{
			case CHECKMATE:
				result = VALUE_OF_CHECKMATE;
				break;
			case STALEMATE:
				//TODO(jontejj): this could be beneficial during the end game
				result = VALUE_OF_STALEMATE;
				break;
			case CHECK:
				result = 0;
				break;
			case PLAYING:
				result = 0;
				break;
		}
		return result;
	}

	/**
	 * 
	 * @param board
	 * @return true if the given board still is in play
	 */
	public static boolean inPlay(final ChessBoard board)
	{
		return ChessState.PLAYING == board.getCurrentState() || ChessState.CHECK == board.getCurrentState();
	}

	public static ChessState getState(final ChessBoard board)
	{
		King currentKing = board.getCurrentKing();
		Collection<Move> movesThreateningKing = board.getAvailableMoves(currentKing.getCurrentPosition(), !currentKing.getAffinity());
		if(movesThreateningKing.size() > 0)
		{
			if(isCheckMate(board, movesThreateningKing, currentKing))
			{
				return ChessState.CHECKMATE;
			}
			return ChessState.CHECK;
		}
		if(isStalemate(board))
		{
			return ChessState.STALEMATE;
		}
		return ChessState.PLAYING;
	}

	private static boolean isCheckMate(final ChessBoard board, final Collection<Move> movesThreateningKing, final King currentKing)
	{
		List<Move> kingMoves = currentKing.getAvailableMoves();

		//First a fast check
		if(movesThreateningKing.size() > 1 && kingMoves.isEmpty())
		{
			return true;
		}

		Set<Move> movesStoppingCheck = Sets.newHashSet();
		if(movesThreateningKing.size() == 1)
		{
			Move threateningMove = movesThreateningKing.iterator().next();
			if(threateningMove instanceof DependantMove)
			{
				//Check if a player can put himself in harms way
				DependantMove move = (DependantMove) threateningMove;
				DependantMove moveThatThisMoveDependUpon = move.getMoveThatIDependUpon();
				while(moveThatThisMoveDependUpon != null)
				{
					Collection<Move> movesStoppingMove = board.getAvailableMoves(moveThatThisMoveDependUpon.getDestination(), !threateningMove.getAffinity());
					for(Move stoppingMove : movesStoppingMove)
					{
						//Clear kings because they can't put themselves in harms way
						if(!(stoppingMove instanceof KingMove))
						{
							movesStoppingCheck.add(stoppingMove);
						}
					}
					moveThatThisMoveDependUpon = moveThatThisMoveDependUpon.getMoveThatIDependUpon();
				}

			}

			//If the threatening piece can be taken in one move then the king can be saved
			Collection<Move> defendingMoves = board.getAvailableMoves(threateningMove.getCurrentPosition(), currentKing.getAffinity());
			if(defendingMoves.size() > 0)
			{
				movesStoppingCheck.addAll(defendingMoves);
			}
		}

		movesStoppingCheck.addAll(kingMoves);
		//Caches the moves available for the current player (I.e invalidating all moves that during a non-check would be available)
		board.setMovesThatStopsKingFromBeingChecked(movesStoppingCheck);

		if(movesStoppingCheck.isEmpty())
		{
			return true;
		}
		return false;
	}

	private static boolean isStalemate(final ChessBoard board)
	{
		//Stalemate when the current player doesn't have any legal moves and the king is safe
		if(board.getAvailableMovesCount(board.getCurrentPlayer()) == 0)
		{
			return true;
		}
		//Only the kings remain
		else if(board.getTotalPieceCount() == 2)
		{
			return true;
		}
		else if(board.getTotalPieceCount() == 3)
		{
			/**
			 * king against king and bishop;
			 * king against king and knight;
			 */
			for(Piece piece : board.getPieces())
			{
				if((piece instanceof Bishop) || (piece instanceof Knight))
				{
					return true;
				}
			}
		}
		else if(board.getTotalPieceCount() == 4)
		{
			Set<Boolean> sameDiagonals = Sets.newHashSet();
			/**
			 * king and bishop against king and bishop, with both bishops on diagonals of the same colour.
			 */
			for(Piece piece : board.getPieces())
			{
				if(piece instanceof Bishop)
				{
					boolean evenDiagonal = ((piece.getCurrentPosition().getColumn() + piece.getCurrentPosition().getRow()) % 2 == 0);
					if(!sameDiagonals.add(evenDiagonal))
					{
						return true;
					}
				}
			}
		}
		return false;
	}
}
