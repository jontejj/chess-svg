package com.jjonsson.chess.gui.components;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

import org.junit.Test;

import com.jjonsson.chess.ChessBoard;
import com.jjonsson.chess.exceptions.InvalidPosition;
import com.jjonsson.chess.gui.ChessWindow;
import com.jjonsson.chess.gui.components.ChessBoardComponent;
import com.jjonsson.chess.moves.Position;
import com.jjonsson.chess.pieces.Queen;
import com.jjonsson.chess.scenarios.TestScenarios;

public class TestChessBoardComponent
{

	@Test
	public void testPerformHintedMove()
	{
		ChessBoard board = new ChessBoard(true);
		ChessWindow window = new ChessWindow(board);
		
		window.displayGame();
		
		ChessBoardComponent component = window.getBoardComponent();
		component.showHint();
		//Show hint should select a piece to move
		assertNotNull(component.getSelectedPiece());
		//TODO(jontejj) validate that it was a good move
		sleep();
		component.positionClicked(component.getHintMove().getPositionIfPerformed());
		sleep();
	}
	
	public static void sleep()
	{
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			fail();
		}
	}

}
