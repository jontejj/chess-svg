package com.jjonsson.chess;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.BoardInconsistencyException;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.PawnMove;
import com.jjonsson.chess.moves.PawnTakeOverMove;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.persistance.MoveLogger;
import com.jjonsson.chess.pieces.Bishop;
import com.jjonsson.chess.pieces.BlackPawn;
import com.jjonsson.chess.pieces.Knight;
import com.jjonsson.chess.pieces.King;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.pieces.Rock;
import com.jjonsson.chess.pieces.WhitePawn;

public class ChessBoard implements Cloneable
{
	public static final byte BOARD_SIZE = 8;
	
	private HashMap<Position, Piece> myWhitePieces;
	private HashMap<Position, Piece> myBlackPieces;
	/**
	 * A map that keeps track of every position reachable by a move by the black player
	 * To each possible position there is a sorted map (sorted by the pieces value) of pieces 
	 * 	and the move that would reach this position
	 */
	private HashMultimap<Position, Move> myBlackAvailableMoves;
	/**
	 * A map that keeps track of every position reachable by a move by the white player
	 * To each possible position there is a sorted map (sorted by the pieces value) of pieces 
	 * 	and the move that would reach this position
	 */
	private HashMultimap<Position, Move> myWhiteAvailableMoves;
	/**
	 * A map that keeps track of every move by the black player that isn't available right now
	 * To each possible position there is a sorted map (by the pieces value) of pieces 
	 * 	and the move that would reach this position
	 */
	private HashMultimap<Position, Move> myBlackNonAvailableMoves;
	private HashMultimap<Position, Move> myWhiteNonAvailableMoves;
	
	private boolean myCurrentPlayer;
	
	private Set<ChessBoardListener> myBoardListeners;
	private MoveLogger myMoveLogger;

	private King	myBlackKing;

	private King	myWhiteKing;

	private ChessState	myCurrentGameState;
	private Set<Move> myMovesThatStopsKingFromBeingChecked;
	
	//Manages the possibility of automatic moves (used during move reverting)
	private boolean myAllowsMoves;
	
	private ChessBoard myOriginatingBoard;
	
	/**
	 * This is a counter that keeps track of how many black moves that protects black pieces 
	 * Note: May be more than the number of black pieces due to pieces being protected by two different moves
	 */
	private long myBlackProtectedPiecesCount;
	/**
	 * This is a counter that keeps track of how many white moves that protects white pieces 
	 * Note: May be more than the number of white pieces due to pieces being protected by two different moves
	 */
	private long myWhiteProtectedPiecesCount;
	
	/**
	 * This is a counter that keeps track of the accumulated (for the current state) take over value for all black moves
	 */
	private long myBlackTakeOverPiecesCount;
	
	/**
	 * This is a counter that keeps track of the accumulated (for the current state) take over value for all white moves
	 */
	private long myWhiteTakeOverPiecesCount;

	/**
	 * Constructs the chess board 
	 * @param placeInitialPieces if true, all the pieces is set to their default locations
	 */
	public ChessBoard(boolean placeInitialPieces)
	{
		myOriginatingBoard = this;
		myAllowsMoves = true;
		myMovesThatStopsKingFromBeingChecked = ImmutableSet.of();
		myBoardListeners = Sets.newHashSet();
		myMoveLogger = new MoveLogger();
		addChessBoardListener(myMoveLogger);
		
		myBlackPieces = Maps.newHashMap();
		myBlackAvailableMoves = HashMultimap.create();
		myBlackNonAvailableMoves = HashMultimap.create();
		
		myWhitePieces = Maps.newHashMap();
		myWhiteAvailableMoves = HashMultimap.create();
		myWhiteNonAvailableMoves = HashMultimap.create();
		if(placeInitialPieces)
			this.reset();
	}
	
	private void setOriginatingBoard(ChessBoard board)
	{
		myOriginatingBoard = board;
	}
	
