package com.jjonsson.chess.evaluators;

import static com.jjonsson.chess.gui.Settings.DEBUG;
import static com.jjonsson.utilities.Logger.LOGGER;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.google.common.annotations.VisibleForTesting;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.orderings.MoveOrdering;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.SearchInterruptedError;
import com.jjonsson.chess.listeners.StatusListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.utilities.ThreadTracker;

/**
 * TODO: clean up this code mess (i.e make this into an "instanceiatable" class)
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
	 * @throws NoMovesAvailableException if the evaluation of available moves didn't return a move
	 * @throws SearchInterruptedError if interrupted by something (e.g the GUI)
	 */
	public static Move getBestMove(final ChessBoard board, final StatusListener listener) throws NoMovesAvailableException
	{
		long startTime = System.nanoTime();
		Move result = null;
		deepestSearch = 0;
		ChessBoard copyOfBoard = board.copy(DEBUG);

		ProgressTracker.setStatusListener(listener);
		SearchLimiter limiter = new SearchLimiter(board.getDifficulty());
		SearchResult searchResult = deepSearch(copyOfBoard, limiter);
		result = searchResult.getBestMove();
		if(result == null)
		{
			throw new NoMovesAvailableException();
		}
		LOGGER.finer("Best move: " + result);
		LOGGER.finer("Best move value: " + searchResult.getBestMoveValue());
		LOGGER.finer("Reached " + deepestSearch + " steps ahead on the deepest path");
		ProgressTracker.done();
		//This fetches the corresponding move from our original board
		result = board.getMove(result);
		if(result == null)
		{
			//Should not happen :)
			throw new NoMovesAvailableException();
		}
		double duration = (double)(System.nanoTime() - startTime) / SECONDS.toNanos(1);
		LOGGER.warning("getBestMove took " + duration + " secs");
		return result;
	}
	/**
	 * Like {@link ChessMoveEvaluator#getBestMove(ChessBoard, StatusListener)} but without progress tracking
	 */
	public static Move getBestMove(final ChessBoard board) throws NoMovesAvailableException
	{
		return getBestMove(board, null);
	}

	/**
	 * Performs a move without progress tracking
	 * @param board
	 * @throws NoMovesAvailableException
	 * @throws SearchInterruptedError
	 */
	public static void performBestMove(final ChessBoard board) throws NoMovesAvailableException
	{
		performBestMove(board, null);
	}

	/**
	 * Performs a move and keeps the StatusListener updated with the latest progress information
	 * @param board
	 * @param listener
	 * @throws NoMovesAvailableException
	 * @throws SearchInterruptedError
	 */
	public static void performBestMove(final ChessBoard board, final StatusListener listener) throws NoMovesAvailableException
	{
		try
		{
			Move bestMove = getBestMove(board, listener);
			if(!bestMove.getPiece().performMove(bestMove, board))
			{
				LOGGER.info("Move: " + bestMove + " is not available, performing random move");
				//In the worst case scenario we make a random move if possible
				board.performRandomMove();
			}
		}
		catch(NoMovesAvailableException evaluationNoMovesException)
		{
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
	 * @throws SearchInterruptedError
	 */
	private static SearchResult deepSearch(final ChessBoard board, final SearchLimiter limiter)
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
			List<Move> moves = board.getAvailableMoves(board.getCurrentPlayer());
			Collections.sort(moves, MoveOrdering.getInstance());
			//TODO: instead of doing DFS do BFS and sort the moves before diving (this will also fix concurrency problems)

			//The deeper we go, the less we branch, this assumes that a reasonable ordering of the moves has been made already
			long movesLeftToEvaluateOnThisBranch = Math.max(limiter.getDepth() * ChessBoard.BOARD_SIZE, 0) + 2;
			CountDownLatch workersDoneSignal = new CountDownLatch(moves.size());
			ThreadTracker threadTracker = new ThreadTracker();
			for(Move move : moves)
			{
				if(move.shouldBeIncludedInMoveTable())
				{
					//TODO(jontejj): how to search deeper when time allows us to
					if(limiter.getDepth() == SearchLimiter.MAX_DEPTH)
					{
						//For each main branch
						limiter.resetMovesLeft();
					}
					movesLeftToEvaluateOnThisBranch--;
					MoveEvaluatingThread moveEvaluator = new MoveEvaluatingThread(board, move, limiter, result, movesLeftToEvaluateOnThisBranch, workersDoneSignal);
					if(moveEvaluator.isRunningInSeperateThread())
					{
						threadTracker.addJob(moveEvaluator.getThread());
					}
					moveEvaluator.advancedRun();
				}
				else
				{
					workersDoneSignal.countDown();
				}
			}
			try
			{
				workersDoneSignal.await();
			}
			catch (InterruptedException e)
			{
				threadTracker.interruptCurrentJobs();
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
	 * @throws SearchInterruptedError
	 */
	@VisibleForTesting
	public static void evaluateMove(final Move move, final ChessBoard board, final SearchLimiter limiter, final SearchResult result, final long movesLeftToEvaluateOnThisBranch)
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

	private static long delveDeeper(final SearchLimiter limiter, final ChessBoard board, final long currentMoveValue)
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

	static boolean shouldContinueDeeper(final ChessBoard board, final SearchLimiter limiter, final long movesLeftOnBranch, final long moveValue, final boolean isTakeOverMove)
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

	static boolean shouldContinueInNewThread(final ChessBoard board, final SearchLimiter limiter, final long movesLeftOnBranch, final Move move)
	{
		if(!ChessBoardEvaluator.inPlay(board))
		{
			//Don't search deeper if we already are at check mate
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
		boolean iTookOverAPiece = (move.isTakeOverMove() && limiter.getScoreFactor() == 1 && limiter.getDepth() <= 0);

		return (minimumDepthNotReached || finalDepthNotReached || iTookOverAPiece);
	}

	/**
	 * Performs the given move and returns a measurement of how good it was
	 * <br>The measurements are:
	 * The differences in available moves and non available moves,
	 * how many pieces that are protected by other pieces,
	 * how many pieces that can be taken over by the other player
	 * how many time the move has been made (a repetitiveness protection)
	 * how progressive a move is
	 * @param move the move to perform
	 * @return the estimated value of the move performed
	 */
	@VisibleForTesting
	public static long performMoveWithMeasurements(final Move move, final ChessBoard board, final SearchLimiter limiter)
	{
		//Save some measurements for the before state
		int takeOverValue = move.getTakeOverValue();
		long accumulatedTakeOverValue = move.getAccumulatedTakeOverValuesForPieceAtDestination();

		long otherPlayerBefore = board.getMeasuredStatusForPlayer(!board.getCurrentPlayer());
		long playerBefore = board.getMeasuredStatusForPlayer(board.getCurrentPlayer());

		if(!move.getPiece().performMove(move, board, false))
		{
			//if(board.isMoveUnavailableDueToCheck(move) || move.isMoveUnavailableDueToCheckMate(board))
			return Long.MIN_VALUE;
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
