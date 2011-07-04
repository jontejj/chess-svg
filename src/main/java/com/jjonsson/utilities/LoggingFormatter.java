package com.jjonsson.utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggingFormatter extends Formatter
{
	Date date = new Date();
    private final static String format = "{0,date} {0,time}";
    private final static MessageFormat formatter = new MessageFormat(format);
    private final static String lineSeparator = System.getProperty("line.separator");
    private Object args[] = new Object[]{date};
    
	@Override
	public String format(LogRecord record)
	{
		StringBuffer sb = new StringBuffer();
		// Minimize memory allocations here.
		date.setTime(record.getMillis());
		StringBuffer text = new StringBuffer();
		formatter.format(args, text, null);
		sb.append("[");
		sb.append(record.getLevel());
		sb.append("] ");
		sb.append(text);
		sb.append(": ");
		String message = formatMessage(record);
		sb.append(message);
		sb.append(lineSeparator);
		if (record.getThrown() != null) {
		    try {
		        StringWriter sw = new StringWriter();
		        PrintWriter pw = new PrintWriter(sw);
		        record.getThrown().printStackTrace(pw);
		        pw.close();
			sb.append(sw.toString());
		    } catch (Exception ex) {
		    }
		}
		return sb.toString();
	}

}
