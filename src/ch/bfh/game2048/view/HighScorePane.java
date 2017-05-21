package ch.bfh.game2048.view;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.bind.JAXBException;

import ch.bfh.game2048.Main;
import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.model.Highscore;
import ch.bfh.game2048.persistence.Config;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

@SuppressWarnings("unchecked")
public class HighScorePane extends VBox {

	TableView table;
	Button okayButton;

	ComboBox<BoardSizes> boardSizeList;

	Highscore highscores;
	Config conf;

	EventHandler<Event> btnSolHandler;

	ObservableList<GameStatistics> masterList;
	FilteredList<GameStatistics> filteredData;

	@SuppressWarnings({ "rawtypes" })
	public HighScorePane(Highscore highscores, int boardSize) {

		this.highscores = highscores;
		conf = Config.getInstance();

		// Assemble titlePane

		HBox titlePane = new HBox();
		titlePane.setPrefSize(770, 100);
		titlePane.setPadding(new Insets(10, 10, 10, 10));
		titlePane.setSpacing(25);
		Label titleLabel = new Label();

		Text titleText = new Text(conf.getPropertyAsString("highscoreListTitle"));
		titleText.setFont(Font.font(null, FontWeight.BOLD, 20));
		titleLabel.setGraphic(titleText);

		// Set comboBox with list of board-sizes:

		boardSizeList = new ComboBox();
		boardSizeList.getItems().setAll(BoardSizes.values());

		BoardSizes sizeToBeSelected = BoardSizes.findStateByBoardSize(boardSize);
		boardSizeList.getSelectionModel().select(sizeToBeSelected);

		boardSizeList.setOnAction((event) -> {
			setContentFromLists();
			highlightRow(0);
		});

		TextField filterField = new TextField();
		filterField.setPromptText(conf.getPropertyAsString("highscoreListFilterText"));

		titlePane.getChildren().addAll(titleLabel, boardSizeList, filterField);

		// Assemble TableView

		table = new TableView<>();
		table.setPrefHeight(500);
		table.setId("yellow_cell");

		TableColumn tblRank = new TableColumn(conf.getPropertyAsString("colTitleRank.dialog"));
		TableColumn tblName = new TableColumn(conf.getPropertyAsString("colTitleName.dialog"));
		TableColumn tblScore = new TableColumn(conf.getPropertyAsString("colTitleScore.dialog"));
		TableColumn tblHighestTile = new TableColumn(conf.getPropertyAsString("colTitleMaxTile.dialog"));
		TableColumn tblDuration = new TableColumn(conf.getPropertyAsString("colTitleDuration.dialog"));
		TableColumn tblNumbOfMoves = new TableColumn(conf.getPropertyAsString("colTitleNumbOfMoves.dialog"));
		TableColumn tblDate = new TableColumn(conf.getPropertyAsString("colTitleDateTime.dialog"));

		tblRank.setCellValueFactory(new PropertyValueFactory<GameStatistics, Integer>("rankAsString"));
		tblName.setCellValueFactory(new PropertyValueFactory<GameStatistics, String>("playerName"));
		tblScore.setCellValueFactory(new PropertyValueFactory<GameStatistics, Integer>("score"));
		tblHighestTile.setCellValueFactory(new PropertyValueFactory<GameStatistics, Integer>("highestValue"));
		tblDuration.setCellValueFactory(new PropertyValueFactory<GameStatistics, Long>("formattedDuration"));
		tblNumbOfMoves.setCellValueFactory(new PropertyValueFactory<GameStatistics, Integer>("amountOfMoves"));
		tblDate.setCellValueFactory(new PropertyValueFactory<GameStatistics, String>("formattedDate"));

		table.getColumns().addAll(tblRank, tblName, tblScore, tblHighestTile, tblDuration, tblNumbOfMoves, tblDate);

		tblRank.setPrefWidth(50);
		tblName.setPrefWidth(150);
		tblScore.setPrefWidth(90);
		tblScore.setStyle("-fx-alignment: CENTER-RIGHT;");
		tblHighestTile.setPrefWidth(90);
		tblHighestTile.setStyle("-fx-alignment: CENTER-RIGHT;");
		tblDuration.setPrefWidth(90);
		tblDuration.setStyle("-fx-alignment: CENTER-RIGHT;");
		tblNumbOfMoves.setPrefWidth(90);
		tblNumbOfMoves.setStyle("-fx-alignment: CENTER-RIGHT;");
		tblDate.setPrefWidth(170);
		tblDate.setStyle("-fx-alignment: CENTER;");

		// Set the way the table-entries are sorted:
		// tblScore.setSortType(TableColumn.SortType.DESCENDING);
		// table.getSortOrder().add(tblScore);

		// Panel with "Back to Game"-Button

		HBox buttonPanel = new HBox();
		buttonPanel.setAlignment(Pos.CENTER_RIGHT);
		buttonPanel.setPadding(new Insets(10, 10, 10, 10));
		okayButton = new Button(conf.getPropertyAsString("backToGame.button"));
		okayButton.addEventHandler(MouseEvent.MOUSE_CLICKED, createSolButtonHandler());
		buttonPanel.getChildren().addAll(okayButton);

		// Set lists with the score-information
		setContentFromLists();

		// Set the filter Predicate whenever the filter changes.
		filterField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(player -> {
				// If filter text is empty, display all score-entries.
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				// Compare player-nickname with the score-entries in the
				// tableview
				String lowerCaseFilter = newValue.toLowerCase();

				if (player.getPlayerName().toLowerCase().contains(lowerCaseFilter)) {
					return true; // Filter matches first name.
				}
				return false; // Does not match.
			});
		});

		this.getChildren().addAll(titlePane, table, buttonPanel);

	}

	private void setContentFromLists() {

		BoardSizes selectedEntry = (BoardSizes) boardSizeList.getSelectionModel().getSelectedItem();

		List<GameStatistics> baseList = highscores.getFilteredHighscoreList(selectedEntry.getBoardSize());
		baseList = highscores.sortSetRanksResizeList(baseList,
				Config.getInstance().getPropertyAsInt("maxNumberOfScores"));

		masterList = FXCollections.observableArrayList(baseList);
		filteredData = new FilteredList<>(masterList, p -> true);
		table.setItems(filteredData);
		table.setEditable(true);

	}

	public void highlightRow(int rowIndex) {

		if (rowIndex + 1 > Config.getInstance().getPropertyAsInt("maxNumberOfScores")) {
			table.scrollTo(0);
		} else {
			table.getSelectionModel().select(rowIndex);
			table.scrollTo(rowIndex);
		}
	}

	public EventHandler<Event> createSolButtonHandler() {
		btnSolHandler = new EventHandler<Event>() {

			@Override
			public void handle(Event event) {

				try {
					Main.switchScene(Scenes.MAINSCENE, 0);
				} catch (FileNotFoundException | JAXBException e) {
					e.printStackTrace();
				}

			}
		};
		return btnSolHandler;
	}

}
