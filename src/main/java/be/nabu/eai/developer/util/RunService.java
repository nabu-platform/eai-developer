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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
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
import be.nabu.jfx.control.ace.AceEditor;
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
import be.nabu.libs.types.binding.api.Window;
import be.nabu.libs.types.binding.json.JSONBinding;
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
	public static final Integer AUTO_LIMIT = Integer.parseInt(System.getProperty("auto.limit", "100"));
	
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
		complexContentEditor.setPrefillBooleans(true);
		Map<? extends String, ? extends Object> state = (Map<? extends String, ? extends Object>) MainController.getInstance().getState(RunService.class, "inputs");
		if (state != null) {
			complexContentEditor.getState().putAll(state);
		}
		Tree<ValueWrapper> tree = complexContentEditor.getTree();
		tree.setStyle("-fx-border-color: #cccccc;-fx-border-style: solid none solid none;-fx-border-width: 1px;");

		pane.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		// make sure the vbox resizes to the pane minus the scroll bar width
		vbox.prefWidthProperty().bind(pane.widthProperty().subtract(20));
		// and the tree to the vbox
//		tree.prefWidthProperty().bind(vbox.widthProperty());
		// it has its own scrollbar...
		tree.prefWidthProperty().bind(vbox.widthProperty().subtract(20));
		
		// expand root (if there is one!)
		if (tree.getRootCell() != null) {
			tree.getRootCell().expandedProperty().set(true);
		}
		
//		vbox.heightProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//				if (newValue != null && newValue.doubleValue() > 100) {
//					// scrollbar size?
//					stage.setHeight(Math.min(newValue.doubleValue(), 500) + 40);
//					treeScroll.minHeightProperty().set(Math.max(100, stage.getHeight() - 400));
//				}
//			}
//		});
		
		TabPane inputTabs = new TabPane();
		AceEditor jsonEditor = new AceEditor();
		
		Button run = new Button("Run");
		run.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				try {
//					ComplexContent result = new ServiceRuntime(service, controller.getRepository().newExecutionContext(null)).run(buildInput());
					if (controller.getRepository().getServiceRunner() != null) {
						
						// if you were inputting json, first parse it!
						if (inputTabs.getSelectionModel().getSelectedItem().getId().equals("json")) {
							JSONBinding jsonBinding = new JSONBinding(complexContentEditor.getContent().getType(), Charset.defaultCharset());
							try {
								jsonBinding.setIgnoreUnknownElements(true);
								ComplexContent unmarshal = jsonBinding.unmarshal(new ByteArrayInputStream(jsonEditor.getContent().getBytes(Charset.defaultCharset())), new Window[0]);
								complexContentEditor.setContent(unmarshal);
								// should be refreshed, expand the root again
								tree.getRootCell().expandedProperty().set(true);
							}
							catch (Exception e) {
								MainController.getInstance().notify(e);
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										controller.showContent(new BeanInstance<Exception>(e));
									}
								});
								// quit it!
								return;
							}
						}
						
						final String features = (String) MainController.getInstance().getState(RunService.class, "features");
						final String runAs = (String) MainController.getInstance().getState(RunService.class, "runAs");
						final String runAsRealm = (String) MainController.getInstance().getState(RunService.class, "runAsRealm");
						final String serviceContext = (String) MainController.getInstance().getState(RunService.class, "serviceContext");
						final String lenient = (String) MainController.getInstance().getState(RunService.class, "lenient");
						Date date = new Date();
