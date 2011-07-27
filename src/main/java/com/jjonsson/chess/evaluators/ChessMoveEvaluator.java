package com.jjonsson.chess.evaluators;

import static com.jjonsson.utilities.Logger.LOGGER;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import com.google.common.collect.ImmutableList;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.orderings.MoveOrdering;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.SearchInterruptedError;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.gui.StatusListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.utilities.Logger;

/**
 * TODO: clean up this code mess
 * @author jonatanjoensson
 *
 */
public final class ChessMoveEvaluator
{
	private ChessMoveEvaluator()
	{
		
	}
	
	private static long deepestSearch = 0;
	/**
	 * Determines how badly we want to repeat a previous made move
	 */
	private static final int REPITIVE_PUNISHMENT_FACTOR = 10;
	
	/**
	 * Performs a directed DFS (not all moves to a given level are evaluated) and tries to return the best move available
	 * @param board
	 * @return the best move for the current player on the given board
	 * @throws NoMovesAvailableException if the evaluation of available moves didn't return a move<p/>
	 * This may also throw SearchInterruptedError if interrupted by something (e.g the GUI)
	 */
	public static Move getBestMove(ChessBoard board, StatusListener listener) throws NoMovesAvailableException
	{
		Move result = null;
		deepestSearch = 0;
		ChessBoard copyOfBoard;
		try
		{
			copyOfBoard = board.clone();
		}
		catch (CloneNotSupportedException e)
		{
			throw new UnsupportedOperationException("Cloning of chessboard not possible", e);
		}
		ProgressTracker.setStatusListener(listener);
		SearchLimiter limiter = new SearchLimiter(board.getDifficulty());
		SearchResult searchResult = deepSearch(copyOfBoard, limiter);
		result = searchResult.getBestMove();
		if(result == null)
		{
			throw new NoMovesAvailableException();
		}
		LOGGER.finest("Best move: " + result);
		LOGGER.finest("Best move value: " + searchResult.getBestMoveValue());
		LOGGER.finest("Reached " + deepestSearch + " steps ahead on the deepest path");
		ProgressTracker.done();
		try
		{
			result = board.getMove(result);
		}
		catch (UnavailableMoveException e)
		{
			//Should not happen :)
			throw new NoMovesAvailableException();
		}
		return result;
	}
	
	/**
	 * 
	 * @param board
	 * @return
	 * @throws NoMovesAvailableException, SearchInterruptedError
	 */
	public static Move getBestMove(ChessBoard board) throws NoMovesAvailableException
	{
		return getBestMove(board, null);
	}
	
	/**
	 * Performs a move without progress tracking
	 * @param board
	 * @throws NoMovesAvailableException, SearchInterruptedError
	 */
	public static void performBestMove(ChessBoard board) throws NoMovesAvailableException
	{
		performBestMove(board, null);
	}
	
