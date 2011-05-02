package com.jjonsson.chess.persistance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.google.common.io.OutputSupplier;
import com.jjonsson.chess.ChessBoard;

public class BoardLoader
{
	/**
	 * 
	 * @param boardFile to file to load the board from
	 * @return the loaded board
	 */
	public static boolean loadFileIntoBoard(String pathToFile, ChessBoard boardToLoadInto)
	{
		if(pathToFile == null)
			return false;
		
		try
		{
			InputStream is = BoardLoader.class.getResourceAsStream(pathToFile);
			BufferedInputStream bis = new BufferedInputStream(is);
			boardToLoadInto.readPersistanceData(bis);
			boardToLoadInto.setPossibleMoves();
			boardToLoadInto.updateGameState();
			return true;
		}
		catch (FileNotFoundException e)
		{	
			return false;
		}
		catch (Throwable t)
		{
			return false;
		}
	}
	
	/**
	 * 
	 * @param board the board to save
	 * @param toFile the file to save the board to
	 * @return true if the board was successfully written to the given file
	 */
	public static boolean saveBoard(ChessBoard board, String pathToFile)
	{
		if(pathToFile == null)
			return false;
		
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
