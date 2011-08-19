package com.jjonsson.chess.gui.components;

import static com.jjonsson.utilities.Logger.LOGGER;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;

import org.apache.batik.swing.svg.JSVGComponent;
import org.w3c.dom.svg.SVGDocument;

import com.jjonsson.chess.board.ChessBoard;
import com.jjonsson.chess.gui.PieceImageCache;
import com.jjonsson.chess.listeners.MoveListener;
import com.jjonsson.chess.moves.Move;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.moves.RevertingMove;
import com.jjonsson.chess.pieces.Piece;

public class ChessPieceComponent extends JSVGComponent implements MoveListener, MouseListener//, SVGLoadEventDispatcherListener, SVGDocumentLoaderListener, GVTTreeBuilderListener, GVTTreeRendererListener
{
	private static final long	serialVersionUID	= 3048642024943627256L;

	private Piece myPieceToDraw;

	private ChessBoardComponent	myBoardComponent;

	public ChessPieceComponent(final Piece pieceToDraw, final ChessBoardComponent boardComponent)
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

	private Point getPointForPiece(final Piece piece)
	{
		Position currentPosition = piece.getCurrentPosition();
		return new Point(currentPosition.getColumn() * myBoardComponent.getCurrentPieceSize().width + myBoardComponent.getPieceBorderSize() + myBoardComponent.getPieceMargin(), (ChessBoard.BOARD_SIZE - currentPosition.getRow() - 1) * myBoardComponent.getCurrentPieceSize().height + myBoardComponent.getPieceBorderSize() + myBoardComponent.getPieceMargin());
	}

	public final void updateSize()
	{
		if(myBoardComponent == null)
		{
			pieceRemoved(myPieceToDraw);
			return;
		}
		setSize(myBoardComponent.getCurrentPieceSize().width - myBoardComponent.getPieceBorderSize() * 2 - myBoardComponent.getPieceMargin() * 2, myBoardComponent.getCurrentPieceSize().height - myBoardComponent.getPieceBorderSize() * 2 - myBoardComponent.getPieceMargin() * 2);
		setLocation(getPointForPiece(myPieceToDraw));
	}

	private void setBackgroundColor()
	{
		//setBackground(null);
		if ( (myPieceToDraw.getCurrentPosition().getRow() % 2) == (myPieceToDraw.getCurrentPosition().getColumn() % 2) )
		{
			setBackground(ChessBoardComponent.LIGHT_BACKGROUND);
		}
		else
		{
			setBackground(ChessBoardComponent.DARK_BACKGROUND);
		}
	}

	private void refresh()
	{
		setBackgroundColor();
		setLocation(getPointForPiece(myPieceToDraw));
		repaint();
	}

	@Override
	public void movePerformed(final Move performedMove)
	{
		refresh();
	}

	@Override
	public void moveReverted(final RevertingMove move)
	{
		refresh();
	}

	@Override
	public void reset()
	{
	}

	@Override
	public final void pieceRemoved(final Piece removedPiece)
	{
		if(removedPiece == myPieceToDraw)
		{
			this.dispose();

			if(this.getParent() != null)
			{
				this.getParent().remove(this);
			}

			myBoardComponent = null;
			myPieceToDraw = null;
		}
	}

	@Override
	public void mouseClicked(final MouseEvent e)
	{
		long startNanos = System.nanoTime();
		/*System.out.println(myBoardComponent.getBoard().getAvailableMoves(myPieceToDraw.getCurrentPosition(), !myPieceToDraw.getAffinity()));
		myBoardComponent.getBoard().updateGameState();
		for(Move m : myPieceToDraw.getPossibleMoves())
		{
			m.updateMove(myBoardComponent.getBoard());
		}
		myBoardComponent.repaint();*/
		LOGGER.finest(this.getPiece() + " clicked");
		//TODO: this should not be needed
		//this.getPiece().initilizePossibilityOfMoves(getPiece().getBoard());
		ChessBoardComponent boardComponent = myBoardComponent;
		Piece currentlySelected = boardComponent.getSelectedPiece();
		if(currentlySelected != null && !currentlySelected.hasSameAffinityAs(this.getPiece()))
		{
			ChessBoard board = boardComponent.getBoard();
			Move takeOverMove = board.getAvailableMove(currentlySelected, this.getPiece().getCurrentPosition());
			if(takeOverMove != null)
			{
				//The currently selected piece is going to take over this one
				if(currentlySelected.performMove(takeOverMove, boardComponent.getBoard()))
				{
					return;
				}
				LOGGER.info("Unavailable move: " + takeOverMove);
			}
		}

		if(this.getPiece().hasSameAffinityAs(boardComponent.getBoard().getCurrentPlayer())
				&& this.getPiece().canMakeAMove(boardComponent.getBoard()))
		{
			//If no takeover was done it means that this piece should be the selected one (given that it's this players turn)
			//Only select pieces that can make a move
			boardComponent.setSelectedPiece(this.getPiece());
		}
		long time = (System.nanoTime() - startNanos);
		BigDecimal bd = new BigDecimal(time).divide(BigDecimal.valueOf(SECONDS.toNanos(1)));
		LOGGER.finest("Seconds: " + bd.toPlainString());
	}
	@Override
	public void mousePressed(final MouseEvent e){}
	@Override
	public void mouseReleased(final MouseEvent e){}
	@Override
	public void mouseEntered(final MouseEvent e){}
	@Override
	public void mouseExited(final MouseEvent e){}
}
