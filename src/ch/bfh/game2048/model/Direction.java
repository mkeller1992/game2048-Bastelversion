package ch.bfh.game2048.model;

/**
 * Direction-Enum
 * 
 * Additionally stores the "Step-Directions" need to go through the board in the given direction.
 * Row-Step and Col-Step can be seen as the differences on the X-/Y-Axis.
 * 
 */
public enum Direction {
	UP(-1, 0),
	RIGHT(0, 1),
	DOWN(1, 0),
	LEFT(0, -1);
	
	private final int rowStep;
	private final int colStep;	
	
	Direction(int rowS, int colS){
		this.rowStep = rowS;
		this.colStep = colS;
	}

	public int getRowStep() {
		return rowStep;
	}

	public int getColStep() {
		return colStep;
	}
}
