package com.jjonsson.chess.utilities;

import static com.jjonsson.utilities.Loggers.STDERR;
import static junit.framework.Assert.fail;

import java.net.URL;

import com.jjonsson.utilities.FileSystem;

public final class VersionControlHelper
{
	private VersionControlHelper(){};

	/**
	 * Make sure a file needed for testing is version controlled
	 */
	public static void assertThatResourceIsVersionControlled(final String resourcePath, final boolean ignoreExceptions)
	{
		try
		{
			URL url = VersionControlHelper.class.getResource(resourcePath);
			if(!FileSystem.isVersionControlled(url.getFile()))
			{
				fail("Test case resource is not under version control: " + url.getFile());
			}
		}
		catch (Exception e)
		{
			if(ignoreExceptions)
			{
				STDERR.debug("Ignoring that a testcase resource isn't version controlled", e);
			}
			else
			{
				fail("Got exception while determining versioning status for : " + resourcePath);
			}
		}
	}
}
