package ch.bfh.game2048.engine;

import ch.bfh.game2048.model.Direction;
import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.model.Tile;
import junit.framework.TestCase;

public class EngineTest extends TestCase {
	Tile[][] board;
	GameEngine game;
	
	private void init(int boardSize){		
		game = new GameEngine(boardSize, new GameStatistics("",boardSize));
		board = new Tile[boardSize][boardSize];
		
		
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				board[i][j] = new Tile();
			}
		}
		game.setBoard(board);
				
	}
	
	public void testRightUp(){						
		init(4);				
		/*
		 4   0   0   0   
		 0   0   0   0   
		 2   2   0   0   
		 0   0   0   0   
		 */
		board[0][0].setValue(4);
		board[2][0].setValue(2);
		board[2][1].setValue(2);		
	
		game.print();
		game.move(Direction.RIGHT);
		game.move(Direction.UP);		
		game.print();
		/*
		0   0   0   8   
		0   0   0   0   
		0   0   0   0   
		0   0   0   0   
		 */

		assertEquals(board[0][3].getValue(), 8);
		assertEquals(game.getStats().getHighestValue(), 8);
		assertEquals(game.getStats().getScore(), 12);
		
	}
	
	
	public void testLeftDoubleMerge(){						
		init(4);				
		/*
		 2   2   2   2   
		 0   0   0   0   
		 0   0   0   0   
		 0   0   0   0   
		 */
		board[0][0].setValue(2);
		board[0][1].setValue(2);
		board[0][2].setValue(2);
		board[0][3].setValue(2);
	
		game.move(Direction.LEFT);		

		/*
		0   0   0   8   
		0   0   0   0   
		0   0   0   0   
		0   0   0   0   
		 */

		assertEquals(board[0][0].getValue(), 4);
		assertEquals(board[0][1].getValue(), 4);
		assertEquals(game.getStats().getScore(), 8);
	}
	
	
	public void testRIGHTDoubleMerge(){						
		init(4);				
		/*
		 2   2   2   2   
		 0   0   0   0   
		 0   0   0   0   
		 0   0   0   0   
		 */
		board[0][0].setValue(2);
		board[0][1].setValue(2);
		board[0][2].setValue(2);
		board[0][3].setValue(2);
	
		game.move(Direction.RIGHT);		

		/*
		0   0   4   4   
		0   0   0   0   
		0   0   0   0   
		0   0   0   0   
		 */

		assertEquals(board[0][2].getValue(), 4);
		assertEquals(board[0][3].getValue(), 4);
		assertEquals(game.getStats().getScore(), 8);
	}
	
	
	public void testDown(){						
		init(4);				
		/*
		4   4   0   0   
		4   0   0   0   
		2   0   0   0   
		2   0   0   0    
		 */
		board[0][0].setValue(4);
		board[1][0].setValue(4);
		board[2][0].setValue(2);
		board[3][0].setValue(2);
		board[0][1].setValue(4);	
		game.print();
		game.move(Direction.DOWN);		
		game.print();
		/*
		0   0   0   0   
		0   0   0   0   
		8   0   0   0   
		4   4   0   0   
		 */

		assertEquals(board[2][0].getValue(), 8);
		assertEquals(board[3][0].getValue(), 4);
		assertEquals(board[3][1].getValue(), 4);
		assertEquals(game.getStats().getScore(), 12);
	}
	
	
	public void testEqualAdjacentTilesAfterMerge(){						
		init(4);				
		/*
		2   4   0   0   
		2   0   0   0   
		2   0   0   0   
		2   0   0   0    
		 */
		board[0][0].setValue(2);
		board[1][0].setValue(2);
		board[2][0].setValue(2);
		board[3][0].setValue(2);
		board[0][1].setValue(4);	
		
		game.move(Direction.DOWN);		
		
		/*
		0   0   0   0   
		0   0   0   0   
		4   0   0   0   
		4   4   0   0   
		 */

		assertEquals(board[2][0].getValue(), 4);
		assertEquals(board[3][0].getValue(), 4);
		assertEquals(board[3][1].getValue(), 4);
		assertEquals(game.getStats().getScore(), 8);
	}
	
	
	
}
