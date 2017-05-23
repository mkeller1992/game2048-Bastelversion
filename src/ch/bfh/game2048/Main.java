package ch.bfh.game2048;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import ch.bfh.game2048.model.GameStatistics;
import ch.bfh.game2048.view.MainUIController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

	static Stage stage;
	static Scene mainScene;
	static MainUIController controller;

	static ReentrantLock lock = new ReentrantLock();
	
	static ArrayList<GameStatistics> finalScoreArray = new ArrayList<GameStatistics>();

	public static Stage getStage() {
		return stage;
	}



	@Override
	public void start(Stage primaryStage) {

		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("view/MainUI.fxml"));
			VBox root = (VBox) loader.load();
			this.controller = (MainUIController) loader.getController();
			this.mainScene = new Scene(root, 760, 580);
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
