package ch.bfh.game2048.view;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import ch.bfh.game2048.engine.GameEngine;
import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.model.Highscore;
import ch.bfh.game2048.model.PushObject;
import ch.bfh.game2048.persistence.Config;
import ch.bfh.game2048.persistence.OnlineScoreHandler;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

@SuppressWarnings("unchecked")
public class LiveHighScoreView extends VBox implements Observer {

	TableView table;

	GameEngine engine;

	Highscore highscoreManager;
	OnlineScoreHandler onlineScoreHandler;
	GameStatistics activeStats;
	Config conf;
	int boardSize;
	int maxNumberOfScoresOnExtract;

	ObservableList<GameStatistics> masterList;

	@SuppressWarnings({ "rawtypes" })
	public LiveHighScoreView(Highscore highscoreManager, int boardSize) {
		
		this.highscoreManager = highscoreManager;
		
		this.boardSize = boardSize;
		maxNumberOfScoresOnExtract = Config.getInstance().getPropertyAsInt("maxNumberOfScoresOnExtract");

		conf = Config.getInstance();	

		// Assemble TableView

		table = new TableView<>();
		table.setPrefHeight(500);
		table.setFocusTraversable(false);
		table.setEditable(false);
		table.setId("yellow_cell");

		TableColumn tblRank = new TableColumn(conf.getPropertyAsString("colTitleRank.dialog"));
		TableColumn tblName = new TableColumn(conf.getPropertyAsString("colTitleName.dialog"));
		TableColumn tblScore = new TableColumn(conf.getPropertyAsString("colTitleScore.dialog"));

		tblRank.setSortable(false);
		tblName.setSortable(false);
		tblScore.setSortable(false);

		tblRank.setCellValueFactory(new PropertyValueFactory<GameStatistics, Integer>("rankAsString"));
		tblName.setCellValueFactory(new PropertyValueFactory<GameStatistics, String>("playerName"));
		tblScore.setCellValueFactory(new PropertyValueFactory<GameStatistics, Integer>("score"));

		table.getColumns().addAll(tblRank, tblName, tblScore);

		tblRank.setPrefWidth(50);
		tblName.setPrefWidth(150);
		tblScore.setPrefWidth(90);
		tblScore.setStyle("-fx-alignment: CENTER-RIGHT;");

		// Set the way the table-entries are sorted:
		// tblScore.setSortType(TableColumn.SortType.DESCENDING);
		// table.getSortOrder().add(tblScore);

		// Panel with "Back to Game"-Button

		HBox buttonPanel = new HBox();
		buttonPanel.setAlignment(Pos.CENTER_RIGHT);
		buttonPanel.setPadding(new Insets(10, 10, 10, 10));

		// Initialize livescore-list on UI with the score-information
		refreshContent();

		table.getStyleClass().add("noheader");
		
		this.getChildren().addAll(table, buttonPanel);
		
	}

	public void setActiveStats(GameStatistics activeStats) {
		this.activeStats = activeStats;
	}
	
	public void removeActiveStats() {
		this.activeStats = null;
	}
	
	public void setOnlineScoreHandler(OnlineScoreHandler onlineScoreHandler){
		this.onlineScoreHandler = onlineScoreHandler;
		onlineScoreHandler.addObserver(this);	
	}
	
	public void setGameEngine(GameEngine engine){
		this.engine = engine;
		engine.addObserver(this);
	}

	public void refreshContent() {

		List scoreList = highscoreManager.getListExtract(boardSize, maxNumberOfScoresOnExtract, activeStats);

		masterList = FXCollections.observableArrayList(scoreList);		
		table.setItems(masterList);
		
		if (activeStats != null) {
			int indexOfCurrentScore = highscoreManager.getRankOfListEntry(scoreList, activeStats)-1;
			table.getSelectionModel().select(indexOfCurrentScore);
		}
		
		table.refresh();	
	}

	@Override
	public void update(Observable o, Object arg) {

		System.out.println("Was here");
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
			
				if(o instanceof OnlineScoreHandler){			
					System.out.println("NEW Highscore!!");
					refreshContent();
					
				}		
				else if(o instanceof GameEngine){			
					refreshContent();				
				}								
			}
		}
	);
	}

}
