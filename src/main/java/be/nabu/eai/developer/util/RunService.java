package be.nabu.eai.developer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import be.nabu.eai.developer.ComplexContentEditor;
import be.nabu.eai.developer.ComplexContentEditor.ValueWrapper;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.repository.util.SystemPrincipal;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeCellValue;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.events.api.ResponseHandler;
import be.nabu.libs.services.ServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
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
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
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
		build(controller, controller.getStage());
	}
	@SuppressWarnings("unchecked")
	public void build(final MainController controller, Stage owner) {
		final Stage stage = new Stage();
		
		stage.setTitle("Run service" + (service instanceof DefinedService ? ": " + ((DefinedService) service).getId() : ""));
		ScrollPane pane = new ScrollPane();

		VBox vbox = new VBox();
		pane.setContent(vbox);
		
		TextField serviceContext = new TextField();
		serviceContext.setText((String) MainController.getInstance().getState(getClass(), "serviceContext"));
		serviceContext.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				MainController.getInstance().setState(RunService.class, "serviceContext", arg2);
			}
		});

//		buildInput(controller, null, service.getServiceInterface().getInputDefinition(), vbox);
//		Tree<Element<?>> tree = buildTree(service.getServiceInterface().getInputDefinition());
		
		final ComplexContentEditor complexContentEditor = new ComplexContentEditor(service.getServiceInterface().getInputDefinition().newInstance(), false, controller.getRepository());
		Map<? extends String, ? extends Object> state = (Map<? extends String, ? extends Object>) MainController.getInstance().getState(RunService.class, "inputs");
		if (state != null) {
			complexContentEditor.getState().putAll(state);
		}
		Tree<ValueWrapper> tree = complexContentEditor.getTree();
		tree.setStyle("-fx-border-color: #cccccc;-fx-border-style: solid none solid none;-fx-border-width: 1px;");

		pane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
		// make sure the vbox resizes to the pane minus the scroll bar width
		vbox.prefWidthProperty().bind(pane.widthProperty().subtract(20));
		// and the tree to the vbox
		tree.prefWidthProperty().bind(vbox.widthProperty());
		
		// expand root
		tree.getRootCell().expandedProperty().set(true);
		
		vbox.heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				if (newValue != null && newValue.doubleValue() > 100) {
					// scrollbar size?
					stage.setHeight(Math.min(newValue.doubleValue(), 500) + 40);
				}
			}
		});
		
		Button run = new Button("Run");
		run.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
