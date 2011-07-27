package com.jjonsson.utilities.logging;

/**
 * A logging handler that logs it's output to stdout instead of stderr as the default ConsoleHandler does.
 * @author jonatanjoensson
 *
 */
public class ConsoleHandler extends java.util.logging.ConsoleHandler
{
	public ConsoleHandler()
	{
		super();
		setOutputStream(System.out);
	}

}
