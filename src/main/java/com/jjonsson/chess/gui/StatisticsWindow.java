package com.jjonsson.chess.gui;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.jjonsson.chess.evaluators.statistics.StatisticsSnapshot;
import com.jjonsson.chess.listeners.StatisticsListener;

public class StatisticsWindow extends JFrame implements StatisticsListener
{
	private static final long	serialVersionUID	= 7251930330794829606L;

	private static final JLabel MOVES_MADE_TEXT = new JLabel("Moves evaluated");
	static
	{
		MOVES_MADE_TEXT.setPreferredSize(new Dimension(100, 30));
	}
	private JLabel myMovesMadeCounter = new JLabel("0");
	private static final JLabel AVERAGE_TIME_TEXT = new JLabel("Average moves per second");
	private JLabel myAverageTime = new JLabel("0");
	private static final JLabel HIGHEST_SPEED_TEXT = new JLabel("Most moves evaluated per second");
	private JLabel myHighestSpeedLabel = new JLabel("0");
	private static final JLabel MOVE_EVALUATION_TIME_TEXT = new JLabel("Running time (in seconds) of evaluation");
	private JLabel myMoveEvaluationTime = new JLabel("0");

	private long myHighestSpeed;

	public StatisticsWindow()
	{
		setTitle("Statistics Window");
		setLayout(new GridLayout(4, 2, 10, 0));
		setSize(150, 60);
		add(MOVES_MADE_TEXT);
		add(myMovesMadeCounter);
		add(AVERAGE_TIME_TEXT);
		add(myAverageTime);
		add(MOVE_EVALUATION_TIME_TEXT);
		add(myMoveEvaluationTime);
		add(HIGHEST_SPEED_TEXT);
		add(myHighestSpeedLabel);
		pack();
	}
	@Override public void newStatistics(final StatisticsSnapshot snapshot)
	{
		if(isVisible())
		{
			myMovesMadeCounter.setText("" + snapshot.getMovesMade());
			long speed = snapshot.getMovesEvaluatedPerSecond();
			myAverageTime.setText("" + speed);
			if(myHighestSpeed < speed)
			{
				myHighestSpeed = speed;
				myHighestSpeedLabel.setText("" + myHighestSpeed);
			}
			myMoveEvaluationTime.setText("" + snapshot.getTotalTimeInSeconds());
		}
	}

	@Override public void wasInterrupted(final InterruptedException ie)
	{
		//TODO: handle/visualize this
	}

	@Override public long notificationIntervalInNanos()
	{
		return NANOSECONDS.convert(200L, MILLISECONDS);
	}
}
