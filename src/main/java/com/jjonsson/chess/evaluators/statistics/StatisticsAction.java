package com.jjonsson.chess.evaluators.statistics;

public enum StatisticsAction
{
	RESET,
	MOVE_EVALUATED,
	MOVE_EVALUATION_STOPPED,
	/**
	 * Action used to stop the tracking thread
	 */
	INTERRUPT_TRACKING
}
