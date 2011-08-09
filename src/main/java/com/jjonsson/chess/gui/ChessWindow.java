package com.jjonsson.chess.gui;

import static com.jjonsson.utilities.CrossPlatformUtilities.USUAL_TITLE_HEIGHT;
import static com.jjonsson.utilities.CrossPlatformUtilities.getExitKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.getLoadKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.getNewKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.getSaveAsKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.getSaveKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.getShowHintKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.getTitleHeightForCurrentPlatform;
import static com.jjonsson.utilities.CrossPlatformUtilities.getUndoKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.getUndoTwiceKeyStroke;
import static com.jjonsson.utilities.CrossPlatformUtilities.isMac;
import static com.jjonsson.utilities.Logger.LOGGER;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.persistance.BoardLoader;
import com.jjonsson.chess.persistance.ChessFileFilter;
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
	public static final String NEW_MENU_ITEM = "New";
	private static final String LOAD_MENU_ITEM = "Load";
	private static final String SAVE_MENU_ITEM = "Save";
	private static final String SAVE_AS_MENU_ITEM = "Save As";
	@VisibleForTesting
	public static final String DISABLE_AI_MENU_ITEM = "Disable Computer Player";
	private static final String ENABLE_AI_MENU_ITEM = "Enable Computer Player";
	@VisibleForTesting
	public static final String EXIT_MENU_ITEM = "Exit";
	@VisibleForTesting
	public static final String UNDO_BLACK_MENU_ITEM = "Undo Last Move";
	private static final String UNDO_WHITE_MENU_ITEM = "Undo Last Two Moves";
	private static final String SHOW_HINT_MENU_ITEM = "Show Hint";

	@VisibleForTesting
	public static final String	SHOW_AVAILABLE_CLICKS_MENU_ITEM	= "Show Available Clicks";

	private static final String	HIDE_AVAILABLE_CLICKS_MENU_ITEM	= "Hide Available Clicks";

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

	public ChessWindow(final ChessBoard board)
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

		this.addWindowListener(new WindowListener());
		this.addComponentListener(new ComponentAdapter(this));

		createStatusBar();
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

	public void displayGame()
	{
		this.setVisible(true);
		myComponent.repaint();
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
		JMenu fileMenu = new JMenu("File");

		JMenuItem newAction = new JMenuItem(NEW_MENU_ITEM);
		newAction.setAccelerator(getNewKeyStroke());
		newAction.addActionListener(this);
		fileMenu.add(newAction);

		JMenuItem loadAction = new JMenuItem(LOAD_MENU_ITEM);
		loadAction.setAccelerator(getLoadKeyStroke());
		loadAction.addActionListener(this);
		fileMenu.add(loadAction);

		JMenuItem saveAction = new JMenuItem(SAVE_MENU_ITEM);
		saveAction.setAccelerator(getSaveKeyStroke());
		saveAction.addActionListener(this);
		fileMenu.add(saveAction);

		JMenuItem saveAsAction = new JMenuItem(SAVE_AS_MENU_ITEM);
		saveAsAction.setAccelerator(getSaveAsKeyStroke());
		saveAsAction.addActionListener(this);
		fileMenu.add(saveAsAction);

		//Mac's already have a default menu with an exit action
		if(!isMac())
		{
			fileMenu.addSeparator();

			JMenuItem exitAction = new JMenuItem(EXIT_MENU_ITEM);
			exitAction.setAccelerator(getExitKeyStroke());
			exitAction.addActionListener(this);
			fileMenu.add(exitAction);
		}
		fileMenu.addSeparator();

		return fileMenu;
	}

	private JMenu createActionsMenu()
	{
		JMenu actionsMenu = new JMenu("Actions");

		JMenuItem undoBlack = new JMenuItem(UNDO_BLACK_MENU_ITEM);
		undoBlack.setAccelerator(getUndoKeyStroke());
		undoBlack.addActionListener(this);
		actionsMenu.add(undoBlack);

		JMenuItem undoWhite = new JMenuItem(UNDO_WHITE_MENU_ITEM);
		undoWhite.setAccelerator(getUndoTwiceKeyStroke());
		undoWhite.addActionListener(this);
		actionsMenu.add(undoWhite);

		JMenuItem showHint = new JMenuItem(SHOW_HINT_MENU_ITEM);
		showHint.setAccelerator(getShowHintKeyStroke());
		showHint.addActionListener(this);
		actionsMenu.add(showHint);

		return actionsMenu;
	}

	private JMenu createSettingsMenu()
	{
		JMenu settingsMenu = new JMenu("Settings");

		JMenuItem showAvailableClicks = null;
		if(Settings.DEBUG)
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
			//TODO(jontejj): make a copy of the previous board, in case something goes wrong with the loading
			getBoard().clear();
			myComponent.clear();
			boolean loadOk = false;

			while(!loadOk && myCurrentBoardFile != null)
			{
				try
				{
					loadOk = BoardLoader.loadStreamIntoBoard(new FileInputStream(selectedFile), getBoard());
				}
				catch (FileNotFoundException e)
				{
				}

				if(loadOk)
				{
					break;
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
				newGame();
			}
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

	private static Map<String, Boolean> noAbortionNecessaryCommands = Maps.newHashMap();
	static
	{
		noAbortionNecessaryCommands.put(SHOW_AVAILABLE_CLICKS_MENU_ITEM, true);
		noAbortionNecessaryCommands.put(HIDE_AVAILABLE_CLICKS_MENU_ITEM, true);
	}

	@Override
	public void actionPerformed(final ActionEvent e)
	{
		LOGGER.finest(e.getActionCommand());
		if(!noAbortionNecessaryCommands.containsKey(e.getActionCommand()))
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
