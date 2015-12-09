package be.nabu.eai.developer.util;

import be.nabu.eai.developer.MainController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Confirm {
	
	public enum ConfirmType {
		ERROR("dialog/dialog-error.png"),
		INFORMATION("dialog/dialog-information.png"),
		WARNING("dialog/dialog-warning.png"),
		QUESTION("dialog/dialog-question.png")
		;

		private String image;

		private ConfirmType(String image) {
			this.image = image;
		}
		
		public String getImage() {
			return image;
		}
	}
	
	public static void confirm(ConfirmType confirmType, String title, String question, EventHandler<ActionEvent> eventHandler) {
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		vbox.setMinWidth(450);
		vbox.setSpacing(20);
		HBox content = new HBox();
		content.setAlignment(Pos.CENTER);
		content.setSpacing(20);
		Label label = new Label(question);
		label.setWrapText(true);
		content.getChildren().addAll(MainController.loadGraphic(confirmType.getImage()), label);
		content.prefWidthProperty().bind(vbox.widthProperty());
		
		final Stage stage = new Stage();
		stage.initOwner(MainController.getInstance().getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setScene(new Scene(vbox));
		stage.setTitle(title);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				if (arg0.getCode() == KeyCode.ESCAPE) {
					stage.hide();
				}
			}
		});
		HBox buttons = new HBox();
		Button confirm = new Button("Ok");
		confirm.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (eventHandler != null) {
					eventHandler.handle(arg0);
				}
				stage.hide();
			}
		});
		buttons.getChildren().add(confirm);
		// only add a cancel if the confirmation has an action attached to it
		if (eventHandler != null) {
			Button cancel = new Button("Cancel");
			cancel.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					stage.hide();
				}
			});
			buttons.getChildren().add(cancel);
		}
		buttons.setAlignment(Pos.CENTER);
		buttons.prefWidthProperty().bind(vbox.widthProperty());
		
		vbox.getChildren().addAll(content, buttons);
		vbox.setPadding(new Insets(20));
		stage.show();
	}
}
