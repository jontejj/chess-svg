package com.jjonsson.chess.gui;

import static com.jjonsson.chess.gui.KeyboardActions.getKeyStrokeForAction;
import static com.jjonsson.chess.gui.KeyboardActions.Action.EXIT;
import static com.jjonsson.chess.gui.KeyboardActions.Action.LOAD;
import static com.jjonsson.chess.gui.KeyboardActions.Action.NEW;
import static com.jjonsson.chess.gui.KeyboardActions.Action.RELOAD;
import static com.jjonsson.chess.gui.KeyboardActions.Action.SAVE;
import static com.jjonsson.chess.gui.KeyboardActions.Action.SAVE_AS;
import static com.jjonsson.chess.gui.KeyboardActions.Action.SHOW_HINT;
import static com.jjonsson.chess.gui.KeyboardActions.Action.SHOW_STATISTICS;
import static com.jjonsson.chess.gui.KeyboardActions.Action.UNDO;
import static com.jjonsson.chess.gui.KeyboardActions.Action.UNDO_TWICE;
import static com.jjonsson.utilities.CrossPlatformUtilities.USUAL_TITLE_HEIGHT;
import static com.jjonsson.utilities.CrossPlatformUtilities.getTitleHeightForCurrentPlatform;
import static com.jjonsson.utilities.Loggers.STDOUT;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.fest.swing.util.Platform;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.board.PiecePlacement;
import com.jjonsson.chess.evaluators.statistics.StatisticsAction;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.listeners.StatusListener;
import com.jjonsson.chess.persistence.BoardLoader;
import com.jjonsson.chess.persistence.ChessFileFilter;
import com.jjonsson.chess.persistence.PersistanceLogging;
import com.jjonsson.utilities.ThreadTracker;

public class ChessWindow extends JFrame implements ActionListener, StatusListener
{
	public static final String VERSION = "0.2";
	public static final String NAME = "Chess";
	public static final String APP_TITLE = NAME + " " + VERSION;

	private static final long	serialVersionUID	= 1L;

	public static final int DEFAULT_WINDOW_WIDTH = 700;
	public static final int DEFAULT_WINDOW_HEIGHT = 700;

	private static final int STATUS_BAR_HEIGHT = 20;
	private static final int WINDOW_BORDER_SIZE = 3;

	@VisibleForTesting
	public static final String	FILE_MENU_NAME	= "File";

	@VisibleForTesting
	public static final String NEW_MENU_ITEM = "New";
	private static final String LOAD_MENU_ITEM = "Load";
	private static final String	RELOAD_MENU_ITEM	= "Reload";
	private static final String SAVE_MENU_ITEM = "Save";
	private static final String SAVE_AS_MENU_ITEM = "Save As";
	@VisibleForTesting
	public static final String EXIT_MENU_ITEM = "Exit";

	public static final String	SETTINGS_MENU_NAME	= "Settings";

	@VisibleForTesting
	public static final String DISABLE_AI_MENU_ITEM = "Disable Computer Player";
	private static final String ENABLE_AI_MENU_ITEM = "Enable Computer Player";
	@VisibleForTesting
	public static final String	SHOW_AVAILABLE_CLICKS_MENU_ITEM	= "Show Available Clicks";
	private static final String	HIDE_AVAILABLE_CLICKS_MENU_ITEM	= "Hide Available Clicks";

	@VisibleForTesting
	public static final String	ACTIONS_MENU_NAME	= "Actions";

	@VisibleForTesting
	public static final String UNDO_BLACK_MENU_ITEM = "Undo Last Move";
	private static final String UNDO_WHITE_MENU_ITEM = "Undo Last Two Moves";
	@VisibleForTesting
	public static final String SHOW_HINT_MENU_ITEM = "Show Hint";
	@VisibleForTesting
	public static final String	SHOW_STATISTICS_MENU_ITEM = "Show Statistics";

	private String lastFileChooserLocation;

