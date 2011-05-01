package com.jjonsson.chess;

public class Chess {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{	
		ChessGame game = new ChessGame();
		/*ChessWindow window = new ChessWindow(game);
		ChessBoardComponent comp = new ChessBoardComponent(window);
		Method meth;
		try
		{
			meth = comp.getClass().getDeclaredMethod("getPositionForPoint", Point.class);
			meth.setAccessible(true);
			Point p = new Point(647, 543);
			Position pos = (Position) meth.invoke(comp, p);
			System.out.println(pos);
		}
		catch (SecurityException e1)
		{
			e1.printStackTrace();
		}
		catch (NoSuchMethodException e1)
		{
			e1.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}*/
		
		game.launch();
	}

}