	/**
	 * Makes a copy of the board without copying the listeners
	 * @return the new board or null if the copy failed
	 */
	@Override
	public ChessBoard clone()
	{
		ChessBoard newBoard = new ChessBoard(false);
		newBoard.setOriginatingBoard(this);
		ByteArrayOutputStream baos = new ByteArrayOutputStream(64);
		try
		{
			writePersistanceData(baos);
			newBoard.readPersistanceData(new ByteArrayInputStream(baos.toByteArray()));
			newBoard.setPossibleMoves();
			newBoard.updateGameState();
			newBoard.copyMoveCounters(this);
		}
		catch (IOException e)
		{
			newBoard = null;
		}
		catch (InvalidPosition e)
		{
			newBoard = null;
		}
		catch (BoardInconsistencyException e)
		{
			newBoard = null;
		}
		return newBoard;
	}
	
	/**
	 * Copies the number of times the moves has been made from the given board
	 * @param chessBoard the board to copy the move counters from
	 * @throws BoardInconsistencyException if the given board doesn't match this board
	 */
	private void copyMoveCounters(ChessBoard chessBoard) throws BoardInconsistencyException
	{
		for(Piece p : getPieces())
		{
			ArrayList<Move> toMoves = p.getPossibleMoves();
			ArrayList<Move> fromMoves = chessBoard.getPiece(p.getCurrentPosition()).getPossibleMoves();
			
			//The arrays should match if the board matches
			if(toMoves.size() != fromMoves.size())
				throw new BoardInconsistencyException();
			
			for(int i = 0;i<toMoves.size();i++)
			{
				toMoves.get(i).copyMoveCounter(fromMoves.get(i));
			}
		}
	}
	
	/**
	 * Resets the number of times the moves has been made to zero
	 */
	public void resetMoveCounters()
	{
		for(Piece p : getPieces())
		{
			for(Move m : p.getPossibleMoves())
			{
				m.resetMoveCounter();
			}
		}
	}

	ChessBoard getOriginatingBoard()
	{
		return myOriginatingBoard;
	}
	
	/**
	 * @return the last move that was mode on this board
	 */
	public Move getLastMove()
	{
		return myMoveLogger.getLastMove();
	}
	
	/**
	 * Sets this board to it's initial state
	 */
	public void reset()
	{
		clear();
		setupWhitePieces();
		setupBlackPieces();
		
		//Set lists of all the possible moves for all of the pieces
		setPossibleMoves();
		myCurrentPlayer = Piece.WHITE;
		
		for(ChessBoardListener listener : myBoardListeners)
			listener.loadingOfBoardDone();	
		
		updateGameState();
	}
	
	public void setMovesThatStopsKingFromBeingChecked(Set<Move> moves)
	{
		myMovesThatStopsKingFromBeingChecked = moves;
	}
	
	public Set<Move> getMovesThatStopsKingFromBeingChecked()
	{
		return myMovesThatStopsKingFromBeingChecked;
	}
	
	/*
	 * A king's move must check if the threatening moves also threatens the square behind the king
	 */
	public boolean isMoveUnavailableDueToCheck(KingMove kingMove)
	{
		ImmutableSet<Move> movesBehindKing = getNonAvailableMoves(kingMove.getPositionIfPerformed(), !kingMove.getAffinity());
		for(Move behindMove : movesBehindKing)
		{
			if(behindMove instanceof DependantMove)
			{
				DependantMove move = ((DependantMove) behindMove).getMoveThatIDependUpon();
				if(move != null)
				{
					Position destinationForMove = move.getPositionIfPerformed();
					//Is the move this belongs to the one that is threatening the king?
					if(destinationForMove != null && destinationForMove.equals(kingMove.getCurrentPosition()))
					{
						if(getAvailableMoves(move.getPositionIfPerformed(),move.getAffinity()).contains(move))
							return true;
					}
				}
			}
		}
		return false;
	}
	public boolean isMoveUnavailableDueToCheck(Move move)
	{
		//This should only limit the current player and when he is under a check
		if(getCurrentState() != ChessState.CHECK || move.getAffinity() != getCurrentPlayer())
			return false;
		
		return !getMovesThatStopsKingFromBeingChecked().contains(move);
	}
	
