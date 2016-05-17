package be.nabu.eai.developer.util;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;

public class EAIDeveloperUtils {
	
	public static Stage buildPopup(final MainController controller, PropertyUpdater updater, String title, final EventHandler<ActionEvent> eventHandler) {
		return buildPopup(controller, updater, title, eventHandler, false);
	}
	
	public static Stage buildPopup(final MainController controller, PropertyUpdater updater, String title, final EventHandler<ActionEvent> eventHandler, boolean refresh) {
		VBox vbox = new VBox();
		controller.showProperties(updater, vbox, refresh);
		HBox buttons = new HBox();
		final Button create = new Button("Ok");
		final Button cancel = new Button("Cancel");
		buttons.getChildren().addAll(create, cancel);
		buttons.setStyle("-fx-padding: 10px 0px 0px 0px");
		buttons.setAlignment(Pos.CENTER);
		vbox.getChildren().add(buttons);
		vbox.setStyle("-fx-padding: 10px");
		final Stage stage = buildPopup(title, vbox);
		create.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (eventHandler != null) {
					create.setDisable(true);
					cancel.setDisable(true);
					eventHandler.handle(arg0);
				}
				stage.hide();
			}
		});
		cancel.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				create.setDisable(true);
				cancel.setDisable(true);
				stage.hide();
			}
		});
		return stage;
	}
	
	public static Stage buildPopup(String title, Pane pane) {
		final Stage stage = new Stage();
		if (!System.getProperty("os.name").contains("nux")) {
			stage.initModality(Modality.WINDOW_MODAL);
		}
		stage.initOwner(MainController.getInstance().getStage());
		Scene scene = new Scene(pane);
		pane.minWidthProperty().set(400);
		pane.prefWidthProperty().bind(scene.widthProperty());
		stage.setScene(scene);
		stage.setTitle(title);
		stage.show();
		pane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					stage.hide();
				}
			}
		});
		return stage;
	}
	
}
