package com.jjonsson.utilities;

import static org.fest.swing.util.Platform.isWindows;

public final class CrossPlatformUtilities
{
	private CrossPlatformUtilities(){}

	private static final int WINDOWS_TITLE_HEIGHT = 58;
	public static final int USUAL_TITLE_HEIGHT = 22;

	public static int getTitleHeightForCurrentPlatform()
	{
		if(isWindows())
		{
			return WINDOWS_TITLE_HEIGHT;
		}
		return USUAL_TITLE_HEIGHT;
	}
}
