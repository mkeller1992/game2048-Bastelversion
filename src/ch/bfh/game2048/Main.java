package ch.bfh.game2048;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBException;

import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.view.HighScorePane;
import ch.bfh.game2048.view.MainUIController;
import ch.bfh.game2048.view.Scenes;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class Main extends Application {

	static Stage stage;
	static Scene mainScene;
	static MainUIController controller;

	static ReentrantLock lock = new ReentrantLock();
	
	static ArrayList<GameStatistics> finalScoreArray = new ArrayList<GameStatistics>();
	static boolean transferOngoing = false;

	public static Stage getStage() {
		return stage;
	}

	public static void switchScene(Scenes nextScene, int rankToHighlight) throws FileNotFoundException, JAXBException {

		switch (nextScene) {
		case MAINSCENE:
			stage.setScene(mainScene);
			return;
		case HIGHSCORE:

			HighScorePane highScorePane = controller.getHighScorePane();
			highScorePane.highlightRow(rankToHighlight - 1);

			Scene scene = new Scene(highScorePane, 770, 550);
			scene.getStylesheets().add(mainScene.getStylesheets().get(0));
			stage.setScene(scene);
			controller.centerStage();
			break;
		case SETTINGS:
			break;
		default:
			break;

		}
	}

	@Override
	public void start(Stage primaryStage) {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/MainUI.fxml"));
			HBox root = (HBox) loader.load();
			this.controller = (MainUIController) loader.getController();
			this.mainScene = new Scene(root, 760, 530);
			this.stage = primaryStage;
			stage.setTitle("2048 by M&M");
			stage.getIcons().add(new Image(getClass().getResourceAsStream("meteor.png")));
			mainScene.getStylesheets().add(getClass().getResource("view/application.css").toExternalForm());
			primaryStage.setScene(mainScene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	    stage.setOnCloseRequest(e -> {
	        controller.writeScores();
	        Platform.exit();
	        System.exit(0);
	    });
		
	}

	public static void main(String[] args) throws IOException {
		 launch(args);	 
	}
}
