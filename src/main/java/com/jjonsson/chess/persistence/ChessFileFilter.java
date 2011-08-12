package com.jjonsson.chess.persistence;

import java.io.File;

import javax.swing.filechooser.FileFilter;;

public class ChessFileFilter extends FileFilter
{
	public static final String FILE_ENDING = ".chess";

	@Override
	public boolean accept(File pathname)
	{				
		return pathname.getAbsolutePath().endsWith(FILE_ENDING);
	}

	@Override
	public String getDescription()
	{
		return "Chess files";
	}

}
