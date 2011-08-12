package com.jjonsson.chess.persistence;

public final class MoveLoggerFactory
{

	public static PersistenceLogger createPersistenceLogger()
	{
		return new PersistenceLogger();
	}

	public static MoveLogger createMoveLogger()
	{
		return new MoveLogger();
	}

}
