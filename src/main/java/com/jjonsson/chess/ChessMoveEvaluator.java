package com.jjonsson.chess;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.ordering.MoveOrdering;
import com.jjonsson.chess.pieces.Piece;

public class ChessMoveEvaluator
{
	private static long deepestSearch = 0;
	
	private static class SearchLimiter
	{
		static final long MAX_DEPTH = 2;
		static final long MAX_BRANCH_MOVES = 30;
		/**
		 * Used to limit the amount of moves to evaluate
		 */
		public long movesLeft;
		/**
		 * Used to limit the depth of the most interesting moves
		 */
		public long depth;
		
		/**
		 * either 1 if the current player is the AI or -1 if the current player is the opponent
		 * this makes it possible to simulate that the player would have made the best move according to the same algorithm
		 */
		public long scoreFactor;
		
		public SearchLimiter()
		{
			movesLeft = MAX_BRANCH_MOVES;
			depth = MAX_DEPTH;
			scoreFactor = 1;
		}
	}
	
	private static class SearchResult
	{
		public Move bestMove = null;
		public long bestMoveValue = Long.MIN_VALUE;
	}
	
	public static void performBestMove(ChessBoard board) throws NoMovesAvailableException, UnavailableMoveException
	{
		deepestSearch = 0;
		ChessBoard copyOfBoard = board.clone();
		try
		{
			SearchResult result = deepSearch(copyOfBoard, new SearchLimiter());
			System.out.println("Best move: " + result.bestMove);
			Piece chosenPiece = board.getPiece(result.bestMove.getCurrentPosition());
			//Hack to revert some weird side effect
			result.bestMove.updateDestination(copyOfBoard);
			Move actualMove = chosenPiece.getAvailableMoveForPosition(result.bestMove.getPositionIfPerformed(), board);
			chosenPiece.performMove(actualMove, board);
		}
		catch(Throwable e)
		{
			//TODO: This shouldn't happen
			e.printStackTrace();
			
			//In the worst case scenario we make a random move if possible
			board.performRandomMove();
		}
		System.out.println("Reached " + deepestSearch + " steps ahead on the best path");
	}
	
	/**
	 * Searches for the move that has the best take over value and that is the closest to the center.
	 * It also takes into account the new game state and if your own piece was taken and the difference in number of available moves for both players
	 * @param board
	 * @param limiter
	 * @return A search result with the best move found and it's evaluated/accumulated value 
	 * 			or a search result with best move set to null if no moves were available
	 * @throws NoMovesAvailableException
	 * @throws UnavailableMoveException
	 * @throws NoSuchElementException
	 */
	private static SearchResult deepSearch(ChessBoard board, SearchLimiter limiter)
	{	
		if(SearchLimiter.MAX_DEPTH - limiter.depth > deepestSearch)
		{
			deepestSearch = SearchLimiter.MAX_DEPTH - limiter.depth;
		}
		SearchResult result = new SearchResult();
		HashMap<Position, String> positionScores = Maps.newHashMap();
		HashMap<Position, Long> positionScoresValues = Maps.newHashMap();
		
		//The game doesn't allow us to traverse further
		if(!ChessBoardEvaluator.inPlay(board))
		{
			//No move to return, only the game state's value
			result.bestMoveValue = ChessBoardEvaluator.valueOfState(board.getCurrentState());
		}
		else
		{
			Collection<Move> moves = board.getAvailableMoves(board.getCurrentPlayer()).values();
			Iterator<Move> sortedMoves = MoveOrdering.instance.greatestOf(moves, moves.size()).iterator();
			
			//The deeper we go, the less we branch, this assumes that a reasonable ordering of the moves has been made already
			long movesLeftToEvaluateOnThisBranch = Math.max(limiter.depth * 8, 0) + 1;
			
			while(sortedMoves.hasNext() && movesLeftToEvaluateOnThisBranch > 0)
			{
				if(limiter.depth == SearchLimiter.MAX_DEPTH)
				{
					//For each main branch we reset the moves left
					limiter.movesLeft = SearchLimiter.MAX_BRANCH_MOVES;
				}
				Move move = sortedMoves.next();
				if(limiter.depth == SearchLimiter.MAX_DEPTH)
					System.out.println("Testing: " + move);
				
				boolean takeOverMove = move.isTakeOverMove();
				long moveValue = performMoveWithMeasurements(move, board, false);
				if(((limiter.depth >= 0 && limiter.movesLeft > 0) || 
					(takeOverMove && limiter.scoreFactor == 1 && limiter.depth <= 0))
						&& moveValue > Long.MIN_VALUE)
				{
					limiter.depth--;
					/*if(!takeOverMove)
						//Regular depth decrementing
						limiter.depth--;
					else if(limiter.scoreFactor == 1)
						//If the opponent takes over a piece we punish that path and searches less deep there
						limiter.depth -= 2;
					else
						////If we took over a piece we dig deeper into that path (the attacking strategy)
						limiter.depth++;*/
					
					
					limiter.movesLeft--;
					limiter.scoreFactor *= -1;
					long deepValue = deepSearch(board, limiter).bestMoveValue;
					
					//Underflow protection
					if(deepValue == Long.MIN_VALUE)
						moveValue = Long.MIN_VALUE;
					else
						moveValue += deepValue;
					limiter.scoreFactor *= -1;
					
					//Reset depth factors for the next iteration
					/*if(!takeOverMove)
						limiter.depth++;
					else if(limiter.scoreFactor == -1)
						limiter.depth += 2;
					else
						limiter.depth--;*/
					limiter.depth++;
					movesLeftToEvaluateOnThisBranch--;
				}
				else if(limiter.movesLeft <= 0)
				{
					System.out.println("Reached moves limit");
				}
				if(board.undoMove(move, false))
				{
					//Only return the move if it was undoable because otherwise it means that it was a bad/invalid move
					if(moveValue > result.bestMoveValue)
					{	
						result.bestMove = move;
						result.bestMoveValue = moveValue;
					}
					
					if(limiter.depth == SearchLimiter.MAX_DEPTH)
					{
						if(move.getPositionIfPerformed() != null)
						{
							Long curVal = positionScoresValues.get(move.getPositionIfPerformed());
							if(curVal == null || curVal.longValue() < moveValue * limiter.scoreFactor)
							{
								if(move.canBeMade(board))
								{
									positionScores.put(move.getPositionIfPerformed(), moveValue * limiter.scoreFactor + ":" + move.getCurrentPosition());
									positionScoresValues.put(move.getPositionIfPerformed(), moveValue * limiter.scoreFactor);
								}
							}
						}
					}
				}
			}
		}
		if(result.bestMoveValue == Long.MIN_VALUE)
		{
			System.out.println("Something weird");
		}
		//Inverses the factor making it possible to evaluate a good move for the other player
		result.bestMoveValue *= limiter.scoreFactor;
		
		if(limiter.depth == SearchLimiter.MAX_DEPTH)
		{
			System.out.println("Best move value: " + result.bestMoveValue);
			board.getOriginatingBoard().newMoveEvaluationHasBeenDone(ImmutableMap.copyOf(positionScores));
		}
		
		return result;
	}
	
