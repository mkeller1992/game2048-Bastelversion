package ch.bfh.game2048;

import java.util.List;

import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.model.Highscore;
import ch.bfh.game2048.persistence.Config;
import ch.bfh.game2048.view.BoardSizes;
import ch.bfh.game2048.view.MainUIController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class Paginatior extends VBox {

    private final static int dataSize = 10_023;
    private final static int rowsPerPage = 1000;
    private Config conf = Config.getInstance();
    
	Highscore highscores;
	int boardSize;
	MainUIController mainUI;
	
	Button okayButton;
	ComboBox<BoardSizes> boardSizeList;	
	
	ObservableList<GameStatistics> masterList;
	FilteredList<GameStatistics> filteredData;
	

    private final TableView table = createTable();


    
    public Paginatior(Highscore highscores, MainUIController mainUI, int boardSize){
    	

		this.highscores = highscores;
		this.boardSize = boardSize;
		this.mainUI = mainUI;
    	

        
        
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
//			setContentFromLists();
//			highlightRow(0);
		});

		TextField filterField = new TextField();
		filterField.setPromptText(conf.getPropertyAsString("highscoreListFilterText"));

		titlePane.getChildren().addAll(titleLabel, boardSizeList, filterField);
		
		this.getChildren().add(titlePane);
		
		
		
		////////////////////////////////////////
		
		List<GameStatistics> baseList = highscores.getFilteredHighscoreList(boardSize);
		baseList = highscores.sortSetRanksResizeList(baseList, Config.getInstance().getPropertyAsInt("maxNumberOfScores"));

		masterList = FXCollections.observableArrayList(baseList);
		filteredData = new FilteredList<>(masterList, p -> true);
		
        Pagination pagination = new Pagination((filteredData.size() / rowsPerPage + 1), 0);
        pagination.setPageFactory(this::createPage);

        
        BorderPane bp = new BorderPane(pagination);
        bp.setPrefSize(1024, 768);
        bp.setMaxSize(1024, 768);
        bp.setMinSize(1024, 768);
        this.getChildren().add(bp);
        
        Scene scene = new Scene(this , 1024, 768);
        Main.getStage().setScene(scene);
        
        ////////////////////////////////////////////
        
        
		// Set the filter Predicate whenever the filter changes.
		filterField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(player -> {
				

				
				// If filter text is empty, display all score-entries.
				if (newValue == null || newValue.isEmpty()) {

//					pagination.setPageFactory(this::createPage);
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
        
        
    }
    
    

    private TableView createTable() {

    	TableView table = new TableView<>();
		table.setPrefHeight(500);
		table.setId("yellow_cell");

		TableColumn tblRank = new TableColumn(conf.getPropertyAsString("colTitleRank.dialog"));
		TableColumn tblName = new TableColumn(conf.getPropertyAsString("colTitleName.dialog"));
		TableColumn tblScore = new TableColumn(conf.getPropertyAsString("colTitleScore.dialog"));
		TableColumn tblHighestTile = new TableColumn(conf.getPropertyAsString("colTitleMaxTile.dialog"));
		TableColumn tblDuration = new TableColumn(conf.getPropertyAsString("colTitleDuration.dialog"));
		TableColumn tblNumbOfMoves = new TableColumn(conf.getPropertyAsString("colTitleNumbOfMoves.dialog"));
		TableColumn tblDate = new TableColumn(conf.getPropertyAsString("colTitleDateTime.dialog"));

		tblRank.setCellValueFactory(new PropertyValueFactory<GameStatistics, String>("rankAsString"));
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
			
		table.setEditable(true);
		
		return table;
    }

    private Node createPage(int pageIndex) {

    	
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, filteredData.size());
        table.setItems(FXCollections.observableArrayList(filteredData.subList(fromIndex, toIndex)));

        return new BorderPane(table);
    }
}