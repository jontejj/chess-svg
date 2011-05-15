package com.jjonsson.chess.persistance;

import java.io.File;

import javax.swing.filechooser.FileFilter;;

public class ChessFileFilter extends FileFilter
{
	public static String fileEnding = ".chess";

	@Override
	public boolean accept(File pathname)
	{				
		return pathname.getAbsolutePath().endsWith(fileEnding);
	}

	@Override
	public String getDescription()
	{
		return "Chess files";
	}

}
