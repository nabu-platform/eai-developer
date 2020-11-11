package be.nabu.eai.developer.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;

import javafx.application.Platform;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.managers.base.BaseConfigurationGUIManager;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.java.BeanInstance;
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
		VBox vbox = new VBox();
		controller.showProperties(updater, vbox, refresh, controller.getRepository(), controller.isInContainer(vbox), false);
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
		return buildPopup(title, pane, true);
	}
	
	public static HBox newHBox(String title, Node node) {
		HBox hbox = new HBox();
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
		pane.minWidthProperty().set(Math.max(400, pane.getPrefWidth()));
		pane.prefWidthProperty().bind(scene.widthProperty());
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
		return new SimplePropertyUpdater(true, new HashSet<Property<?>>(createProperties), values.toArray(new Value[0])) {
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
}
