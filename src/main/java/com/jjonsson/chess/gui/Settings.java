package com.jjonsson.chess.gui;

public final class Settings
{
	private Settings()
	{

	}

	public static final boolean DEBUG = Boolean.valueOf(System.getenv("debug")) || Boolean.getBoolean("debug");
	public static final boolean DEMO = Boolean.valueOf(System.getenv("demomode")) || Boolean.getBoolean("demomode");
}
