package com.jjonsson.chess.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.persistance.BoardLoader;
import com.jjonsson.chess.persistance.ChessFileFilter;

public class ChessWindow extends JFrame implements ActionListener, KeyListener
{
	private static final long	serialVersionUID	= 1L;
	
	public static final int DEFAULT_WINDOW_WIDTH = 800;
	public static final int DEFAULT_WINDOW_HEIGHT = 800;
	
	private static final int FILE_MENU_HEIGHT = 59;
	private static final int STATUS_BAR_HEIGHT = 20;
	
	private static final String NEW_MENU_ITEM = "New";
	private static final String LOAD_MENU_ITEM = "Load";
	private static final String SAVE_MENU_ITEM = "Save (Ctrl + s)";
	private static final String SAVE_AS_MENU_ITEM = "Save As (Ctrl + Shift + s)";
	private static final String EXIT_MENU_ITEM = "Exit (Left Alt + F4)";
	private static final String UNDO_MENU_ITEM = "Undo (Ctrl + z)";
	
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
	    this.setSize(ChessWindow.DEFAULT_WINDOW_WIDTH + 3, ChessWindow.DEFAULT_WINDOW_HEIGHT + FILE_MENU_HEIGHT + STATUS_BAR_HEIGHT);
	    
	    myComponent = new ChessBoardComponent(this);
	    this.setContentPane(myComponent);
	    
	    this.addWindowListener(new WindowListener(this));
	    this.addKeyListener(this);
	    WindowUtilities.setNativeLookAndFeel();
	    
	    createMenuBar();
	    createStatusBar();
	}
	
	private void createStatusBar() 
	{
		myStatusBar = new JLabel();
		updateStatusBar();
		myStatusBar.setSize(ChessWindow.DEFAULT_WINDOW_WIDTH, STATUS_BAR_HEIGHT);
		myStatusBar.setLocation(0, ChessWindow.DEFAULT_WINDOW_HEIGHT);
		myStatusBar.setOpaque(true);
		myStatusBar.setBackground(Color.white);
	    add(myStatusBar, java.awt.BorderLayout.SOUTH);
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
	
	/**
	 * 
	 * @param game
	 */
	public void displayGame()
	{
		this.setVisible(true);
		myComponent.repaint();
	}
	
	public void resizeWindow(Dimension newWindowSize)
	{
		myComponent.resizeBoard(newWindowSize);
	}
	private void createMenuBar()
	{
	    JMenuBar menuBar = new JMenuBar();
	    setJMenuBar(menuBar);
	    
	    JMenu fileMenu = new JMenu("File");
	    
	    JMenuItem newAction = new JMenuItem(NEW_MENU_ITEM);
	    JMenuItem loadAction = new JMenuItem(LOAD_MENU_ITEM);
	    JMenuItem saveAction = new JMenuItem(SAVE_MENU_ITEM);
	    JMenuItem saveAsAction = new JMenuItem(SAVE_AS_MENU_ITEM);
	    JMenuItem exitAction = new JMenuItem(EXIT_MENU_ITEM);
	    fileMenu.add(newAction);
	    fileMenu.add(loadAction);
	    fileMenu.add(saveAction);
	    fileMenu.add(saveAsAction);
	    fileMenu.addSeparator();
	    fileMenu.add(exitAction);
	    
	    menuBar.add(fileMenu);
	    
	    JMenu actionsMenu = new JMenu("Actions");
	    
	    JMenuItem undo = new JMenuItem(UNDO_MENU_ITEM);
	    actionsMenu.add(undo);
	    
	    menuBar.add(actionsMenu);
	    
	    newAction.addActionListener(this);
	    loadAction.addActionListener(this);
	    saveAction.addActionListener(this);
	    exitAction.addActionListener(this);
	    saveAsAction.addActionListener(this);
	    undo.addActionListener(this);
	}
	
	private void save(boolean forceDialog)
	{
		if(myCurrentBoardFile == null || forceDialog)
			selectFile("Save Chess File");
		
		if(BoardLoader.saveBoard(getBoard(), myCurrentBoardFile))
			myStatusBar.setText(myGameStatus + " (Saved successfully)");
		else
			myStatusBar.setText(myGameStatus + " (Save failed)");
			
	}
	
	private void newGame()
	{
		myComponent.clear();
		myGame.getBoard().reset();
	}
	
	private void load()
	{
		File selectedFile = selectFile("Load Chess File");
		if(myCurrentBoardFile != null)
		{
			//TODO: make a copy of the previous board, in case something goes wrong with the loading
			getBoard().clear();
			myComponent.clear();
			boolean loadOk = false;

			while(!loadOk && myCurrentBoardFile != null)
			{
				try 
				{
					loadOk = BoardLoader.loadFileIntoBoard(new FileInputStream(selectedFile), getBoard());
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
				myStatusBar.setText(myGameStatus + " (Load Ok)");
			else
			{
				myStatusBar.setText(myGameStatus + " (Load Cancelled)");
				newGame();
			}
		}
	}
	
	private void undo() 
	{
		try 
		{
			getBoard().undoLastMove();
		} 
		catch (UnavailableMoveException e) 
		{
			myStatusBar.setText(myGameStatus + " (Undo not possible)");
		}
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
		else if(e.getActionCommand().equals(UNDO_MENU_ITEM))
		{
			undo();
		}	
		else if(e.getActionCommand().equals(EXIT_MENU_ITEM))
		{
			exit();
		}
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
			if(!selectedFile.getAbsolutePath().endsWith(".chess"))
			{
				myCurrentBoardFile = selectedFile.getAbsolutePath() + ".chess";
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

	@Override
	public void keyTyped(KeyEvent e) 
	{
	}

	@Override
	public void keyPressed(KeyEvent e) 
	{
	}

	@Override
	public void keyReleased(KeyEvent e) 
	{
		if(e.isControlDown())
		{
			switch(e.getKeyCode())
			{
				case KeyEvent.VK_S:
					save(e.isShiftDown()); //Save [as]
					break;
				case KeyEvent.VK_Z:
					undo();
			}
		}
		else if(e.isAltDown() && e.getKeyCode() == KeyEvent.VK_F4 && !e.isShiftDown())
		{
			exit();
		}
	}
}
