package com.jjonsson.chess.gui.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;

import org.apache.batik.swing.svg.JSVGComponent;
import org.w3c.dom.svg.SVGDocument;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.gui.PieceImageCache;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;

public class ChessPieceComponent extends JSVGComponent implements MoveListener, MouseListener//, SVGLoadEventDispatcherListener, SVGDocumentLoaderListener, GVTTreeBuilderListener, GVTTreeRendererListener
{
	private static final long	serialVersionUID	= 3048642024943627256L;

	private Piece myPieceToDraw;

	private ChessBoardComponent	myBoardComponent;
	
	public ChessPieceComponent(Piece pieceToDraw, ChessBoardComponent boardComponent)
	{
		super();
		myBoardComponent = boardComponent;
		myPieceToDraw = pieceToDraw;
		/*addSVGDocumentLoaderListener(this);
		addSVGLoadEventDispatcherListener(this);
		addGVTTreeBuilderListener(this);
		addGVTTreeRendererListener(this);*/
		
		SVGDocument doc = PieceImageCache.getSVGForPiece(myPieceToDraw);
		if(doc != null)
		{
			updateSize();
			//Makes sure that the image is displayed without a background
			setBackgroundColor(); 
			
			setSVGDocument(doc);
			addMouseListener(this);
			myPieceToDraw.addMoveListener(this);
		}
	}
	
	public Piece getPiece()
	{
		return myPieceToDraw;
	}
	
	private Point getPointForPiece(Piece piece)
	{
		Position currentPosition = piece.getCurrentPosition();
		Point p = new Point(currentPosition.getColumn() * myBoardComponent.getCurrentPieceSize().width + myBoardComponent.getPieceBorderSize() + myBoardComponent.getPieceMargin(), (ChessBoard.BOARD_SIZE - currentPosition.getRow() - 1) * myBoardComponent.getCurrentPieceSize().height + myBoardComponent.getPieceBorderSize() + myBoardComponent.getPieceMargin());
		return p;
	}

	public void updateSize()
	{

		setSize(myBoardComponent.getCurrentPieceSize().width - myBoardComponent.getPieceBorderSize() * 2 - myBoardComponent.getPieceMargin() * 2, myBoardComponent.getCurrentPieceSize().height - myBoardComponent.getPieceBorderSize() * 2 - myBoardComponent.getPieceMargin() * 2);
		setLocation(getPointForPiece(myPieceToDraw));
	}
	
	private void setBackgroundColor()
	{
		//setBackground(null);
        if ( (myPieceToDraw.getCurrentPosition().getRow() % 2) == (myPieceToDraw.getCurrentPosition().getColumn() % 2) )
        	setBackground(Color.lightGray);
         else
        	 setBackground(Color.darkGray); 	
	}

	@Override
	public void movePerformed(Move performedMove)
	{	
		setBackgroundColor();
		setLocation(getPointForPiece(myPieceToDraw));
		repaint();
	}

	@Override
	public void pieceRemoved(Piece removedPiece)
	{
		if(this.getPiece() != null)
			this.getPiece().removeMoveListener(this);
		else
		{
			System.out.println("Something wrong with" + this);
		}
		this.dispose();
		
		if(this.getParent() != null)
			this.getParent().remove(this);
		
		myBoardComponent = null;
		myPieceToDraw = null;
	}

	@Override
	public void mouseClicked(MouseEvent e)
	{
		long startNanos = System.nanoTime();
		/*System.out.println(myBoardComponent.getBoard().getAvailableMoves(myPieceToDraw.getCurrentPosition(), !myPieceToDraw.getAffinity()));
		myBoardComponent.getBoard().updateGameState();
		for(Move m : myPieceToDraw.getPossibleMoves())
		{
			m.updateMove(myBoardComponent.getBoard());
		}
		myBoardComponent.repaint();*/
		System.out.println(this.getPiece() + " clicked");
		ChessBoardComponent boardComponent = myBoardComponent;
		Piece currentlySelected = boardComponent.getSelectedPiece();
		if(currentlySelected != null && !currentlySelected.hasSameAffinityAs(this.getPiece()))
		{
			Move takeOverMove = currentlySelected.getAvailableMoveForPosition(this.getPiece().getCurrentPosition(), boardComponent.getBoard());
			if(takeOverMove != null)
			{
				//The currently selected piece is going to take over this one
				try
				{
					currentlySelected.performMove(takeOverMove, boardComponent.getBoard());
					return;
				}
				catch (UnavailableMoveException ume)
				{
					System.out.println(ume);
				}
			}
		}
		if(this.getPiece().hasSameAffinityAs(boardComponent.getBoard().getCurrentPlayer()))
		{
			//If no takeover was done it means that this piece should be the selected one (given that it's this players turn)
			if(this.getPiece().canMakeAMove(boardComponent.getBoard()))
			{
				//Only select pieces that can make a move
				boardComponent.setSelectedPiece(this.getPiece());
			}
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
}