	public void addChessBoardListener(ChessBoardListener listener)
	{
		myBoardListeners.add(listener);
	}
	
	public King getCurrentKing()
	{
		if(myCurrentPlayer == Piece.BLACK)
			return myBlackKing;
		
		return myWhiteKing;
	}
	
	public ChessState getCurrentState()
	{
		return myCurrentGameState;
	}
	
	/**
	 * 
	 * @return true if black, false if white
	 */
	public boolean getCurrentPlayer()
	{
		return myCurrentPlayer;
	}
	
	/**
	 * 
	 * @return Black if it's blacks turn, White if it's whites turn
	 */
	public String getCurrentPlayerString()
	{
		return myCurrentPlayer ? "Black" : "White";
	}
	
	/**
	 * 
	 * @return the inverse of getCurrentPlayerString()
	 */
	public String getPreviousPlayerString()
	{
		return myCurrentPlayer ? "White" : "Black";
	}
	
	public void nextPlayer()
	{
		myCurrentPlayer = !myCurrentPlayer;
		//TODO(jontejj): could this be cached?
		for(Move m : getKing(myCurrentPlayer).getPossibleMoves())
		{
			m.updateMove(this);
		}
		updateGameState();
		
		for(ChessBoardListener listener : myBoardListeners)
			listener.nextPlayer();
	}
	/**
	 * Performs a random move for the current player
	 */
	public void performRandomMove() throws NoMovesAvailableException
	{
		if(!ChessBoardEvaluator.inPlay(this))
			throw new NoMovesAvailableException();
		
		HashMultimap<Position, Move> availableMoves = getAvailableMoves(getCurrentPlayer());
		
		
		List<Move> shuffledMoves = Lists.newArrayList(availableMoves.values());
		Collections.shuffle(shuffledMoves);
		
		try
		{
			for(Move randomMove : shuffledMoves)
			{
				Piece piece = randomMove.getPiece();
				if(piece == null)
					throw new NoMovesAvailableException();
				if(randomMove.canBeMade(this))
				{
					piece.performMove(randomMove, this);
					break;
				}
			}
		}
		catch(UnavailableMoveException ume)
		{
			throw new NoMovesAvailableException();
		}
	}
	
	public void addPiece(Piece p, boolean initializePossibleMoves, boolean loadingInProgress)
	{
		if(p instanceof King)
		{
			King k = (King)p;
			if(k.getAffinity() == Piece.BLACK)
				myBlackKing = k;
			else
				myWhiteKing = k;
		}
		getMapForAffinity(p.getAffinity()).put(p.getCurrentPosition(), p);
		if(initializePossibleMoves)
			p.initilizePossibilityOfMoves(this);
		
		if(loadingInProgress)
		{
			for(ChessBoardListener listener : myBoardListeners)
				listener.piecePlacedLoadingInProgress(p);
		}
		else
		{
			for(ChessBoardListener listener : myBoardListeners)
				listener.piecePlaced(p);	
		}
	}
	
	/**
	 * Construct and place the white pieces
	 */
	private void setupWhitePieces()
	{	
		try
		{
			for(int column = 1; column <= ChessBoard.BOARD_SIZE; column++)
			{
				addPiece(new WhitePawn(Position.createPosition(2, column), this), false, true);
			}
			
			addPiece(new Knight(Position.createPosition(1, Position.B), Piece.WHITE, this), false, true);
			addPiece(new Knight(Position.createPosition(1, Position.G), Piece.WHITE, this), false, true);
			
			addPiece(new Rock(Position.createPosition(1, Position.A), Piece.WHITE, this), false, true);
			addPiece(new Rock(Position.createPosition(1, Position.H), Piece.WHITE, this), false, true);
			
			addPiece(new Queen(Position.createPosition(1, Position.D), Piece.WHITE, this), false, true);
			addPiece(new Bishop(Position.createPosition(1, Position.C), Piece.WHITE, this), false, true);
			addPiece(new Bishop(Position.createPosition(1, Position.F), Piece.WHITE, this), false, true);
			
			addPiece(new King(Position.createPosition(1, Position.E), Piece.WHITE, this), false, true);
		}
		catch(InvalidPosition ip)
		{
			System.out.println("Something wrong with board setup, got " + ip);
		}
	}
	
