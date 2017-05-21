package ch.bfh.game2048.engine;

import java.util.Observable;

import ch.bfh.game2048.model.Direction;
import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.model.Tile;

public class GameEngine extends Observable {
	int boardSize;
	Tile[][] board;
	GameStatistics stats;

	/**
	 * Constructor 1 of the GameEngine, Initializes the board of a given size
	 * with empty Tiles, yet without any spawned numbers
	 * 
	 * @param boardSize
	 *            size of the board
	 */
	
	public GameEngine(int boardSize){
		this.boardSize = boardSize;
		board = new Tile[boardSize][boardSize];
		initGameBoard();
	}
	
	
	/**
	 * Constructor 2 of the GameEngine, Initializes the board with the given size and spawns random tiles.
	 * All the statistics are recorded in the given GameStatistics-Object.
	 * 
	 * @param boardSize
	 *            size of the board
	 * @param stats
	 *            object to store the game statistics
	 */
	public GameEngine(int boardSize, GameStatistics stats) {
		this.boardSize = boardSize;
		this.stats = stats;

		board = new Tile[boardSize][boardSize];

		initGameBoard();
		
		spawnRandomTile();
		spawnRandomTile();
		
	}

	/**
	 * Initializes the gameBoard with new (empty) Tile-Objects
	 */
	private void initGameBoard() {

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				board[i][j] = new Tile();
			}
		}
	}

	private void spawnRandomTile() {
		boolean done = false;

		while (!done) {
			int row = (int) (Math.random() * (boardSize));
			int col = (int) (Math.random() * (boardSize));

			if (board[row][col].getValue() == 0) {
				board[row][col].setValue(getRandomValue());
				board[row][col].setSpawned(true);
				done = true;
			}
		}
	}

	/**
	 * Moves the whole board in a given Direction.
	 * 
	 * If an action was performed a new random tile will be spawned.
	 * 
	 * @param dir
	 *            the direction to move the Board
	 * @return boolean true if something was moved
	 */
	public boolean move(Direction dir) {
		boolean moved = false;
				
		resetMergedInfo();

		moved = moveBoard(dir);

		if (moved) {
			stats.incrementMoves();
			spawnRandomTile();
			if(isGameOver()){
			stats.setGameOver(true);
			}
		}		
		
		return moved;
	}

	/**
	 * Executes the moving and merging.
	 * 
	 * Each Tile is moved and then merged individually. The moving-method is
	 * recursive and returns the amount steps the tile was moved.
	 * 
	 * @param dir
	 * @return
	 */
	private boolean moveBoard(Direction dir) {
		boolean validMove = false;

		/*
		 * Loop-Start and Loop-Step-Direction differ depending on the direction
		 * (if we move up, we start from the uppermost tile working us down and
		 * vice versa, therefore the step is counter-logic to the direction,
		 * hence * -1 )
		 */
		int start = 0;
		int step = -1 * ((dir.getColStep() != 0) ? dir.getColStep() : dir.getRowStep());
		if (step < 0) {
			start = boardSize - 1;
		}

		// loop through rows/columns
		for (int i = 0; i < boardSize; i++) {
			// loop through Tiles
			for (int j = start; j < boardSize && j >= 0; j += step) {

				// if we move horizontal the outer loop is the row and the inner
				// loop is the column of the array, for a vertical move we have
				// to switch row and column
				int row = dir.equals(Direction.LEFT) || dir.equals(Direction.RIGHT) ? i : j;
				int col = dir.equals(Direction.LEFT) || dir.equals(Direction.RIGHT) ? j : i;

				// we only need to do something if we are not on a blank tile
				if (board[row][col].getValue() != 0) {
					/*
					 * Each Tile in a row gets individually moved and then
					 * merged. Therefore we need to know by how much we moved
					 * the Tile and in which direction.
					 */
					int moveBy = moveTile(row, col, dir);
					boolean merged = mergeTile(row + (moveBy * dir.getRowStep()), col + (moveBy * dir.getColStep()),
							dir);

					if (moveBy > 0 || merged) {
						validMove = true;
					}
				}
			}
		}
		return validMove;
	}

	/**
	 * Moves the Tile in the given Direction, until it is next to the border or
	 * to another Tile.
	 * 
	 * @param row
	 * @param col
	 * @param dir
	 * @return movedBy number of steps the tile was moved
	 */
	private int moveTile(int row, int col, Direction dir) {
		if (col + dir.getColStep() < boardSize && col + dir.getColStep() >= 0 && row + dir.getRowStep() < boardSize
				&& row + dir.getRowStep() >= 0) {

			if (board[row + dir.getRowStep()][col + dir.getColStep()].getValue() == 0) {
				Tile tmp = board[row + dir.getRowStep()][col + dir.getColStep()];
				board[row + dir.getRowStep()][col + dir.getColStep()] = board[row][col];
				board[row][col] = tmp;

				return 1 + moveTile(row + dir.getRowStep(), col + dir.getColStep(), dir);
			}

		}
		return 0;
	}

	/**
	 * Tries to merge the Tile in the given Direction, Returns true if a merge
	 * was made and updates GameStatistics
	 * 
	 * @param row
	 * @param col
	 * @param dir
	 * @return true if a merge was made
	 */
	private boolean mergeTile(int row, int col, Direction dir) {
		// check for borders
		if (row + dir.getRowStep() >= 0 && row + dir.getRowStep() < boardSize && col + dir.getColStep() >= 0
				&& col + dir.getColStep() < boardSize) {

			Tile tile1 = board[row][col];
			Tile tile2 = board[row + dir.getRowStep()][col + dir.getColStep()];

			// was either of the tiles already merged this round
			if (tile1.isMerged() || tile2.isMerged()) {
				return false;
			}

			// do the tiles have the same value
			if (tile1.getValue() == tile2.getValue()) {

				int mergedValue = 2 * tile2.getValue();

				board[row][col] = new Tile();
				board[row + dir.getRowStep()][col + dir.getColStep()].setValue(mergedValue);
				board[row + dir.getRowStep()][col + dir.getColStep()].setMerged(true);

				stats.addScore(mergedValue);
				
				if(mergedValue > 0){
					System.out.println("Notify");
					setChanged();
					notifyObservers();						
				}
								
				if (mergedValue > stats.getHighestValue()) {
					stats.setHighestValue(mergedValue);
				}
				return true;
			}
		}
		return false;
	}
	

	private boolean isGameOver() {
		boolean boardFull = true;

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (board[i][j].getValue() == 0)
					boardFull = false;
			}
		}

		if (boardFull) {
			for (int row = 0; row < boardSize; row++) {
				for (int col = 0; col < boardSize; col++) {
					for (Direction dir : Direction.values()) {
						if (row + dir.getRowStep() >= 0 && row + dir.getRowStep() < boardSize
								&& col + dir.getColStep() >= 0 && col + dir.getColStep() < boardSize) {
							Tile tile1 = board[row][col];
							Tile tile2 = board[row + dir.getRowStep()][col + dir.getColStep()];
							if (tile1.getValue() == tile2.getValue()) {
								return false;
							}
						}
					}
				}
			}
		}
		return boardFull;
	}

	/**
	 * Resets the merged-attribute on all the tiles.
	 */
	private void resetMergedInfo() {
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				board[i][j].setMerged(false);
				board[i][j].setSpawned(false);
			}
		}
	}

	private int getRandomValue() {
		return Math.random() > 0.9 ? 4 : 2;
	}

	public void print() {

		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.print(board[i][j].getValue() + "   ");
			}
			System.out.println();
		}
	}

	public GameStatistics getStats() {
		return stats;
	}

	/**
	 * Needed for Unit-Testing
	 * 
	 * @param board
	 */
	protected void setBoard(Tile[][] board) {
		this.board = board;
	}
	
	public Tile[][] getBoard(){
		return board;
	}

}
