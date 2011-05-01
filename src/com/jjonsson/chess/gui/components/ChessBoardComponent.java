package com.jjonsson.chess.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.ChessBoardEvaluator.ChessState;
import com.jjonsson.chess.ChessBoardListener;
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
	
	public ChessBoardComponent(ChessWindow window)
	{	
		super();
		myWindow = window;
		pieces = new HashSet<ChessPieceComponent>();
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
	}
	
	private void drawGrid(Graphics g)
	{
		for (int row = 0;  row < ChessBoard.BOARD_SIZE;  row++ ) {
            for (int col = 0;  col < ChessBoard.BOARD_SIZE;  col++ ) {
                int x = ChessPieceComponent.SIZE*col;
                int y = ChessPieceComponent.SIZE*row;
                if ( (row % 2) == (col % 2) )
                   g.setColor(Color.darkGray);
                else
                   g.setColor(Color.lightGray);
                g.fillRect(x,y,ChessPieceComponent.SIZE,ChessPieceComponent.SIZE);
            }
         }
	}
	/**
	 * TODO: This doesn't seem to work
	 * @param newWindowSize
	 */
	public void resizeBoard(Dimension newWindowSize)
	{
		for(ChessPieceComponent p : pieces)
		{
			p.setSize((int)(newWindowSize.getWidth() / ChessBoard.BOARD_SIZE), (int)(newWindowSize.getHeight() / ChessBoard.BOARD_SIZE));
			p.repaint();
		}
	}
	
	private void markSquaresAsAvailable(Graphics2D graphics)
	{
		//Only draw possible moves if the game is in play
		if(getBoard().inPlay())
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
					markSquare(m.getPositionIfPerformed(),Color.GREEN, graphics);
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
		graphics.fillRect(point.x, point.y, ChessPieceComponent.SIZE, ChessPieceComponent.BORDER_SIZE / 2);
		//Bottom line
		graphics.fillRect(point.x, point.y + ChessPieceComponent.SIZE - ChessPieceComponent.BORDER_SIZE / 2, ChessPieceComponent.SIZE, ChessPieceComponent.BORDER_SIZE / 2);
		//Left line
		graphics.fillRect(point.x, point.y + ChessPieceComponent.BORDER_SIZE / 2, ChessPieceComponent.BORDER_SIZE / 2, ChessPieceComponent.SIZE - ChessPieceComponent.BORDER_SIZE);
		//Right line
		graphics.fillRect(point.x + ChessPieceComponent.SIZE - ChessPieceComponent.BORDER_SIZE / 2, point.y + ChessPieceComponent.BORDER_SIZE / 2, ChessPieceComponent.BORDER_SIZE / 2, ChessPieceComponent.SIZE - ChessPieceComponent.BORDER_SIZE);
	}
	
	private Point getInnerBorderUpperLeftCornerPointForSquare(Position pos)
	{
		Point p = new Point(pos.getColumn() * ChessPieceComponent.SIZE, (ChessBoard.BOARD_SIZE - pos.getRow() - 1) * ChessPieceComponent.SIZE);
		return p;
	}
	
	private Position getPositionForPoint(Point p)
	{
		int roundedRow = ChessBoard.BOARD_SIZE - (int)Math.floor(p.y / ChessPieceComponent.SIZE);
		return Position.createPosition(roundedRow, p.x / ChessPieceComponent.SIZE + 1);
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
		System.out.println("Board clicked");
		Point p = e.getPoint();
		Position selectedPosition = getPositionForPoint(p);
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
					setSelectedPiece(null);
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
	}

	@Override
	public void nextPlayer()
	{
		if(getBoard().inPlay())
		{
			if(getBoard().getCurrentPlayer() == Piece.BLACK)
			{
				try
				{
					getBoard().performRandomMove();
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
		else
		{
			repaint();
			ChessState state = getBoard().getCurrentState();
			if(state == ChessState.CHECKMATE)
			{
				String winner = null;
				if(getBoard().getCurrentPlayer() == Piece.BLACK)
					winner = "White";
				else
					winner = "Black";
				
				JOptionPane.showMessageDialog(myWindow, "Checkmate! " + winner + " won.");
			}
			if(state == ChessState.STALEMATE)
			{				
				JOptionPane.showMessageDialog(myWindow, "Stalemate! Draw.");
			}	
		}
	}
}
