package com.jjonsson.chess.persistence;

import static com.jjonsson.utilities.Logger.LOGGER;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.exceptions.DuplicatePieceException;
import com.jjonsson.chess.exceptions.InvalidBoardException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;

public final class BoardLoader
{
	private BoardLoader()
	{

	}

	/**
	 * @param input the stream to load the board from
	 * @param boardToLoadInto the board to load the board into
	 * @return true if the board was loaded successfully
	 */
	public static boolean loadStreamIntoBoard(final InputStream input, final ChessBoard boardToLoadInto)
	{
		BufferedInputStream bis = new BufferedInputStream(input);
		try
		{
			ByteBuffer buffer = ByteBuffer.wrap(ByteStreams.toByteArray(bis));
			return loadBufferIntoBoard(buffer, boardToLoadInto);
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public static boolean loadFileIntoBoard(final File file, final ChessBoard boardToLoadInto)
	{
		try
		{
			ByteBuffer buffer = ByteBuffer.wrap(Files.toByteArray(file));
			return loadBufferIntoBoard(buffer, boardToLoadInto);
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public static boolean loadBufferIntoBoard(final ByteBuffer buffer, final ChessBoard boardToLoadInto)
	{
		try
		{
			boardToLoadInto.readPersistenceData(buffer);
			boardToLoadInto.setPossibleMoves();
			boardToLoadInto.updateGameState();
			boardToLoadInto.updatePersistenceLogger();
			boardToLoadInto.applyMoveHistory();
			return true;
		}
		catch (DuplicatePieceException e)
		{
			LOGGER.severe("Got a duplicate piece: " + e.getDuplicatePiece() + ", conflicted with: " + e.getExistingPiece());
		}
		catch (InvalidBoardException e)
		{
			LOGGER.severe("Faulty board, detected that only one king exists. The possibility of moves needs to be fixed.");
		}
		catch (UnavailableMoveItem e)
		{
			LOGGER.info("Could only apply " + boardToLoadInto.getMoveLogger().getMovesMade() + " moves because: " + e);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			LOGGER.warning("Faulty position, index that is faulty: " + e);
		}
		return false;
	}

	/**
	 * 
	 * @param board the board to save
	 * @param pathToFile a path to the file to save the board to
	 * @return true if the board was successfully written to the given file
	 */
	public static boolean saveBoard(final ChessBoard board, final String pathToFile)
	{
		try
		{
			//TODO: perhaps this should be optional?
			boolean includeMovesPossible = board.hasPersistencePossibility();
			ByteBuffer buffer = ByteBuffer.allocate(board.getPersistenceSize(includeMovesPossible));

			board.writePersistenceData(buffer, includeMovesPossible);
			buffer.flip();
			Files.write(buffer.array(), new File(pathToFile));
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}
}
