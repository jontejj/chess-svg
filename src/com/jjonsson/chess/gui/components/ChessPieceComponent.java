package com.jjonsson.chess.gui.components;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.apache.batik.swing.svg.JSVGComponent;
import org.w3c.dom.svg.SVGDocument;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.UnavailableMoveException;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.PieceImageCache;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.MoveListener;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Piece;

public class ChessPieceComponent extends JSVGComponent implements MoveListener, MouseListener//, SVGLoadEventDispatcherListener, SVGDocumentLoaderListener, GVTTreeBuilderListener, GVTTreeRendererListener
{
	private static final long	serialVersionUID	= 3048642024943627256L;
	
	public static final int BORDER_SIZE = 6;
	public static final int MARGIN = 10;
	public static final int SIZE = ChessWindow.DEFAULT_WINDOW_WIDTH / ChessBoard.BOARD_SIZE;

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
			setSize(pieceSize(), pieceSize());
			//Makes sure that the image is displayed without a background
			setBackgroundColor(); 
            
			setLocation(getPointForPiece(myPieceToDraw));
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
		Point p = new Point(currentPosition.getColumn() * SIZE + BORDER_SIZE + MARGIN, (ChessBoard.BOARD_SIZE - currentPosition.getRow() - 1) * SIZE + BORDER_SIZE + MARGIN);
		return p;
	}
	
	private static final int pieceSize()
	{
		return ChessPieceComponent.SIZE - MARGIN * 2 - BORDER_SIZE * 2;
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
	/*
	@Override
	public void documentLoadingStarted(SVGDocumentLoaderEvent e)
	{
		System.out.println("Started to load document" + e);
	}


	@Override
	public void documentLoadingCompleted(SVGDocumentLoaderEvent e)
	{
		System.out.println("Completed document loading " + e);
	}


	@Override
	public void documentLoadingCancelled(SVGDocumentLoaderEvent e)
	{
		System.out.println("Document Loading Cancelled " + e);
	}


	@Override
	public void documentLoadingFailed(SVGDocumentLoaderEvent e)
	{
		System.out.println("Document Loading Failed: " + e);
	}


	@Override
	public void gvtRenderingPrepare(GVTTreeRendererEvent e)
	{
		System.out.println("Rendering in prepare phase" + e);
	}


	@Override
	public void gvtRenderingStarted(GVTTreeRendererEvent e)
	{
		System.out.println("Rendering started phase" + e);
	}


	@Override
	public void gvtRenderingCompleted(GVTTreeRendererEvent e)
	{
		System.out.println("Rendering complete" + e);
	}


	@Override
	public void gvtRenderingCancelled(GVTTreeRendererEvent e)
	{
		System.out.println("Rendering cancelled" + e);
	}


	@Override
	public void gvtRenderingFailed(GVTTreeRendererEvent e)
	{
		System.out.println("Rendering failed" + e);
	}


	@Override
	public void gvtBuildStarted(GVTTreeBuilderEvent e)
	{
		System.out.println("gvtBuildStarted" + e);
	}


	@Override
	public void gvtBuildCompleted(GVTTreeBuilderEvent e)
	{
		System.out.println("gvtBuildCompleted" + e);
	}


	@Override
	public void gvtBuildCancelled(GVTTreeBuilderEvent e)
	{
		System.out.println("gvtBuildCancelled" + e);
	}


	@Override
	public void gvtBuildFailed(GVTTreeBuilderEvent e)
	{
		System.out.println("gvtBuildFailed" + e);
	}


	@Override
	public void svgLoadEventDispatchStarted(SVGLoadEventDispatcherEvent e)
	{
		System.out.println("svgLoadEventDispatchStarted" + e);
	}


	@Override
	public void svgLoadEventDispatchCompleted(SVGLoadEventDispatcherEvent e)
	{
		System.out.println("svgLoadEventDispatchCompleted" + e);
	}


	@Override
	public void svgLoadEventDispatchCancelled(SVGLoadEventDispatcherEvent e)
	{
		System.out.println("svgLoadEventDispatchCancelled" + e);
	}


	@Override
	public void svgLoadEventDispatchFailed(SVGLoadEventDispatcherEvent e)
	{
		System.out.println("svgLoadEventDispatchFailed" + e);
	}*/

	@Override
	public void mouseClicked(MouseEvent e)
	{
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
					boardComponent.setSelectedPiece(null);
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
