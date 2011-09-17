package com.jjonsson.chess.persistence;

import static com.jjonsson.chess.gui.Settings.disableSaving;
import static com.jjonsson.chess.gui.Settings.enableSaving;
import static com.jjonsson.chess.persistence.PersistanceLogging.SKIP_PERSISTANCE_LOGGING;
import static com.jjonsson.chess.persistence.PersistanceLogging.USE_PERSISTANCE_LOGGING;
import static com.jjonsson.utilities.Loggers.STDERR;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Set;

import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.board.PiecePlacement;
import com.jjonsson.chess.exceptions.DuplicatePieceError;
import com.jjonsson.chess.exceptions.InvalidBoardException;
import com.jjonsson.chess.exceptions.UnavailableMoveItem;
import com.jjonsson.chess.gui.Settings;

public final class BoardLoader
{
	private BoardLoader(){}

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
		catch (DuplicatePieceError e)
		{
			STDERR.error("Got a duplicate piece: " + e.getDuplicatePiece() + ", conflicted with: " + e.getExistingPiece());
		}
		catch (InvalidBoardException e)
		{
			STDERR.error("Faulty board, detected that only one king exists. The possibility of moves needs to be fixed.");
		}
		catch (UnavailableMoveItem e)
		{
			STDERR.error("Could only apply " + boardToLoadInto.getMoveLogger().getMovesMade() + " moves because: " + e);
		}
		catch (ArrayIndexOutOfBoundsException e)
		{
			STDERR.error("Faulty position, index that is faulty: " + e);
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
			if(Settings.DISABLE_SAVING)
			{
				return false;
			}
			//TODO: perhaps this should be optional?
			PersistanceLogging persistanceLogging = board.hasPersistencePossibility() ? USE_PERSISTANCE_LOGGING : SKIP_PERSISTANCE_LOGGING;
			ByteBuffer buffer = ByteBuffer.allocate(board.getPersistenceSize(persistanceLogging));

			board.writePersistenceData(buffer, persistanceLogging);
			buffer.flip();
			Files.write(buffer.array(), new File(pathToFile));
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	public static void cleanUnloadableBoards(final String path)
	{
		disableSaving();
		ChessBoard board = new ChessBoard(PiecePlacement.DONT_PLACE_PIECES, PersistanceLogging.USE_PERSISTANCE_LOGGING);

		File faultyBoardsDir = new File(path);
		Set<File> failedBoards = Sets.newHashSet();
		for(File faultyBoard : faultyBoardsDir.listFiles())
		{
			board.clear();
			if(!loadFileIntoBoard(faultyBoard, board))
			{
				failedBoards.add(faultyBoard);
			}
		}
		for(File failedBoard : failedBoards)
		{
			System.out.println("Deleted: " + failedBoard);
			failedBoard.delete();
		}
		System.out.println("Failed boards: " + failedBoards.size());
		enableSaving();
	}
}