	private ChessBoard myBoard;
	private ChessBoardComponent myComponent;

	private String myCurrentBoardFile;

	private JLabel myStatusBar;

	/**
	 * Tells us the status of the game, who's turn it is, the last move that was made
	 */
	private String myGameStatus;

	/**
	 * The current text within the parentheses in the status bar, loading/saving result, reverting moves result
	 */
	private String myInteractionResultText;

	private ThreadTracker myTracker;

	/**
	 * Makes it possible to view the current speed of evaluations
	 */
	private StatisticsWindow myStatisticsWindow;

	public ChessWindow(final ChessBoard board, final DisplayOption displayOption)
	{
		super(NAME);
		myBoard = board;
		myTracker = new ThreadTracker();

		this.setBackground(Color.DARK_GRAY);
		createMenuBar();

		this.setSize(ChessWindow.DEFAULT_WINDOW_WIDTH + WINDOW_BORDER_SIZE, ChessWindow.DEFAULT_WINDOW_HEIGHT + WINDOW_BORDER_SIZE + getJMenuBar().getHeight() + STATUS_BAR_HEIGHT);
		myComponent = new ChessBoardComponent(myBoard, getBoardComponentSize());

		myComponent.setStatusListener(this);
		this.setContentPane(myComponent);

		myStatisticsWindow = new StatisticsWindow();
		board.setStatisticsListener(myStatisticsWindow);

		this.addWindowListener(new WindowListener());
		this.addComponentListener(new ResizeComponentAdapter(this));

		createStatusBar();
		if(displayOption.shouldDisplay())
		{
			displayGame();
		}
	}

	/**
	 * Calling this has the same affect as calling {@link ChessWindow#ChessWindow(ChessBoard, DisplayOption) with {@link DisplayOption#DISPLAY}
	 */
	public void displayGame()
	{
		this.setVisible(true);
		myComponent.repaint();
		if(Settings.DEMO)
		{
			myComponent.nextPlayer();
		}
	}

	@Override
	public void dispose()
	{
		myStatisticsWindow.dispose();
		getBoard().performStatisticsAction(StatisticsAction.INTERRUPT_TRACKING);
		super.dispose();
	}

	public ThreadTracker getThreadTracker()
	{
		return myTracker;
	}

	@VisibleForTesting
	public ChessBoardComponent getBoardComponent()
	{
		return myComponent;
	}

	public final Dimension getBoardComponentSize()
	{
		return new Dimension(getSize().width + WINDOW_BORDER_SIZE, getSize().height - getJMenuBar().getHeight() - STATUS_BAR_HEIGHT - USUAL_TITLE_HEIGHT);
	}

	private void createStatusBar()
	{
		myStatusBar = new JLabel();
		resizeStatusBar();
		updateStatusBar();
		myStatusBar.setOpaque(true);
		myStatusBar.setBackground(Color.white);
		add(myStatusBar, java.awt.BorderLayout.SOUTH);
	}

	private void resizeStatusBar()
	{
		myStatusBar.setSize(getSize().width, STATUS_BAR_HEIGHT);
		myStatusBar.setLocation(0, getSize().height - STATUS_BAR_HEIGHT - getTitleHeightForCurrentPlatform());
	}

	public final void updateStatusBar()
	{
		myInteractionResultText = "";
		myGameStatus = getBoard().getStatusString();
		myStatusBar.setText(myGameStatus);
	}

	@Override
	public void setTitle(final String info)
	{
		super.setTitle(NAME + " - " + info);
	}


	public ChessBoard getBoard()
	{
		return myBoard;
	}

	public void resizeWindow()
	{
		resizeStatusBar();
		myComponent.resizeBoard(getBoardComponentSize());
	}
	private void createMenuBar()
	{
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		menuBar.add(createFileMenu());
		menuBar.add(createActionsMenu());
		menuBar.add(createSettingsMenu());
	}

