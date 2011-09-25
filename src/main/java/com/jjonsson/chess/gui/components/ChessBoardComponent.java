package com.jjonsson.chess.gui.components;

import static com.jjonsson.chess.gui.Settings.DEMO;
import static com.jjonsson.chess.pieces.Piece.BLACK;
import static com.jjonsson.utilities.Loggers.STDOUT;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.lang.Thread.UncaughtExceptionHandler;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator;
import com.jjonsson.chess.evaluators.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.evaluators.ChessMoveEvaluator;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.SearchInterruptedError;
import com.jjonsson.chess.gui.Settings;
import com.jjonsson.chess.gui.WindowUtilities;
import com.jjonsson.chess.listeners.ChessBoardListener;
import com.jjonsson.chess.listeners.StatusListener;
import com.jjonsson.chess.moves.ImmutablePosition;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;
import com.jjonsson.utilities.ThreadTracker;

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

	static final Color DARK_BACKGROUND				= Color.DARK_GRAY;
	static final Color LIGHT_BACKGROUND				= Color.LIGHT_GRAY;
	static final Color AVAILABLE_POSITION_BORDER 	= Color.GREEN;
	static final Color AVAILABLE_PIECE_BORDER 		= Color.MAGENTA;
	static final Color SELECT_PIECE_BORDER 			= Color.CYAN;
	static final Color HINT_MOVE_DESTINATION_BORDER = Color.ORANGE;

	private Set<ChessPieceComponent> pieces;

	private Piece myCurrentlySelectedPiece;

	private Dimension mySize;

	private Dimension myCurrentPieceSize;
	private int myPieceBorderSize;
	private int myPieceMargin;

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
	public ChessBoardComponent(final ChessBoard board, final Dimension size)
	{
		super();
		if(Settings.DEBUG || Settings.DEMO)
		{
			myShowAvailableClicks = true;
		}
		mySize = size;
		myBoard = board;
		myTracker = new ThreadTracker();

		pieces = Sets.newIdentityHashSet();
		setCurrentPieceSize();
		setSize(size);
		addMouseListener(this);
		myBoard.addChessBoardListener(this);

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

	public void setStatusListener(final StatusListener sl)
	{
		myStatusListener = sl;
	}

	private void setResultOfInteraction(final String result)
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

	public void setAIEnabled(final boolean enable)
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

	@Override
	public void paintComponent(final Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHints(WindowUtilities.getRenderingHints());
		setBackground(DARK_BACKGROUND);
		drawGrid(g2d);
		if(myShowAvailableClicks)
		{
			markPiecesAsAvailable(g2d);
			markAvailableSquares(g2d);
		}
		markSelectedSquare(g2d);

		if(myHintMove != null)
		{
			markSquare(myHintMove.getCurrentPosition(), AVAILABLE_POSITION_BORDER, g2d);
			markSquare(myHintMove.getDestination(), HINT_MOVE_DESTINATION_BORDER, g2d);
		}
	}

	private void drawGrid(final Graphics g)
	{
		for (int row = 0;  row < ChessBoard.BOARD_SIZE;  row++ )
		{
			for (int col = 0;  col < ChessBoard.BOARD_SIZE;  col++ )
			{
				int x = myCurrentPieceSize.width*col;
				int y = myCurrentPieceSize.height*row;
				if ( (row % 2) == (col % 2) )
				{
					g.setColor(DARK_BACKGROUND);
				}
				else
				{
					g.setColor(LIGHT_BACKGROUND);
				}
				g.fillRect(x,y,myCurrentPieceSize.width,myCurrentPieceSize.height);
			}
		}
	}
	/**
	 * @param newComponentSize
	 */
	public void resizeBoard(final Dimension newComponentSize)
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
			if(getBoard().getCurrentPlayer() == Piece.WHITE || myAIdisabled)
			{
				myHintMove = ChessMoveEvaluator.getBestMove(getBoard());
				setResultOfInteraction("Hint: " + myHintMove);
				//Makes it easy to make the move
				setSelectedPiece(myHintMove.getPiece());
				repaint();
			}
		}
		catch (NoMovesAvailableException e)
		{
			myHintMove = null;
			setResultOfInteraction("No hint could be found");
		}
		catch(SearchInterruptedError sie)
		{
			STDOUT.info("Aborted the search for a hint move");
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
	public void showAvailableClicks(final boolean show)
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
	private void markPiecesAsAvailable(final Graphics2D graphics)
	{
		//Only draw possible moves if the game is in play
		if(ChessBoardEvaluator.inPlay(getBoard()))
		{
			//Mark pieces with available moves
			for(Piece p : ImmutableList.copyOf(getBoard().getPieces()))
			{
				if(p.hasSameAffinityAs(getBoard().getCurrentPlayer()) && p.canMakeAMove())
				{
					markSquare(p.getCurrentPosition(),AVAILABLE_PIECE_BORDER, graphics);
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
	private void markAvailableSquares(final Graphics2D graphics)
	{
		if(ChessBoardEvaluator.inPlay(getBoard()) && myCurrentlySelectedPiece != null)
		{
			//Mark available moves for the selected piece
			List<Move> moves = ImmutableList.copyOf(myCurrentlySelectedPiece.getAvailableMoves());
			for(Move m : moves)
			{
				markSquare(m.getDestination(),AVAILABLE_POSITION_BORDER, graphics);
			}
		}
	}

	private void markSelectedSquare(final Graphics2D graphics)
	{
		if(ChessBoardEvaluator.inPlay(getBoard()) && myCurrentlySelectedPiece != null)
		{
			//Mark the selected piece
			markSquare(myCurrentlySelectedPiece.getCurrentPosition(), SELECT_PIECE_BORDER, graphics);
		}
	}

	/**
	 * Marks the given position with the given color on the given graphics object
	 * @param pos the position to surround with a color
	 * @param markingColor the color to surround the position with
	 * @param graphics the object to draw the square on
	 * @throws NullPointerException if either of {@code pos} or {@code graphics} is null
	 */
	private void markSquare(final Position pos, final Color markingColor, final Graphics2D graphics)
	{
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
	 * 
	 * @param pos
	 * @return the upper left inner point of the border for the given position
	 * @throws NullPointerException if {@code pos} is null
	 */
	private Point getInnerBorderUpperLeftCornerPointForSquare(final Position pos)
	{
		return new Point(pos.getColumn() * myCurrentPieceSize.width, (ChessBoard.BOARD_SIZE - pos.getRow() - 1) * myCurrentPieceSize.height);
	}

	@VisibleForTesting
	public ImmutablePosition getPositionForPoint(final Point p) throws InvalidPosition
	{
		int roundedRow = ChessBoard.BOARD_SIZE - 1 - (int)Math.floor((double)p.y / myCurrentPieceSize.height);
		return ImmutablePosition.of(roundedRow, p.x / myCurrentPieceSize.width);
	}

	public Point getPointForPosition(final ImmutablePosition p)
	{
		//TODO: why 60?
		return new Point(p.getColumn() * myCurrentPieceSize.width, (ChessBoard.BOARD_SIZE - 1 - p.getRow()) * myCurrentPieceSize.height + 60);
	}

	/**
	 * Sets the currently selected piece, it also repaints the grid
	 * @param p
	 */
	public void setSelectedPiece(final Piece p)
	{
		//You shouldn't select the AI's players while he's thinking
		if(!myAIdisabled && getBoard().getCurrentPlayer() == BLACK && p != null)
		{
			return;
		}

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
	public void mouseClicked(final MouseEvent e)
	{
		long startNanos = System.nanoTime();
		STDOUT.debug("Board clicked");
		Point p = e.getPoint();
		ImmutablePosition selectedPosition = null;
		try
		{
			selectedPosition = getPositionForPoint(p);
		}
		catch (InvalidPosition ip)
		{
			//User must have clicked outside the board
			STDOUT.info("Out of bounds position: " + ip);
			return;
		}

		STDOUT.debug("Point: " + p + ", Position: " + selectedPosition);

		positionClicked(selectedPosition);

		long time = (System.nanoTime() - startNanos);
		BigDecimal bd = new BigDecimal(time).divide(BigDecimal.valueOf(SECONDS.toNanos(1)));
		STDOUT.debug("Seconds: " + bd.toPlainString());
	}
	@Override
	public void mousePressed(final MouseEvent e){}
	@Override
	public void mouseReleased(final MouseEvent e){}
	@Override
	public void mouseEntered(final MouseEvent e){}
	@Override
	public void mouseExited(final MouseEvent e){}

	public void positionClicked(final ImmutablePosition positionThatWasClicked)
	{
		if(myCurrentlySelectedPiece != null)
		{
			Move m = getBoard().getAvailableMove(myCurrentlySelectedPiece, positionThatWasClicked);
			if(m != null)
			{
				STDOUT.debug("Destination available: " + positionThatWasClicked);
				if(myCurrentlySelectedPiece.performMove(m, getBoard()))
				{
					return;
				}
			}
			else
			{
				STDOUT.debug("Destination not available: " + positionThatWasClicked);
			}
		}

		Piece pieceAtSelectedPosition = getBoard().getPiece(positionThatWasClicked);
		if(pieceAtSelectedPosition != null && pieceAtSelectedPosition.hasSameAffinityAs(getBoard().getCurrentPlayer()))
		{
			setSelectedPiece(pieceAtSelectedPosition);
		}
	}

	@Override
	public void piecePlaced(final Piece p)
	{
		ChessPieceComponent comp = new ChessPieceComponent(p, this);
		pieces.add(comp);
		this.add(comp);
		repaint();
	}

	@Override
	public void gameStateChanged(final ChessState newState)
	{
		STDOUT.debug("" + newState);
		statusChange();
	}

	@Override
	public void piecePlacedLoadingInProgress(final Piece p)
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

	private class PerformBestMoveThread extends Thread
	{
		public PerformBestMoveThread()
		{
			super(PerformBestMoveThread.class.getName());
		}

		@Override
		public void run()
		{
			try
			{
				ChessMoveEvaluator.performBestMove(getBoard());
			}
			catch (NoMovesAvailableException e)
			{
				setResultOfInteraction("No valid moves found");
			}
			catch(SearchInterruptedError interrupted)
			{
				STDOUT.info("Aborted searching for a move");
				return;
			}
			statusChange();
			repaint();
			myTracker.removeJob(this);
		}
	}

	@Override
	public void nextPlayer()
	{
		setSelectedPiece(null);
		myHintMove = null;
		statusChange();
		if(!myAIdisabled && ChessBoardEvaluator.inPlay(getBoard()) && getBoard().allowsMoves())
		{
			if(getBoard().getCurrentPlayer() == Piece.BLACK)
			{
				setResultOfInteraction("Thinking ...");
				//Run in a seperate thread to let the eventQueue run along
				PerformBestMoveThread t = new PerformBestMoveThread();
				myTracker.addJob(t);
				t.setUncaughtExceptionHandler(this);
				t.start();
			}
			else
			{
				if(DEMO)
				{
					//just for fun
					try
					{
						getBoard().performRandomMove();
					}
					catch (NoMovesAvailableException e)
					{
						STDOUT.warn("No moves available for the random player");
					}
				}
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
	public boolean supportsPawnPromotionDialog()
	{
		return true;
	}

	/**
	 *  TODO(jontejj): popup a choice
	 */
	@Override
	public Piece getPawnPromotionFromDialog()
	{
		return null;
	}

	@Override
	public void undoDone()
	{
		nextPlayer();
	}

	@Override
	public void uncaughtException(final Thread t, final Throwable e)
	{
		STDOUT.fatal("Uncaught exception received in 'main' thread: " + e + ", for thread: " + t);
		STDOUT.info("Exception trace: ", e);
		STDOUT.info("AI move will not be made");
	}
}
