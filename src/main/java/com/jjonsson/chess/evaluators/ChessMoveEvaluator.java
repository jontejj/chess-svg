package com.jjonsson.chess.evaluators;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.orderings.MoveOrdering;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;

public class ChessMoveEvaluator
{
	private static long deepestSearch = 0;
	
	/**
	 * Performs a directed DFS (not all moves to a given level are evaluated) and tries to return the best move available
	 * @param board
	 * @return the best move for the current player on the given board
	 * @throws NoMovesAvailableException if the evaluation of available moves didn't return a move
	 */
	public static Move getBestMove(ChessBoard board) throws NoMovesAvailableException
	{
		Move bestMove = null;
		deepestSearch = 0;
		ChessBoard copyOfBoard = board.clone();
		SearchLimiter limiter = new SearchLimiter();
		SearchResult result = deepSearch(copyOfBoard, limiter);
		if(result.bestMove == null)
		{
			throw new NoMovesAvailableException();
		}
		System.out.println("Best move: " + result.bestMove);
		System.out.println("Best move value: " + result.bestMoveValue);
		Piece chosenPiece = board.getPiece(result.bestMove.getCurrentPosition());
		if(chosenPiece == null)
		{
			//TODO(jontejj) this shouldn't be needed
			throw new NoMovesAvailableException();
		}
		bestMove = chosenPiece.getAvailableMoveForPosition(result.bestMove.getPositionIfPerformed(), board);
		if(bestMove == null)
		{
			//TODO(jontejj) this shouldn't be needed
			throw new NoMovesAvailableException();
		}
		System.out.println("Reached " + deepestSearch + " steps ahead on the deepest path");
		
		return bestMove;
	}
	
	public static void performBestMove(ChessBoard board) throws NoMovesAvailableException
	{
		try
		{
			Move bestMove = getBestMove(board);
			bestMove.getPiece().performMove(bestMove, board);
		}
		catch(NoMovesAvailableException evaluationNoMovesException)
		{	
			//In the worst case scenario we make a random move if possible
			board.performRandomMove();
		}
		catch (UnavailableMoveException evaluationFailedException)
		{
			evaluationFailedException.printStackTrace();
			//In the worst case scenario we make a random move if possible
			board.performRandomMove();
		}
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
			long movesLeftToEvaluateOnThisBranch = Math.max(limiter.depth * 8, 0) + 2;
			
			while(sortedMoves.hasNext())
			{
				//TODO(jontejj): how to search deeper when time allows us to
				if(limiter.depth == SearchLimiter.MAX_DEPTH)
				{
					//For each main branch we reset the moves left
					limiter.movesLeft = SearchLimiter.MAX_BRANCH_MOVES;
				}
				Move move = sortedMoves.next();
				//if(limiter.depth == SearchLimiter.MAX_DEPTH)
				//	System.out.println("Testing: " + move);
				
				boolean takeOverMove = move.isTakeOverMove();
				long moveValue = performMoveWithMeasurements(move, board, limiter);
				movesLeftToEvaluateOnThisBranch--;
				boolean deeperSearch = shouldContinueDeeper(board, limiter, movesLeftToEvaluateOnThisBranch, moveValue, takeOverMove);
				if(deeperSearch)
				{
					limiter.depth--;
					/*if(!takeOverMove)
						//Regular depth decrementing
						limiter.depth--;
					else if(limiter.scoreFactor == -1)
						//If the opponent takes over a piece we punish that path and searches less deep there
						limiter.depth -= 2;
					else
						////If we took over a piece we dig deeper into that path (the attacking strategy)
						limiter.depth++;*/
					
					limiter.movesLeft--;
					limiter.scoreFactor *= -1;
					SearchResult deepResult = deepSearch(board, limiter);
					long deepValue = deepResult.bestMoveValue;
					limiter.scoreFactor *= -1;
					
					deepValue *= limiter.scoreFactor;
					
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
				}
				if(board.undoMove(move, false))
				{
					//Moves that has not been searched deeper than one level risks an immediate take over from the other player 
					//so to avoid making really stupid moves we only make those moves if they have a really high value
					//
					long moveValueWithMarginForAPossibleTakeOver = moveValue - move.getPiece().getValue();
					
					if(deeperSearch || moveValueWithMarginForAPossibleTakeOver > result.bestMoveValue)
					{
						//Only return the move if it was undoable because otherwise it means that it was a bad/invalid move
						if(moveValue > result.bestMoveValue)
						{	
							result.bestMove = move;
							result.bestMoveValue = moveValue;
						}
					}
					
					if(limiter.depth == SearchLimiter.MAX_DEPTH)
					{
						if(move.getPositionIfPerformed() != null)
						{
							Long curVal = positionScoresValues.get(move.getPositionIfPerformed());
							if(curVal == null || moveValue > curVal.longValue())
							{
								if(move.canBeMade(board))
								{
									positionScores.put(move.getPositionIfPerformed(), moveValue + ":" + move.getCurrentPosition());
									positionScoresValues.put(move.getPositionIfPerformed(), moveValue);
								}
							}
						}
					}
				}
			}
		}
		//Inverses the factor making it possible to evaluate a good move for the other player
		result.bestMoveValue *= limiter.scoreFactor;
		
