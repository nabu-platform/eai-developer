package be.nabu.eai.developer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
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
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.SimpleTypeWrapper;
import be.nabu.libs.types.api.TypeConverter;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.Container;

public class RunService {
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	private Map<String, TextField> fields = new LinkedHashMap<String, TextField>();
	private Map<String, Object> values = new HashMap<String, Object>();
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
						Principal principal = null;
						final String runAs = (String) MainController.getInstance().getState(RunService.class, "runAs");
						if (runAs != null && !runAs.trim().isEmpty()) {
							principal = new Principal() {
								@Override
								public String getName() {
									return runAs;
								}
							};
						}
						Future<ServiceResult> result = controller.getRepository().getServiceRunner().run(service, controller.getRepository().newExecutionContext(principal), buildInput()); 
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
		TextField runAs = new TextField();
		runAs.setText((String) MainController.getInstance().getState(getClass(), "runAs"));
		runAs.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				MainController.getInstance().setState(RunService.class, "runAs", arg2);
			}
		});
		buttons.getChildren().addAll(new Label("Run As: "), runAs, run);
		vbox.getChildren().add(buttons);
		
		stage.initOwner(controller.getStage());
		stage.initModality(Modality.WINDOW_MODAL);
		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					stage.hide();
					event.consume();
				}
				else if (event.getCode() == KeyCode.ENTER) {
					run.fire();
				}
			}
		});
		Scene scene = new Scene(pane);
		stage.setWidth(500);
		stage.setHeight(500);
		stage.setScene(scene);
		stage.show();
	}
	
	@SuppressWarnings("unchecked")
	private ComplexContent buildInput() {
		ComplexContent input = service.getServiceInterface().getInputDefinition().newInstance();
		Map<String, String> state = (Map<String, String>) MainController.getInstance().getState(getClass(), "inputs");
		if (state == null) {
			state = new HashMap<String, String>();
			MainController.getInstance().setState(getClass(), "inputs", state);
		}
		for (String path : fields.keySet()) {
			input.set(path, fields.get(path).getText() == null || fields.get(path).getText().trim().isEmpty() ? values.get(path) : fields.get(path).getText());
			if (fields.get(path).getText() != null && !fields.get(path).getText().trim().isEmpty()) {
				state.put(path, fields.get(path).getText());
			}
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
					@SuppressWarnings("unchecked")
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
									final TextField field = new TextField();
									// the path will include the root which it shouldn't
									final String path = TreeDragDrop.getPath(item).substring(("/" + root.getName() + "/").length() - 1) + index;
									Map<String, String> state = (Map<String, String>) MainController.getInstance().getState(RunService.class, "inputs");
									if (state != null) {
										field.setText(state.get(path));
									}
									fields.put(path, field);
									field.getStyleClass().add("serviceInput");
									// doesn't get picked up in css?
									field.setStyle("-fx-text-fill: #AAAAAA;-fx-font-size: 9pt;");
									field.setPrefHeight(18);
									field.setMaxHeight(18);
									hbox.getChildren().add(field);
									// for byte[] and inputstream, provide a button to upload a file
									if (item.itemProperty().get().getType() instanceof SimpleType && (((SimpleType<?>) item.itemProperty().get().getType()).getInstanceClass().equals(byte[].class) || ((SimpleType<?>) item.itemProperty().get().getType()).getInstanceClass().equals(InputStream.class))) {
										Button button = new Button("Load File");
										button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
											@Override
											public void handle(ActionEvent arg0) {
												FileChooser fileChooser = new FileChooser();
												File file = fileChooser.showOpenDialog(MainController.getInstance().getStage());
												if (file != null && file.exists() && file.isFile()) {
													// can no longer fill in text
													field.setText("");
													field.setDisable(true);
													try {
														Container<ByteBuffer> wrap = IOUtils.wrap(file);
														try {
															values.put(path, IOUtils.toBytes(wrap));
														}
														finally {
															wrap.close();
														}
													}
													catch (IOException e) {
														logger.error("Could not load file: " + file, e);
													}
												}
											}
										});
										hbox.getChildren().add(button);
									}
									// we have a date, add a date picker
									// TODO: add date picker
//									else if (item.itemProperty().get().getType() instanceof SimpleType && (((SimpleType<?>) item.itemProperty().get().getType()).getInstanceClass().equals(Date.class))) {
//										hbox.getChildren().add(button);
//									}
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
		// force it to load, otherwise we might not set all the textfields and stuff
		tree.forceLoad();
		return tree;
	}
}
