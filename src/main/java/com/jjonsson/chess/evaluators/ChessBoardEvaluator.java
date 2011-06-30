package com.jjonsson.chess.evaluators;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;

public final class ChessBoardEvaluator
{
	private static final long VALUE_OF_CHECKMATE = 50000;
	private static final long VALUE_OF_STALEMATE = -10000;
	private ChessBoardEvaluator()
	{
		
	}
	
	public enum ChessState
	{
		CHECKMATE,
		CHECK,
		STALEMATE,
		PLAYING
	}
	
	public static long valueOfState(ChessState state)
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
	public static boolean inPlay(ChessBoard board)
	{
		return ChessState.PLAYING == board.getCurrentState() || ChessState.CHECK == board.getCurrentState();
	}

	public static ChessState getState(ChessBoard board)
	{
		King currentKing = board.getCurrentKing();
		ImmutableSet<Move> movesThreateningKing = board.getAvailableMoves(currentKing.getCurrentPosition(), !currentKing.getAffinity());
		if(movesThreateningKing.size() > 0)
		{
			List<Move> kingMoves = currentKing.getAvailableMoves(Piece.NO_SORT, board);
			Set<Move> movesStoppingCheck = Sets.newHashSet();
			int stoppableMoves = 0;
			for(Move threateningMove : movesThreateningKing)
			{
				boolean moveIsStoppable = false;
				if(threateningMove instanceof DependantMove)
				{
					//Check if a player can put himself in harms way
					DependantMove move = (DependantMove) threateningMove;
					DependantMove moveThatThisMoveDependUpon = move.getMoveThatIDependUpon();
					while(moveThatThisMoveDependUpon != null)
					{
						ImmutableSet<Move> movesStoppingMove = board.getAvailableMoves(moveThatThisMoveDependUpon.getDestination(), !threateningMove.getAffinity());
						if(movesStoppingMove.size() > 0)
						{
							movesStoppingCheck.addAll(movesStoppingMove);
							moveIsStoppable = true;
						}
						moveThatThisMoveDependUpon = moveThatThisMoveDependUpon.getMoveThatIDependUpon();
					}
					
				}
				
				//If the threatening piece can be taken in one move then the king can be saved
				ImmutableSet<Move> defendingMoves = board.getAvailableMoves(threateningMove.getCurrentPosition(), currentKing.getAffinity());
				if(defendingMoves.size() > 0)
				{
					movesStoppingCheck.addAll(defendingMoves);
					moveIsStoppable = true;
				}
				
				if(moveIsStoppable)
				{
					stoppableMoves++;
				}
			}
			
			movesStoppingCheck.addAll(kingMoves);
			//Caches the moves available for the current player (I.e invalidating all moves that during a non-check would be available)
			board.setMovesThatStopsKingFromBeingChecked(movesStoppingCheck);
			
			if(stoppableMoves != movesThreateningKing.size() && kingMoves.size() == 0)
			{
				return ChessState.CHECKMATE;
			}
			
			return ChessState.CHECK;
		}
		//Stalemate when the current player doesn't have any legal moves and the king is safe
		if(board.getAvailableMoves(board.getCurrentPlayer()).size() == 0)
		{
			return ChessState.STALEMATE;
		}
		/*TODO(jontejj) there are more stalemate games:
		The game is immediately drawn when one of the following endings arises:

		    king against king;
		    king against king and bishop;
		    king against king and knight;
		    king and bishop against king and bishop, with both bishops on diagonals of the same colour.
		*/

		
		return ChessState.PLAYING;
	}
}
