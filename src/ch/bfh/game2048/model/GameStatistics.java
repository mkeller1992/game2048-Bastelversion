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

@XmlType(propOrder = { "playerName", "score", "highestValue", "amountOfMoves", "startMil", "durationMil", "boardSize", "timeOfAddingToScoreList" })
public class GameStatistics {

	private String playerName;

	@XmlElement(name = "Points")
	private int score;

	@XmlElement(name = "NumberOfMoves")
	private int amountOfMoves;
	private int highestValue;

	private long startMil;
	private long durationMil;
	private long TimeOfAddingToScoreList;
	private int rank;
	private int boardSize;

	DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault());
	String timeFormat = Config.getInstance().getPropertyAsString("timerTimeFormat");

	public GameStatistics() {

	}

	public GameStatistics(String playerName, int boardSize) {

		this.playerName = playerName;
		this.score = 0;
		this.amountOfMoves = 0;
		this.highestValue = 0;
		this.startMil = 0;
		this.durationMil = 0;
		this.boardSize = boardSize;
	}

	private void setRankAsString(String rankAsString) {
	}

	private void setFormattedDuration(String formattedDuration) {
	}

	private void setFormattedDate(String formattedDate) {
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	@XmlElement(name = "Nickname")
	public String getPlayerName() {
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

	@XmlElement(name = "durationMil")
	public long getDurationMil() {
		return durationMil;
	}

	public void setDurationMil(long durationMil) {
		this.durationMil = durationMil;
	}

	public String getFormattedDuration() {
		return DurationFormatUtils.formatDuration(getDurationMil(), timeFormat);
	}

	@XmlElement(name = "BoardSize")
	public int getBoardSize() {
		return boardSize;
	}

	public void setBoardSize(int boardSize) {
		this.boardSize = boardSize;
	}

	public long getTimeOfAddingToScoreList() {
		return TimeOfAddingToScoreList;
	}

	public void setTimeOfAddingToScoreList(long timeOfAddingToScoreList) {
		TimeOfAddingToScoreList = timeOfAddingToScoreList;
	}

	/**
	 * To check this Game-Statistics equals another GameStatistics-object
	 * 
	 * @param gameStats
	 * @return true if the two Game-Statistics objects are equal
	 */

	public boolean equals(GameStatistics gameStats) {

		if (this.getPlayerName() != gameStats.getPlayerName()) {
			return false;
		}
		if (this.getScore() != gameStats.getScore()) {
			return false;
		}
		if (this.getHighestValue() != gameStats.getHighestValue()) {
			return false;
		}
		if (this.getAmountOfMoves() != gameStats.getAmountOfMoves()) {
			return false;
		}
		if (this.getTimeOfAddingToScoreList() != gameStats.getTimeOfAddingToScoreList()) {
			return false;
		}
		if (this.getStartMil() != gameStats.getStartMil()) {
			return false;
		}
		return true;
	}
}
