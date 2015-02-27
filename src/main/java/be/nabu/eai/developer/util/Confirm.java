package be.nabu.eai.developer.util;

import be.nabu.eai.developer.MainController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Confirm {
	public static void confirm(MainController controller, String question, EventHandler<ActionEvent> eventHandler) {
		GridPane grid = new GridPane();
		grid.setVgap(5);
		grid.setHgap(10);
		int row = 0;
		final Stage stage = new Stage();
		stage.initOwner(controller.getStage().getOwner());
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setScene(new Scene(grid));
		stage.setTitle("Confirm");

		Label name = new Label(question);
		grid.add(name, 0, row++);
		HBox buttons = new HBox();
		grid.add(buttons, 0, row++);

		Button confirm = new Button("Confirm");
		confirm.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				eventHandler.handle(arg0);
				stage.hide();
			}
		});
		Button cancel = new Button("Cancel");
		cancel.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				stage.hide();
			}
		});
		buttons.getChildren().addAll(confirm, cancel);
		stage.show();
	}
}
