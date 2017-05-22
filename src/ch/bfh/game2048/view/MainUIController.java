package ch.bfh.game2048.view;

import java.io.FileNotFoundException;
import java.util.Observable;
import java.util.Observer;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.time.DurationFormatUtils;

import com.google.firebase.database.DatabaseReference;

import ch.bfh.game2048.Main;
import ch.bfh.game2048.engine.GameEngine;
import ch.bfh.game2048.model.Direction;
import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.model.Highscore;
import ch.bfh.game2048.model.PushObject;
import ch.bfh.game2048.model.Tile;
import ch.bfh.game2048.persistence.Config;
import ch.bfh.game2048.persistence.OnlineScoreHandler;
import ch.bfh.game2048.persistence.ScoreHandler;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class MainUIController implements Observer {

	@FXML
	private BorderPane leftMainPane;

	@FXML
	private VBox rightMainPane;

	@FXML
	private GridPane gameBoard;

	@FXML
	private Button startButton;

	@FXML
	private Label labelScoreName;

	@FXML
	private Label labelScoreNumber;

	@FXML
	private Label labelTimerTime;

	@FXML
	private ComboBox boardSizeComboBox;

	@FXML
	private Label labelTimerName;

	@FXML
	private Button pauseResumeButton;

	@FXML
	private VBox vBoxLiveScore;

	@FXML
	private Button scoreListsButton;

	@FXML
	private Label labelLiveScore;

	@FXML
	private Label newScoreArrivedDate;
	
	@FXML
	private Label newScoreArrivedPoints;

    @FXML
    private Pane tickerPane;

	private SuperLabel[][] labelList;

	private LiveHighScoreView liveHighscorePane;

	GameEngine game;

	private ScoreHandler scoreHandler;
	private OnlineScoreHandler onlineScoreHandler;
	private Timer timer;
	private Highscore highscoreList;
	private String playerName;
	private Config conf;
	


	private int numberOfColumns = 4;

	private boolean isRunning = false;

	@FXML
	public void initialize() throws FileNotFoundException, JAXBException {

		conf = Config.getInstance();
		scoreHandler = new ScoreHandler();
		game = new GameEngine(numberOfColumns);

		highscoreList = scoreHandler.readScores(conf.getPropertyAsString("highscoreFileName"));

		// setSizeOfBoard(4);
		initializeBoard();

		initializeLiveScorePane();
		pauseResumeButton.setVisible(false);
		initializeBoardSizeComboBox();

		onlineScoreHandler = new OnlineScoreHandler();
		onlineScoreHandler.setHighscore(highscoreList);
		onlineScoreHandler.addObserver(this);
		liveHighscorePane.setOnlineScoreHandler(onlineScoreHandler);

	}

	public void writeScores() {

		try {
			scoreHandler.writeScores(highscoreList, conf.getPropertyAsString("highscoreFileName"));
		} catch (JAXBException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Setter and Getter Methods:

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getSizeOfBoard() {
		return numberOfColumns;
	}

	public void setSizeOfBoard(int sizeOfBoard) {
		this.numberOfColumns = sizeOfBoard;
	}

	public HighScorePane getHighScorePane() throws FileNotFoundException, JAXBException {
		return new HighScorePane(highscoreList, numberOfColumns);

	}

	// Event-Handlers

	@FXML
	void startGame(ActionEvent event) {

		installEventHandler(startButton.getScene());

		GameStatistics stats = new GameStatistics("", numberOfColumns);
		stats.setPlayerName("ACTIVE PLAYER");
		liveHighscorePane.setActiveStats(stats);

		if (timer != null) {
			timer.stop();
		}

		game = new GameEngine(numberOfColumns, stats);
		game.addObserver(this);
		
		liveHighscorePane.setGameEngine(game);

		timer = new Timer();
		timer.addObserver(this);
		fromIntToLabel(game.getBoard());
		labelScoreNumber.setText("0");
		startButton.setText(conf.getPropertyAsString("restart.button"));
		labelLiveScore.setText("");
		pauseResumeButton.setVisible(true);
		isRunning = true;
	}


	@FXML
	void pauseResume(ActionEvent event) {

		// If a game is currently ongoing or paused --> Switch between pause and
		// resume
		if (game.getStats() != null && game.isGameOver() == false) {
			if (isRunning) {
				timer.stop();
				game.getStats().pauseTime();
				pauseResumeButton.setText(conf.getPropertyAsString("resume.button"));
				isRunning = false;
			} else {
				timer.start();
				game.getStats().resumeTime();
				pauseResumeButton.setText(conf.getPropertyAsString("pause.button"));
				isRunning = true;
			}
		}
	}

	@FXML
	void showHighScore(ActionEvent event) {
		showHighscoreList(1);
	}

	private void installEventHandler(final Scene scene) {
		// handler for enter key press / release events, other keys are
		// handled by the parent (keyboard) node handler
		final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
			public void handle(final KeyEvent keyEvent) {

				if (isRunning) {
					System.out.println(keyEvent.getCode());
					boolean moved = false;
					if (keyEvent.getCode() == KeyCode.UP) {
						moved = game.move(Direction.UP);
					}
					if (keyEvent.getCode() == KeyCode.DOWN) {
						moved = game.move(Direction.DOWN);
					}
					if (keyEvent.getCode() == KeyCode.LEFT) {
						moved = game.move(Direction.LEFT);
					}
					if (keyEvent.getCode() == KeyCode.RIGHT) {
						moved = game.move(Direction.RIGHT);
					}

					if (moved) {
						fromIntToLabel(game.getBoard());
						labelScoreNumber.setText("" + game.getStats().getFormattedScore() + " Pts");
						// liveHighscorePane.refreshContent();
					}
				}
				keyEvent.consume();
			}
		};
		scene.setOnKeyPressed(keyEventHandler);
	}

	// Initialization of GUI elements:

	private void initializeLiveScorePane() {
		liveHighscorePane = new LiveHighScoreView(highscoreList, numberOfColumns);
		vBoxLiveScore.getChildren().add(liveHighscorePane);
	}

	@SuppressWarnings("unchecked")
	public void initializeBoardSizeComboBox() {

		// Set comboBox with list of board-sizes:

		boardSizeComboBox.getItems().setAll(BoardSizes.values());

		BoardSizes elementToBeSelected = BoardSizes.findStateByBoardSize(numberOfColumns);
		boardSizeComboBox.getSelectionModel().select(elementToBeSelected);

		boardSizeComboBox.setOnAction((event) -> {
			BoardSizes selectedEntry = (BoardSizes) boardSizeComboBox.getSelectionModel().getSelectedItem();
			switchBoardSize(selectedEntry.getBoardSize());
		});
	}

	private void initializeBoard() {

		gameBoard.getChildren().clear();

		int boardWidth = conf.getPropertyAsInt("boardWidth");
		int boardHeight = conf.getPropertyAsInt("boardHeight");

		gameBoard.setPrefSize(boardWidth, boardHeight);
		gameBoard.setMinSize(boardWidth, boardHeight);
		gameBoard.setMaxSize(boardWidth, boardHeight);

		labelList = new SuperLabel[numberOfColumns][numberOfColumns];
		double labelSize = (boardWidth * 1.0) / numberOfColumns;

		for (int i = 0; i < numberOfColumns; i++) {
			for (int j = 0; j < numberOfColumns; j++) {

				SuperLabel label = new SuperLabel(0, labelSize);
				label.setPrefSize(labelSize, labelSize);
				label.setAlignment(Pos.CENTER);
				GridPane.setConstraints(label, j, i);
				gameBoard.getChildren().add(label);

				labelList[i][j] = label;
			}
		}
	}

	// moves the stage to the center of the screen --> After change of scene
	public void centerStage() {
		Stage stage = Main.getStage();
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
		stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);

	}

	private void switchBoardSize(int newBoardSize) {

		if (timer != null) {
			timer.stop();
		}
		isRunning = false;
		setSizeOfBoard(newBoardSize);
		game = new GameEngine(numberOfColumns);
		initializeBoard();
		liveHighscorePane.getChildren().clear();
		initializeLiveScorePane();
		pauseResumeButton.setVisible(false);
		startButton.setText(conf.getPropertyAsString("start.button"));

	}

	private void showHighscoreList(int rankToHighlight) {
		try {
			Main.switchScene(Scenes.HIGHSCORE, rankToHighlight);
		} catch (FileNotFoundException | JAXBException e) {
			e.printStackTrace();
		}
	}

	private void fromIntToLabel(Tile[][] tileArray) {

		int i = 0;
		int j = 0;
		for (SuperLabel[] row : labelList) {
			for (SuperLabel label : row) {

				label.setTileNumber(tileArray[i][j].getValue());

				if (tileArray[i][j].isMerged()) {
					// fadeIn(label, 300, 0.5, 1.0, 3);
				} else if (tileArray[i][j].isSpawned()) {
					fadeIn(label, 400, 0.0, 1.0, 1);
				}
				j++;
			}
			j = 0;
			i++;
		}
	}

	// Fade-in effect when a tile is spawned
	private void fadeIn(Label label, int durationMillis, double from, double to, int nbOfcycles) {

		FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMillis), label);
		fadeTransition.setFromValue(from);
		fadeTransition.setToValue(to);
		fadeTransition.setCycleCount(nbOfcycles);
		fadeTransition.play();
	}

	public void update(Observable o, Object arg) {

		if (arg instanceof PushObject) {

			Thread thread = new Thread(() -> {
				try {
					PushObject newScore = (PushObject) arg;
					displayNewIncomingScore(newScore);
					Thread.sleep(15000);
				} catch (InterruptedException e) {
				}
				removeNewIncomingScore();
				System.out.println("Has ended");

			});
			thread.start();

		}

		else if (o instanceof Timer) {

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					long millis = ((Timer) o).getMillisElapsed();
					labelTimerTime.setText(DurationFormatUtils.formatDuration(millis, conf.getPropertyAsString("timerTimeFormat")));
				}
			});
		}

		else if (arg instanceof String) {

			Platform.runLater(new Runnable() {
				@Override
				public void run() {

					String pushObject = (String) arg;

					if (pushObject.equals("gameOver")) {
						processGameOver(game.getStats());
					}

					else if (pushObject.equals("gameWon")) {

						VictoryAlert dialog = new VictoryAlert(conf.getPropertyAsString("victoryTitle.alert"), conf.getPropertyAsString("victoryText.alert"));
						boolean continuation = dialog.show();
						if (continuation) {
							game.setGameContinue(true);
						} else {
							game.getStats().stopTime(true);
							processGameOver(game.getStats());
						}
					}
				}
			});
		}
	}

	private void displayNewIncomingScore(PushObject newScore) {

		Platform.runLater(new Runnable() {
			@Override
			public void run() {

				String playerName = newScore.getStatisticsObject().getPlayerName();
				int score = newScore.getStatisticsObject().getScore();
				String date = newScore.getStatisticsObject().getFormattedDate();
				newScoreArrivedDate.setText("Latest score committed:" + "\n");
				newScoreArrivedPoints.setText(playerName+" "+score+" Pts.");
				System.out.println(newScore.getStatisticsObject().getPlayerName() + " made a new record");

			}
		});

	}
	
	private void removeNewIncomingScore(){
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				newScoreArrivedDate.setText("");
				newScoreArrivedPoints.setText("");
			}
		});
	}
	
	

	private void processGameOver(GameStatistics stats) {

		timer.stop();
		isRunning = false;
		pauseResumeButton.setVisible(false);
		labelLiveScore.setText("Top Scores:");
		startButton.setText(conf.getPropertyAsString("restart.button"));
		GameOverDialog dialog = new GameOverDialog(conf.getPropertyAsString("gameOverDialog.title"), stats.getScore());
		if (dialog.showAndWait().isPresent()) {

			stats.setPlayerName(dialog.getPlayerName());
			highscoreList.addHighscore(stats);

			int rankOnHighscoreList = highscoreList.getRankOfListEntry(highscoreList.getFilteredHighscoreList(numberOfColumns), stats);
			showHighscoreList(rankOnHighscoreList);

			liveHighscorePane.removeActiveStats();
			liveHighscorePane.refreshContent();

			writeScores();

			DatabaseReference databaseRef = onlineScoreHandler.getScoreRef();
			databaseRef.push().setValue(stats);
		}
	}

}