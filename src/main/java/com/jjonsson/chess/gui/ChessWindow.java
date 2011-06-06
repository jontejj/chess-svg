package com.jjonsson.chess.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.ChessGame;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.persistance.BoardLoader;
import com.jjonsson.chess.persistance.ChessFileFilter;
import com.jjonsson.utilities.CrossPlatformUtilities;

public class ChessWindow extends JFrame implements ActionListener
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int DEFAULT_WINDOW_WIDTH = 700;
	public static final int DEFAULT_WINDOW_HEIGHT = 700;
	
	private static final int STATUS_BAR_HEIGHT = 20;
	private static final int WINDOW_BORDER_SIZE = 3;
	private static final int TITLE_BAR_HEIGHT = 22;
	
	private static final String NEW_MENU_ITEM = "New";
	private static final String LOAD_MENU_ITEM = "Load";
	private static final String SAVE_MENU_ITEM = "Save";
	private static final String SAVE_AS_MENU_ITEM = "Save As";
	private static final String DISABLE_AI_MENU_ITEM = "Disable Computer Player";
	private static final String ENABLE_AI_MENU_ITEM = "Enable Computer Player";
	private static final String EXIT_MENU_ITEM = "Exit";
	private static final String UNDO_BLACK_MENU_ITEM = "Undo Last Move";
	private static final String UNDO_WHITE_MENU_ITEM = "Undo Last Two Moves";
	private static final String SHOW_HINT_MENU_ITEM = "Show Hint";

	private static final String	SHOW_AVAILABLE_CLICKS_MENU_ITEM	= "Show Available Clicks";

	private static final String	HIDE_AVAILABLE_CLICKS_MENU_ITEM	= "Hide Available Clicks";
	
	private String lastFileChooserLocation;
	
	private ChessGame myGame;
	ChessBoardComponent myComponent;
	
	private String myCurrentBoardFile;
	
	private JLabel myStatusBar;
	
	private String myGameStatus;
	
	public ChessWindow(ChessGame chessGame)
	{
		super(ChessGame.NAME);
		myGame = chessGame;
		
	    this.setBackground(Color.DARK_GRAY);
	    createMenuBar();

	    this.setSize(ChessWindow.DEFAULT_WINDOW_WIDTH + WINDOW_BORDER_SIZE, ChessWindow.DEFAULT_WINDOW_HEIGHT + WINDOW_BORDER_SIZE + getJMenuBar().getHeight() + STATUS_BAR_HEIGHT);
	    myComponent = new ChessBoardComponent(this);
	    this.setContentPane(myComponent);
	    
	    this.addWindowListener(new WindowListener());
	    this.addComponentListener(new ComponentAdapter(this));
	    
	    createStatusBar();
	}
	
	public Dimension getBoardComponentSize()
	{
		Dimension boardSize = new Dimension(getSize().width + WINDOW_BORDER_SIZE, getSize().height - getJMenuBar().getHeight() - STATUS_BAR_HEIGHT - TITLE_BAR_HEIGHT);
		return boardSize;
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
		myStatusBar.setLocation(0, getSize().height - STATUS_BAR_HEIGHT - TITLE_BAR_HEIGHT);
	}

	public void updateStatusBar()
	{
		myGameStatus = getBoard().getStatusString();
		myStatusBar.setText(myGameStatus);
	}
	
	public void setTitle(String info)
	{
		super.setTitle(ChessGame.NAME + " - " + info);
	}
	
	
	public ChessBoard getBoard()
	{
		return myGame.getBoard();
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
	    
	    JMenu fileMenu = new JMenu("File");
	    
	    JMenuItem newAction = new JMenuItem(NEW_MENU_ITEM);
	    newAction.setAccelerator(CrossPlatformUtilities.getNewKeyStroke());
	    newAction.addActionListener(this);
	    fileMenu.add(newAction);
	    
	    JMenuItem loadAction = new JMenuItem(LOAD_MENU_ITEM);
	    loadAction.setAccelerator(CrossPlatformUtilities.getLoadKeyStroke());
	    loadAction.addActionListener(this);
	    fileMenu.add(loadAction);
	    
	    JMenuItem saveAction = new JMenuItem(SAVE_MENU_ITEM);
	    saveAction.setAccelerator(CrossPlatformUtilities.getSaveKeyStroke());
	    saveAction.addActionListener(this);
	    fileMenu.add(saveAction);
	    
	    JMenuItem saveAsAction = new JMenuItem(SAVE_AS_MENU_ITEM);
	    saveAsAction.setAccelerator(CrossPlatformUtilities.getSaveAsKeyStroke());
	    saveAsAction.addActionListener(this);
	    fileMenu.add(saveAsAction);
	    
	    fileMenu.addSeparator();
	    
	    JMenuItem disableAI = new JMenuItem(DISABLE_AI_MENU_ITEM);
	    disableAI.addActionListener(this);
	    fileMenu.add(disableAI);
	    
	    //Mac's already have a default menu with an exit action
	    if(!CrossPlatformUtilities.isMac())
	    {
		    fileMenu.addSeparator();
		    
		    JMenuItem exitAction = new JMenuItem(EXIT_MENU_ITEM);
		    exitAction.setAccelerator(CrossPlatformUtilities.getExitKeyStroke());
		    exitAction.addActionListener(this);
		    fileMenu.add(exitAction);
	    }
	    
	    JMenu actionsMenu = new JMenu("Actions");
	    
	    JMenuItem undoBlack = new JMenuItem(UNDO_BLACK_MENU_ITEM);
	    undoBlack.setAccelerator(CrossPlatformUtilities.getUndoKeyStroke());
	    undoBlack.addActionListener(this);
	    actionsMenu.add(undoBlack);
	    
	    JMenuItem undoWhite = new JMenuItem(UNDO_WHITE_MENU_ITEM);
	    undoWhite.setAccelerator(CrossPlatformUtilities.getUndoTwiceKeyStroke());
	    undoWhite.addActionListener(this);
	    actionsMenu.add(undoWhite);
	    
	    fileMenu.addSeparator();
	    
	    JMenuItem showHint = new JMenuItem(SHOW_HINT_MENU_ITEM);
	    showHint.setAccelerator(CrossPlatformUtilities.getShowHintKeyStroke());
	    showHint.addActionListener(this);
	    actionsMenu.add(showHint);
	    
	    JMenu settingsMenu = new JMenu("Settings");
	    
	    JMenuItem showAvailableClicks = null;
	    if(Settings.DEBUG)
	    {
		    showAvailableClicks = new JMenuItem(HIDE_AVAILABLE_CLICKS_MENU_ITEM);
	    }
	    else 
	    	showAvailableClicks = new JMenuItem(SHOW_AVAILABLE_CLICKS_MENU_ITEM);
	    
		showAvailableClicks.addActionListener(this);
		settingsMenu.add(showAvailableClicks);
	    
	    
	    menuBar.add(fileMenu);
	    menuBar.add(actionsMenu);
	    menuBar.add(settingsMenu);
	}
	
	private void save(boolean forceDialog)
	{
		if(myCurrentBoardFile == null || forceDialog)
			selectFile("Save Chess File");
		
		if(BoardLoader.saveBoard(getBoard(), myCurrentBoardFile))
			setResultOfInteraction("Saved successfully");
		else
			setResultOfInteraction("Save failed");		
	}
	
	private void newGame()
	{
		myComponent.clear();
		myGame.getBoard().reset();
	}
	
	/**
	 * Sets the text within the parentheses of the status bar
	 * @param msg
	 */
	public void setResultOfInteraction(String msg)
	{
		myStatusBar.setText(myGameStatus + " (" + msg + ")");
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
					break;
				
				myStatusBar.setText(myGameStatus + " (Invalid board file format, Select new file to load)");
				
				selectedFile = selectFile("Load Chess File");
				
			}
			myComponent.loadingOfBoardDone();
			
			if(loadOk)
				setResultOfInteraction("Load Ok");
			else
			{
				setResultOfInteraction("Load Cancelled");
				newGame();
			}
		}
	}
	
	private void undo(int nrOfMoves) 
	{
		int undoneMoves = getBoard().undoMoves(nrOfMoves);
		updateStatusBar();
		if(undoneMoves == 0)
			setResultOfInteraction("Undo not possible");
		else
			setResultOfInteraction("Reverted " + undoneMoves + " moves");
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
	
	private void switchAI(boolean enable)
	{
		myComponent.setAIEnabled(enable);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		System.out.println(e.getActionCommand());
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
		myComponent.showHint();
	}

	private File selectFile(String buttonText)
	{
		myCurrentBoardFile = null;
		JFileChooser jfc = new JFileChooser(getFileChooserPath());
		jfc.setAcceptAllFileFilterUsed(true);
		jfc.setFileFilter(new ChessFileFilter());
		jfc.showDialog(this.getRootPane(), buttonText);
		File selectedFile = jfc.getSelectedFile();
		if(selectedFile != null)
		{
			if(!selectedFile.getAbsolutePath().endsWith(ChessFileFilter.fileEnding))
			{
				myCurrentBoardFile = selectedFile.getAbsolutePath() + ChessFileFilter.fileEnding;
			}
			else
				myCurrentBoardFile = selectedFile.getAbsolutePath();
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
	
	private void saveFileChooserPath(String path)
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
}