	/**
	 * Performs the given move and returns a measurement of how good it was
	 * @param move the move to perform (if it isn't available right now
	 * @param undoMoveAfterMeasurement true if you only want the value of the move without it actually being performed
	 * @return the estimated value of the move performed 
	 * (Note that this will be misleading if there are ChessBoardListener's that performs another move when nextPlayer is called)
	 */
	public static long performMoveWithMeasurements(Move move, ChessBoard board, boolean undoMoveAfterMeasurement)
	{
		//Save some measurements for the before state
		int takeOverValue = move.getTakeOverValue();
		int otherPlayerNrOfAvailableMoves = board.getAvailableMoves(!board.getCurrentPlayer()).size();
		int otherPlayerNrOfNonAvailableMoves = board.getNonAvailableMoves(!board.getCurrentPlayer()).size();
		int playerNrOfAvailableMoves = board.getAvailableMoves(board.getCurrentPlayer()).size();
		int playerNrOfNonAvailableMoves = board.getNonAvailableMoves(board.getCurrentPlayer()).size();
		
		long otherPlayerProtectiveMoves = board.getProtectedPiecesCount(!board.getCurrentPlayer());
		long playerProtectiveMoves = board.getProtectedPiecesCount(board.getCurrentPlayer());
		
		boolean didMove = false;
		try
		{
			if(move.getPiece() != null)
			{
				move.getPiece().performMove(move, board, false);
				didMove = true;
			}
		}
		catch(UnavailableMoveException ume)
		{
			//if(board.isMoveUnavailableDueToCheck(move) || move.isMoveUnavailableDueToCheckMate(board))
			return Long.MIN_VALUE;
			
			//return 0;
		}
		
		//Save some measurements for the after state (the current player has changed now so that's why the getCurrentPlayer has been inverted)
		long stateValue = ChessBoardEvaluator.valueOfState(board.getCurrentState());
		int otherPlayerNrOfAvailableMovesAfter = board.getAvailableMoves(board.getCurrentPlayer()).size();
		int otherPlayerNrOfNonAvailableMovesAfter = board.getNonAvailableMoves(board.getCurrentPlayer()).size();
		int playerNrOfAvailableMovesAfter = board.getAvailableMoves(!board.getCurrentPlayer()).size();
		int playerNrOfNonAvailableMovesAfter = board.getNonAvailableMoves(!board.getCurrentPlayer()).size();
		long otherPlayerProtectiveMovesAfter = board.getProtectedPiecesCount(board.getCurrentPlayer());
		long playerProtectiveMovesAfter = board.getProtectedPiecesCount(!board.getCurrentPlayer());
		
		//The higher the value, the better the move
		long moveValue = takeOverValue;
		moveValue += stateValue;
		moveValue += (otherPlayerNrOfAvailableMoves - otherPlayerNrOfAvailableMovesAfter);
		moveValue += (otherPlayerNrOfNonAvailableMovesAfter - otherPlayerNrOfNonAvailableMoves);
		moveValue += (playerNrOfAvailableMovesAfter - playerNrOfAvailableMoves);
		moveValue += (playerNrOfNonAvailableMovesAfter - playerNrOfNonAvailableMoves);
		
		moveValue += (playerProtectiveMovesAfter - playerProtectiveMoves);
		moveValue += (otherPlayerProtectiveMoves - otherPlayerProtectiveMovesAfter);
		
		if(undoMoveAfterMeasurement && didMove)
			board.undoMoves(1, false);
		
		return moveValue;
	}
}
