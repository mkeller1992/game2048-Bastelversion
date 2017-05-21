package ch.bfh.game2048.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ch.bfh.game2048.engine.ScoreComparator;
import ch.bfh.game2048.persistence.Config;
import ch.bfh.game2048.persistence.OnlineScoreHandler;

@XmlRootElement(name = "HighscoreList")
public class Highscore {

	private ReentrantLock lock = new ReentrantLock();

	private ScoreComparator comparator;

	@XmlElementWrapper(name = "Highscores")
	@XmlElement(name = "PlayerScore")
	private ArrayList<GameStatistics> highscores;

	public Highscore() {
		comparator = new ScoreComparator();
		highscores = new ArrayList<GameStatistics>();
	}

	public void setHighscores(ArrayList<GameStatistics> highscores) {

		lock.lock();
		this.highscores = highscores;
		lock.unlock();
	}

	public void addHighscore(GameStatistics highscore) {
		lock.lock();
		highscores.add(highscore);
		lock.unlock();
	}

	public ArrayList<GameStatistics> getCompleteHighscoreList() {

		lock.lock();
		try {
			return highscores;
		} finally {
			lock.unlock();
		}
	}

	public List<GameStatistics> getFilteredHighscoreList(int boardSize) {

		
		
		List<GameStatistics> filteredList = getCompleteHighscoreList().stream().filter(h -> h.getBoardSize() == boardSize).collect(Collectors.toList());

		return filteredList;
	}

	// Cut Highscore-List to the number of allowed entries (specified in
	// Properties)
	// Set a rank for each score according to the criteria in "ScoreComparator"

	public List<GameStatistics> sortSetRanksResizeList(List<GameStatistics> filteredList, int maxNumberOfScores) {

		sortAndSetRanks(filteredList);
		List<GameStatistics> list = resizeList(filteredList, 1, maxNumberOfScores);
		return list;

	}

	public void sortList(List<GameStatistics> filteredList) {
		Collections.sort(filteredList, comparator);
	}

	public List<GameStatistics> resizeList(List<GameStatistics> filteredList, int firstRank, int lastRank) {

		if (filteredList.size() > (lastRank - firstRank + 1)) {
			filteredList = new ArrayList<GameStatistics>(filteredList.subList(firstRank - 1, lastRank));
		}
		return filteredList;
	}

	public void sortAndSetRanks(List<GameStatistics> filteredList) {

		sortList(filteredList);

		for (int i = 0; i < filteredList.size(); i++) {
			filteredList.get(i).setRank(i + 1);
		}
	}

	public int getRankOfListEntry(List<GameStatistics> filteredList, GameStatistics scorelistEntry) {

		sortList(filteredList);

		int i = 0;

		for (GameStatistics s : filteredList) {
			if (s.equals(scorelistEntry)) {
				return i + 1;
			} else {
				i++;
			}
		}
		return 0;
	}

	/////////////

	public List<GameStatistics> getListExtract(int boardSize, int maxExtractSize, GameStatistics activeStats) {

		List<GameStatistics> listExtract = getFilteredHighscoreList(boardSize);

		if (listExtract == null) {
			listExtract = new ArrayList<GameStatistics>();
			listExtract.add(activeStats);
			return null;
		}

		if (activeStats == null) {
			sortAndSetRanks(listExtract);
			listExtract = resizeList(listExtract, 1, Math.min(listExtract.size(), maxExtractSize));
			return listExtract;
		}

		listExtract.add(activeStats);
		sortAndSetRanks(listExtract);
		int currentRank = getRankOfListEntry(listExtract, activeStats);
		int numberOfScoresAfter = 3;

		if (listExtract.size() < maxExtractSize) {

		} else if (currentRank + numberOfScoresAfter < maxExtractSize) {
			listExtract = resizeList(listExtract, 1, maxExtractSize);
		} else if (currentRank + numberOfScoresAfter > listExtract.size()) {
			System.out.println((listExtract.size() - maxExtractSize + 1));
			System.out.println(listExtract.size());
			listExtract = resizeList(listExtract, (listExtract.size() - maxExtractSize + 1), listExtract.size());
		} else {
			listExtract = resizeList(listExtract, (currentRank + numberOfScoresAfter - maxExtractSize + 1), (currentRank + numberOfScoresAfter));
		}
		return listExtract;
	}

}
