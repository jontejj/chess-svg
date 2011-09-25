package com.jjonsson.chess.utilities;

import static com.jjonsson.utilities.Loggers.STDERR;
import static junit.framework.Assert.fail;

import java.io.FileNotFoundException;
import java.net.URL;

import com.jjonsson.utilities.FileSystem;

public final class VersionControlHelper
{
	private VersionControlHelper(){};

	public enum ExceptionHandling
	{
		IGNORE_EXCEPTION,
		FAIL_ON_EXCEPTION;

		public boolean shouldFailBecauseOfException()
		{
			return this == FAIL_ON_EXCEPTION;
		}
	}
	/**
	 * Make sure a file needed for testing is version controlled
	 */
	public static void assertThatResourceIsVersionControlled(final String resourcePath, final ExceptionHandling exceptionHandling)
	{
		try
		{
			URL url = VersionControlHelper.class.getResource(resourcePath);
			if(url == null)
			{
				throw new FileNotFoundException(resourcePath);
			}
			if(!FileSystem.isVersionControlled(url.getFile()))
			{
				fail("Resource is not under version control: " + url.getFile());
			}
		}
		catch (Exception e)
		{
			if(exceptionHandling.shouldFailBecauseOfException())
			{
				STDERR.debug("", e);
				fail("Got exception while determining versioning status for : " + resourcePath);
			}
			else
			{
				STDERR.debug("Ignoring that a resource isn't version controlled", e);
			}
		}
	}
}
