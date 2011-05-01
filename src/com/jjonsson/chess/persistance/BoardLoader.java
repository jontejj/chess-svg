package com.jjonsson.chess.persistance;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
	public static boolean loadFileIntoBoard(File fileToLoad, ChessBoard boardToLoadInto)
	{
		if(fileToLoad == null)
			return false;
		
		try
		{
			FileInputStream fis = new FileInputStream(fileToLoad);
			BufferedInputStream bis = new BufferedInputStream(fis);
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
	public static boolean saveBoard(ChessBoard board, File toFile)
	{
		if(toFile == null)
			return false;
		
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
