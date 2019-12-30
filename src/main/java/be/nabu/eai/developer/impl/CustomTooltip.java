package be.nabu.eai.developer.impl;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class CustomTooltip {
	
	private String text;
	private Stage stage;
	
	public CustomTooltip(String text) {
		this.text = text;
	}
	
	public void install(Node node) {
		node.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				show(node.getScene().getWindow(), event.getScreenX(), event.getScreenY());
			}
		});
		node.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				hide();
			}
		});
	}
	
	public void show(Window parentWindow, double x, double y) {
		stage = new Stage();
		stage.initOwner(parentWindow);
		AnchorPane pane = new AnchorPane();
		Scene scene = new Scene(pane);
		Label label = new Label(text);
		pane.getChildren().add(label);
		label.setStyle("-fx-background-color: #333333; -fx-text-fill: white");
		label.setPadding(new Insets(10));
		stage.setScene(scene);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setX(x);
		stage.setY(y);
		stage.show();
	}
	
	public void hide() {
		if (stage != null) {
			stage.hide();
			stage = null;
		}
	}
}
