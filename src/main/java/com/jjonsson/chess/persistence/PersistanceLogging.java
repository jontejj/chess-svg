package com.jjonsson.chess.persistence;

public enum PersistanceLogging
{
	/**
	 * When this is used moves are saved when the board is saved
	 */
	USE_PERSISTANCE_LOGGING,
	/**
	 * When this is used move history isn't saved when the board is saved
	 */
	SKIP_PERSISTANCE_LOGGING;

	public boolean usePersistanceLogging()
	{
		return this == USE_PERSISTANCE_LOGGING;
	}
}