//					ComplexContent result = new ServiceRuntime(service, controller.getRepository().newExecutionContext(null)).run(buildInput());
					if (controller.getRepository().getServiceRunner() != null) {
						final String runAs = (String) MainController.getInstance().getState(RunService.class, "runAs");
						final String runAsRealm = (String) MainController.getInstance().getState(RunService.class, "runAsRealm");
						final String serviceContext = (String) MainController.getInstance().getState(RunService.class, "serviceContext");
						Date date = new Date();
//						Future<ServiceResult> result = controller.getRepository().getServiceRunner().run(service, controller.getRepository().newExecutionContext(runAs != null && !runAs.trim().isEmpty() ? new SystemPrincipal(runAs) : null), buildInput());
						MainController.getInstance().setState(RunService.class, "inputs", complexContentEditor.getState());
						Runnable runnable = new Runnable() {
							public void run() {
								try {
									// set it globally
									ServiceRuntime.setGlobalContext(new HashMap<String, Object>());
									ServiceRuntime.getGlobalContext().put("service.context", serviceContext);
									Future<ServiceResult> result = controller.getRepository().getServiceRunner().run(service, controller.getRepository().newExecutionContext(runAs != null && !runAs.trim().isEmpty() ? new SystemPrincipal(runAs, runAsRealm) : null), complexContentEditor.getContent());
									ServiceResult serviceResult = result.get();
									Boolean shouldContinue = MainController.getInstance().getDispatcher().fire(serviceResult, this, new ResponseHandler<ServiceResult, Boolean>() {
										@Override
										public Boolean handle(ServiceResult event, Object response, boolean isLast) {
											return response instanceof Boolean ? (Boolean) response : null;
										}
									});
									if (shouldContinue == null || shouldContinue) {
										String message = "Ran " + (service instanceof DefinedService ? ((DefinedService) service).getId() : "anonymous") + " in: " + (new Date().getTime() - date.getTime()) + "ms";
										MainController.getInstance().notify(new ValidationMessage(Severity.INFO, message));
										if (serviceResult.getException() != null) {
											throw serviceResult.getException();
										}
										else {
											Platform.runLater(new Runnable() {
												@Override
												public void run() {
													Button showContent = new Button("Result");
													showContent.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
														@Override
														public void handle(ActionEvent event) {
															controller.showContent(serviceResult.getOutput());
															controller.getStage().requestFocus();
														}
													});
													controller.showContent(serviceResult.getOutput());
													controller.getNotificationHandler().notify(message, 4000l, Severity.INFO, showContent);
												}
											});
										}
									}
								}
								catch (Exception e) {
									Platform.runLater(new Runnable() {
										@Override
										public void run() {
											controller.showContent(new BeanInstance<Exception>(e));
										}
									});
								}
								finally {
									// unset it
									ServiceRuntime.setGlobalContext(null);
								}
							}
						};
						controller.offload(runnable, true, "Running service");
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
		HBox runAsBox = EAIDeveloperUtils.newHBox("Run As", runAs);
		
		TextField runAsRealm = new TextField();
		runAsRealm.setText((String) MainController.getInstance().getState(getClass(), "runAsRealm"));
		runAsRealm.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				MainController.getInstance().setState(RunService.class, "runAsRealm", arg2);
			}
		});
		HBox runAsRealmBox = EAIDeveloperUtils.newHBox("In Realm", runAsRealm);
		HBox serviceContextBox = EAIDeveloperUtils.newHBox("Service Context", serviceContext);

		vbox.getChildren().add(tree);
		
		vbox.getChildren().add(serviceContextBox);
		vbox.getChildren().addAll(runAsBox);
		vbox.getChildren().addAll(runAsRealmBox);
		
		vbox.getChildren().add(EAIDeveloperUtils.newHBox(EAIDeveloperUtils.newCloseButton("Close", stage), run));
		
		stage.initOwner(owner);
		if (!System.getProperty("os.name").contains("nux")) {
			stage.initModality(Modality.WINDOW_MODAL);
		}
		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ESCAPE) {
					stage.close();
					event.consume();
				}
				else if (event.getCode() == KeyCode.ENTER) {
					run.fire();
				}
			}
		});
		Scene scene = new Scene(pane);
		stage.setMaxHeight(800);
		stage.setWidth(800);
		//stage.setHeight(500);
		stage.setScene(scene);
		
		// inherit stylesheets
//		stage.getScene().getStylesheets().addAll(MainController.getInstance().getStage().getScene().getStylesheets());
				
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
			// only set a value if there is one, otherwise we can have odd stuff...
			if (fields.get(path).getText() != null && !fields.get(path).getText().trim().isEmpty()) {
				input.set(path, fields.get(path).getText() == null || fields.get(path).getText().trim().isEmpty() ? values.get(path) : fields.get(path).getText());
			}
			state.put(path, fields.get(path).getText());
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
									String tmpPath = null;
									TreeItem<Element<?>> current = item;
									// don't include the root element in the path
									while (current.getParent() != null) {
										String tmpIndex = "";
										if (current.itemProperty().get().getType().isList(current.itemProperty().get().getProperties())) {
											tmpIndex = "[0]";
										}
										if (tmpPath == null) {
											tmpPath = current.getName() + tmpIndex;
										}
										else {
											tmpPath = current.getName() + tmpIndex + "/" + tmpPath;
										}
										current = current.getParent();
									}
									final String path = tmpPath;
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
