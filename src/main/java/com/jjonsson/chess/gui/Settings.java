package com.jjonsson.chess.gui;

public final class Settings
{
	private Settings(){}

	public static boolean DEBUG = Boolean.valueOf(System.getenv("debug")) || Boolean.getBoolean("debug");
	public static final boolean DEMO = Boolean.valueOf(System.getenv("demomode")) || Boolean.getBoolean("demomode");

	public static boolean DISABLE_SAVING = false;

	public static void enableSaving()
	{
		DISABLE_SAVING = false;
	}
	public static void disableSaving()
	{
		DISABLE_SAVING = true;
	}

	public static void enableDebug()
	{
		DEBUG = true;
	}

	public static void disableDebug()
	{
		DEBUG = false;
	}
}