	private JMenu createFileMenu()
	{
		JMenu fileMenu = new JMenu(FILE_MENU_NAME);

		JMenuItem newAction = new JMenuItem(NEW_MENU_ITEM);
		newAction.setAccelerator(getKeyStrokeForAction(NEW));
		newAction.addActionListener(this);
		fileMenu.add(newAction);

		JMenuItem loadAction = new JMenuItem(LOAD_MENU_ITEM);
		loadAction.setAccelerator(getKeyStrokeForAction(LOAD));
		loadAction.addActionListener(this);
		fileMenu.add(loadAction);

		JMenuItem reloadAction = new JMenuItem(RELOAD_MENU_ITEM);
		reloadAction.setAccelerator(getKeyStrokeForAction(RELOAD));
		reloadAction.addActionListener(this);
		fileMenu.add(reloadAction);

		JMenuItem saveAction = new JMenuItem(SAVE_MENU_ITEM);
		saveAction.setAccelerator(getKeyStrokeForAction(SAVE));
		saveAction.addActionListener(this);
		fileMenu.add(saveAction);

		JMenuItem saveAsAction = new JMenuItem(SAVE_AS_MENU_ITEM);
		saveAsAction.setAccelerator(getKeyStrokeForAction(SAVE_AS));
		saveAsAction.addActionListener(this);
		fileMenu.add(saveAsAction);

		//Mac's already have a default menu with an exit action
		if(!Platform.isMacintosh())
		{
			fileMenu.addSeparator();

			JMenuItem exitAction = new JMenuItem(EXIT_MENU_ITEM);
			exitAction.setAccelerator(getKeyStrokeForAction(EXIT));
			exitAction.addActionListener(this);
			fileMenu.add(exitAction);
		}
		fileMenu.addSeparator();

		return fileMenu;
	}

	private JMenu createActionsMenu()
	{
		JMenu actionsMenu = new JMenu(ACTIONS_MENU_NAME);

		JMenuItem undoBlack = new JMenuItem(UNDO_BLACK_MENU_ITEM);
		undoBlack.setAccelerator(getKeyStrokeForAction(UNDO));
		undoBlack.addActionListener(this);
		actionsMenu.add(undoBlack);

		JMenuItem undoWhite = new JMenuItem(UNDO_WHITE_MENU_ITEM);
		undoWhite.setAccelerator(getKeyStrokeForAction(UNDO_TWICE));
		undoWhite.addActionListener(this);
		actionsMenu.add(undoWhite);

		JMenuItem showHint = new JMenuItem(SHOW_HINT_MENU_ITEM);
		showHint.setAccelerator(getKeyStrokeForAction(SHOW_HINT));
		showHint.addActionListener(this);
		actionsMenu.add(showHint);

		JMenuItem showStatistics = new JMenuItem(SHOW_STATISTICS_MENU_ITEM);
		showStatistics.setAccelerator(getKeyStrokeForAction(SHOW_STATISTICS));
		showStatistics.addActionListener(this);
		actionsMenu.add(showStatistics);

		return actionsMenu;
	}

	private JMenu createSettingsMenu()
	{
		JMenu settingsMenu = new JMenu(SETTINGS_MENU_NAME);

		JMenuItem showAvailableClicks = null;
		if(Settings.DEBUG || Settings.DEMO)
		{
			showAvailableClicks = new JMenuItem(HIDE_AVAILABLE_CLICKS_MENU_ITEM);
		}
		else
		{
			showAvailableClicks = new JMenuItem(SHOW_AVAILABLE_CLICKS_MENU_ITEM);
		}

		showAvailableClicks.addActionListener(this);
		settingsMenu.add(showAvailableClicks);

		JMenuItem disableAI = new JMenuItem(DISABLE_AI_MENU_ITEM);
		disableAI.addActionListener(this);
		settingsMenu.add(disableAI);

		//TODO: make it possible to change difficulty

		return settingsMenu;
	}

