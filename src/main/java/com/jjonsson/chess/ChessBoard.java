package com.jjonsson.chess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.moves.DependantMove;
import com.jjonsson.chess.moves.KingMove;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
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

public class ChessBoard 
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
	
	/**
	 * Constructs the chess board and sets all the pieces to their default locations
	 */
	public ChessBoard()
	{
		myMovesThatStopsKingFromBeingChecked = Collections.emptySet();
		myBoardListeners = new HashSet<ChessBoardListener>();
		myMoveLogger = new MoveLogger();
		
		myBlackPieces = new HashMap<Position, Piece>();
		myBlackAvailableMoves = HashMultimap.create();
		myBlackNonAvailableMoves = HashMultimap.create();
		
		myWhitePieces = new HashMap<Position, Piece>();
		myWhiteAvailableMoves = HashMultimap.create();
		myWhiteNonAvailableMoves = HashMultimap.create();
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
	
	public void nextPlayer()
	{
		myCurrentPlayer = !myCurrentPlayer;
		//TODO: could this be cached?
		for(Move m : getKing(myCurrentPlayer).getPossibleMoves())
		{
			m.updateMove(this);
		}
		updateGameState();
		
		for(ChessBoardListener listener : myBoardListeners)
			listener.nextPlayer();
	}
	
	public void performRandomMove() throws NoMovesAvailableException, UnavailableMoveException
	{
		HashMultimap<Position, Move> availableMoves = getAvailableMoves(getCurrentPlayer());
		
		boolean movePerformed = false;
		while(!movePerformed)
		{
			try
			{
				//TODO: perhaps we should perform the best move?
				Move randomMove = Ordering.arbitrary().max(availableMoves.values());
				Piece piece = getPiece(randomMove.getCurrentPosition());
				if(piece == null)
					throw new NoMovesAvailableException();
				if(randomMove.canBeMade(this))
				{
					System.out.println("Performing: " + randomMove);
					piece.performMove(randomMove, this);
					movePerformed = true;
					break;
				}
			}
			catch(NoSuchElementException empty)
			{
				throw new NoMovesAvailableException();
			}
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
		/* *****************************
		  	
		 * ******************************/
		for(int column = 1; column <= ChessBoard.BOARD_SIZE; column++)
		{
			addPiece(new WhitePawn(Position.createPosition(2, column)), false, true);
		}
		
		addPiece(new Knight(Position.createPosition(1, Position.B), Piece.WHITE), false, true);
		addPiece(new Knight(Position.createPosition(1, Position.G), Piece.WHITE), false, true);
		
		addPiece(new Rock(Position.createPosition(1, Position.A), Piece.WHITE), false, true);
		addPiece(new Rock(Position.createPosition(1, Position.H), Piece.WHITE), false, true);
		
		addPiece(new Queen(Position.createPosition(1, Position.D), Piece.WHITE), false, true);
		addPiece(new Bishop(Position.createPosition(1, Position.C), Piece.WHITE), false, true);
		addPiece(new Bishop(Position.createPosition(1, Position.F), Piece.WHITE), false, true);
		
		addPiece(new King(Position.createPosition(1, Position.E), Piece.WHITE), false, true);
	}
	
	/**
	 * Construct and place the black pieces
	 */
	private void setupBlackPieces()
	{
		for(int column = 1; column <= ChessBoard.BOARD_SIZE; column++)
		{
			addPiece(new BlackPawn(Position.createPosition(7, column)), false, true);
		}
		
		addPiece(new Knight(Position.createPosition(8, Position.B), Piece.BLACK), false, true);
		addPiece(new Knight(Position.createPosition(8, Position.G), Piece.BLACK), false, true);
		
		addPiece(new Rock(Position.createPosition(8, Position.A), Piece.BLACK), false, true);
		addPiece(new Rock(Position.createPosition(8, Position.H), Piece.BLACK), false, true);
		
		addPiece(new Queen(Position.createPosition(8, Position.D), Piece.BLACK), false, true);
		addPiece(new Bishop(Position.createPosition(8, Position.C), Piece.BLACK), false, true);
		addPiece(new Bishop(Position.createPosition(8, Position.F), Piece.BLACK), false, true);
		
		addPiece(new King(Position.createPosition(8, Position.E), Piece.BLACK), false, true);
	}
	
	public void setPossibleMoves()
	{	
		for(Piece p : myWhitePieces.values())
			p.initilizePossibilityOfMoves(this);
		for(Piece p : myBlackPieces.values())
			p.initilizePossibilityOfMoves(this);
	}
	
	public void removePiece(Piece p)
	{
		getMapForAffinity(p.getAffinity()).remove(p.getCurrentPosition());
		myMoveLogger.pieceRemoved(p);
	}
	
	/**
	 * Removes the pawn from the board and replaces him with a Queen
	 * @param p the pawn to replace
	 */
	public void replacePawn(Piece p)
	{
		//Remove pawn
		p.removeFromBoard(this);
		
		//Replace him with a Queen
		addPiece(new Queen(p.getCurrentPosition(), p.getAffinity()), true, false);
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
	
	public HashMultimap<Position, Move> getAvailableMoves(boolean affinity)
	{
		if(affinity == Piece.WHITE)
			return myWhiteAvailableMoves;
		
		return myBlackAvailableMoves;	
	}
	
	/**
	 * 
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
				else
				{
					if(!m.isPieceBlockingMe(position, pieceThatWonders.getCurrentPosition()))
						return m;
				}
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
	 * 
	 * @param position the threatened position
	 * @param affinity the affinity of the player that should threaten the position
	 * @return true if the position is threatened
	 */
	public boolean isMoveThreateningPositionRightNow(Position position, boolean affinity)
	{
		return getAvailableMoves(position, affinity).size() > 0;
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
		List<Piece> list = new ArrayList<Piece>();
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
		updatePossibiltyForMapOfMoves(myWhiteAvailableMoves, position);
		updatePossibiltyForMapOfMoves(myWhiteNonAvailableMoves, position);
		updatePossibiltyForMapOfMoves(myBlackAvailableMoves, position);
		updatePossibiltyForMapOfMoves(myBlackNonAvailableMoves, position);
	}
	
	/**
	 * 
	 * @param moves
	 * @param pos
	 */
	private void updatePossibiltyForMapOfMoves(HashMultimap<Position, Move> moves, Position pos)
	{
		//As the move may be removed during this iteration will need a shallow copy of the move map
		ImmutableSet<Move> copy = ImmutableSet.copyOf(moves.get(pos));
		for(Move m : copy)
			m.updateMove(this);
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

	public void clear()
	{
		myWhitePieces.clear();
		myWhiteAvailableMoves.clear();
		myWhiteNonAvailableMoves.clear();
		myBlackPieces.clear();
		myBlackAvailableMoves.clear();
		myBlackNonAvailableMoves.clear();
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
	
	public void readPersistanceData(InputStream stream) throws IOException
	{
		//Read the state of the game
		readGameStatePersistanceData(stream);
		
		//Read each piece to the file
		byte[] piece = new byte[2];
		int readBytes = 0;
		while(readBytes != -1)
		{
			readBytes = stream.read(piece);
			if(readBytes == 2)
			{
				Piece p = Piece.getPieceFromPersistanceData((short)(piece[0] << 8 | piece[1]));
				if(p != null)
					addPiece(p, false, false);
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

	public boolean inPlay()
	{
		return ChessState.PLAYING == getCurrentState() || ChessState.CHECK == getCurrentState();
	}

}
