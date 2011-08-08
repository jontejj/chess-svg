package com.jjonsson.utilities.logging;

import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import com.jjonsson.utilities.Logger;

public class LoggingFormatter extends Formatter
{
	private Date date = new Date();
    private static final String FORMAT = "{0,date} {0,time}";
    private static final MessageFormat FORMATTER = new MessageFormat(FORMAT);
    private static final String LINE_SEPERATOR = System.getProperty("line.separator");
    private Object args[] = new Object[]{date};
    
	@Override
	public String format(LogRecord record)
	{
		StringBuffer sb = new StringBuffer();
		// Minimize memory allocations here.
		date.setTime(record.getMillis());
		StringBuffer text = new StringBuffer();
		FORMATTER.format(args, text, null);
		sb.append("[");
		sb.append(record.getLevel());
		sb.append("] ");
		sb.append(text);
		sb.append(": ");
		String message = formatMessage(record);
		sb.append(message);
		sb.append(LINE_SEPERATOR);
		if (record.getThrown() != null) 
		{
			sb.append(Logger.stackTraceToString(record.getThrown()));
		}
		return sb.toString();
	}

}
