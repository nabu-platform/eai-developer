package be.nabu.eai.developer.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;

public class CustomTooltip {
	
	private String text;
	private Stage stage;
	private Double maxWidth;
	private boolean useNativeTooltips = true;
	private Tooltip tooltip;
	private List<String> classes = new ArrayList<String>();
	
	public CustomTooltip(String text) {
		this.text = text;
	}
	
	private void trySetDelay(Tooltip tooltip) {
		for (Method method : tooltip.getClass().getMethods()) {
			if (method.getName().equals("setShowDelay")) {
				try {
					method.invoke(tooltip, Duration.millis(100));
				}
				catch (Exception e) {
					// ignore
				}
			}
		}
	}
	
	private void trySetDuration(Tooltip tooltip, int seconds) {
		for (Method method : tooltip.getClass().getMethods()) {
			if (method.getName().equals("setShowDuration")) {
				try {
					method.invoke(tooltip, Duration.seconds(seconds));
				}
				catch (Exception e) {
					// ignore
				}
			}
		}
	}
	
	public void setText(String text) {
		if (tooltip != null) {
			tooltip.setText(text);
		}
		this.text = text;
	}
	
	public void install(Node node) {
		if (useNativeTooltips) {
			tooltip = new Tooltip(text);
			tooltip.getStyleClass().addAll(classes);
			tooltip.setMaxWidth(maxWidth == null ? 400 : maxWidth);
			tooltip.setWrapText(true);
			trySetDelay(tooltip);
			// there is no reason to remove it fast, you need to have enough time to read everything
			trySetDuration(tooltip, 5*60);
			// only works 9+
//			tooltip.setShowDelay(Duration.millis(100));
			Tooltip.install(node, tooltip);
		}
		else {
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
	}
	
	public void show(Window parentWindow, double x, double y) {
		stage = new Stage();
		stage.initOwner(parentWindow);
		AnchorPane pane = new AnchorPane();
		pane.getStyleClass().add("custom-tooltip");
		pane.getStyleClass().addAll(classes);
		Scene scene = new Scene(pane);
		Label label = new Label(text);
		pane.getChildren().add(label);
		label.getStyleClass().add("custom-tooltip-label");
		label.setStyle("-fx-background-color: #333333; -fx-text-fill: white");
		label.setPadding(new Insets(10));
		label.setWrapText(true);
		if (maxWidth != null) {
			label.setMaxWidth(maxWidth);
		}
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

	public Double getMaxWidth() {
		return maxWidth;
	}

	public void setMaxWidth(Double maxWidth) {
		this.maxWidth = maxWidth;
	}

	public List<String> getStyleClass() {
		return classes;
	}

}
