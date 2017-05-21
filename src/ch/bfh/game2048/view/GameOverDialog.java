package ch.bfh.game2048.view;

import java.text.MessageFormat;

import ch.bfh.game2048.persistence.Config;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class GameOverDialog extends Dialog<String> implements InvalidationListener {

	TextField nameField;
	Node okButton;
	Config conf;

	public GameOverDialog(String title, int finalScore) {
		
		conf = Config.getInstance();
		this.setTitle(title);
		this.setHeaderText(MessageFormat.format(conf.getPropertyAsString("gameOverText1.dialog"), new Object[] {finalScore}));
		
		Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
		stage.getIcons().add(new Image(getClass().getResourceAsStream("../meteor.png")));
		
		Image image = new Image(getClass().getResource("images/LosingSmiley.png").toExternalForm());
		ImageView imageView = new ImageView(image);
		this.setGraphic(imageView);

		// Set the button types.
		ButtonType loginButtonType = new ButtonType(conf.getPropertyAsString("ok.button"), ButtonData.OK_DONE);
		this.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		// Create the pane, the labels and the fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 50, 10, 10));

		nameField = new TextField();
		nameField.setPromptText(conf.getPropertyAsString("promptTextName.dialog"));

		grid.add(new Label(conf.getPropertyAsString("gameOverText2.dialog")), 0, 0);
		grid.add(nameField, 1, 0);

		// Enable/Disable OK button depending on whether a Channel Name / URL
		// was entered.
		okButton = this.getDialogPane().lookupButton(loginButtonType);
		okButton.setDisable(true);

		// Do some validation
		nameField.textProperty().addListener(this);
		
		// Save user-input as String values
		// Convert the result to a channel-name/url-pair when the OK button is
		// clicked.
		this.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				return nameField.getText();
			}
			return null;
		});

		this.getDialogPane().setContent(grid);
	}

	public String getPlayerName() {
		return nameField.getText();
	}

	@Override
	public void invalidated(Observable arg0) {

		if (!nameField.getText().equals("")) {

			okButton.setDisable(false);
		} else {
			okButton.setDisable(true);
		}
	}
}