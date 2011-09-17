package com.jjonsson.utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.jjonsson.chess.exceptions.UnableToDetermineStatusException;

public final class FileSystem
{
	private FileSystem(){};

	/**
	 * Calculates the checksum of all the files directly in the given path and removes the first one
	 *  when duplicates are detected.
	 * @param path
	 * @return the amount of duplicates that were deleted, or -1 if any exception occurred
	 */
	public static int deleteDuplicateFiles(final String path)
	{
		try
		{
			File faultyBoardsDir = new File(path);
			Checksum crc = new CRC32();
			HashMap<Long, File> sums = Maps.newHashMap();
			Set<File> duplicates = Sets.newHashSet();
			for(File faultyBoard : faultyBoardsDir.listFiles())
			{
				long sum = Files.getChecksum(faultyBoard, crc);
				File oldFile = sums.put(sum, faultyBoard);
				if(oldFile != null)
				{
					duplicates.add(oldFile);
				}
			}
			for(File duplicate : duplicates)
			{
				duplicate.delete();
				System.out.println("Deleted duplicate file: " + duplicate.getAbsolutePath());
			}
			System.out.println("Deleted " + duplicates.size() + " files.");
			return duplicates.size();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * This checks if the given filePath is under version control by mercurial
	 * @param filePath
	 * @return
	 * @throws UnableToDetermineStatusException
	 * @throws IOException
	 */
	public static boolean isVersionControlled(final String filePath) throws UnableToDetermineStatusException, IOException
	{
		File hgBinary = new File("/usr/local/bin/hg");
		//TODO: add support for more VC's and for more operating systems
		if(hgBinary.canExecute())
		{
			Process p = Runtime.getRuntime().exec(hgBinary.getAbsolutePath() + " " + filePath);
			int hgStatusForFile = p.getInputStream().read();
			if((char)hgStatusForFile == '?')
			{
				return false;
			}
		}
		else
		{
			throw new UnableToDetermineStatusException("Could not execute hg");
		}
		return true;
	}
}