	/**
	 * Construct and place the black pieces
	 */
	private void setupBlackPieces()
	{
		try
		{
			for(int column = 1; column <= ChessBoard.BOARD_SIZE; column++)
			{
				addPiece(new BlackPawn(Position.createPosition(7, column), this), false, true);
			}
			
			addPiece(new Knight(Position.createPosition(8, Position.B), Piece.BLACK, this), false, true);
			addPiece(new Knight(Position.createPosition(8, Position.G), Piece.BLACK, this), false, true);
			
			addPiece(new Rock(Position.createPosition(8, Position.A), Piece.BLACK, this), false, true);
			addPiece(new Rock(Position.createPosition(8, Position.H), Piece.BLACK, this), false, true);
			
			addPiece(new Queen(Position.createPosition(8, Position.D), Piece.BLACK, this), false, true);
			addPiece(new Bishop(Position.createPosition(8, Position.C), Piece.BLACK, this), false, true);
			addPiece(new Bishop(Position.createPosition(8, Position.F), Piece.BLACK, this), false, true);
			
			addPiece(new King(Position.createPosition(8, Position.E), Piece.BLACK, this), false, true);
		}
		catch(InvalidPosition ip)
		{
			System.out.println("Something wrong with board setup, got " + ip);
		}
	}
	
	public void setPossibleMoves()
	{	
		for(Piece p : myWhitePieces.values())
			p.initilizePossibilityOfMoves(this);
		for(Piece p : myBlackPieces.values())
			p.initilizePossibilityOfMoves(this);
		
		//The king's moves needs to be evaluated one more time as they rely on that all other moves have been updated
		myWhiteKing.initilizePossibilityOfMoves(this);
		myBlackKing.initilizePossibilityOfMoves(this);
	}
	
	public void removePiece(Piece p)
	{
		getMapForAffinity(p.getAffinity()).remove(p.getCurrentPosition());
		myMoveLogger.pieceRemoved(p);
	}
	
	/**
	 * Removes the pawn from the board and replaces him with a new piece
	 * @param pawn the pawn to replace
	 * @return the piece that replaced the pawn
	 */
	public Piece replacePawn(Piece pawn)
	{
		//Remove pawn
		pawn.removeFromBoard(this);
		
		Piece newPiece = null;
		
		//Asks the GUI for a replacement piece
		for(ChessBoardListener cbl : myBoardListeners)
		{
			if(cbl.supportsPawnReplacementDialog())
			{
				newPiece = cbl.getPawnReplacementFromDialog();
				//TODO(jontejj): set position
				break;
			}
		}
		if(newPiece == null)
		{
			//Replace him with a Queen
			newPiece = new Queen(pawn.getCurrentPosition(), pawn.getAffinity(), this);
		}
		newPiece.setIsPawnReplacementPiece();
		
		addPiece(newPiece, true, false);
		
		//Update moves that can reach the queen
		this.updatePossibilityOfMovesForPosition(newPiece.getCurrentPosition());
		
		return newPiece;
	}
	
	public void removeAvailableMove(Position pos, Piece piece, Move move)
	{
		HashMultimap<Position, Move> availableMoves = getAvailableMoves(piece.getAffinity());
		availableMoves.remove(pos, move);
	}

	public void removeNonAvailableMove(Position pos, Piece piece, Move move)
	{
		HashMultimap<Position, Move> nonAvailableMoves = getNonAvailableMoves(piece.getAffinity());
		nonAvailableMoves.remove(pos, move);
	}
	
	public void addNonAvailableMove(Position pos, Piece piece, Move move)
	{
		//Out of bounds moves aren't handled here
		if(pos != null)
		{	
			HashMultimap<Position, Move> nonAvailableMoves = getNonAvailableMoves(piece.getAffinity());
			nonAvailableMoves.put(pos, move);
			
			//Check if the opposite king previously couldn't move into this position, if so then maybe he can now?
			if(!(piece instanceof King))
			{
				King oppositeKing = getOppositeKing(piece.getAffinity());
				Move kingMove = oppositeKing.getNonAvailableMoveForPosition(pos, this);
				if(kingMove != null)
				{
					kingMove.updateMove(this);
				}
			}
		}
	}
	
