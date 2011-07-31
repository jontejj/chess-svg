package com.jjonsson.chess.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.Thread.UncaughtExceptionHandler;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.SearchInterruptedError;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.gui.Settings;
import com.jjonsson.chess.gui.StatusListener;
import com.jjonsson.chess.gui.WindowUtilities;
import com.jjonsson.chess.listeners.ChessBoardListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.utilities.Logger;
import com.jjonsson.utilities.ThreadTracker;

import static com.jjonsson.utilities.Logger.LOGGER;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ChessBoardComponent extends JComponent implements MouseListener, ChessBoardListener, UncaughtExceptionHandler
{
	private static final long	serialVersionUID	= -6866444162384406903L;

	/**
	 * How much space of each square that should be used as a border
	 */
	private static final double	BORDERSIZE_PERCENTAGE = 0.07;
	/**
	 * How much space of each square that should surround the chess piece images
	 */
	private static final double	MARGINSIZE_PERCENTAGE = 0.1;
	
	private Set<ChessPieceComponent> pieces;
	
	private Piece myCurrentlySelectedPiece;
	
	private Dimension mySize;

	private Dimension myCurrentPieceSize;
	private int myPieceBorderSize;
	private int myPieceMargin;
	
	private ImmutableMap<Position, String> myPositionScores;
	
	private boolean myAIdisabled;
	
	private Move myHintMove;
	
	private boolean myShowAvailableClicks;
	
	private ChessBoard myBoard;

	private StatusListener	myStatusListener;
	
	private ThreadTracker myTracker;
	
	/**
	 * 
	 * @param size the dimensions for this component
	 */
	public ChessBoardComponent(ChessBoard board, Dimension size)
	{	
		super();
		if(Settings.DEBUG)
		{
			myShowAvailableClicks = true;
		}
		mySize = size;
		myBoard = board;
		myTracker = new ThreadTracker();
		
		myPositionScores = ImmutableMap.of();
		pieces = Sets.newHashSet();
		setCurrentPieceSize();
		setSize(size);
		addMouseListener(this);
		getBoard().addChessBoardListener(this);
		for(Piece p : myBoard.getPieces())
		{
			ChessPieceComponent comp = new ChessPieceComponent(p, this);
			pieces.add(comp);
		}
		for(ChessPieceComponent p : pieces)
		{
			this.add(p);
		}
	}
	
	public void setStatusListener(StatusListener sl)
	{
		myStatusListener = sl;
	}
	
	private void setResultOfInteraction(String result)
	{
		if(myStatusListener != null)
		{
			myStatusListener.setResultOfInteraction(result);
		}
	}
	
	private void statusChange()
	{
		if(myStatusListener != null)
		{
			myStatusListener.statusHasBeenUpdated();
		}
	}
	
	public void setAIEnabled(boolean enable)
	{
		myAIdisabled = !enable;
		//Makes the AI make a move directly if it's his turn
		nextPlayer();
	}

	public int getPieceMargin()
	{
		return myPieceMargin;
	}

	public int getPieceBorderSize()
	{
		return myPieceBorderSize;
	}

	public Dimension getCurrentPieceSize()
	{
		return myCurrentPieceSize;
	}
	
	public void setComponentSize(Dimension newSize)
	{
		mySize = newSize;
	}
	
	private void setCurrentPieceSize()
	{
		myCurrentPieceSize = new Dimension(mySize.width / ChessBoard.BOARD_SIZE, mySize.height / ChessBoard.BOARD_SIZE);
		myPieceMargin = (int) (myCurrentPieceSize.height * MARGINSIZE_PERCENTAGE);
		myPieceBorderSize = (int) (myCurrentPieceSize.height * BORDERSIZE_PERCENTAGE);
	}
	
	/**
	 * Removes all the pieces from this GUI component
	 */
	public void clear()
	{
		myCurrentlySelectedPiece = null;
		for(ChessPieceComponent comp : pieces)
		{
			comp.pieceRemoved(comp.getPiece());
		}
		pieces.clear();
	}
	
	public Piece getSelectedPiece()
	{
		return myCurrentlySelectedPiece;
	}
	
	public ChessBoard getBoard()
	{
		return myBoard;
	}
	
	public void paintComponent(Graphics g) 
	{
		  super.paintComponent(g);
		  Graphics2D g2d = (Graphics2D)g;
		  g2d.setRenderingHints(WindowUtilities.getRenderingHints());
		  setBackground(Color.darkGray);
		  drawGrid(g2d);
		  if(myShowAvailableClicks)
		  {
			  markPiecesAsAvailable(g2d);
			  markAvailableSquares(g2d);
		  }
		  markSelectedSquare(g2d);
		  
		  if(Settings.DEBUG)
		  {
			  drawPositionScores(g2d);
		  }
		  
		  if(myHintMove != null)
		  {
			  markSquare(myHintMove.getCurrentPosition(), Color.GREEN, g2d);
			  markSquare(myHintMove.getDestination(), Color.ORANGE, g2d);
		  }
	}

	private void drawPositionScores(Graphics g)
	{
		g.setColor(Color.MAGENTA);
		ImmutableSet<Entry<Position, String>> scores  = myPositionScores.entrySet();
		for(Entry<?, ?> entry : scores)
		{
			Position pos = (Position) entry.getKey();
			drawTextInSquare(pos, entry.getValue().toString(), g);
		}
	}
	
	private void drawGrid(Graphics g)
	{
		for (int row = 0;  row < ChessBoard.BOARD_SIZE;  row++ ) 
		{
            for (int col = 0;  col < ChessBoard.BOARD_SIZE;  col++ ) 
            {
                int x = myCurrentPieceSize.width*col;
                int y = myCurrentPieceSize.height*row;
                if ( (row % 2) == (col % 2) )
                {
                   g.setColor(Color.darkGray);
                }
                else
                {
                   g.setColor(Color.lightGray);
                }
                g.fillRect(x,y,myCurrentPieceSize.width,myCurrentPieceSize.height);
            }
         }
	}
	/**
	 * @param newComponentSize
	 */
	public void resizeBoard(Dimension newComponentSize)
	{
		mySize = newComponentSize;
		setCurrentPieceSize();
		for(ChessPieceComponent p : pieces)
		{
			p.updateSize();
			p.repaint();
		}
		repaint();
	}
	
	public void showHint()
	{
		try
		{
			myHintMove = ChessMoveEvaluator.getBestMove(getBoard());
			setResultOfInteraction("Hint: " + myHintMove);
			//Makes it easy to make the move
			setSelectedPiece(myHintMove.getPiece());
			repaint();
		}
		catch (NoMovesAvailableException e)
		{
			myHintMove = null;
			setResultOfInteraction("No hint could be found");
		}
		catch(SearchInterruptedError sie)
		{
			LOGGER.info("Aborted the search for a hint move");
		}
	}
	
	public Move getHintMove()
	{
		return myHintMove;
	}
	
	/**
	 * 
	 * @param show true if the available clicks should be marked, false if they shouldn't
	 */
	public void showAvailableClicks(boolean show)
	{
		if(show != myShowAvailableClicks)
		{
			myShowAvailableClicks = show;
			repaint();
		}
	}
	
	/**
	 * Marks pieces that can make a move
	 * @param graphics
	 */
	private void markPiecesAsAvailable(Graphics2D graphics)
	{
		//Only draw possible moves if the game is in play
		if(ChessBoardEvaluator.inPlay(getBoard()))
		{
			//Mark pieces with available moves
			Collection<Piece> currentPlayerPieces = getBoard().getPiecesForAffinity(getBoard().getCurrentPlayer());
			for(Piece p : currentPlayerPieces)
			{
				if(p.canMakeAMove(getBoard()))
				{
					markSquare(p.getCurrentPosition(),Color.MAGENTA, graphics);
				}
			}	
		}
		/*
		//Mark Square Testing
		markSquare(Position.createPosition(1, Position.B), Color.cyan);
		markSquare(Position.createPosition(1, Position.A), Color.GREEN);
		markSquare(Position.createPosition(1, Position.C), Color.RED);
		markSquare(Position.createPosition(2, Position.A), Color.BLUE);
		markSquare(Position.createPosition(2, Position.B), Color.BLACK);
		markSquare(Position.createPosition(2, Position.C), Color.yellow);
		markSquare(Position.createPosition(3, Position.A), Color.MAGENTA);
		markSquare(Position.createPosition(3, Position.B), Color.pink);
		markSquare(Position.createPosition(3, Position.C), Color.WHITE);
		*/
	}
	
	/**
	 * Marks the squares available for the selected piece
	 * @param graphics
	 */
	private void markAvailableSquares(Graphics2D graphics)
	{
		if(ChessBoardEvaluator.inPlay(getBoard()) && myCurrentlySelectedPiece != null)
		{
			//Mark available moves for the selected piece
			List<Move> moves = myCurrentlySelectedPiece.getAvailableMoves(Piece.NO_SORT, getBoard());
			for(Move m : moves)
			{
				markSquare(m.getDestination(),Color.GREEN, graphics);
			}
		}
	}
	
	private void markSelectedSquare(Graphics2D graphics)
	{
		if(ChessBoardEvaluator.inPlay(getBoard()) && myCurrentlySelectedPiece != null)
		{
			//Mark the selected piece
			markSquare(myCurrentlySelectedPiece.getCurrentPosition(), Color.CYAN, graphics);
		}
	}
	
	/**
	 * Marks the given position with the given color on the given graphics object
	 * @param pos the position to surround with a color
	 * @param markingColor the color to surround the position with
	 * @param graphics the object to draw the square on
	 */
	private void markSquare(Position pos, Color markingColor, Graphics2D graphics)
	{
		if(pos == null)
		{
			return;
		}
		
		graphics.setColor(markingColor);
		Point point = getInnerBorderUpperLeftCornerPointForSquare(pos);
		//Upper line
		graphics.fillRect(point.x, point.y, myCurrentPieceSize.width, myPieceBorderSize / 2);
		//Bottom line
		graphics.fillRect(point.x, point.y + myCurrentPieceSize.height - myPieceBorderSize / 2, myCurrentPieceSize.width, myPieceBorderSize / 2);
		//Left line
		graphics.fillRect(point.x, point.y + myPieceBorderSize / 2, myPieceBorderSize / 2, myCurrentPieceSize.height - myPieceBorderSize);
		//Right line
		graphics.fillRect(point.x + myCurrentPieceSize.width - myPieceBorderSize / 2, point.y + myPieceBorderSize / 2, myPieceBorderSize / 2, myCurrentPieceSize.height - myPieceBorderSize);
	}
	
	/**
	 * Marks the given position with the given color on the given graphics object
	 * @param pos the position to surround with a color
	 * @param markingColor the color to surround the position with
	 * @param graphics the object to draw the square on
	 */
	private void drawTextInSquare(Position pos, String text, Graphics graphics)
	{
		//graphics.setColor(markingColor);
		graphics.setFont(Font.decode("Helvetica-BOLD-16"));
		Point point = getInnerBorderUpperLeftCornerPointForSquare(pos);
		
		graphics.drawString(text, point.x + getPieceBorderSize(), point.y + getPieceBorderSize()*4);
	}
	
	private Point getInnerBorderUpperLeftCornerPointForSquare(Position pos)
	{
		return new Point(pos.getColumn() * myCurrentPieceSize.width, (ChessBoard.BOARD_SIZE - pos.getRow() - 1) * myCurrentPieceSize.height);
	}
	
	private Position getPositionForPoint(Point p) throws InvalidPosition
	{
		int roundedRow = ChessBoard.BOARD_SIZE - (int)Math.floor(p.y / myCurrentPieceSize.height);
		return Position.createPosition(roundedRow, p.x / myCurrentPieceSize.width + 1);
	}
	
	/**
	 * Sets the currently selected piece, it also repaints the grid
	 * @param p
	 */
	public void setSelectedPiece(Piece p)
	{
		Piece oldPiece = myCurrentlySelectedPiece;
		
		myCurrentlySelectedPiece = p;
		
		if(oldPiece != myCurrentlySelectedPiece)
		{
			//If we choose another piece the hint move should disappear
			if(myHintMove != null && myHintMove.getPiece() != p)
			{
				myHintMove = null;
			}
			repaint();
		}
	}
	@Override
	public void mouseClicked(MouseEvent e)
	{
		long startNanos = System.nanoTime();
		LOGGER.finest("Board clicked");
		Point p = e.getPoint();
		Position selectedPosition = null;
		try
		{
			selectedPosition = getPositionForPoint(p);
		}
		catch (InvalidPosition e1)
		{
			//User must have clicked outside the board
			LOGGER.info("Out of bounds: " + e1);
			return;
		}
		
		LOGGER.finest("Point: " + p + ", Position: " + selectedPosition);
		
		positionClicked(selectedPosition);
		
		long time = (System.nanoTime() - startNanos);
		BigDecimal bd = new BigDecimal(time).divide(BigDecimal.valueOf(SECONDS.toNanos(1)));
		LOGGER.finest("Seconds: " + bd.toPlainString());
	}
	@Override
	public void mousePressed(MouseEvent e){}
	@Override
	public void mouseReleased(MouseEvent e){}
	@Override
	public void mouseEntered(MouseEvent e){}
	@Override
	public void mouseExited(MouseEvent e){}
	
	public void positionClicked(Position positionThatWasClicked)
	{
		if(myCurrentlySelectedPiece != null)
		{
			Move m = getBoard().getAvailableMove(myCurrentlySelectedPiece, positionThatWasClicked);
			if(m != null)
			{
				LOGGER.finest("Destination available: " + positionThatWasClicked);
				try
				{
					myCurrentlySelectedPiece.performMove(m, getBoard());
					return;
				}
				catch (UnavailableMoveException ume)
				{
					LOGGER.info(ume.toString());
				}
			}
		}
		
		Piece pieceAtSelectedPosition = getBoard().getPiece(positionThatWasClicked);
		if(pieceAtSelectedPosition != null && pieceAtSelectedPosition.hasSameAffinityAs(getBoard().getCurrentPlayer()))
		{
			setSelectedPiece(pieceAtSelectedPosition);
		}
	}

	@Override
	public void piecePlaced(Piece p)
	{
		ChessPieceComponent comp = new ChessPieceComponent(p, this);
		pieces.add(comp);
		this.add(comp);
		repaint();
	}

	@Override
	public void gameStateChanged(ChessState newState)
	{
		LOGGER.finer("" + newState);
		statusChange();
	}

	@Override
	public void piecePlacedLoadingInProgress(Piece p)
	{
		ChessPieceComponent comp = new ChessPieceComponent(p, this);
		pieces.add(comp);
		this.add(comp);
	}

	@Override
	public void loadingOfBoardDone()
	{
		nextPlayer();
		repaint();
		statusChange();
	}

	@Override
	public void nextPlayer()
	{
		setSelectedPiece(null);
		myHintMove = null;
		statusChange();
		if(!myAIdisabled && ChessBoardEvaluator.inPlay(getBoard()) && getBoard().allowsMoves())
		{
			/*try
			{
				Thread.sleep(3000);
			}
			catch (InterruptedException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			if(getBoard().getCurrentPlayer() == Piece.BLACK)
			{
				setResultOfInteraction("Thinking ...");
				//Run in a seperate thread to let the eventQueue run along
				Thread t = new Thread()
				{
					@Override
					public void run()
					{
						try
						{
							ChessMoveEvaluator.performBestMove(getBoard(), myStatusListener);
						}
						catch (NoMovesAvailableException e)
						{
							setResultOfInteraction("No valid moves found");
						}
						catch(SearchInterruptedError interrupted)
						{
							LOGGER.info("Aborted searching for a move");
							return;
						}
						statusChange();
						repaint();
						myTracker.removeJob(this);
					}
				};
				myTracker.addJob(t);
				t.setUncaughtExceptionHandler(this);
				t.start();
			}
			else
			{
				//just for fun
				/*try
				{
					getBoard().performRandomMove();
				}
				catch (NoMovesAvailableException e)
				{
				}*/
			}
		}
	}
	
	public boolean isWorking()
	{
		return myTracker.isWorking();
	}
	
	public void interruptCurrentJobs()
	{
		myTracker.interruptCurrentJobs();
	}

	@Override
	public boolean supportsPawnReplacementDialog() 
	{
		return true;
	}

	/**
	 *  TODO(jontejj): popup a choice
	 */
	@Override
	public Piece getPawnReplacementFromDialog() 
	{
		return null;
	}

	@Override
	public void undoDone()
	{
		nextPlayer();
	}

	@Override
	public void squareScores(ImmutableMap<Position, String> positionScores)
	{
		myPositionScores = positionScores;
		repaint();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		LOGGER.warning("Uncaught exception received in 'main' thread: " + e + ", for thread: " + t);
		LOGGER.info("Exception trace: " + Logger.stackTraceToString(e));
	}
}
