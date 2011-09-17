package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.persistence.PersistanceLogging.SKIP_PERSISTANCE_LOGGING;
import static com.jjonsson.chess.persistence.PersistanceLogging.USE_PERSISTANCE_LOGGING;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.listeners.MoveListener;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.pieces.Piece;

public class PersistenceLogger implements MoveListener
{

	public static final int BYTES_PER_MOVE	= 2;
	private static final byte END_OF_MOVES_MARKER = (byte) 0xFF;

	private ChessBoard myStartBoard;
	private Deque<MoveItem> myPersistenceStorage;
	private boolean myIsApplyingMoveHistory;

	PersistenceLogger()
	{
		myPersistenceStorage = new ArrayDeque<MoveItem>();
	}

	public void setStartBoard(final ChessBoard board)
	{
		myStartBoard = board.copy(SKIP_PERSISTANCE_LOGGING);
	}

	public void writeMoveHistory(final ByteBuffer buffer)
	{
		buffer.put(myStartBoard.getGameStateSettingsByte(USE_PERSISTANCE_LOGGING));
		Iterator<MoveItem> fromFirstMoveToLastIterator = myPersistenceStorage.descendingIterator();
		while(fromFirstMoveToLastIterator.hasNext())
		{
			fromFirstMoveToLastIterator.next().put(buffer);
		}
		//End of moves indicator
		buffer.put(END_OF_MOVES_MARKER);
		myStartBoard.writePieces(buffer);
	}

	public int getPersistenceSize()
	{
		return myStartBoard.getPersistenceSize(SKIP_PERSISTANCE_LOGGING) + myPersistenceStorage.size() * BYTES_PER_MOVE + 1; //+1 for the end of moves indicator
	}

	public void readMoveHistory(final ByteBuffer buffer)
	{
		while(buffer.remaining() > 0)
		{
			byte fromByte = buffer.get();
			if(fromByte == END_OF_MOVES_MARKER)
			{
				//End of moves
				break;
			}
			ImmutablePosition fromPos = ImmutablePosition.from(fromByte);
			ImmutablePosition toPos = ImmutablePosition.from(buffer.get());
			myPersistenceStorage.push(MoveItem.from(fromPos, toPos));
		}
	}

	public void applyMoveHistory(final ChessBoard board) throws UnavailableMoveItem
	{
		myIsApplyingMoveHistory = true;
		Iterator<MoveItem> fromFirstMoveToLastIterator = myPersistenceStorage.descendingIterator();
		while(fromFirstMoveToLastIterator.hasNext())
		{
			MoveItem moveItem = fromFirstMoveToLastIterator.next();
			moveItem.perform(board);
		}
		myIsApplyingMoveHistory = false;
	}

	@Override
	public void movePerformed(final Move performedMove)
	{
		if(!myIsApplyingMoveHistory && !performedMove.isPartOfAnotherMove())
		{
			myPersistenceStorage.push(MoveItem.from(performedMove));
		}
	}

	@Override
	public void pieceRemoved(final Piece removedPiece)
	{
	}

	@Override
	public void moveReverted(final RevertingMove move)
	{
		if(!move.isPartOfAnotherMove())
		{
			myPersistenceStorage.pop();
		}
	}

	@Override
	public void reset()
	{
		myPersistenceStorage.clear();
	}
}
