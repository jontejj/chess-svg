package com.jjonsson.chess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;

public class ChessBoardEvaluator
{
	public enum ChessState
	{
		CHECKMATE,
		CHECK,
		STALEMATE,
		PLAYING
	}

	public static ChessState getState(ChessBoard board)
	{
		King currentKing = board.getCurrentKing();
		HashMap<Piece, Move> movesThreateningKing = board.getMovesThreateningPosition(currentKing.getCurrentPosition(), !currentKing.getAffinity());
		if(movesThreateningKing != null && movesThreateningKing.size() > 0)
		{
			List<Move> kingMoves = currentKing.getAvailableMoves(false, board);
			Set<Move> movesStoppingCheck = new HashSet<Move>();
			int stoppableMoves = 0;
			for(Entry<Piece, Move> entry : movesThreateningKing.entrySet())
			{
				boolean moveIsStoppable = false;
				if(entry.getValue() instanceof DependantMove)
				{
					//Check if a player can put himself in harms way
					DependantMove move = (DependantMove) entry.getValue();
					DependantMove moveThatThisMoveDependUpon = move.getMoveThatIDependUpon();
					while(moveThatThisMoveDependUpon != null)
					{
						HashMap<Piece, Move> movesStoppingMove = board.getMovesThreateningPosition(moveThatThisMoveDependUpon.getPositionIfPerformed(), !entry.getKey().getAffinity());
						if(movesStoppingMove != null && movesStoppingMove.size() > 0)
						{
							movesStoppingCheck.addAll(movesStoppingMove.values());
							moveIsStoppable = true;
						}
						moveThatThisMoveDependUpon = moveThatThisMoveDependUpon.getMoveThatIDependUpon();
					}
					
				}
				
				//If the threatening piece can be taken in one move then the king can be saved
				HashMap<Piece, Move> defendingMoves = board.getMovesThreateningPosition(entry.getKey().getCurrentPosition(), currentKing.getAffinity());
				if(defendingMoves != null && defendingMoves.size() > 0)
				{
					movesStoppingCheck.addAll(defendingMoves.values());
					moveIsStoppable = true;
				}
				
				if(moveIsStoppable)
					stoppableMoves++;
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
		
		return ChessState.PLAYING;
	}
}
