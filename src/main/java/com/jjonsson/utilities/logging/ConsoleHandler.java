package com.jjonsson.utilities.logging;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A logging handler that logs it's output to stdout instead of stderr as the default ConsoleHandler does.
 * @author jonatanjoensson
 *
 */
public class ConsoleHandler extends java.util.logging.ConsoleHandler
{
	/**
	 * In contrast to min level, this is the max level that will be logged, useful when a error handler handles
	 * higher level logging
	 */
	Level myMaxLevel;

	public ConsoleHandler(final Level maxLevel, final OutputStream logStream)
	{
		super();
		myMaxLevel = maxLevel;
		setOutputStream(logStream);
	}

	@Override
	public void publish(final LogRecord record)
	{
		if(record.getLevel().intValue() <= myMaxLevel.intValue())
		{
			super.publish(record);
			flush();
		}
	}

}
