package ch.bfh.game2048.model;

import java.text.NumberFormat;
import java.util.Locale;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class PushObject {

	GameStatistics statisticsObject;

	public PushObject(GameStatistics statisticsObject) {
		super();
		this.statisticsObject = statisticsObject;
	}

	public GameStatistics getStatisticsObject() {
		return statisticsObject;
	}

	public TextFlow toText() {

		Text name = new Text(statisticsObject.getPlayerName());
		Text reached = new Text(" reached ");
		String formattedScore = NumberFormat.getNumberInstance(Locale.getDefault()).format(statisticsObject.getScore());
		Text score = new Text("" + formattedScore + " Pts");

		Text boardSize = new Text(" on a " + statisticsObject.getBoardSize() + "x" + statisticsObject.getBoardSize() + " board");

		TextFlow result = new TextFlow();
		result.getChildren().addAll(name, reached, score, boardSize);

		name.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
		name.setFill(Color.WHITE);
		reached.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
		reached.setFill(Color.LIGHTGREY);
		score.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
		score.setFill(Color.WHITE);
		boardSize.setFont(Font.font("Verdana", FontWeight.NORMAL, 20));
		boardSize.setFill(Color.LIGHTGREY);

		return result;

	}

}
