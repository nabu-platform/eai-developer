package be.nabu.eai.developer.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

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
import be.nabu.libs.services.api.ServiceResult;
import be.nabu.libs.types.BaseTypeInstance;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.TypeConverterFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleTypeWrapper;
import be.nabu.libs.types.api.TypeConverter;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.java.BeanInstance;

public class RunService {
	
	private Map<String, TextField> fields = new LinkedHashMap<String, TextField>();
	private Service service;
	private TypeConverter typeConverter = TypeConverterFactory.getInstance().getConverter();
	private SimpleTypeWrapper simpleTypeWrapper = SimpleTypeWrapperFactory.getInstance().getWrapper();
	
	public RunService(Service service) {
		this.service = service;
	}
	public void build(final MainController controller) {
		final Stage stage = new Stage();
		ScrollPane pane = new ScrollPane();
		VBox vbox = new VBox();
		pane.setContent(vbox);
//		buildInput(controller, null, service.getServiceInterface().getInputDefinition(), vbox);
		Tree<Element<?>> tree = buildTree(service.getServiceInterface().getInputDefinition());
		vbox.getChildren().add(tree);
		
		// make sure the vbox resizes to the pane
		vbox.prefWidthProperty().bind(pane.widthProperty());
		// and the tree to the vbox
		tree.prefWidthProperty().bind(vbox.widthProperty());
		
		HBox buttons = new HBox();
		Button run = new Button("Run");
		run.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
//					ComplexContent result = new ServiceRuntime(service, controller.getRepository().newExecutionContext(null)).run(buildInput());
					if (controller.getRepository().getServiceRunner() != null) {
						Future<ServiceResult> result = controller.getRepository().getServiceRunner().run(service, controller.getRepository().newExecutionContext(null), buildInput(), null); 
						ServiceResult serviceResult = result.get();
						if (serviceResult.getException() != null) {
							throw serviceResult.getException();
						}
						else {
							controller.showContent(serviceResult.getOutput());
						}
					}
				}
				catch (Exception e) {
					controller.showContent(new BeanInstance<Exception>(e));
				}
				stage.hide();
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
								if (item.itemProperty().get().getType() instanceof be.nabu.libs.types.api.Unmarshallable || typeConverter.canConvert(new BaseTypeInstance(simpleTypeWrapper.wrap(String.class)), item.itemProperty().get())) {
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
