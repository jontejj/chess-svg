package com.jjonsson.chess.utilities;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.google.common.collect.Sets;
import com.jjonsson.chess.exceptions.UnableToDetermineStatusException;
import com.jjonsson.chess.persistence.ChessFileFilter;
import com.jjonsson.chess.utilities.VersionControlHelper.ExceptionHandling;
import com.jjonsson.utilities.FileSystem;


public class TestFilesystem
{
	@Test
	public void testDuplicateDetectionOfFiles() throws IOException
	{
		Set<File> actualDuplicates = setOfResourceFiles("/duplicate_test/", "file_a.txt", "file1.txt");

		String path = getClass().getResource("/duplicate_test/").getFile();
		Set<File> duplicates = FileSystem.getDuplicateFiles(path);

		assertEquals(actualDuplicates, duplicates);
		/*for(File duplicate : duplicates)
		{
			duplicate.delete();
			System.out.println("Deleted duplicate file: " + duplicate.getAbsolutePath());
		}*/
	}

	/**
	 * 
	 * @param resourcePath
	 * @param files
	 * @return a set containing File objects for each file in {@link files} that is found within the {@link resourcePath}
	 * @throws NullPointerException if a file in {@link files} doesn't exist directly under {@link resourcePath}
	 */
	private static Set<File> setOfResourceFiles(final String resourcePath, final String ...files)
	{
		Set<File> fileSet = Sets.newHashSet();
		for(String file : files)
		{
			fileSet.add(new File(Set.class.getResource(resourcePath + file).getFile()));
		}
		return fileSet;
	}

	@Test
	public void testIsVersionControlled()
	{
		//This file should be in version control
		VersionControlHelper.assertThatResourceIsVersionControlled("/scenarios/enpassant_possible" + ChessFileFilter.FILE_ENDING,
				ExceptionHandling.FAIL_ON_EXCEPTION);
	}

	@Test
	public void testIsNotVersionControlled() throws UnableToDetermineStatusException, IOException
	{
		//This file should not in version control
		assertFalse(FileSystem.isVersionControlled("/not_version_controlled"));
	}
}