		if(limiter.depth == SearchLimiter.MAX_DEPTH)
		{
			board.getOriginatingBoard().newMoveEvaluationHasBeenDone(ImmutableMap.copyOf(positionScores));
		}
		
		return result;
	}
	
	private static boolean shouldContinueDeeper(ChessBoard board, SearchLimiter limiter, long movesLeftOnBranch, long moveValue, boolean isTakeOverMove)
	{
		if(!ChessBoardEvaluator.inPlay(board)) //Don't search deeper if we already are at check mate
			return false;
		
		if(moveValue == Long.MIN_VALUE) //For invalid moves we don't continue
			return false;
		
		boolean minimumDepthNotReached = (limiter.getCurrentDepth() <= SearchLimiter.MINIMUM_DEPTH_TO_SEARCH);
		
		if(movesLeftOnBranch <= 0 && !minimumDepthNotReached) //This filters out deeper searches for moves that initially don't look so good
			return false;
		
		boolean finalDepthNotReached = (limiter.depth >= 0 && limiter.movesLeft > 0);
		//If we take over a piece we continue that path to not give too positive results
		boolean iTookOverAPiece = (isTakeOverMove && limiter.scoreFactor == 1 && limiter.depth <= 0);
		
		return minimumDepthNotReached || finalDepthNotReached || iTookOverAPiece;
	}
	
	/**
	 * Performs the given move and returns a measurement of how good it was
	 * <br>The measurements are: 
	 * The differences in available moves and non available moves, 
	 * how many pieces that are protected by other pieces,
	 * how many pieces that can be taken over by the other player
	 * how many time the move has been made (a repetitiveness protection)
	 * how progressive a move is
	 * @param move the move to perform (if it isn't available right now
	 * @return the estimated value of the move performed 
	 * (Note that this will be misleading if there are ChessBoardListener's that performs another move when nextPlayer is called)
	 */
	private static long performMoveWithMeasurements(Move move, ChessBoard board, SearchLimiter limiter)
	{
		//Save some measurements for the before state
		int takeOverValue = move.getTakeOverValue();
		long accumulatedTakeOverValue = move.getAccumulatedTakeOverValuesForPieceAtDestination();
		
		long otherPlayerBefore = board.getMeasuredStatusForPlayer(!board.getCurrentPlayer());
		long playerBefore = board.getMeasuredStatusForPlayer(board.getCurrentPlayer());

		try
		{
			move.getPiece().performMove(move, board, false);
		}
		catch(UnavailableMoveException ume)
		{
			//if(board.isMoveUnavailableDueToCheck(move) || move.isMoveUnavailableDueToCheckMate(board))
			return Long.MIN_VALUE;
			
			//return 0;
		}
		
		//Save some measurements for the after state (the current player has changed now so that's why the getCurrentPlayer has been inverted)
		long stateValue = ChessBoardEvaluator.valueOfState(board.getCurrentState()) / limiter.getCurrentDepth();
		
		long otherPlayerAfter = board.getMeasuredStatusForPlayer(board.getCurrentPlayer());
		long playerAfter = board.getMeasuredStatusForPlayer(!board.getCurrentPlayer());
		
		//The higher the value, the better the move
		long moveValue = takeOverValue;
		moveValue += stateValue;
		moveValue += move.getProgressiveValue();
		moveValue += otherPlayerBefore - otherPlayerAfter;
		moveValue += playerAfter - playerBefore;
		moveValue += accumulatedTakeOverValue;
		
		//If we have made this move recently we punish it for being repetitive
		moveValue -= (move.getMovesMade() - 1) * 10;
		
		return moveValue;
	}
}