	public void addAvailableMove(Position pos, Piece piece, Move move)
	{	
		HashMultimap<Position, Move> availableMoves = getAvailableMoves(piece.getAffinity());
		availableMoves.put(pos, move);
		
		//Check if the opposite king previously could move into this position, if so remove that move because now he can't
		King oppositeKing = getOppositeKing(piece.getAffinity());
		Move kingMove = oppositeKing.getAvailableMoveForPosition(pos, this);
		if(kingMove != null)
		{
			kingMove.updateMove(this);
		}
	}
	
	public King getOppositeKing(boolean affinity)
	{
		if(affinity == Piece.BLACK)
			return myWhiteKing;
		
		return myBlackKing;
	}
	

	public Piece getKing(boolean affinity)
	{
		if(affinity == Piece.BLACK)
			return myBlackKing;
		
		return myWhiteKing;
	}
	
	public MoveLogger getMoveLogger()
	{
		return myMoveLogger;
	}
	
	private HashMap<Position, Piece> getMapForAffinity(boolean affinity)
	{
		if(affinity == Piece.BLACK)
			return myBlackPieces;
		
		return myWhitePieces;	
	}
	
	public Collection<Piece> getPiecesForAffinity(boolean affinity)
	{
		if(affinity == Piece.BLACK)
			return myBlackPieces.values();
		
		return myWhitePieces.values();	
	}
	
	/**
	 * Note that this may contain false positives as the game may be in check.
	 * @param affinity the affinity of the player's moves that should be returned
	 * @return the available moves for the given affinity
	 */
	public HashMultimap<Position, Move> getAvailableMoves(boolean affinity)
	{
		if(affinity == Piece.WHITE)
			return myWhiteAvailableMoves;
		
		return myBlackAvailableMoves;	
	}
	
	/**
	 * Note that this may contain false positives as the game may be in check.
	 * @param position the wanted position
	 * @param affinity the affinity of the player that should be able to move into the position
	 */
	public ImmutableSet<Move> getAvailableMoves(Position position, boolean affinity)
	{
		Set<Move> moves = null;
		if(affinity == Piece.WHITE)
			moves = myWhiteAvailableMoves.get(position);
		else
			moves = myBlackAvailableMoves.get(position);
		
		return ImmutableSet.copyOf(moves);	
	}
	
	/**
	 * Note that this may return a false positive as the game may be in check.
	 * @param position the wanted position
	 * @param affinity the affinity of the player that should be able to move into the position
	 * @return the first available move for the given position and the given affinity
	 * @throws NoSuchElementException if no move is available
	 */
	public Move getAvailableMove(Position position, boolean affinity) throws NoSuchElementException
	{
		return getAvailableMoves(position, affinity).iterator().next();	
	}
	
	public HashMultimap<Position, Move> getNonAvailableMoves(boolean affinity)
	{
		HashMultimap<Position, Move> moves = null;
		if(affinity == Piece.WHITE)
			moves = myWhiteNonAvailableMoves;
		else
			moves = myBlackNonAvailableMoves;
		
		return moves;	
	}

	/**
	 * 
	 * @param position
	 * @param affinity
	 */
	public ImmutableSet<Move> getNonAvailableMoves(Position position, boolean affinity)
	{
		Set<Move> moves = null;
		if(affinity == Piece.BLACK)
			moves  = myBlackNonAvailableMoves.get(position);
		else
			moves = myWhiteNonAvailableMoves.get(position);
		
		return ImmutableSet.copyOf(moves);	
	}
	
