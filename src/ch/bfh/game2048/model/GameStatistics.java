package ch.bfh.game2048.model;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Observable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang3.time.DurationFormatUtils;

import ch.bfh.game2048.persistence.Config;

@XmlType(propOrder = { "playerName", "score", "highestValue", "amountOfMoves", "startMil" ,"endMil","boardSize", "timeOfAddingToScoreList" })
public class GameStatistics {
	
	
	private String playerName;

	@XmlElement(name = "Points")
	private int score;

	@XmlElement(name = "NumberOfMoves")
	private int amountOfMoves;
	private int highestValue;

	private long startMil;
	private long endMil;
	private long pauseTimeMil;
	private long TimeOfAddingToScoreList;
	private int rank;
	private int boardSize;	


	DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
	NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
	String timeFormat = Config.getInstance().getPropertyAsString("timerTimeFormat");

	public GameStatistics() {
		
	}

	public GameStatistics(String playerName, int boardSize) {

		this.playerName = playerName;
		this.score = 0;
		this.amountOfMoves = 0;
		this.highestValue = 0;
		this.startMil = System.currentTimeMillis();
		this.endMil = 0;
		this.pauseTimeMil = 0;
		this.boardSize = boardSize;			
	}

	
	
	private void setRankAsString(String rankAsString){		
	}
	
	private void setFormattedScore(String formattedScore) {
	}
	
	private void setFormattedDuration(String formattedDuration){			
	}
	
	private void setFormattedDate(String formattedDate){		
	}
	
	public void setPlayerName(String playerName){
		this.playerName = playerName;
	}
	
	@XmlElement(name = "Nickname")
	public String getPlayerName(){
		return playerName;
	}


	@XmlTransient
	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getRankAsString() {
		return new String(rank + ".");
	}

	public void addScore(int score) {
		this.score += score;	
	}
	
	public int getScore() {
		return score;
	}

	public String getFormattedScore() {
		return numberFormat.format(score);
	}

	public int getAmountOfMoves() {
		return amountOfMoves;
	}

	public void incrementMoves() {
		this.amountOfMoves++;
	}

	@XmlElement(name = "HighestTile")
	public int getHighestValue() {
		return highestValue;
	}

	public void setHighestValue(int highestValue) {
		this.highestValue = highestValue;
	}

	public String getFormattedDate() {
		return dateFormat.format(startMil);
	}

	@XmlElement(name = "StartMillis")
	public long getStartMil() {
		return startMil;
	}

	public void setStartMil(long startMil) {
		this.startMil = startMil;
	}

	@XmlElement(name = "EndMillis")
	public long getEndMil() {
		return endMil;
	}

	public void setEndMil(long endMil) {
		this.endMil = endMil;
	}
	
	public String getFormattedDuration() {
		return DurationFormatUtils.formatDuration(getEndMil()-getStartMil(), timeFormat);
	}

	@XmlElement(name = "BoardSize")
	public int getBoardSize() {
		return boardSize;
	}

	public void setBoardSize(int boardSize) {
		this.boardSize = boardSize;
	}

	public void stopTime(boolean gameOver) {
		setEndMil(System.currentTimeMillis()-pauseTimeMil);
		setTimeOfAddingToScoreList(System.currentTimeMillis());

	}
	
	public void pauseTime(){
		setEndMil(System.currentTimeMillis());
	}

	public void resumeTime(){
		this.pauseTimeMil += System.currentTimeMillis()-endMil;
		
	}

	public long getTimeOfAddingToScoreList() {
		return TimeOfAddingToScoreList;
	}

	public void setTimeOfAddingToScoreList(long timeOfAddingToScoreList) {
		TimeOfAddingToScoreList = timeOfAddingToScoreList;
	}
	
}
