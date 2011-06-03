package com.jjonsson.chess.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JComponent;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.ChessBoardEvaluator;
import com.jjonsson.chess.ChessMoveEvaluator;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.ChessBoardListener;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.exceptions.NoMovesAvailableException;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.WindowUtilities;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;

public class ChessBoardComponent extends JComponent implements MouseListener, ChessBoardListener
{
	private static final long	serialVersionUID	= -6866444162384406903L;
	
	private Set<ChessPieceComponent> pieces;
	
	private Piece myCurrentlySelectedPiece;
	
	private ChessWindow myWindow;

	private Dimension myCurrentPieceSize;
	public int myPieceBorderSize;
	private int myPieceMargin;
	
	private ImmutableMap<Position, String> myPositionScores;
	
	private boolean myAIdisabled;
	
	public ChessBoardComponent(ChessWindow window)
	{	
		super();
		myWindow = window;
		myPositionScores = ImmutableMap.of();
		pieces = new HashSet<ChessPieceComponent>();
		setCurrentPieceSize();
		setSize(myWindow.getBoardComponentSize());
		addMouseListener(this);
		getBoard().addChessBoardListener(this);
		for(Piece p : window.getBoard().getPieces())
		{
			ChessPieceComponent comp = new ChessPieceComponent(p, this);
			pieces.add(comp);
		}
		for(ChessPieceComponent p : pieces)
		{
			this.add(p);
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
	
	private void setCurrentPieceSize()
	{
		myCurrentPieceSize = new Dimension(myWindow.getBoardComponentSize().width / ChessBoard.BOARD_SIZE, myWindow.getBoardComponentSize().height / ChessBoard.BOARD_SIZE);
		myPieceMargin = (int) (myCurrentPieceSize.height * 0.1);
		myPieceBorderSize = (int) (myCurrentPieceSize.height * 0.07);
	}
	
	/**
	 * Removes all the pieces from this GUI component
	 */
	public void clear()
	{
		myCurrentlySelectedPiece = null;
		Iterator<ChessPieceComponent> iterator = pieces.iterator();
		while(iterator.hasNext())
		{
			ChessPieceComponent comp = iterator.next();
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
		return myWindow.getBoard();
	}
	
	public void paintComponent(Graphics g) 
	{
		  super.paintComponent(g);
		  Graphics2D g2d = (Graphics2D)g;
		  g2d.setRenderingHints(WindowUtilities.getRenderingHints());
		  setBackground(Color.darkGray);
		  drawGrid(g2d);
		  markSquaresAsAvailable(g2d);
		  drawPositionScores(g2d);
	}
	
	private void drawPositionScores(Graphics g)
	{
		ImmutableSet<Entry<Position, String>> scores  = myPositionScores.entrySet();
		for(Entry<?, ?> entry : scores)
		{
			Position pos = (Position) entry.getKey();
			drawTextInSquare(pos, entry.getValue().toString(), g);
		}
	}
	
	private void drawGrid(Graphics g)
	{
		for (int row = 0;  row < ChessBoard.BOARD_SIZE;  row++ ) {
            for (int col = 0;  col < ChessBoard.BOARD_SIZE;  col++ ) {
                int x = myCurrentPieceSize.width*col;
                int y = myCurrentPieceSize.height*row;
                if ( (row % 2) == (col % 2) )
                   g.setColor(Color.darkGray);
                else
                   g.setColor(Color.lightGray);
                g.fillRect(x,y,myCurrentPieceSize.width,myCurrentPieceSize.height);
            }
         }
	}
	/**
	 * @param newWindowSize
	 */
	public void resizeBoard(Dimension newWindowSize)
	{
		setCurrentPieceSize();
		for(ChessPieceComponent p : pieces)
		{
			p.updateSize();
			p.repaint();
		}
		repaint();
	}
	
	private void markSquaresAsAvailable(Graphics2D graphics)
	{
		//Only draw possible moves if the game is in play
		if(ChessBoardEvaluator.inPlay(getBoard()))
		{
			//Mark pieces with available moves
			Collection<Piece> currentPlayerPieces = getBoard().getPiecesForAffinity(getBoard().getCurrentPlayer());
			for(Piece p : currentPlayerPieces)
			{
				if(p.canMakeAMove(getBoard()))
					markSquare(p.getCurrentPosition(),Color.MAGENTA, graphics);
			}	
			
			if(myCurrentlySelectedPiece != null)
			{
				//Mark available moves for the selected piece
				List<Move> moves = myCurrentlySelectedPiece.getAvailableMoves(false, getBoard());
				for(Move m : moves)
				{
					try
					{
						markSquare(m.getPositionIfPerformed(),Color.GREEN, graphics);
					}
					catch(NullPointerException npe)
					{}
				}
				//Mark the selected piece
				markSquare(myCurrentlySelectedPiece.getCurrentPosition(), Color.CYAN, graphics);
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
	 * Marks the given position with the given color on the given graphics object
	 * @param pos the position to surround with a color
	 * @param markingColor the color to surround the position with
	 * @param graphics the object to draw the square on
	 */
	private void markSquare(Position pos, Color markingColor, Graphics2D graphics)
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
		
		graphics.drawString(text, point.x, point.y + 15);
	}
	
	private Point getInnerBorderUpperLeftCornerPointForSquare(Position pos)
	{
		Point p = new Point(pos.getColumn() * myCurrentPieceSize.width, (ChessBoard.BOARD_SIZE - pos.getRow() - 1) * myCurrentPieceSize.height);
		return p;
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
			repaint();
	}
	@Override
	public void mouseClicked(MouseEvent e)
	{
		/*for(Move m : myCurrentlySelectedPiece.getPossibleMoves())
		{
			m.updateMove(getBoard());
		}
		getBoard().updateGameState();*/
		long startNanos = System.nanoTime();
		System.out.println("Board clicked");
		Point p = e.getPoint();
		Position selectedPosition = null;
		try
		{
			selectedPosition = getPositionForPoint(p);
		}
		catch (InvalidPosition e1)
		{
			//User must have clicked outside the board
			System.out.println("Out of bounds: " + e1);
			return;
		}
		
		System.out.println("Point: " + p + ", Position: " + selectedPosition);
		
		if(myCurrentlySelectedPiece != null)
		{
			Move m = myCurrentlySelectedPiece.getAvailableMoveForPosition(selectedPosition, getBoard());
			if(m != null)
			{
				System.out.println("Destination available: " + selectedPosition);
				try
				{
					myCurrentlySelectedPiece.performMove(m, myWindow.getBoard());
					return;
				}
				catch (UnavailableMoveException ume)
				{
					System.out.println(ume);
				}
			}
		}
		
		Piece pieceAtSelectedPosition = getBoard().getPiece(selectedPosition);
		if(pieceAtSelectedPosition != null && pieceAtSelectedPosition.hasSameAffinityAs(getBoard().getCurrentPlayer()))
		{
			setSelectedPiece(pieceAtSelectedPosition);
		}
		long time = (System.nanoTime() - startNanos);
		BigDecimal bd = new BigDecimal(time).divide(BigDecimal.valueOf(1000000000));
		System.out.println("Seconds: " + bd.toPlainString());
	}
	@Override
	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseReleased(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseEntered(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public void mouseExited(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
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
		System.out.println(newState);
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
		repaint();
		myWindow.updateStatusBar();
	}

	@Override
	public void nextPlayer()
	{
		setSelectedPiece(null);
		if(!myAIdisabled && ChessBoardEvaluator.inPlay(getBoard()) && getBoard().allowsMoves())
		{
			if(getBoard().getCurrentPlayer() == Piece.BLACK)
			{
				try
				{
					ChessMoveEvaluator.performBestMove(getBoard());
				}
				catch (NoMovesAvailableException e)
				{
					e.printStackTrace();
				}
				catch (UnavailableMoveException e)
				{
					e.printStackTrace();
				}
			}
		}
		myWindow.updateStatusBar();
	}

	@Override
	public boolean supportsPawnReplacementDialog() 
	{
		return true;
	}

	/**
	 *  TODO: popup a choice
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
}
