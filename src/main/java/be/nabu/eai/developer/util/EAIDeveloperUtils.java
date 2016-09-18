package be.nabu.eai.developer.util;

import java.util.Arrays;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
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
	
	// use the offset to regulate where the arrow should be on the line, offset of 0 means at the very end
	public static List<Shape> drawArrow(Line line, double offset) {
		Line line1 = new Line();
		Line line2 = new Line();

		calculateArrows(line, line1, line2, offset);

		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				calculateArrows(line, line1, line2, offset);
			}
		};
		line.startXProperty().addListener(changeListener);
		line.startYProperty().addListener(changeListener);
		line.endXProperty().addListener(changeListener);
		line.endYProperty().addListener(changeListener);
        // Shape.union creates a new shape where the separate lines don't respond to x, y changes anymore
        return Arrays.asList(line1, line2);
	}

	private static void calculateArrows(Line line, Line line1, Line line2, double offset) {
		// based on: http://www.guigarage.com/2014/11/hand-drawing-effect-javafx/
		double x1 = line.startXProperty().get();
		double x2 = line.endXProperty().get();
		double y1 = line.startYProperty().get();
		double y2 = line.endYProperty().get();
		
		double yDelta = (y2 - y1) * offset;
		y2 -= yDelta;
		
		double xDelta = (x2 - x1) * offset;
		x2 -= xDelta;
		
		double arrowlength = line.strokeWidthProperty().get() * 5;
        double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        double unrotatedX = x2 + ((x1 - x2) / distance) * arrowlength;
        double unrotatedY = y2 + ((y1 - y2) / distance) * arrowlength;
        
        Point2D rotated1 = new Point2D(x2 + (unrotatedX - x2) * Math.cos(0.5) - (unrotatedY - y2) * Math.sin(0.5), y2 + (unrotatedX - x2) * Math.sin(0.5) + (unrotatedY - y2) * Math.cos(0.5));
        Point2D rotated2 = new Point2D(x2 + (unrotatedX - x2) * Math.cos(-0.5) - (unrotatedY - y2) * Math.sin(-0.5), y2 + (unrotatedX - x2) * Math.sin(-0.5) + (unrotatedY - y2) * Math.cos(-0.5));
        
        line1.startXProperty().set(rotated1.getX());
		line1.startYProperty().set(rotated1.getY());
		line1.endXProperty().set(x2);
		line1.endYProperty().set(y2);
		
		line2.startXProperty().set(rotated2.getX());
		line2.startYProperty().set(rotated2.getY());
		line2.endXProperty().set(x2);
		line2.endYProperty().set(y2);
	}
}
