package com.jjonsson.chess.persistance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;

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
	public static boolean loadStreamIntoBoard(InputStream input, ChessBoard boardToLoadInto)
	{
		BufferedInputStream bis = new BufferedInputStream(input);
		try
		{
			boardToLoadInto.readPersistanceData(bis);
		}
		catch (IOException e)
		{
			return false;
		}
		catch (InvalidPosition e)
		{
			return false;
		}
		boardToLoadInto.setPossibleMoves();
		boardToLoadInto.updateGameState();

		return true;
	}
	
	/**
	 * 
	 * @param board the board to save
	 * @param pathToFile a path to the file to save the board to
	 * @return true if the board was successfully written to the given file
	 */
	public static boolean saveBoard(ChessBoard board, String pathToFile)
	{
		if(pathToFile == null)
		{
			return false;
		}
		
		File toFile = new File(pathToFile);
		if(!toFile.exists())
		{
			try
			{
				toFile.createNewFile();
			}
			catch (IOException e)
			{
				return false;
			}
		}
		boolean wroteToFile = false;
		FileOutputStream fos;
		BufferedOutputStream bos = null;
		try
		{
			fos = new FileOutputStream(toFile);
			bos = new BufferedOutputStream(fos);
			board.writePersistanceData(bos);
			wroteToFile = true;
		}
		catch (FileNotFoundException e)
		{
		}
		catch (IOException e)
		{
		}
		finally
		{
			try
			{
				bos.close();
			}
			catch (IOException e)
			{
				wroteToFile = false;
			}
		}
		return wroteToFile;
	}
}