	/**
	 * This method checks for a move that could reach the given position by the other player in one move
	 * @param position the position to check
	 * @param affinity the affinity of the threatening player
	 * @return a move if the player with the given affinity could move into position in one move, otherwise null
	 */
	public Move moveThreateningPosition(Position position, boolean affinity, Piece pieceThatWonders)
	{
		ImmutableSet<Move> moves = getAvailableMoves(position, affinity);
		
		//Pieces may be able to move into this position if a piece moves there so 
		//we check all pieces that has a currently non possible move that leads to this position
		ImmutableSet<Move> possibleTakeOverMoves = getNonAvailableMoves(position, affinity);
		
		if(possibleTakeOverMoves != null)
		{
			for(Move m : possibleTakeOverMoves)
			{
				if(m instanceof PawnTakeOverMove)
				{
					return m;
				}
				else if(m instanceof PawnMove)
				{
					//No threatening move
				}
				else if(!m.isPieceBlockingMe(position, pieceThatWonders.getCurrentPosition()))
						return m;
			}
		}
		
		//Select first move that would take this square over
		Iterator<Move> iterator = moves.iterator();
		while(iterator.hasNext())
		{
			Move threateningMove = iterator.next();
			//A pawn move can't be made if there is something standing in this square and thus is it not threatening this square
			if(!(threateningMove instanceof PawnMove))
			{
				//We have found our first move that really is threatening this square
				return threateningMove;
			}
		}
		
		return null;
	}
	
	/**
	 * This method checks for a how many moves that could reach the given position by the other player in one move and take over a piece standing there
	 * @param position the position to check
	 * @param affinity the affinity of the threatening player
	 * @return a move if the player with the given affinity could move into position in one move, otherwise null
	 */
	public int getNumberOfMovesThreateningPosition(Position position, boolean affinity, Piece pieceThatWonders)
	{
		int numberOfMoves = 0;
		ImmutableSet<Move> moves = getAvailableMoves(position, affinity);
		
		//Pieces may be able to move into this position if a piece moves there so 
		//we check all pieces that has a currently non possible move that leads to this position
		ImmutableSet<Move> possibleTakeOverMoves = getNonAvailableMoves(position, affinity);
		
		if(possibleTakeOverMoves != null)
		{
			for(Move m : possibleTakeOverMoves)
			{
				//No threatening move
				if(!(m instanceof PawnMove))
				{
					if(m instanceof PawnTakeOverMove)
					{
						numberOfMoves++;
					}
					else if(!m.isPieceBlockingMe(position, pieceThatWonders.getCurrentPosition()))
						numberOfMoves++;
				}
			}
		}
		
		Iterator<Move> iterator = moves.iterator();
		while(iterator.hasNext())
		{
			Move threateningMove = iterator.next();
			//A pawn move can't be made if there is something standing in this square and thus is it not threatening this square
			if(!(threateningMove instanceof PawnMove))
			{
				numberOfMoves++;
			}
		}
		
		return numberOfMoves;
	}

	/**
	 * @param atPosition
	 * @return the Piece that is at the position provided, returns null if the position is free
	 */
	public Piece getPiece(Position atPosition)
	{
		Piece whitePiece = myWhitePieces.get(atPosition);
		if(whitePiece != null)
			return whitePiece;
		
		return myBlackPieces.get(atPosition);
	}
	
	public List<Piece> getPieces()
	{
		List<Piece> list = Lists.newArrayList();
		list.addAll(myBlackPieces.values());
		list.addAll(myWhitePieces.values());
		return list;
	}
	
	public void movePiece(Piece pieceToMove, Move moveToPerform)
	{
		Position newPosition = moveToPerform.getPositionIfPerformed();
		Position oldPosition = pieceToMove.getCurrentPosition();
		HashMap<Position, Piece> map = getMapForAffinity(pieceToMove.getAffinity());
		map.remove(oldPosition);
		map.put(newPosition, pieceToMove);
		
		myMoveLogger.movePerformed(moveToPerform);
	}

