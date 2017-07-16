package be.nabu.eai.developer.impl;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.components.RepositoryBrowser.RepositoryTreeItem;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.repository.DocumentationManager;
import be.nabu.eai.repository.DocumentationManager.DocumentedImpl;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.jfx.control.ace.AceEditor;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.TimestampedResource;
import be.nabu.libs.resources.api.WritableResource;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.io.api.WritableContainer;

public class DocumentationContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		if (entry instanceof ResourceEntry) {
			Menu menu = new Menu("Documentation");
			MenuItem edit = new MenuItem("Edit");
			edit.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@SuppressWarnings({ "rawtypes" })
				@Override
				public void handle(ActionEvent event) {
					try {
						ResourceContainer<?> container = ((ResourceEntry) entry).getContainer();
						Resource readme = ResourceUtils.touch(container, EAIResourceRepository.PROTECTED + "/documentation/readme.md");
						TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
						if (resolve instanceof RepositoryTreeItem) {
							((RepositoryTreeItem) resolve).documentedProperty().set(true);
						}
						final String id = entry.getId() + " (documentation)";
						Tab tab = MainController.getInstance().getTab(id);
						if (tab == null) {
							File localEditFolder = new File(".nabu/attachments/" + entry.getId());
							
							ResourceContainer<?> documentationFolder = (ResourceContainer) ResourceUtils.resolve(container, EAIResourceRepository.PROTECTED + "/documentation");
							ResourceContainer<?> attachmentFolder = ResourceUtils.mkdirs(documentationFolder, "attachments");
							
							final Tab newTab = MainController.getInstance().newTab(id);
							AnchorPane pane = new AnchorPane();
							VBox vbox = new VBox();
							
							// a new tabpane for all the pages
							TabPane pages = new TabPane();
							pages.setSide(Side.RIGHT);
							pages.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
							
							Map<Resource, DocumentedImpl> documentation = new HashMap<Resource, DocumentedImpl>();
							
							EventHandler<Event> saveHandler = new EventHandler<Event>() {
								@Override
								public void handle(Event arg0) {
									for (Resource resource : documentation.keySet()) {
										DocumentationManager.write(resource, documentation.get(resource));
									}
									if (localEditFolder.exists()) {
										for (File child : localEditFolder.listFiles()) {
											if (child.isFile()) {
												Resource attachment = attachmentFolder.getChild(child.getName());
												// if we don't have an attachment with that name, delete in the edit folder
												if (attachment == null) {
													child.delete();
												}
												else if (attachment instanceof TimestampedResource) {
													Date lastModified = ((TimestampedResource) attachment).getLastModified();
													// if the child contains an update, save it
													if (lastModified.before(new Date(child.lastModified()))) {
														try {
															copyFileToResource(child, attachment);
														}
														catch (Exception e) {
															MainController.getInstance().notify(e);
														}
													}
												}
											}
										}
									}
									// remove trailing *
									if (newTab.getText().endsWith("*")) {
										newTab.setText(newTab.getText().replace(" *", ""));
									}
								}
							};
							
							// first the main page
							DocumentedImpl documented = DocumentationManager.read(readme);
							documentation.put(readme, documented);
							Tab mainTab = new Tab("Main");
							mainTab.setClosable(false);
							mainTab.setContent(showDocumented(newTab, documented, readme, entry.getRepository(), saveHandler));
							pages.getTabs().add(mainTab);
							pages.getSelectionModel().select(mainTab);
							
							// add the other pages
							for (Resource page : documentationFolder) {
								if (page instanceof ReadableResource && !page.getName().equals("readme.md")) {
									newTab(entry, newTab, pages, documentation, documentationFolder, page, saveHandler);
								}
 							}
							
							HBox buttons = new HBox();
							buttons.getStyleClass().add("documentation-buttons");
							Button save = new Button("Save All");
							save.addEventHandler(ActionEvent.ANY, saveHandler);
							
							Button addPage = new Button("Add Page");
							addPage.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@SuppressWarnings("unchecked")
								@Override
								public void handle(ActionEvent arg0) {
									Set set = new HashSet();
									set.add(new SimpleProperty<String>("Name", String.class, true));
									SimplePropertyUpdater updater = new SimplePropertyUpdater(true, set);
									EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Add Page", new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent arg0) {
											String name = updater.getValue("Name");
											if (name != null) {
												try {
													Resource create = ((ManageableContainer<?>) documentationFolder).create(name + ".md", "text/x-markdown");
													newTab(entry, newTab, pages, documentation, documentationFolder, create, saveHandler);
												}
												catch (IOException e) {
													MainController.getInstance().notify(e);
												}
											}
										}
									});
								}
							});
							
							HBox attachments = new HBox();
							attachments.setPrefHeight(200);
							attachments.prefWidthProperty().bind(pane.widthProperty());
							AnchorPane preview = new AnchorPane();
							ListView<Resource> attachmentList = new ListView<Resource>();
							attachmentList.setCellFactory(new Callback<ListView<Resource>, 
								ListCell<Resource>>() {
									@Override
									public ListCell<Resource> call(ListView<Resource> list) {
										return new ListCell<Resource>() {
											@Override
											protected void updateItem(Resource item, boolean empty) {
												super.updateItem(item, empty);
												if (item == null) {
													setText("");
												}
												else {
													setText(item.getName());
												}
											}
										};
									}
								});
							// load the current attachments
							for (Resource resource : attachmentFolder) {
								attachmentList.getItems().add(resource);
							}
							attachmentList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Resource>() {
								@Override
								public void changed(ObservableValue<? extends Resource> arg0, Resource oldValue, Resource newValue) {
									if (newValue.getContentType().startsWith("image/")) {
										try {
											ReadableContainer<ByteBuffer> readable = ((ReadableResource) newValue).getReadable();
											try {
												Image image = new Image(IOUtils.toInputStream(readable));
												ImageView view = new ImageView(image);
												view.setPreserveRatio(true);
												view.setFitHeight(190);
												preview.getChildren().add(view);
											}
											finally {
												readable.close();
											}
										}
										catch (Exception e) {
											MainController.getInstance().notify(e);
										}
									}
									else {
										preview.getChildren().clear();
									}
								}
							});
							attachments.getChildren().addAll(attachmentList, preview);
							
							Button addAttachment = new Button("Add Attachment");
							addAttachment.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@SuppressWarnings("unchecked")
								@Override
								public void handle(ActionEvent arg0) {
									Set set = new HashSet();
									SimpleProperty<File> fileProperty = new SimpleProperty<File>("File", File.class, true);
									fileProperty.setInput(true);
									set.add(fileProperty);
									set.add(new SimpleProperty<String>("Name", String.class, false));
									SimplePropertyUpdater updater = new SimplePropertyUpdater(true, set);
									EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Add Attachment", new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent arg0) {
											File file = updater.getValue("File");
											if (file != null) {
												String name = updater.getValue("Name");
												if (name == null) {
													name = file.getName();
												}
												try {
													Resource create = ((ManageableContainer<?>) attachmentFolder).create(name, URLConnection.guessContentTypeFromName(file.getName()));
													copyFileToResource(file, create);
													attachmentList.getItems().add(create);
												}
												catch (IOException e) {
													MainController.getInstance().notify(e);
												}
											}
										}

									});
								}
							});
							
							Button deleteAttachment = new Button("Delete Attachment");
							deleteAttachment.disableProperty().bind(attachmentList.getSelectionModel().selectedItemProperty().isNull());
							deleteAttachment.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									Confirm.confirm(ConfirmType.INFORMATION, "Delete " + attachmentList.getSelectionModel().getSelectedItem().getName(), "Are you sure you want to delete this attachment? This action can not be undone", new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent arg0) {
											Resource selectedItem = attachmentList.getSelectionModel().getSelectedItem();
											try {
												((ManageableContainer<?>) attachmentFolder).delete(selectedItem.getName());
												attachmentList.getItems().remove(selectedItem);
											}
											catch (Exception e) {
												MainController.getInstance().notify(e);
											}
										}
									});
								}
							});
							
							Button editLocally = new Button("View Attachment");
							editLocally.disableProperty().bind(attachmentList.getSelectionModel().selectedItemProperty().isNull());
							editLocally.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									if (!localEditFolder.exists()) {
										localEditFolder.mkdirs();
									}
									Resource selectedItem = attachmentList.getSelectionModel().getSelectedItem();
									File target = new File(localEditFolder, selectedItem.getName());
									try {
										copyResourceToFile(selectedItem, target);
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													Desktop.getDesktop().open(target);
												}
												catch (IOException e) {
													MainController.getInstance().notify(e);
												}												
											}
										}).start();
									}
									catch (Exception e) {
										MainController.getInstance().notify(e);
									}
								}
							});
							
							buttons.getChildren().addAll(save, addPage, addAttachment, deleteAttachment);
							
							if (Desktop.isDesktopSupported()) {
								buttons.getChildren().add(editLocally);
							}
							
							vbox.getChildren().addAll(pages, attachments, buttons);
							VBox.setVgrow(pages, Priority.ALWAYS);
							VBox.setVgrow(buttons, Priority.NEVER);
							pane.getChildren().add(vbox);
							AnchorPane.setBottomAnchor(vbox, 0d);
							AnchorPane.setRightAnchor(vbox, 0d);
							AnchorPane.setTopAnchor(vbox, 0d);
							AnchorPane.setLeftAnchor(vbox, 0d);
							newTab.setContent(pane);
						}
						else {
							MainController.getInstance().activate(id);
						}
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}

				
			});
			edit.setGraphic(MainController.loadGraphic("edit-edit.png"));
			
			MenuItem delete = new MenuItem("Delete All");
			delete.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					Confirm.confirm(ConfirmType.WARNING, "Delete all documentation for: " + entry.getId(), "Are you sure you want to delete all the documentation? This action can not be undone", new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							try {
								ResourceContainer<?> container = ((ResourceEntry) entry).getContainer();
								ResourceContainer<?> protectedFolder = (ResourceContainer<?>) container.getChild(EAIResourceRepository.PROTECTED);
								if (protectedFolder != null) {
									ResourceContainer<?> documentationFolder = (ResourceContainer<?>) protectedFolder.getChild("documentation");
									if (documentationFolder != null) {
										((ManageableContainer<?>) protectedFolder).delete("documentation");
									}
								}
								TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
								if (resolve instanceof RepositoryTreeItem) {
									((RepositoryTreeItem) resolve).documentedProperty().set(false);
								}
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					});
				}
			});
			delete.setGraphic(MainController.loadGraphic("edit-delete.png"));
			
			menu.getItems().addAll(edit, delete);
			return menu;
		}
		return null;
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static VBox showDocumented(Tab parentTab, DocumentedImpl documented, Resource resource, Repository repository, EventHandler<Event> saveHandler) {
		SimpleProperty<String> titleProperty = new SimpleProperty<String>("Title", String.class, false);
		SimpleProperty tagsProperty = new SimpleProperty("Tags", String.class, false);
		tagsProperty.setList(true);
		
		SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet(Arrays.asList(titleProperty, tagsProperty)), 
			new ValueImpl<String>(titleProperty, documented.getTitle()),
			new ValueImpl(tagsProperty, documented.getTags())) {
				@Override
				public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
					if (property.getName().equals("Title")) {
						documented.setTitle(value == null ? null : value.toString());
					}
					else if (property.getName().equals("Tags")) {
						documented.setTags(value == null ? null : (Collection) value);
					}
					if (!parentTab.getText().endsWith("*")) {
						parentTab.setText(parentTab.getText() + " *");
					}
					return super.updateProperty(property, value);
				}
		};
		
		AnchorPane properties = new AnchorPane();
		MainController.getInstance().showProperties(updater, properties, true, repository, false);
		AceEditor editor = new AceEditor();
		editor.setContent("text/x-markdown", documented.getDescription());
		
		editor.subscribe(AceEditor.SAVE, saveHandler);
		editor.subscribe(AceEditor.CLOSE, new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				MainController.getInstance().close(parentTab.getId());
			}
		});
		editor.subscribe(AceEditor.CHANGE, new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				if (!parentTab.getText().endsWith("*")) {
					parentTab.setText(parentTab.getText() + " *");
				}
				// in the event itself, the latest change (e.g. type) has not yet been added, so delay the setting
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						documented.setDescription(editor.getContent());
					}
				});
			}
		});
		editor.subscribe(AceEditor.PASTE, new EventHandler<Event>() {
			@Override
			public void handle(Event arg0) {
				if (!parentTab.getText().endsWith("*")) {
					parentTab.setText(parentTab.getText() + " *");
				}
				// in the event itself, the latest change (e.g. type) has not yet been added, so delay the setting
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						documented.setDescription(editor.getContent());
					}
				});
			}
		});
		editor.setWrap(true);
		
		VBox vbox = new VBox();
		vbox.getChildren().addAll(properties, editor.getWebView());
		VBox.setVgrow(editor.getWebView(), Priority.ALWAYS);
		
		return vbox;
	}
	
	private static void newTab(Entry entry, final Tab newTab, TabPane pages, Map<Resource, DocumentedImpl> documentation, ResourceContainer<?> documentationFolder, Resource page, EventHandler<Event> saveHandler) {
		DocumentedImpl documented;
		Tab pageTab = new Tab(page.getName().replace(".md", ""));
		documented = DocumentationManager.read(page);
		documentation.put(page, documented);
		pageTab.setContent(showDocumented(newTab, documented, page, entry.getRepository(), saveHandler));
		pageTab.setOnCloseRequest(new EventHandler<Event>() {
			@Override
			public void handle(Event event) {
				// always consume the event to prevent "normal" closure
				event.consume();
				Confirm.confirm(ConfirmType.QUESTION, "Delete " + pageTab.getText(), "Are you sure you want to delete this page? This action can not be undone", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						try {
							((ManageableContainer<?>) documentationFolder).delete(page.getName());
							documentation.remove(page);
							pages.getTabs().remove(pageTab);
						}
						catch (IOException e) {
							MainController.getInstance().notify(e);
						}
					}
				});
			}
		});
		pages.getTabs().add(pageTab);
	}
	
	private static void copyFileToResource(File file, Resource create) throws IOException, FileNotFoundException {
		WritableContainer<ByteBuffer> output = ((WritableResource) create).getWritable();
		try {
			InputStream input = new BufferedInputStream(new FileInputStream(file));
			try {
				IOUtils.copyBytes(IOUtils.wrap(input), output);
			}
			finally {
				input.close();
			}
		}
		finally {
			output.close();
		}
	}
	
	private static void copyResourceToFile(Resource create, File file) throws IOException, FileNotFoundException {
		ReadableContainer<ByteBuffer> input = ((ReadableResource) create).getReadable();
		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			try {
				IOUtils.copyBytes(input, IOUtils.wrap(output));
			}
			finally {
				output.close();
			}
		}
		finally {
			input.close();
		}
	}
}
