package ch.bfh.game2048.view;

import java.io.FileNotFoundException;
import java.text.NumberFormat;
import java.util.Locale;
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
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.scene.text.TextFlow;
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
	private ComboBox<BoardSizes> boardSizeComboBox;

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
	private Config conf;

	private Timeline timeline;
	private long timeOfLastArrivalMilis = 0;

	@FXML
	public void initialize() throws FileNotFoundException, JAXBException {

		conf = Config.getInstance();
		scoreHandler = new ScoreHandler();

		game = new GameEngine(4);
		game.addObserver(this);

		timer = new Timer();
		timer.addObserver(this);

		highscoreList = scoreHandler.readScores(conf.getPropertyAsString("highscoreFileName"));

		initializeBoard();

		initializeBoardSizeComboBox();

		onlineScoreHandler = new OnlineScoreHandler();
		onlineScoreHandler.setHighscore(highscoreList);
		onlineScoreHandler.addObserver(this);

		// Initialization / addition of live highscore-pane

		liveHighscorePane = new LiveHighScoreView(game, onlineScoreHandler, highscoreList);
		vBoxLiveScore.getChildren().add(liveHighscorePane);
		
		labelScoreName.setVisible(false);

	}

	// Switch from main-screen to highscore-screen and vice versa:

	public void switchScene(Scenes nextScene, int rankToHighlight) {

		switch (nextScene) {

		case MAINSCENE:
			Scene scene1 = startButton.getScene();
			Main.getStage().setScene(scene1);
			break;
		case HIGHSCORE:

			if (game.isGameHasBeenStarted()) {
				timer.stop();
				game.timePause();
				pauseResumeButton.setText(conf.getPropertyAsString("resume.button"));
			}

			HighScorePane highScorePane = new HighScorePane(highscoreList, this, game.getBoardSize());
			highScorePane.highlightRow(rankToHighlight - 1);
			Scene scene2 = new Scene(highScorePane, 770, 550);
			scene2.getStylesheets().add(startButton.getScene().getStylesheets().get(0));
			Main.getStage().setScene(scene2);
			break;
		default:
			break;
		}

		centerStage();
	}

	// If "Start"-button was pressed, initialize and start new game:

	@FXML
	void startGame(ActionEvent event) {

		installEventHandler(startButton.getScene());

		GameStatistics stats = new GameStatistics("YOUR SCORE", game.getBoardSize());
		game.setGameStats(stats);
		game.resetGame();
		game.startGame();

		liveHighscorePane.setActiveStats(stats);

		timer.reset();
		timer.start();

		mapTilesOnLabels(game.getBoard());

		startButton.setText(conf.getPropertyAsString("restart.button"));
		pauseResumeButton.setText("Pause");
		pauseResumeButton.setVisible(true);
		labelScoreName.setVisible(true);
		
		gameBoard.requestFocus();
	}

	// If a game is currently ongoing or paused --> Switch between pause and resume

	@FXML
	void pauseResume(ActionEvent event) {

		// If there is an unfinished game on the board:

		if (game.isGameHasBeenStarted()) {

			// After "Pause" was clicked:

			if (game.isActiveAndRunning()) {
				timer.stop();
				game.timePause();
				pauseResumeButton.setText(conf.getPropertyAsString("resume.button"));

				// After "Resume" was clicked:

			} else {
				timer.start();
				game.timeResume();
				pauseResumeButton.setText(conf.getPropertyAsString("pause.button"));
			}
		}
		gameBoard.requestFocus();
	}

	// Show the Highscore-Screen

	@FXML
	void showHighScore(ActionEvent event) {
		switchScene(Scenes.HIGHSCORE, 1);
		
		gameBoard.requestFocus();
	}

	// Navigation: Move tiles by keystrokes: up, down, left, right
	// If move was valid: Paint new constellation on game-board

	private void installEventHandler(final Scene scene) {

		final EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
			public void handle(final KeyEvent keyEvent) {

				if (game.isActiveAndRunning()) {
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
						mapTilesOnLabels(game.getBoard());
					}
				}
				keyEvent.consume();
			}
		};
		scene.setOnKeyPressed(keyEventHandler);
	}

	// Set comboBox with list of board-sizes:

	public void initializeBoardSizeComboBox() {

		boardSizeComboBox.getItems().setAll(BoardSizes.values());

		BoardSizes elementToBeSelected = BoardSizes.findStateByBoardSize(game.getBoardSize());
		boardSizeComboBox.getSelectionModel().select(elementToBeSelected);

		boardSizeComboBox.setOnAction((event) -> {
			BoardSizes selectedEntry = (BoardSizes) boardSizeComboBox.getSelectionModel().getSelectedItem();
			switchBoardSize(selectedEntry.getBoardSize());
			gameBoard.requestFocus();
		});
		boardSizeComboBox.setFocusTraversable(false);
	}

	// Assemble board with empty tiles:

	private void initializeBoard() {

		gameBoard.getChildren().clear();
		gameBoard.setStyle("-fx-background-color: #CCC0B4;");

		int boardWidth = conf.getPropertyAsInt("boardWidth");
		int boardHeight = conf.getPropertyAsInt("boardHeight");

		gameBoard.setPrefSize(boardWidth, boardHeight);
		gameBoard.setMinSize(boardWidth, boardHeight);
		gameBoard.setMaxSize(boardWidth, boardHeight);

		int numberOfColumns = game.getBoardSize();

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

	private void centerStage() {
		Stage stage = Main.getStage();
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
		stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
	}

	// Switch from one board size to another (triggered by Combobox)

	private void switchBoardSize(int boardSize) {

		timer.reset();

		labelScoreNumber.setText("");
		pauseResumeButton.setText(conf.getPropertyAsString("pause.button"));
		
		game.resetGame();
		game.setBoardSize(boardSize);
		initializeBoard();

		liveHighscorePane.removeActiveStats();
		liveHighscorePane.refreshContent();

		pauseResumeButton.setVisible(false);
		startButton.setText(conf.getPropertyAsString("start.button"));
		labelScoreName.setVisible(false);
	}

	// put text and style on labels based on current Tile-Array

	private void mapTilesOnLabels(Tile[][] tileArray) {

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

	public void writeScores() {
		try {
			scoreHandler.writeScores(highscoreList, conf.getPropertyAsString("highscoreFileName"));
		} catch (JAXBException | FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	// if information comes from Online-Score-Handler (incoming scores)

	public void update(Observable o, Object arg) {

		if (arg instanceof PushObject) {

			PushObject newScore = (PushObject) arg;

			new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Thread.sleep(30000);
					return null;
				}

				@Override
				protected void running() {
					if (highscoreList.getLastLocalScore() == null || (!highscoreList.getLastLocalScore().equals(newScore.getStatisticsObject()) && System.currentTimeMillis() - timeOfLastArrivalMilis > 41000)) {
						timeOfLastArrivalMilis = System.currentTimeMillis();
						displayTickerText(newScore.toText());
					}
					displayTopRightText(newScore.getStatisticsObject());
					labelLiveScore.setText("");
				}

				@Override
				protected void succeeded() {
					removeNewIncomingScore();
					labelLiveScore.setText("Live-Scores:");
				}
			}).start();
		}

		// if information comes from stopWatch (Timer)

		else if (o instanceof Timer) {

			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					long millis = ((Timer) o).getMillisElapsed();
					labelTimerTime.setText(DurationFormatUtils.formatDuration(millis, conf.getPropertyAsString("timerTimeFormat")));
				}
			});
		}

		// if information comes from game-engine

		else if (arg instanceof String) {

			Platform.runLater(new Runnable() {
				@Override
				public void run() {

					String infoFromEngine = (String) arg;

					if (infoFromEngine.equals("wasMerged")) {
						String formattedScore = NumberFormat.getNumberInstance(Locale.getDefault()).format(game.getStats().getScore());
						labelScoreNumber.setText("" + formattedScore + " Pts");
					}

					else if (infoFromEngine.equals("gameOver")) {
						processGameOver(game.getStats());
					}

					else if (infoFromEngine.equals("gameWon")) {
						VictoryAlert dialog = new VictoryAlert(conf.getPropertyAsString("victoryTitle.alert"), conf.getPropertyAsString("victoryText.alert"));
						boolean continuation = dialog.show();
						if (continuation) {
							game.setGameContinue(true);
						} else {
							game.setGameOver(true);
							processGameOver(game.getStats());
						}
					}
				}
			});
		}
	}

	/**
	 * Live-Ticker to display just achieved or incoming scores on main-screen
	 * 
	 * @param tickerText
	 *            contains the information about the latest score
	 */

	private void displayTickerText(TextFlow tickerText) {

		if (timeline != null) {
			timeline.stop();
			timeline.getKeyFrames().clear();
			tickerPane.getChildren().clear();
		}

		TextFlow tickerMessage = tickerText;

		tickerPane.getChildren().add(tickerMessage);

		double sceneWidth = Main.getStage().getScene().getWidth();
		double msgWidth = tickerMessage.prefWidth(-1);

		KeyValue initKeyValue = new KeyValue(tickerMessage.translateXProperty(), sceneWidth);
		KeyFrame initFrame = new KeyFrame(Duration.ZERO, initKeyValue);

		KeyValue endKeyValue = new KeyValue(tickerMessage.translateXProperty(), -1.0 * msgWidth);
		KeyFrame endFrame = new KeyFrame(Duration.seconds(10), endKeyValue);

		timeline = new Timeline(initFrame, endFrame);

		timeline.setCycleCount(4);

		// timeline.setCycleCount(Timeline.INDEFINITE);

		timeline.play();
	}

	/**
	 * Display local or incoming scores on the top-right of the screen
	 * 
	 * @param newScore
	 *            local or incoming object containing game-statistics
	 */

	private void displayTopRightText(GameStatistics newScore) {

		String formattedScore = NumberFormat.getNumberInstance(Locale.getDefault()).format(newScore.getScore());
		newScoreArrivedDate.setText("Latest score committed:" + "\n");
		newScoreArrivedPoints.setText(newScore.getPlayerName() + " " + formattedScore + " Pts.");
	}

	// Make the score displayed on the top-right disappear:

	private void removeNewIncomingScore() {
		newScoreArrivedDate.setText("");
		newScoreArrivedPoints.setText("");
	}

	// In case of game-over: Stop time, add game-statistics, write them to internal and external DB

	private void processGameOver(GameStatistics stats) {

		timer.stop();
		pauseResumeButton.setVisible(false);
		startButton.setText(conf.getPropertyAsString("restart.button"));

		GameOverDialog dialog = new GameOverDialog(conf.getPropertyAsString("gameOverDialog.title"), stats.getScore());
		if (dialog.showAndWait().isPresent()) {

			stats.setPlayerName(dialog.getPlayerName());
			stats.setTimeOfAddingToScoreList(System.currentTimeMillis());
			highscoreList.addHighscore(stats);

			// To be able to recognize if a new score was local or incoming
			highscoreList.setLastLocalScore(stats);

			int rankOnHighscoreList = highscoreList.getRankOfListEntry(highscoreList.getFilteredHighscoreList(game.getBoardSize()), stats);
			switchScene(Scenes.HIGHSCORE, rankOnHighscoreList);

			liveHighscorePane.removeActiveStats();
			liveHighscorePane.refreshContent();

			writeScores();

			DatabaseReference databaseRef = onlineScoreHandler.getScoreRef();
			databaseRef.push().setValue(stats);
		}
	}
}