	/**
	 * This method should be called every time a position has been taken over by a piece or when a piece has been removed from this position
	 * @param position the position that has been changed
	 */
	public void updatePossibilityOfMovesForPosition(Position position)
	{
		ImmutableSet<Move> updatedMoves = ImmutableSet.of();
		updatedMoves = updatePossibiltyForMapOfMoves(myWhiteAvailableMoves, position, updatedMoves);
		updatePossibiltyForMapOfMoves(myWhiteNonAvailableMoves, position, updatedMoves);
		updatedMoves = ImmutableSet.of();
		updatedMoves = updatePossibiltyForMapOfMoves(myBlackAvailableMoves, position, updatedMoves);
		updatePossibiltyForMapOfMoves(myBlackNonAvailableMoves, position, updatedMoves);
	}
	
	/**
	 * 
	 * @param moves
	 * @param pos
	 * @param exclusionMap a map of moves that won't need an update
	 * @return a map of the moves that was updated
	 */
	private ImmutableSet<Move> updatePossibiltyForMapOfMoves(HashMultimap<Position, Move> moves, Position pos, ImmutableSet<Move> exclusionMap)
	{
		//As the move may be removed during this iteration will need a shallow copy of the move map
		ImmutableSet<Move> copy = ImmutableSet.copyOf(moves.get(pos));
		for(Move m : copy)
		{
			if(!exclusionMap.contains(m))
				m.updateMove(this);
		}
		return copy;
	}