	private void save(final boolean forceDialog)
	{
		if(myCurrentBoardFile == null || forceDialog)
		{
			selectFile("Save Chess File");
		}

		if(BoardLoader.saveBoard(getBoard(), myCurrentBoardFile))
		{
			setResultOfInteraction("Saved successfully");
		}
		else
		{
			setResultOfInteraction("Save failed");
		}
	}

	private void newGame()
	{
		myComponent.clear();
		myBoard.reset();
		myComponent.nextPlayer();
	}

	/**
	 * Sets the text within the parentheses of the status bar
	 * @param msg
	 */
	@Override
	public void setResultOfInteraction(final String msg)
	{
		myInteractionResultText = msg;
		myStatusBar.setText(myGameStatus + " (" + msg + ")");
	}

	@Override
	public void setProgressInformation(final String msg)
	{
		myStatusBar.setText(myGameStatus + " (" + myInteractionResultText + ") (" + msg + ")");
	}

	private void load()
	{
		File selectedFile = selectFile("Load Chess File");
		if(myCurrentBoardFile != null)
		{
			boolean loadOk = false;
			while(!loadOk && myCurrentBoardFile != null)
			{
				//First make a test load
				if(BoardLoader.loadFileIntoBoard(selectedFile, new ChessBoard(PiecePlacement.DONT_PLACE_PIECES, PersistanceLogging.USE_PERSISTANCE_LOGGING)))
				{
					//The board seems to look fine, replace the board connected to the GUI with this one
					getBoard().clear();
					myComponent.clear();
					loadOk = BoardLoader.loadFileIntoBoard(selectedFile, getBoard());
					if(loadOk)
					{
						break;
					}
				}
				myStatusBar.setText(myGameStatus + " (Invalid board file format, Select new file to load)");
				selectedFile = selectFile("Load Chess File");
			}
			myComponent.loadingOfBoardDone();

			if(loadOk)
			{
				setResultOfInteraction("Load Ok");
			}
			else
			{
				setResultOfInteraction("Load Cancelled");
			}
		}
	}

	private void reload()
	{
		if(myCurrentBoardFile != null)
		{
			getBoard().clear();
			myComponent.clear();
			if(BoardLoader.loadFileIntoBoard(new File(myCurrentBoardFile), getBoard()))
			{
				setResultOfInteraction("Reload Ok");
			}
			else
			{
				setResultOfInteraction("Reload failed! Starting New Game.");
				newGame();
			}
		}
		else
		{
			setResultOfInteraction("No game to reload, use load first.");
		}
	}

	private void undo(final int nrOfMoves)
	{
		int undoneMoves = getBoard().undoMoves(nrOfMoves);
		updateStatusBar();
		if(undoneMoves == 0)
		{
			setResultOfInteraction("Undo not possible");
		}
		else
		{
			setResultOfInteraction("Reverted " + undoneMoves + " moves");
		}
		myComponent.repaint();
	}

	/**
	 * Re-translates an exit action into a real window event
	 */
	private void exit()
	{

		WindowEvent we = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		for(java.awt.event.WindowListener wl : this.getWindowListeners())
		{
			wl.windowClosing(we);
		}
	}

	private void switchAI(final boolean enable)
	{
		myComponent.setAIEnabled(enable);
	}

	private static Set<String> noAbortionNecessaryCommands = Sets.newHashSet(
			SHOW_AVAILABLE_CLICKS_MENU_ITEM,
			HIDE_AVAILABLE_CLICKS_MENU_ITEM,
			SHOW_STATISTICS_MENU_ITEM);

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		STDOUT.debug(e.getActionCommand());
		if(!noAbortionNecessaryCommands.contains(e.getActionCommand()))
		{
			//Cancel current jobs such as when the AI is thinking of the next move or when a hint move is searched for
			myComponent.interruptCurrentJobs();
			myTracker.interruptCurrentJobs();
		}

