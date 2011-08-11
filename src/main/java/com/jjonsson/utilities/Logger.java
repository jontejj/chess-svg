package com.jjonsson.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;

import com.jjonsson.utilities.logging.ConsoleHandler;
import com.jjonsson.utilities.logging.LoggingFormatter;

public final class Logger
{
	private Logger()
	{

	}

	/**
	 * Non-Critical exceptions should be logged to info,
	 * Regular debug printouts should use finest
	 */
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("global");

	static
	{
		try
		{
			//Log unimportant events to stdout and important events to stderr
			ConsoleHandler stdout = new ConsoleHandler(Level.INFO, System.out);
			stdout.setFormatter(new LoggingFormatter());
			stdout.setLevel(Level.ALL);

			ConsoleHandler stderr = new ConsoleHandler(Level.SEVERE, System.err);
			stderr.setFormatter(new LoggingFormatter());
			stderr.setLevel(Level.WARNING);

			// create a 'file' handler (default level = ALL)
			/*FileHandler fh = new FileHandler("myAppLogFile%g.log", 50000, 10, false);
			fh.setFormatter(new SimpleFormatter());
			fh.setLevel(Level.ALL);*/

			LOGGER.setUseParentHandlers(false); // turn off the default handler
			LOGGER.addHandler(stdout);
			LOGGER.addHandler(stderr);
			LOGGER.setLevel(Level.ALL);
		}
		catch (SecurityException e)
		{
		}
	}

	public static String stackTraceToString(final Throwable t)
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}
}