	/**
	 * Performs a move and keeps the StatusListener updated with the latest progress information
	 * @param board
	 * @param listener
	 * @throws NoMovesAvailableException, SearchInterruptedError
	 */
	public static void performBestMove(ChessBoard board, StatusListener listener) throws NoMovesAvailableException
	{
		try
		{
			Move bestMove = getBestMove(board, listener);
			bestMove.getPiece().performMove(bestMove, board);
		}
		catch(NoMovesAvailableException evaluationNoMovesException)
		{	
			//In the worst case scenario we make a random move if possible
			board.performRandomMove();
		}
		catch (UnavailableMoveException evaluationFailedException)
		{
			LOGGER.info(Logger.stackTraceToString(evaluationFailedException));
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
		if(SearchLimiter.MAX_DEPTH - limiter.getDepth() > deepestSearch)
		{
			deepestSearch = SearchLimiter.MAX_DEPTH - limiter.getDepth();
		}
		SearchResult result = new SearchResult();
		//The game doesn't allow us to traverse further
		if(!ChessBoardEvaluator.inPlay(board))
		{
			//No move to return, only the game state's value
			result.setBestMoveIfBetter(null, ChessBoardEvaluator.valueOfState(board.getCurrentState()));
		}
		else
		{
			Collection<Move> moves = board.getAvailableMoves(board.getCurrentPlayer()).values();
			ImmutableList<Move> sortedMoves = MoveOrdering.getInstance().immutableSortedCopy(moves);
			
			//The deeper we go, the less we branch, this assumes that a reasonable ordering of the moves has been made already
			long movesLeftToEvaluateOnThisBranch = Math.max(limiter.getDepth() * ChessBoard.BOARD_SIZE, 0) + 2;
			CountDownLatch workersDoneSignal = new CountDownLatch(sortedMoves.size());
			for(Move move : sortedMoves)
			{
				//TODO(jontejj): how to search deeper when time allows us to
				if(limiter.getDepth() == SearchLimiter.MAX_DEPTH)
				{
					//For each main branch
					limiter.resetMovesLeft();
				}
				movesLeftToEvaluateOnThisBranch--;
				MoveEvaluatingThread moveEvaluator = new MoveEvaluatingThread(board, move, limiter, result, movesLeftToEvaluateOnThisBranch, workersDoneSignal);
				moveEvaluator.advancedRun();
			}
			try
			{
				workersDoneSignal.await();
			}
			catch (InterruptedException e)
			{
				throw new SearchInterruptedError(e);
			}
		}
		//Inverses the factor making it possible to evaluate a good move for the other player
		result.applyPlayerAffinityFactor(limiter.getScoreFactor());
		
		return result;
	}
	
	/**
	 * Evaluates the given move and if the limits allows it, this also searches deeper
	 * if the move is better than the move in the given result
	 * @param move
	 * @param board
	 * @param limiter
	 * @param result
	 * @param movesLeftToEvaluateOnThisBranch
	 */
	static void evaluateMove(Move move, ChessBoard board, SearchLimiter limiter, SearchResult result, long movesLeftToEvaluateOnThisBranch)
	{
		boolean takeOverMove = move.isTakeOverMove();
		long moveValue = performMoveWithMeasurements(move, board, limiter);
		ProgressTracker.moveHasBeenMade();
		boolean deeperSearch = shouldContinueDeeper(board, limiter, movesLeftToEvaluateOnThisBranch, moveValue, takeOverMove);
		if(deeperSearch)
		{
			moveValue = delveDeeper(limiter, board, moveValue);
		}
		if(board.undoMove(move, false))
		{
			//Moves that has not been searched deeper than one level risks an immediate take over from the other player 
			//so to avoid making really stupid moves we only make those moves if they have a really high value
			//
			long moveValueWithMarginForAPossibleTakeOver = moveValue - move.getPiece().getValue();
			
			if(deeperSearch || moveValueWithMarginForAPossibleTakeOver > result.getBestMoveValue())
			{
				//Only return the move if it was undoable because otherwise it means that it was a bad/invalid move
				result.setBestMoveIfBetter(move, moveValue);
			}
		}
	}
	
	private static long delveDeeper(SearchLimiter limiter, ChessBoard board, long currentMoveValue)
	{
		long totalMoveValue = currentMoveValue;
		limiter.goDown();
		SearchResult deepResult = deepSearch(board, limiter);
		long deepValue = deepResult.getBestMoveValue();

		deepValue *= limiter.getScoreFactor() * -1;
		
		//Underflow protection
		if(deepValue == Long.MIN_VALUE)
		{
			totalMoveValue = Long.MIN_VALUE;
		}
		else
		{
			totalMoveValue += deepValue;
		}
		limiter.goUp();
		return totalMoveValue;
	}

	static boolean shouldContinueDeeper(ChessBoard board, SearchLimiter limiter, long movesLeftOnBranch, long moveValue, boolean isTakeOverMove)
	{
		if(!ChessBoardEvaluator.inPlay(board))
		{ 
			//Don't search deeper if we already are at check mate
			return false;
		}
		
		if(moveValue == Long.MIN_VALUE) 
		{
			//For invalid moves we don't continue
			return false;
		}
		
		boolean minimumDepthNotReached = (limiter.getCurrentDepth() <= limiter.getMinimumDepthToSearch());
		
		if(movesLeftOnBranch <= 0 && !minimumDepthNotReached)
		{ 
			//This filters out deeper searches for moves that initially don't look so good
			return false;
		}
		
		boolean finalDepthNotReached = (limiter.getDepth() >= 0 && limiter.getMovesLeft() > 0);
		//If we take over a piece we continue that path to not give too positive results
		boolean iTookOverAPiece = (isTakeOverMove && limiter.getScoreFactor() == 1 && limiter.getDepth() <= 0);
		
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
		moveValue -= (move.getMovesMade() - 1) * REPITIVE_PUNISHMENT_FACTOR;
		
		return moveValue;
	}
}
