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

package be.nabu.eai.developer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.function.Predicate;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Shape;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;
import be.nabu.eai.developer.Main;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.managers.base.BaseConfigurationGUIManager;
import be.nabu.eai.developer.managers.base.BasePropertyOnlyGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ExtensibleEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.line.CubicCurve;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class EAIDeveloperUtils {
	
	public static interface PropertiesHandler {
		public void handle(SimplePropertyUpdater updater);
	}
	
	public static void setPrompt(Node parent, String query, String prompt) {
		Node lookup = parent.lookup(query);
		if (lookup instanceof TextInputControl) {
			((TextInputControl) lookup).setPromptText(prompt);
		}
		else if (lookup instanceof ComboBox) {
			((ComboBox<?>) lookup).setPromptText(prompt);
		}
	}
	
	public static void runIn(Runnable runnable, int milliseconds) {
		Timeline waiter = new Timeline(
			new KeyFrame(Duration.millis(milliseconds), new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					runnable.run();
				}
			})
		);
		waiter.play();
	}

	public static RepositoryEntry mkdir(RepositoryEntry entry, String name) {
		try {
			Entry child = entry.getChild(name);
			if (child == null) {
				child = entry.createDirectory(name);
				created(child.getId());
			}
			return (RepositoryEntry) child;
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void created(String id) {
		reloadParent(id);
		// this does not work properly if you are not using the REST connection
		// the server is reloaded asynchronously, it may not see the resources by the time the collaborators get the message
		try {
			MainController.getInstance().getAsynchronousRemoteServer().reload(id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		MainController.getInstance().getCollaborationClient().created(id, "Created");
	}
	
	public static void updated(String id) {
		reload(id);
		try {
			MainController.getInstance().getAsynchronousRemoteServer().reload(id);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		MainController.getInstance().getCollaborationClient().updated(id, "Updated");
		// refresh any tabs or whatever you have open
		MainController.getInstance().refresh(id);
	}
	
	public static void delete(String id) {
		Entry entry = MainController.getInstance().getRepository().getEntry(id);
		if (entry != null) {
			// first we unload in remote server
			// unloading (especially when not specifically resetting the file system because of optimized localhost access) can corrupt the target server
			try {
				MainController.getInstance().getAsynchronousRemoteServer().unload(id);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			try {
				// then we delete
				((ExtensibleEntry) entry.getParent()).deleteChild(entry.getName(), true);
			}
			catch (Exception e) {
				MainController.getInstance().notify(e);
			}
			// then we send out the events
			deleted(id, true);
		}
	}
	
	public static void deleted(String id) {
		deleted(id, false);
	}
	
	private static void deleted(String id, boolean unloaded) {
		reloadParent(id);
		if (!unloaded) {
			try {
				MainController.getInstance().getAsynchronousRemoteServer().unload(id);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		MainController.getInstance().getCollaborationClient().deleted(id, "Deleted");
		// close any tabs you might have open
		MainController.getInstance().close(id);
	}
	
	public static void reloadParent(String id) {
		reloadParent(id, false);
	}
	
	public static void reloadParent(String id, boolean force) {
		int lastIndexOf = id.lastIndexOf('.');
		if (lastIndexOf < 0) {
			MainController.getInstance().getTree().refresh();
			MainController.getInstance().getRepositoryBrowser().refresh();
		}
		else {
			reload(id.substring(0, lastIndexOf), force);
		}
	}
	
	public static void reload(String id) {
		reload(id, false);
	}
	
	public static void reload(String id, boolean force) {
		// a last ditch effort 
		if (MainController.getInstance().isLocalServer()) {
			TreeCell<Entry> locate = MainController.getInstance().locate(id, false);
			// if it's a folder, make sure we expand it to initiate loading
			if (locate != null && !locate.getItem().leafProperty().get()) {
				locate.expandedProperty().set(true);
			}
		}
		TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(id.replace('.', '/'), false);
		if (resolve == null && force) {
			reloadParent(id, force);
			resolve = MainController.getInstance().getTree().resolve(id.replace('.', '/'), false);
		}
		if (resolve != null) {
			// in local mode we must reload it explicitly to trigger the start/stop
			if (MainController.getInstance().isLocalServer()) {
				MainController.getInstance().getRepository().reload(id);
				// make sure we see the changes in the repository tree!
				resolve.itemProperty().get().refresh(false);
			}
			resolve.refresh();
			TreeCell<Entry> treeCell = MainController.getInstance().getRepositoryBrowser().getControl().getTreeCell(resolve);
			treeCell.refresh();
		}
	}
	
	public static void refresh(String id) {
		TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(id.replace('.', '/'), false);
		if (resolve != null) {
			resolve.refresh();
			TreeCell<Entry> treeCell = MainController.getInstance().getRepositoryBrowser().getControl().getTreeCell(resolve);
			treeCell.refresh();
		}
	}
	
	public static Stage buildPopup(final MainController controller, String title, Collection<Property<?>> properties, PropertiesHandler handler, boolean refresh, Stage owner, Value<?>...values) {
		final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(properties), values);
		return buildPopup(controller, updater, title, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				handler.handle(updater);
			}
		}, refresh, owner, true);
	}
	
	public static Stage buildPopup(final MainController controller, PropertyUpdater updater, String title, final EventHandler<ActionEvent> eventHandler) {
		return buildPopup(controller, updater, title, eventHandler, false);
	}
	
	public static Stage buildPopup(final MainController controller, PropertyUpdater updater, String title, final EventHandler<ActionEvent> eventHandler, boolean refresh) {
		return buildPopup(controller, updater, title, eventHandler, refresh, controller.getStage(), true);
	}
	
	public static Stage buildPopup(final MainController controller, PropertyUpdater updater, String title, final EventHandler<ActionEvent> eventHandler, boolean refresh, Stage owner, boolean show) {
		Function<Exception, Void> function = new Function<Exception, Void>() {
			@Override
			public Void apply(Exception e) {
				Confirm.confirm(ConfirmType.ERROR, "Action failed", "An error occurred: " + e.getMessage(), null);
				return null;
			}
		};
		return buildPopup(controller, updater, title, eventHandler, function, refresh, owner, show);
	}
	
	public static Stage buildPopup(final MainController controller, PropertyUpdater updater, String title, final EventHandler<ActionEvent> eventHandler, Function<Exception, Void> exceptionHandler, boolean refresh, Stage owner, boolean show) {
		VBox vbox = new VBox();
		VBox properties = new VBox();
		controller.showProperties(updater, properties, refresh, controller.getRepository(), controller.isInContainer(vbox), false);
		vbox.getChildren().add(properties);
		HBox buttons = new HBox();
		final Button create = new Button("Ok");
		create.getStyleClass().add("primary");
		final Button cancel = new Button("Cancel");
		buttons.getChildren().addAll(create, cancel);
		buttons.setStyle("-fx-padding: 10px 0px 0px 0px");
		buttons.setAlignment(Pos.CENTER);
		vbox.getChildren().add(buttons);
		vbox.setStyle("-fx-padding: 10px");
		final Stage stage = buildPopup(title, vbox, owner, null, show);
		create.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
					if (eventHandler != null) {
						create.setDisable(true);
						cancel.setDisable(true);
						eventHandler.handle(arg0);
					}
					stage.hide();
				}
				catch (Exception e) {
					exceptionHandler.apply(e);
					stage.hide();
				}
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
		return buildPopup(title, pane, true);
	}
	
	public static HBox newHBox(String title, Node node) {
		HBox hbox = new HBox();
		hbox.setAlignment(Pos.CENTER_LEFT);
		hbox.setPadding(new Insets(10));
		Label label = new Label(title + ":");
		label.setPrefWidth(150);
		label.setWrapText(true);
		label.setAlignment(Pos.CENTER_RIGHT);
		label.setPadding(new Insets(4, 10, 0, 5));
		HBox.setHgrow(label, Priority.SOMETIMES);
		hbox.getChildren().addAll(label, node);
		HBox.setHgrow(node, Priority.ALWAYS);
		return hbox;
	}
	
	public static void setDefaultPadding(Label label) {
		label.setPadding(new Insets(4, 10, 0, 5));
	}
	
	public static GridPane newGridPane(HBox...boxes) {
		GridPane grid = new GridPane();
		int row = 0;
		for (HBox box : boxes) {
			int column = 0;
			for (Node child : new ArrayList<Node>(box.getChildren())) {
				grid.add(child, column++, row++);
			}
		}
		return grid;
	}
	
	public static Button newCloseButton(String title, Stage stage) {
		Button button = new Button(title);
		button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
		return button;
	}
	
	public static void focusLater(Stage stage) {
		if (stage != null) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					stage.requestFocus();
				}
			});
		}
	}
	
	public static void alignHBoxes(Node...nodes) {
		double maxWidth = 0;
		for (Node box : nodes) {
			if (box instanceof HBox) {
				for (Node child : ((HBox) box).getChildren()) {
					if (child instanceof Label) {
						if (((Label) child).getWidth() > maxWidth) {
							maxWidth = ((Label) child).getWidth();
						}
					}
				}
			}
		}
		if (maxWidth > 0) {
			for (Node box : nodes) {
				if (box instanceof HBox) {
					for (Node child : ((HBox) box).getChildren()) {
						if (child instanceof Label) {
							((Label) child).setPrefWidth(maxWidth);
							((Label) child).setMinWidth(maxWidth);
							((Label) child).setAlignment(Pos.CENTER_RIGHT);
						}
					}
				}
			}
		}
	}
	
	public static HBox newHBox(Button...buttons) {
		HBox actions = new HBox();
		actions.setPadding(new Insets(10));
		actions.setAlignment(Pos.CENTER_RIGHT);
		actions.getChildren().addAll(buttons);
		return actions;
	}
	
	public static Stage buildPopup(String title, Pane pane, boolean modal) {
		return buildPopup(title, pane, modal ? MainController.getInstance().getStage() : null, null, true);
	}
	
	public static Stage buildPopup(String windowTitle, String contentTitle, Object parameters, EventHandler<ActionEvent> ok, boolean refresh) {
		VBox box = new VBox();
		box.getStyleClass().add("popup-form");
		if (contentTitle != null) {
			Label label = new Label(contentTitle);
			label.getStyleClass().add("h1");
			box.getChildren().add(label);
		}
		SimplePropertyUpdater updater = createUpdater(parameters, null);
		return buildPopup(MainController.getInstance(), updater, windowTitle, ok, refresh, MainController.getInstance().getActiveStage(), false);
	}
	
	public static Stage buildPopup(String title, Pane pane, Stage owner, StageStyle style, boolean show) {
		final Stage stage = new Stage();
		if (owner != null && !System.getProperty("os.name").contains("nux")) {
			stage.initModality(Modality.WINDOW_MODAL);
		}
		if (owner != null) {
			stage.initOwner(owner);
		}
		if (style != null) {
			stage.initStyle(style);
		}
		Scene scene = new Scene(pane);
		scene.getStylesheets().addAll(MainController.getInstance().getStage().getScene().getStylesheets());
//		pane.minWidthProperty().set(Math.max(400, pane.getPrefWidth()));
//		pane.prefWidthProperty().bind(scene.widthProperty());
		stage.setScene(scene);
		stage.setTitle(title);
		if (show) {
			stage.show();
		}
		// workaround: don't allow decorated windows to be closed so easily
		if (style == null || !style.equals(StageStyle.DECORATED)) {
			pane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ESCAPE) {
						stage.hide();
					}
				}
			});
		}
		return stage;
	}
	
	public static double getDistance(Point2D from, Point2D to) {
		double x = (int) Math.abs(from.getX() - to.getX());
		double y = (int) Math.abs(from.getY() - to.getY());
		return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
	}
	
	/**
	 * Evaluate the cubic curve at a parameter 0<=t<=1, returns a Point2D
	 */
	public static Point2D getPositionOnCurve(CubicCurve c, float t) {
		float r = 1 - t;
		Point2D p = new Point2D(
			Math.pow(r, 3) * c.getStartX()
				+ 3 * t * Math.pow(r, 2) * c.getControlX1()
				+ 3 * r * t * t * c.getControlX2()
				+ Math.pow(t, 3) * c.getEndX(),
			Math.pow(r, 3) * c.getStartY() 
				+ 3 * t * Math.pow(r, 2) * c.getControlY1()
				+ 3 * r * t * t * c.getControlY2()
				+ Math.pow(t, 3) * c.getEndY());
	    return p;
	}
	
	/**
	 * Evaluate the tangent of the cubic curve at a parameter 0<=t<=1, returns a Point2D
	 */
	private static Point2D evalDt(CubicCurve c, float t) {
		float r = 1 - t;
		Point2D p = new Point2D(
				-3 * Math.pow(r, 2) * c.getStartX() 
					+ 3 * (Math.pow(r, 2) - 2 * t * r) * c.getControlX1()
					+ 3 * (r * 2 * t - t * t) * c.getControlX2() 
					+ 3 * Math.pow(t, 2) * c.getEndX(),
				-3 * Math.pow(r, 2) * c.getStartY() 
					+ 3 * (Math.pow(r, 2) - 2 * t * r) * c.getControlY1()
					+ 3 * (r * 2 * t - t * t) * c.getControlY2() 
					+ 3 * Math.pow(t, 2) * c.getEndY());
		return p;
	}

	public static void drawNode(CubicCurve curve, Node node, float offset, int x, int y) {
		// TODO: draw a node at the offset with an additional x, y offset!
		// meant to position the label in the workflow
		// give the label a background and stuff so it doesn't matter if it has a line going through it!
	}
	
	/**
	 * Based on: https://stackoverflow.com/questions/26702519/javafx-line-curve-with-arrow-head
	 * Which is based on: https://stackoverflow.com/questions/19605179/drawing-tangent-lines-for-each-point-in-bezier-curve/19608919#19608919
	 */
	public static List<Shape> drawArrow(CubicCurve curve, float offset, boolean toEnd) {
		Path arrowEnd = new Path();
		calculateArrow(curve, offset, toEnd, arrowEnd);
		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				calculateArrow(curve, offset, toEnd, arrowEnd);		
			}
		};
		curve.startXProperty().addListener(changeListener);
		curve.controlX1Property().addListener(changeListener);
		curve.controlX2Property().addListener(changeListener);
		curve.endXProperty().addListener(changeListener);
		curve.startYProperty().addListener(changeListener);
		curve.controlY1Property().addListener(changeListener);
		curve.controlY2Property().addListener(changeListener);
		curve.endYProperty().addListener(changeListener);
		return Arrays.asList(arrowEnd);
	}
	
	private static void calculateArrow(CubicCurve curve, float offset, boolean toEnd, Path path) {
		Point2D ori = getPositionOnCurve(curve, offset);
		Point2D tan = evalDt(curve, offset).normalize().multiply(20);
		path.getElements().clear();
		if (toEnd) {
			path.getElements().add(new MoveTo(ori.getX() - 0.2 * tan.getX() - 0.2 * tan.getY(),
					ori.getY() - 0.2 * tan.getY() + 0.2 * tan.getX()));
			path.getElements().add(new LineTo(ori.getX(), ori.getY()));
			path.getElements().add(new LineTo(ori.getX() - 0.2 * tan.getX() + 0.2 * tan.getY(),
					ori.getY() - 0.2 * tan.getY() - 0.2 * tan.getX()));
		}
		else {
			path.getElements().add(new MoveTo(ori.getX() + 0.2 * tan.getX() - 0.2 * tan.getY(),
					ori.getY() + 0.2 * tan.getY() + 0.2 * tan.getX()));
			path.getElements().add(new LineTo(ori.getX(), ori.getY()));
			path.getElements().add(new LineTo(ori.getX() + 0.2 * tan.getX() + 0.2 * tan.getY(),
					ori.getY() + 0.2 * tan.getY() - 0.2 * tan.getX()));
		}
	}
	
	public static List<Shape> drawArrow(CubicCurve curve) {
		double size = Math.max(curve.getBoundsInLocal().getWidth(), curve.getBoundsInLocal().getHeight());
		double scale = size / 4d;
		
		scale = 20;

		// points towards source at 30% offset
		Point2D ori = getPositionOnCurve(curve, 0.3f);
		Point2D tan = evalDt(curve, 0.3f).normalize().multiply(scale);
		Path arrowIni = new Path();
		arrowIni.getElements().add(new MoveTo(ori.getX() + 0.2 * tan.getX() - 0.2 * tan.getY(),
				ori.getY() + 0.2 * tan.getY() + 0.2 * tan.getX()));
		arrowIni.getElements().add(new LineTo(ori.getX(), ori.getY()));
		arrowIni.getElements().add(new LineTo(ori.getX() + 0.2 * tan.getX() + 0.2 * tan.getY(),
				ori.getY() + 0.2 * tan.getY() - 0.2 * tan.getX()));

		// points towards target at 70% offset
		ori = getPositionOnCurve(curve, 1);
		tan = evalDt(curve, 1).normalize().multiply(scale);
		
		Path arrowEnd = new Path();
		arrowEnd.getElements().add(new MoveTo(ori.getX() - 0.2 * tan.getX() - 0.2 * tan.getY(),
				ori.getY() - 0.2 * tan.getY() + 0.2 * tan.getX()));
		arrowEnd.getElements().add(new LineTo(ori.getX(), ori.getY()));
		arrowEnd.getElements().add(new LineTo(ori.getX() - 0.2 * tan.getX() + 0.2 * tan.getY(),
				ori.getY() - 0.2 * tan.getY() - 0.2 * tan.getX()));
		
		ori = getPositionOnCurve(curve, 0.5f);
		tan = evalDt(curve, 0.5f).normalize().multiply(scale);
		
		Path arrowEnd2 = new Path();
		arrowEnd2.getElements().add(new MoveTo(ori.getX() - 0.2 * tan.getX() - 0.2 * tan.getY(),
				ori.getY() - 0.2 * tan.getY() + 0.2 * tan.getX()));
		arrowEnd2.getElements().add(new LineTo(ori.getX(), ori.getY()));
		arrowEnd2.getElements().add(new LineTo(ori.getX() - 0.2 * tan.getX() + 0.2 * tan.getY(),
				ori.getY() - 0.2 * tan.getY() - 0.2 * tan.getX()));

		Line line = new Line();
		line.setStartX(ori.getX() - 0.2 * tan.getX() - 0.2 * tan.getY());
		line.setStartY(ori.getY() - 0.2 * tan.getY() + 0.2 * tan.getX());
		line.setEndX(ori.getX() - 0.2 * tan.getX() + 0.2 * tan.getY());
		line.setEndY(ori.getY() - 0.2 * tan.getY() - 0.2 * tan.getX());
		
		List<Shape> shapes = new ArrayList<Shape>();
		shapes.add(arrowEnd);
		shapes.add(arrowEnd2);
		shapes.add(line);
		return shapes;
	}
	
	public static List<Shape> drawArrow(Line line, double offset) {
		return drawArrow(line, offset, null);
	}
	
	// use the offset to regulate where the arrow should be on the line, offset of 0 means at the very end
	public static List<Shape> drawArrow(Line line, double offset, Double chosenArrowLength) {
		Line line1 = new Line();
		Line line2 = new Line();

		calculateArrows(line, line1, line2, offset, chosenArrowLength);

		ChangeListener<Number> changeListener = new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				calculateArrows(line, line1, line2, offset, chosenArrowLength);
			}
		};
		line.startXProperty().addListener(changeListener);
		line.startYProperty().addListener(changeListener);
		line.endXProperty().addListener(changeListener);
		line.endYProperty().addListener(changeListener);
        // Shape.union creates a new shape where the separate lines don't respond to x, y changes anymore
        return Arrays.asList(line1, line2);
	}

	private static void calculateArrows(Line line, Line line1, Line line2, double offset, Double chosenArrowLength) {
		// based on: http://www.guigarage.com/2014/11/hand-drawing-effect-javafx/
		double x1 = line.startXProperty().get();
		double x2 = line.endXProperty().get();
		double y1 = line.startYProperty().get();
		double y2 = line.endYProperty().get();
		
		double yDelta = (y2 - y1) * offset;
		y2 -= yDelta;
		
		double xDelta = (x2 - x1) * offset;
		x2 -= xDelta;
		
		double arrowlength = chosenArrowLength == null ? line.strokeWidthProperty().get() * 5 : chosenArrowLength;
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
	
	public static interface PropertyUpdaterListener {
		public boolean updateProperty(Property<?> property, Object value);
	}
	
	public static SimplePropertyUpdater createUpdater(Object object, PropertyUpdaterListener listener, String...blacklisted) {
		List<String> blacklist = Arrays.asList(blacklisted);
		return createUpdater(object, listener, new Predicate<Property<?>>() {
			@Override
			public boolean test(Property<?> property) {
				for (String blacklistedName : blacklist) {
					if (property.getName().equals(blacklistedName) || property.getName().matches(blacklistedName) || property.getName().startsWith(blacklistedName + "/")) {
						return false;
					}	
				}
				return true;
			}
		});
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static SimplePropertyUpdater createUpdater(Object object, PropertyUpdaterListener listener, Predicate<Property<?>> filter) {
		List<Property<?>> createProperties = BaseConfigurationGUIManager.createProperties(object.getClass());
		Iterator<Property<?>> iterator = createProperties.iterator();
		BeanInstance instance = new BeanInstance(object);
		for (Property<?> property : createProperties) {
			if (property instanceof SimpleProperty && (((SimpleProperty) property).getShow() != null || ((SimpleProperty) property).getHide() != null)) {
				((SimpleProperty) property).setHiddenCalculator(BasePropertyOnlyGUIManager.newHiddenCalculator(((SimpleProperty) property).getShow(), ((SimpleProperty) property).getHide(), instance));
			}
		}
		List<Value<?>> values = new ArrayList<Value<?>>();
		values: while (iterator.hasNext()) {
			Property<?> next = iterator.next();
			if (!filter.test(next)) {
				iterator.remove();
				continue values;
			}
			Object value = instance.get(next.getName());
			if (value != null) {
				values.add(new ValueImpl(next, value));
			}
		}
		return new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(createProperties), values.toArray(new Value[0])) {
			@Override
			public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
				if (listener != null) {
					if (!listener.updateProperty(property, value)) {
						return Arrays.asList(new ValidationMessage(Severity.ERROR, "Could not update property: " + property.getName()));
					}
				}
				MainController.getInstance().setChanged();
				instance.set(property.getName(), value);
				return super.updateProperty(property, value);
			}
		};
	}
	
	public static Menu findOrCreate(MenuBar menuBar, String title) {
		Menu menuToFind = null;
		for (Menu menu : menuBar.getMenus()) {
			if (menu.getText().equals(title)) {
				menuToFind = menu;
				break;
			}
		}
		if (menuToFind == null) {
			menuToFind = new Menu(title);
			menuBar.getMenus().add(menuToFind);
		}
		return menuToFind;
	}
	
	public static void addElementExpansionHandler(final Tree<Element<?>> tree) {
		tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.E && event.isControlDown()) {
					TreeCell<Element<?>> selectedItem = tree.getSelectionModel().getSelectedItem();
					if (event.isShiftDown()) {
						selectedItem.collapseAll();
					}
					else {
						selectedItem.expandAll(3);
					}
				}				
			}
		});
		
		tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeCell<Element<?>>>() {
			@Override
			public void changed(ObservableValue<? extends TreeCell<Element<?>>> arg0, TreeCell<Element<?>> arg1, TreeCell<Element<?>> arg2) {
				TreeCell<Element<?>> selectedItem = tree.getSelectionModel().getSelectedItem();
				tree.setContextMenu(null);
				if (selectedItem != null) {
					Element<?> element = selectedItem.getItem().itemProperty().get();
					if (element.getType() instanceof DefinedType) {
						String id = ((DefinedType) element.getType()).getId();
						if (id != null) {
							Artifact resolve = MainController.getInstance().getRepository().resolve(id);
							if (resolve != null) {
								MenuItem item = new MenuItem("Open reference: " + id);
								item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										MainController.getInstance().open(id);
									}
								});
								ContextMenu menu = new ContextMenu(item);
								tree.setContextMenu(menu);
							}
							// it is a valid artifact, just not in the tree, we can still try to show it though
							else {
								MenuItem item = new MenuItem("View: " + id + " (read only)");
								item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										MainController.getInstance().show((DefinedType) element.getType()); 
									}
								});
								ContextMenu menu = new ContextMenu(item);
								tree.setContextMenu(menu);
							}
						}
					}
				}
			}
		});
	}
	
	public static Line getLine(TreeCell<?> from, TreeCell<?> to) {
		Line line = new Line();
		
		Endpoint lineEnd = new Endpoint(line.endXProperty(), line.endYProperty());
		Endpoint fromLeft = new Endpoint(from.leftAnchorXProperty(), from.leftAnchorYProperty());
		Endpoint fromRight = new Endpoint(from.rightAnchorXProperty(), from.rightAnchorYProperty());
		EndpointPicker endpointPicker = new EndpointPicker(lineEnd, fromLeft, fromRight);
		line.startXProperty().bind(endpointPicker.x);
		line.startYProperty().bind(endpointPicker.y);
		
		Endpoint lineStart = new Endpoint(line.startXProperty(), line.startYProperty());
		Endpoint toLeft = new Endpoint(to.leftAnchorXProperty(), to.leftAnchorYProperty());
		Endpoint toRight = new Endpoint(to.rightAnchorXProperty(), to.rightAnchorYProperty());
		endpointPicker = new EndpointPicker(lineStart, toLeft, toRight);
		line.endXProperty().bind(endpointPicker.x);
		line.endYProperty().bind(endpointPicker.y);
		
		return line;
	}
	
	public static class Endpoint {
		private DoubleExpression x, y;

		public Endpoint(DoubleExpression x, DoubleExpression y) {
			this.x = x;
			this.y = y;
		}
		public DoubleExpression xProperty() {
			return x;
		}
		public DoubleExpression yProperty() {
			return y;
		}
	}
	
	public static class EndpointPicker {
		private Endpoint[] possibleBindPoints;
		private Endpoint endpointToBind;
		
		private Endpoint lastWinner;
		
		private SimpleDoubleProperty x = new SimpleDoubleProperty();
		private SimpleDoubleProperty y = new SimpleDoubleProperty();

		public EndpointPicker(Endpoint endpointToBind, Endpoint...possibleBindPoints) {
			this.endpointToBind = endpointToBind;
			this.possibleBindPoints = possibleBindPoints;
			calculate();
			
			ChangeListener<Number> recalculationListener = new ChangeListener<Number>() {
				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					calculate();
				}
			};
			endpointToBind.xProperty().addListener(recalculationListener);
			endpointToBind.yProperty().addListener(recalculationListener);
			
			for (Endpoint endpoint : possibleBindPoints) {
				endpoint.xProperty().addListener(recalculationListener);
				endpoint.yProperty().addListener(recalculationListener);
			}
		}
		
		private void calculate() {
			double minDistance = Double.MAX_VALUE;
			Endpoint winner = null;
			double x1 = endpointToBind.xProperty().get();
			double y1 = endpointToBind.yProperty().get();
			for (Endpoint possibleBindPoint : possibleBindPoints) {
				double x2 = possibleBindPoint.xProperty().get();
				double y2 = possibleBindPoint.yProperty().get();
				double distance = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
				if (distance < minDistance) {
					winner = possibleBindPoint;
					minDistance = distance;
				}
			}
			if (lastWinner == null || !lastWinner.equals(winner)) {
				if (x.isBound()) {
					x.unbind();
				}
				x.bind(winner.xProperty());
				if (y.isBound()) {
					y.unbind();
				}
				y.bind(winner.yProperty());
				lastWinner = winner;
			}
		}
		
		public ReadOnlyDoubleProperty xProperty() {
			return x;
		}
		public ReadOnlyDoubleProperty yProperty() {
			return y;
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void validationView(TableView<Validation<?>> tblValidations) {
		for (int i = tblValidations.getColumns().size() - 1; i < 5; i++) {
			tblValidations.getColumns().add(new TableColumn<Validation<?>, String>());
		}
		
		List<TableColumn<Validation<?>, ?>> columns = tblValidations.getColumns();

		TableColumn<Validation<?>, String> levelColumn = (TableColumn<Validation<?>, String>) columns.get(0);
		levelColumn.setText("Status");
		levelColumn.setCellValueFactory(
			new Callback<TableColumn.CellDataFeatures<Validation<?>,String>, ObservableValue<String>>() {
				@Override
				public ObservableValue<String> call(CellDataFeatures<Validation<?>, String> arg0) {
					return new SimpleStringProperty(arg0.getValue().getSeverity() == Severity.INFO ? "INFO" : "ERROR");
				}
			}
		);
		levelColumn.setCellFactory(new Callback<TableColumn<Validation<?>, String>, TableCell<Validation<?>, String>>() {
			@Override
			public TableCell<Validation<?>, String> call(TableColumn<Validation<?>, String> arg0) {
				return new TableCell<Validation<?>, String>() {
					@Override
					protected void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						setText(item);
						TableRow<Validation<?>> row = getTableRow();
						if (row != null) {
							if (item != null) {
								if (Severity.ERROR.name().equals(item) || Severity.CRITICAL.name().equals(item) || "ERROR".equals(item)) {
									row.setStyle("-fx-control-inner-background: #ffd5d6;");
								}
								else if (Severity.WARNING.name().equals(item)) {
									row.setStyle("-fx-control-inner-background: #ffe190;");
								}
								else {
									row.setStyle("-fx-control-inner-background: #ecfdc3;");
								}
							}
							else {
								row.setStyle("");
							}
						}
					}
				};
			}
		});
		
		TableColumn<Validation<?>, String> messageColumn = (TableColumn<Validation<?>, String>) columns.get(2);
		messageColumn.setText("Description");
		messageColumn.setCellValueFactory(
		    new PropertyValueFactory<Validation<?>, String>("description")
		);
		messageColumn.minWidthProperty().set(350);
		
		TableColumn<Validation<?>, String> checkColumn = (TableColumn<Validation<?>, String>) columns.get(1);
		checkColumn.setText("Message");
		checkColumn.setCellValueFactory(
		    new PropertyValueFactory<Validation<?>, String>("message")
		);
		checkColumn.minWidthProperty().set(450);
		
		TableColumn<Validation<?>, String> scriptColumn = (TableColumn<Validation<?>, String>) columns.get(3);
		scriptColumn.setText("Location");
		scriptColumn.setCellValueFactory(
		    new Callback<TableColumn.CellDataFeatures<Validation<?>,String>, ObservableValue<String>>() {
				@SuppressWarnings("rawtypes")
				@Override
				public ObservableValue<String> call(CellDataFeatures<Validation<?>, String> arg0) {
					StringBuilder builder = new StringBuilder();
					List<?> callStack = new ArrayList(arg0.getValue().getContext());
					Collections.reverse(callStack);
					for (Object item : callStack) {
						if (!builder.toString().isEmpty()) {
							builder.append(" > ");
						}
						builder.append(item.toString());
					}
					return new SimpleStringProperty(builder.toString());
				}
			}
		);
		scriptColumn.minWidthProperty().set(200);
		
		TableColumn<Validation<?>, String> lineColumn = (TableColumn<Validation<?>, String>) columns.get(4);
		lineColumn.setText("Code");
		lineColumn.setCellValueFactory(
		    new Callback<TableColumn.CellDataFeatures<Validation<?>,String>, ObservableValue<String>>() {
				@Override
				public ObservableValue<String> call(CellDataFeatures<Validation<?>, String> arg0) {
					return new SimpleStringProperty(arg0.getValue().getCode());
				}
			}
		);
	}
}