	public void updateGameState()
	{
		ChessState oldState = myCurrentGameState;
		ChessState newState = ChessBoardEvaluator.getState(this);
		if(newState != oldState)
		{
			myCurrentGameState = newState;
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.gameStateChanged(newState);
			}
		}
	}

	/**
	 * Removes all the pieces from the board
	 */
	public void clear()
	{
		myWhitePieces.clear();
		myWhiteAvailableMoves.clear();
		myWhiteNonAvailableMoves.clear();
		myBlackPieces.clear();
		myBlackAvailableMoves.clear();
		myBlackNonAvailableMoves.clear();
		myMoveLogger.clear();
		myWhiteKing = null;
		myBlackKing = null;
	}

	public void writePersistanceData(OutputStream stream) throws IOException
	{
		//Write the state of the game
		stream.write(getGameStatePersistanceData());
		List<Piece> pieces = getPieces();
		//Write each piece to the file
		for(Piece p : pieces)
		{
			short persistanceForPiece = p.getPersistanceData();
			stream.write(new byte[]{(byte)(persistanceForPiece >> 8), (byte)(persistanceForPiece & 0xFF)});
		}
	}
	
	public void readPersistanceData(InputStream stream) throws IOException, InvalidPosition
	{
		//Read the state of the game
		readGameStatePersistanceData(stream);
		
		//Read each piece from the file
		byte[] piece = new byte[2];
		int readBytes = 0;
		while(readBytes != -1)
		{
			readBytes = stream.read(piece);
			if(readBytes == 2)
			{
				Piece p = Piece.getPieceFromPersistanceData((short)(piece[0] << 8 | piece[1]), this);
				//Don't place a piece where one is already placed
				if(p != null && this.getPiece(p.getCurrentPosition()) == null)
					addPiece(p, false, true);
			}
		}
	}
	
	private byte[] getGameStatePersistanceData()
	{
		byte[] settings = new byte[1];
		
		//Left most bit tell us the current player
		if(myCurrentPlayer == Piece.BLACK)
			settings[0] = (byte) (1 << 7);
		
		return settings;
	}
	
	private void readGameStatePersistanceData(InputStream stream) throws IOException
	{
		byte[] settings = new byte[1];
		int readBytes = stream.read(settings);
		if(readBytes == 1)
		{
			//Left most bit tell us the current player
			if((settings[0] & 0x80) == 0x80)
				myCurrentPlayer = Piece.BLACK;
			else
				myCurrentPlayer = Piece.WHITE;
		}
	}

	/**
	 * 
	 * @return a descriptive string of the status for this board (which players turn it is etc.)
	 */
	public String getStatusString() 
	{
		String status = "";
		if(ChessBoardEvaluator.inPlay(this))
			status = getCurrentPlayerString() + "s turn";
		else
		{
			ChessState state = getCurrentState();
			if(state == ChessState.CHECKMATE)
				status = "Checkmate. " + getPreviousPlayerString() + " won.";
			else if(state == ChessState.STALEMATE)				
				status = "Stalemate! Draw. ";
		}
		if(getLastMove() != null)
		{
			status += " (Last Move: " + getLastMove().logMessageForLastMove() + ")";
		}
		return status;
	}
	
	/**
	 * Undo the given number of moves
	 * @param movesToUndo the number of moves to undo
	 * @return moves that could be reverted
	 */
	public int undoMoves(int movesToUndo)
	{
		return undoMoves(movesToUndo, true);
	}
	/**
	 * Undo the given number of moves
	 * @param movesToUndo the number of moves to undo
	 * @return moves that could be reverted
	 */
	public int undoMoves(int movesToUndo, boolean printOuts)
	{
		myAllowsMoves = false;
		int movesReverted = 0;
		while(movesReverted < movesToUndo)
		{
			try
			{
				Move lastMove = myMoveLogger.getLastMove();
				lastMove.getPiece().performMove(lastMove.getRevertingMove(), this, printOuts);
				movesReverted++;
			}
			catch (Exception e)
			{
				break;
			}		
		}
		myAllowsMoves = true;
		
		for(ChessBoardListener listener : myBoardListeners)
		{
			listener.undoDone();
		}
		
		return movesReverted;
	}
	
	/**
	 * If the given move was the last one to be made it is undone by this function
	 * @param moveToUndo the move to undo
	 * @return true if the move could be undone
	 */
	public boolean undoMove(Move moveToUndo, boolean printOuts)
	{
		boolean wasUndone = true;
		
		myAllowsMoves = false;
		try
		{
			moveToUndo.getPiece().performMove(moveToUndo.getRevertingMove(), this, printOuts);
		}
		catch (Exception e)
		{
			wasUndone = false;
		}		
		myAllowsMoves = true;
		
		if(wasUndone)
		{
			for(ChessBoardListener listener : myBoardListeners)
			{
				listener.undoDone();
			}
		}
		
		return wasUndone;
	}

	/**
	 * @return false if no automatic moves is allowed
	 */
	public boolean allowsMoves()
	{
		return myAllowsMoves;
	}
	
	public void newMoveEvaluationHasBeenDone(ImmutableMap<Position, String> positionScores)
	{
		for(ChessBoardListener cbl : myBoardListeners)
		{
			cbl.squareScores(positionScores);
		}
	}

	public Move popLastMoveIfEqual(Move moveToPop)
	{
		Move lastMove = getLastMove();
		if(lastMove == moveToPop)
			return myMoveLogger.popMove();
		
		return null;
	}

	public void decreaseProtectedPiecesCounter(boolean affinity, int decrementValue)
	{
		if(affinity == Piece.BLACK)
			myBlackProtectedPiecesCount -= decrementValue;
		else
			myWhiteProtectedPiecesCount -= decrementValue;
	}

	public void increaseProtectedPiecesCounter(boolean affinity, int incrementValue)
	{
		if(affinity == Piece.BLACK)
			myBlackProtectedPiecesCount += incrementValue;
		else
			myWhiteProtectedPiecesCount += incrementValue;
	}
	
	public long getProtectedPiecesCount(boolean affinity)
	{
		if(affinity == Piece.BLACK)
			return myBlackProtectedPiecesCount;
		
		return myWhiteProtectedPiecesCount;
	}
	
	public void decreaseTakeOverPiecesCounter(boolean affinity, int decrementValue)
	{
		if(affinity == Piece.BLACK)
			myBlackTakeOverPiecesCount -= decrementValue;
		else
			myWhiteTakeOverPiecesCount -= decrementValue;
	}

	public void increaseTakeOverPiecesCounter(boolean affinity, int incrementValue)
	{
		if(affinity == Piece.BLACK)
			myBlackTakeOverPiecesCount += incrementValue;
		else
			myWhiteTakeOverPiecesCount += incrementValue;
	}
	
	public long getTakeOverPiecesCount(boolean affinity)
	{
		if(affinity == Piece.BLACK)
			return myBlackTakeOverPiecesCount;
		
		return myWhiteTakeOverPiecesCount;
	}

}
