package ch.bfh.game2048.engine;
import java.util.Comparator;

import ch.bfh.game2048.model.GameStatistics;

public class ScoreComparator implements Comparator<GameStatistics> {

	@Override
	public int compare(GameStatistics p1, GameStatistics p2) {
		
		int points1 = p1.getScore();
		int points2 = p2.getScore();
		
		int highestTile1 = p1.getHighestValue();
		int highestTile2 = p2.getHighestValue();
		
		long duration1 = p1.getDurationMil();
		long duration2 = p2.getDurationMil();
		
		int numbOfMoves1 = p1.getAmountOfMoves();
		int numbOfMoves2 = p2.getAmountOfMoves();
		
		
		if(points1 > points2){
			return -1;
		} 
		if (points1 < points2){
			return 1;
		}

		if(highestTile1 > highestTile2){
			return -1;
		}
		if(highestTile1 < highestTile2){
			return 1;
		}
	
		if(duration1 > duration2){
			return -1;
		}
		if(duration1 < duration2){
			return 1;
		}
	
		if(numbOfMoves1 < numbOfMoves2){
			return -1;
		}
		if(numbOfMoves1 > numbOfMoves2){
			return 1;
		}
		return 0;
	}	
}
