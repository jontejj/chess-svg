package com.jjonsson.chess;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.ordering.MoveOrdering;
import com.jjonsson.chess.pieces.Piece;

public class ChessMoveEvaluator
{
	private static class SearchLimiter
	{
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
			movesLeft = 1000;
			depth = 3;
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
		ChessBoard copyOfBoard = board.clone();
		SearchResult result = deepSearch(copyOfBoard, new SearchLimiter());
		
		if(result.bestMove == null)
			throw new NoMovesAvailableException();
		
		Piece chosenPiece = board.getPiece(result.bestMove.getCurrentPosition());
		
		Move actualMove = chosenPiece.getAvailableMoveForPosition(result.bestMove.getPositionIfPerformed(), board);
		
		try
		{
			chosenPiece.performMove(actualMove, board);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
			//TODO: This shouldn't happen
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
		SearchResult result = new SearchResult();
		Collection<Move> moves = board.getAvailableMoves(board.getCurrentPlayer()).values();
		Iterator<Move> sortedMoves = MoveOrdering.instance.greatestOf(moves, moves.size()).iterator();
		
		//The game doesn't allow us to traverse further
		if(!ChessBoardEvaluator.inPlay(board))
		{
			//No move to return, only the game state's value
			result.bestMoveValue = ChessBoardEvaluator.valueOfState(board.getCurrentState()) * limiter.scoreFactor;
		}
		//The search limiter tells us that we have gone down far enough in our search tree
		else if(limiter.depth == 0 || limiter.movesLeft == 0)
		{
			if(sortedMoves.hasNext())
			{
				result.bestMove = sortedMoves.next();
				result.bestMoveValue = performMoveWithMeasurements(result.bestMove, board, true) * limiter.scoreFactor;
			}
		}
		else
		{
			while(sortedMoves.hasNext())
			{
				Move move = sortedMoves.next();
				long moveValue = performMoveWithMeasurements(move, board, false);
				
				if(moveValue >= result.bestMoveValue)
				{	
					result.bestMove = move;
					result.bestMoveValue = moveValue * limiter.scoreFactor;
					
					limiter.depth--;
					limiter.movesLeft--;
					//Inverses the factor making it possible to evaluate a good move for the other player
					limiter.scoreFactor *= -1;
					result.bestMoveValue += deepSearch(board, limiter).bestMoveValue;
					limiter.depth++;
				}
				board.undoMoves(1, false);	
			}
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
			//Just continue on and evaluate how good it was for us that this move couldn't be done
		}
		
		//Save some measurements for the after state (the current player has changed now so that's why the getCurrentPlayer has been inverted)
		long stateValue = ChessBoardEvaluator.valueOfState(board.getCurrentState());
		int otherPlayerNrOfAvailableMovesAfter = board.getAvailableMoves(board.getCurrentPlayer()).size();
		int otherPlayerNrOfNonAvailableMovesAfter = board.getNonAvailableMoves(board.getCurrentPlayer()).size();
		int playerNrOfAvailableMovesAfter = board.getAvailableMoves(!board.getCurrentPlayer()).size();
		int playerNrOfNonAvailableMovesAfter = board.getNonAvailableMoves(!board.getCurrentPlayer()).size();
		
		//The higher the value, the better the move
		long moveValue = takeOverValue;
		moveValue += stateValue;
		moveValue += (otherPlayerNrOfAvailableMoves - otherPlayerNrOfAvailableMovesAfter);
		moveValue += (otherPlayerNrOfNonAvailableMovesAfter - otherPlayerNrOfNonAvailableMoves);
		moveValue += (playerNrOfAvailableMovesAfter - playerNrOfAvailableMoves);
		moveValue += (playerNrOfNonAvailableMovesAfter - playerNrOfNonAvailableMoves);
		
		if(undoMoveAfterMeasurement && didMove)
			board.undoMoves(1, false);
		
		return moveValue;
	}
}
