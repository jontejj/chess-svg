package com.jjonsson.utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * Non-Critical exceptions should be logged to STDOUT,
 * Critical errors should be logged to STDERR
 */
public final class Loggers
{
	private Loggers(){}

	/**
	 * A logger that prints it's messages to standard err stream
	 */
	public static final Logger STDERR;
	/**
	 * A logger that prints it's messages to standard out
	 */
	public static final Logger STDOUT;
	static
	{
		PropertyConfigurator.configure(Loggers.class.getResource("/logging/log4j.conf"));
		STDERR = Logger.getLogger("stderr");
		STDOUT = Logger.getLogger("stdout");
		STDERR.setLevel(Level.ALL);
		STDOUT.setLevel(Level.INFO);
	}

}