		if(e.getActionCommand().equals(NEW_MENU_ITEM))
		{
			newGame();
		}
		else if(e.getActionCommand().equals(SAVE_MENU_ITEM))
		{
			save(false);
		}
		else if(e.getActionCommand().equals(SAVE_AS_MENU_ITEM))
		{
			save(true);
		}
		else if(e.getActionCommand().equals(LOAD_MENU_ITEM))
		{
			load();
		}
		else if(e.getActionCommand().equals(RELOAD_MENU_ITEM))
		{
			reload();
		}
		else if(e.getActionCommand().equals(UNDO_BLACK_MENU_ITEM))
		{
			undo(1);
		}
		else if(e.getActionCommand().equals(UNDO_WHITE_MENU_ITEM))
		{
			undo(2);
		}
		else if(e.getActionCommand().equals(SHOW_HINT_MENU_ITEM))
		{
			showHint();
		}
		else if(e.getActionCommand().equals(DISABLE_AI_MENU_ITEM))
		{
			switchAI(false);
			JMenuItem.class.cast(e.getSource()).setText(ENABLE_AI_MENU_ITEM);
		}
		else if(e.getActionCommand().equals(ENABLE_AI_MENU_ITEM))
		{
			switchAI(true);
			JMenuItem.class.cast(e.getSource()).setText(DISABLE_AI_MENU_ITEM);
		}
		else if(e.getActionCommand().equals(SHOW_AVAILABLE_CLICKS_MENU_ITEM))
		{
			myComponent.showAvailableClicks(true);
			JMenuItem.class.cast(e.getSource()).setText(HIDE_AVAILABLE_CLICKS_MENU_ITEM);
		}
		else if(e.getActionCommand().equals(HIDE_AVAILABLE_CLICKS_MENU_ITEM))
		{
			myComponent.showAvailableClicks(false);
			JMenuItem.class.cast(e.getSource()).setText(SHOW_AVAILABLE_CLICKS_MENU_ITEM);
		}
		else if(e.getActionCommand().equals(SHOW_STATISTICS_MENU_ITEM))
		{
			myStatisticsWindow.setVisible(true);
		}
		else if(e.getActionCommand().equals(EXIT_MENU_ITEM))
		{
			exit();
		}
	}

	private void showHint()
	{
		setResultOfInteraction("Thinking of a hint");
		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				myComponent.showHint();
				myTracker.removeJob(this);
			}
		};
		myTracker.addJob(t);
		t.start();

	}

	private File selectFile(final String buttonText)
	{
		myCurrentBoardFile = null;
		JFileChooser jfc = new JFileChooser(getFileChooserPath());
		jfc.setAcceptAllFileFilterUsed(true);
		jfc.setFileFilter(new ChessFileFilter());
		jfc.showDialog(this.getRootPane(), buttonText);
		File selectedFile = jfc.getSelectedFile();
		if(selectedFile != null)
		{
			if(!selectedFile.getAbsolutePath().endsWith(ChessFileFilter.FILE_ENDING))
			{
				myCurrentBoardFile = selectedFile.getAbsolutePath() + ChessFileFilter.FILE_ENDING;
			}
			else
			{
				myCurrentBoardFile = selectedFile.getAbsolutePath();
			}

			saveFileChooserPath(selectedFile.getParent());
			setTitle(selectedFile.getName());
		}
		return selectedFile;
	}

	private String getFileChooserPath()
	{
		if(lastFileChooserLocation == null)
		{
			try
			{
				lastFileChooserLocation = Files.toString(new File("chess.settings"), Charsets.UTF_8);
			}
			catch (IOException e)
			{
			}
		}
		return lastFileChooserLocation;
	}

	private void saveFileChooserPath(final String path)
	{
		if(path != null)
		{
			try
			{
				Files.write(path, new File("chess.settings"), Charsets.UTF_8);
			}
			catch (IOException e)
			{
			}
			lastFileChooserLocation = path;
		}
	}

	@Override
	public void statusHasBeenUpdated()
	{
		updateStatusBar();
	}
}
