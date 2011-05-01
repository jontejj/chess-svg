package com.jjonsson.chess.persistance;

import java.io.File;

import javax.swing.filechooser.FileFilter;;

public class ChessFileFilter extends FileFilter
{

	@Override
	public boolean accept(File pathname)
	{
		return pathname.getAbsolutePath().endsWith(".chess");
	}

	@Override
	public String getDescription()
	{
		return "Chess files";
	}

}
