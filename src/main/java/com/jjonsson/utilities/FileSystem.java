package com.jjonsson.utilities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.jjonsson.chess.exceptions.UnableToDetermineStatusException;

public final class FileSystem
{
	private FileSystem(){};

	/**
	 * Calculates the checksum of all the files directly in the given path and returns a set of duplicates.
	 * Only the first of two duplicates is returned, which means that the set only contains one of the files that had the same checksum.
	 * @param path the path to look for duplicates in (non-recursive)
	 * @return a set of duplicate files
	 * @throws IOException - if an I/O error occurs
	 */
	public static Set<File> getDuplicateFiles(final String path) throws IOException
	{
		Set<File> duplicates = Sets.newHashSet();
		File duplicatesDir = new File(path);
		Checksum crc = new CRC32();
		HashMap<Long, File> sums = Maps.newHashMap();
		for(File file : duplicatesDir.listFiles())
		{
			long sum = Files.getChecksum(file, crc);
			File oldFile = sums.put(sum, file);
			if(oldFile != null)
			{
				duplicates.add(oldFile);
			}
		}
		return duplicates;
	}
	/**
	 * This checks if the given filePath is under version control by mercurial
	 * @param filePath a path to a file, directories aren't supported yet
	 * @return
	 * @throws UnableToDetermineStatusException
	 * @throws IOException
	 */
	public static boolean isVersionControlled(final String filePath) throws UnableToDetermineStatusException, IOException
	{
		File hgBinary = new File("/usr/local/bin/hg");
		//TODO: add support for more VCS's and for more operating systems
		if(!hgBinary.canExecute())
		{
			throw new UnableToDetermineStatusException("Could not execute hg");
		}

		Process statusCheckProcess = new ProcessBuilder(hgBinary.getAbsolutePath(), "status", filePath).start();
		byte[] errors = ByteStreams.toByteArray(statusCheckProcess.getErrorStream());
		//Assume that any error message means that the file isn't version controlled
		if(errors.length > 0)
		{
			return false;
		}
		int hgStatusForFile = statusCheckProcess.getInputStream().read();
		/** hg status returns:
			empty string : for a version controlled file that hasn't been changed in since the last commit
			A : for an added file since the last commit
			M : for a modified file since the last commit
		 */
		if(hgStatusForFile == -1 || (char)hgStatusForFile == 'A' || (char)hgStatusForFile == 'M')
		{
			return true;
		}
		return false;
	}
}
