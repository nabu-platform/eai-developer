/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
		// if we don't account for the additional pixels, the bar will overlap the border
		barContainer.setPadding(new Insets(1, 0, 1, 0));
		horizontalBar.setPrefHeight(width - 2);
		horizontalBar.setMinHeight(width - 2);
		horizontalBar.setMaxHeight(width - 2);
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
		barContainer.setPadding(new Insets(0, 1, 0, 1));
		verticalBar = new VBox();
		verticalBar.setPrefWidth(width - 2);
		verticalBar.setMinWidth(width - 2);
		verticalBar.setMaxWidth(width - 2);
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
			verticalBar.setPrefHeight((value / max) * (maxHeight - 2));
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
			horizontalBar.setPrefWidth((value / max) * (maxHeight - 2));
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
