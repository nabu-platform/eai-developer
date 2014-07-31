package be.nabu.eai.developer.util;

import java.util.LinkedHashMap;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.util.ElementTreeItem;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeCellValue;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.services.api.ServiceInstance;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;

public class RunService {
	
	private Map<String, TextField> fields = new LinkedHashMap<String, TextField>();
	private Service service;
	
	public RunService(Service service) {
		this.service = service;
	}
	public void build(final MainController controller) {
		final Stage stage = new Stage();
		ScrollPane pane = new ScrollPane();
		VBox vbox = new VBox();
		pane.setContent(vbox);
//		buildInput(controller, null, service.getServiceInterface().getInputDefinition(), vbox);
		vbox.getChildren().add(buildTree(service.getServiceInterface().getInputDefinition()));
		
		HBox buttons = new HBox();
		Button run = new Button("Run");
		run.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				ServiceInstance instance = service.newInstance();
				try {
					ComplexContent result = instance.execute(controller.getRepository().newRuntime(null), buildInput());
					stage.hide();
					controller.showContent(result);
				}
				catch (ServiceException e) {
					// TODO: better handling
					throw new RuntimeException(e);
				}
			}
		});
		buttons.getChildren().add(run);
		vbox.getChildren().add(buttons);
		
		stage.initOwner(controller.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		Scene scene = new Scene(pane);
		stage.setWidth(500);
		stage.setHeight(500);
		stage.setScene(scene);
		stage.show();
	}
	
	private ComplexContent buildInput() {
		ComplexContent input = service.getServiceInterface().getInputDefinition().newInstance();
		for (String path : fields.keySet()) {
			input.set(path, fields.get(path).getText().isEmpty() ? null : fields.get(path).getText());
		}
		return input;
	}
	
	private Tree<Element<?>> buildTree(ComplexType type) {
		final ElementTreeItem root = new ElementTreeItem(new RootElement(type), null, false, false);
		Tree<Element<?>> tree = new Tree<Element<?>>(new Callback<TreeItem<Element<?>>, TreeCellValue<Element<?>>> () {
			@Override
			public TreeCellValue<Element<?>> call(final TreeItem<Element<?>> item) {
				return new TreeCellValue<Element<?>>() {
					private ObjectProperty<TreeCell<Element<?>>> cell = new SimpleObjectProperty<TreeCell<Element<?>>>();
					private HBox hbox;
					@Override
					public ObjectProperty<TreeCell<Element<?>>> cellProperty() {
						return cell;
					}
					@Override
					public Region getNode() {
						if (hbox == null) {
							hbox = new HBox();
							String index = "";
							if (item.itemProperty().get().getType().isList(item.itemProperty().get().getProperties())) {
								// currently always allow you to fill in the first element
								// will need to provide a button to add second etc
								index += "[0]";
							}
							Label labelName = new Label(item.getName() + index);
							hbox.getChildren().add(labelName);
							if (item.leafProperty().get()) {
								if (item.itemProperty().get().getType() instanceof be.nabu.libs.types.api.Unmarshallable) {
									TextField field = new TextField();
									String path = TreeDragDrop.getPath(item);
									// the path will include the root which it shouldn't
									path = path.substring(("/" + root.getName() + "/").length() - 1) + index;
									fields.put(path, field);
									field.getStyleClass().add("serviceInput");
									// doesn't get picked up in css?
									field.setStyle("-fx-text-fill: #AAAAAA;-fx-font-size: 9pt;");
									field.setPrefHeight(18);
									field.setMaxHeight(18);
									hbox.getChildren().add(field);
								}
								else {
									hbox.getChildren().add(new Label("Can not be unmarshalled"));
								}
							}
						}
						return hbox;
					}

					@Override
					public void refresh() {
						hbox = null;
					}
				};
			}
		});
		tree.rootProperty().set(root);
		return tree;
	}
}
