package be.nabu.eai.developer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class MetricBar {
	private String title;
	private double max;
	private VBox verticalBar;
	private double maxHeight = 35;
	private double width = 10;
	private Label label;
	private HBox horizontalBar;
	
	public MetricBar(String title, double max) {
		this.title = title;
		this.max = max;
	}
	
	public Node getHorizontalNode() {
		HBox barContainer = new HBox();
		barContainer.setPrefHeight(width);
		barContainer.setMinHeight(width);
		barContainer.setMaxHeight(width);
		barContainer.setPrefWidth(maxHeight);
		barContainer.setMinWidth(maxHeight);
		barContainer.setMaxWidth(maxHeight);
		barContainer.setStyle("-fx-border-color: #666666; -fx-border-radius: 2 2 2 2");
		barContainer.setAlignment(Pos.CENTER_LEFT);
		horizontalBar = new HBox();
		horizontalBar.setPrefHeight(width);
		horizontalBar.setMinHeight(width);
		horizontalBar.setMaxHeight(width);
		update(0);
		barContainer.getChildren().add(horizontalBar);
		
		VBox total = new VBox();
		total.getChildren().add(barContainer);
		total.setAlignment(Pos.BOTTOM_CENTER);
		
		if (title != null) {
			label = new Label(this.title);
			label.setPadding(new Insets(3));
			label.setStyle("-fx-font-size: 0.7em");
			total.getChildren().add(label);
		}
		total.setPadding(new Insets(3));
		return total;
	}
	
	public Node getVerticalNode() {
		VBox barContainer = new VBox();
		barContainer.setPrefHeight(maxHeight);
		barContainer.setPrefWidth(width);
		barContainer.setMinHeight(maxHeight);
		barContainer.setMaxHeight(maxHeight);
		barContainer.setMinWidth(width);
		barContainer.setMaxWidth(width);
		barContainer.setStyle("-fx-border-color: #666666; -fx-border-radius: 2 2 2 2");
		barContainer.setAlignment(Pos.BOTTOM_CENTER);
		verticalBar = new VBox();
		verticalBar.setPrefWidth(width);
		verticalBar.setMinWidth(width);
		verticalBar.setMaxWidth(width);
		update(0);
		barContainer.getChildren().add(verticalBar);
		
		VBox total = new VBox();
		total.getChildren().add(barContainer);
		total.setAlignment(Pos.BOTTOM_CENTER);
		
		if (title != null) {
			label = new Label(this.title);
			label.setPadding(new Insets(3));
			label.setStyle("-fx-font-size: 0.7em");
			total.getChildren().add(label);
		}
		total.setPadding(new Insets(3));
		return total;
	}
	
	public void update(double value) {
		if (verticalBar != null) {
			verticalBar.setPrefHeight((value / max) * maxHeight);
			verticalBar.setMinHeight(verticalBar.getPrefHeight());
			verticalBar.setMaxHeight(verticalBar.getPrefHeight());
			if (value / max < 0.5) {
				verticalBar.setStyle("-fx-background-color: #449f32");
			}
			else if (value / max < 0.8) {
				verticalBar.setStyle("-fx-background-color: #e5e353");
			}
			else {
				verticalBar.setStyle("-fx-background-color: #9f3232");
			}
		}
		if (horizontalBar != null) {
			horizontalBar.setPrefWidth((value / max) * maxHeight);
			horizontalBar.setMinWidth(horizontalBar.getPrefWidth());
			horizontalBar.setMaxWidth(horizontalBar.getPrefWidth());
			if (value / max < 0.5) {
				horizontalBar.setStyle("-fx-background-color: #449f32");
			}
			else if (value / max < 0.8) {
				horizontalBar.setStyle("-fx-background-color: #e5e353");
			}
			else {
				horizontalBar.setStyle("-fx-background-color: #9f3232");
			}
		}
		if (label != null) {
			label.setTooltip(new Tooltip(Double.toString(value)));
		}
	}
}