//						Future<ServiceResult> result = controller.getRepository().getServiceRunner().run(service, controller.getRepository().newExecutionContext(runAs != null && !runAs.trim().isEmpty() ? new SystemPrincipal(runAs) : null), buildInput());
						MainController.getInstance().setState(RunService.class, "inputs", complexContentEditor.getState());
						Runnable runnable = new Runnable() {
							public void run() {
								try {
									String localFeatures = features;
									if ("true".equals(lenient)) {
										if (localFeatures == null) {
											localFeatures = "";
										}
										else {
											localFeatures += ",";
										}
										localFeatures += "LENIENT";
									}
									// set it globally
									ServiceRuntime.setGlobalContext(new HashMap<String, Object>());
									ServiceRuntime.getGlobalContext().put("service.context", serviceContext);
									ServiceRuntime.getGlobalContext().put("features.additional", localFeatures);
									ComplexContent content = complexContentEditor.getContent();
									if (content != null && AUTO_LIMIT > 0) {
										Element<?> limit = content.getType().get("limit");
										Element<?> offset = content.getType().get("offset");
										// if we have both a limit and an offset and you didn't fill in a limit, we add one to protect you from requesting too much
										if (limit != null && offset != null) {
											// they must be numeric
											if (limit.getType() instanceof SimpleType && Number.class.isAssignableFrom(((SimpleType<?>) limit.getType()).getInstanceClass())
													&& offset.getType() instanceof SimpleType && Number.class.isAssignableFrom(((SimpleType<?>) offset.getType()).getInstanceClass())) {
												if (content.get("limit") == null) {
													content.set("limit", AUTO_LIMIT);
												}
											}
										}
									}
									Future<ServiceResult> result = controller.getRepository().getServiceRunner().run(service, controller.getRepository().newExecutionContext(runAs != null && !runAs.trim().isEmpty() ? new SystemPrincipal(runAs, runAsRealm) : null), content);
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
													if (Boolean.parseBoolean(System.getProperty("result.in.tab", "true"))) {
														if (serviceResult.getOutput() != null) {
															Tab newTab = MainController.getInstance().newTab("Result Viewer");
															ScrollPane scroll = new ScrollPane();
															AnchorPane contentPane = new AnchorPane();
															scroll.setContent(contentPane);
															newTab.setContent(scroll);
															scroll.setFitToWidth(true);
															controller.showContent(contentPane, serviceResult.getOutput(), null);
														}
													}
													else {
														controller.showContent(serviceResult.getOutput());
													}
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

		TextField features = new TextField();
		features.setText((String) MainController.getInstance().getState(getClass(), "features"));
		features.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
				MainController.getInstance().setState(RunService.class, "features", arg2);
			}
		});
		HBox featureBox = EAIDeveloperUtils.newHBox("Features", features);
		
		CheckBox lenient = new CheckBox();
		lenient.setSelected("true".equals(MainController.getInstance().getState(getClass(), "lenient")));
		lenient.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				MainController.getInstance().setState(RunService.class, "lenient", arg2 == null ? null : arg2.toString());
			}
		});
		HBox lenientBox = EAIDeveloperUtils.newHBox("Lenient", lenient);
		
		Tab tabInput = new Tab("Input");
		tabInput.setId("input");
		inputTabs.getTabs().add(tabInput);
		ScrollPane treeScroll = new ScrollPane(tree);
		treeScroll.prefWidthProperty().bind(vbox.prefWidthProperty());
		treeScroll.setPrefHeight(350);
		tabInput.setContent(treeScroll);
		
		// this expands forever!! the tree is probably autoresizing due to the additional 50...
//		tabs.minHeightProperty().bind(tree.heightProperty().add(50));
		
		Tab tabJson = new Tab("JSON");
		tabJson.setId("json");
		inputTabs.getTabs().add(tabJson);
		jsonEditor.getWebView().prefHeightProperty().bind(treeScroll.prefHeightProperty());
		tabJson.setContent(jsonEditor.getWebView());
		
		inputTabs.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {
			@Override
			public void changed(ObservableValue<? extends Tab> arg0, Tab oldTab, Tab newTab) {
				// if we are moving away from the old tab, update the json content
				if (oldTab.getId().equals("input")) {
					if (complexContentEditor.getContent() != null) {
						try {
							JSONBinding jsonBinding = new JSONBinding(complexContentEditor.getContent().getType(), Charset.defaultCharset());
							jsonBinding.setPrettyPrint(true);
							ByteArrayOutputStream output = new ByteArrayOutputStream();
							jsonBinding.marshal(output, complexContentEditor.getContent());
							jsonEditor.setContent("application/json", new String(output.toByteArray(), Charset.defaultCharset()));
						}
						catch (Exception e) {
							StringWriter string = new StringWriter();
							PrintWriter writer = new PrintWriter(string);
							e.printStackTrace(writer);
							writer.flush();
							jsonEditor.setContent("text/plain", string.toString());
						}
					}
					else {
						jsonEditor.setContent("application/json", "{}");
					}
				}
				else if (oldTab.getId().equals("json")) {
					JSONBinding jsonBinding = new JSONBinding(complexContentEditor.getContent().getType(), Charset.defaultCharset());
					try {
						jsonBinding.setIgnoreUnknownElements(true);
						ComplexContent unmarshal = jsonBinding.unmarshal(new ByteArrayInputStream(jsonEditor.getContent().getBytes(Charset.defaultCharset())), new Window[0]);
						complexContentEditor.setContent(unmarshal);
						// should be refreshed, expand the root again
						tree.getRootCell().expandedProperty().set(true);
					}
					catch (Exception e) {
						MainController.getInstance().notify(e);
					}
				}
			}
		});
		
		vbox.getChildren().add(inputTabs);
//		vbox.getChildren().add(tree);
		
		vbox.getChildren().add(serviceContextBox);
		vbox.getChildren().addAll(runAsBox);
		vbox.getChildren().addAll(runAsRealmBox, featureBox, lenientBox);
		
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
					// this now triggers in the ace editor and it is inconsistent in the complex content editor anyway...
//					run.fire();
				}
			}
		});
		Scene scene = new Scene(pane);
		scene.getStylesheets().addAll(MainController.getInstance().getStage().getScene().getStylesheets());
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
