package be.nabu.eai.developer.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.components.RepositoryBrowser.RepositoryTreeItem;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.DocumentationManager;
import be.nabu.eai.repository.DocumentationManager.DocumentedImpl;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.jfx.control.ace.AceEditor;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;

public class DocumentationContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		if (entry instanceof ResourceEntry) {
			Menu menu = new Menu("Documentation");
			MenuItem edit = new MenuItem("Edit");
			edit.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@SuppressWarnings({ "unchecked", "rawtypes" })
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
							final Tab newTab = MainController.getInstance().newTab(id);
							AnchorPane pane = new AnchorPane();
							VBox vbox = new VBox();
							
							DocumentedImpl documented = DocumentationManager.read(readme);
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
										if (!newTab.getText().endsWith("*")) {
											newTab.setText(newTab.getText() + " *");
										}
										return super.updateProperty(property, value);
									}
							};
							
							AnchorPane properties = new AnchorPane();
							MainController.getInstance().showProperties(updater, properties, true, entry.getRepository(), false);
							
							AceEditor editor = new AceEditor();
							editor.setContent("text/x-markdown", documented.getDescription());
							
							HBox buttons = new HBox();
							Button save = new Button("Save");
							save.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									documented.setDescription(editor.getContent());
									DocumentationManager.write(readme, documented);
									// remove trailing *
									if (newTab.getText().endsWith("*")) {
										newTab.setText(newTab.getText().replace(" *", ""));
									}
								}
							});
							editor.subscribe(AceEditor.SAVE, new EventHandler<Event>() {
								@Override
								public void handle(Event arg0) {
									documented.setDescription(editor.getContent());
									DocumentationManager.write(readme, documented);
									// remove trailing *
									if (newTab.getText().endsWith("*")) {
										newTab.setText(newTab.getText().replace(" *", ""));
									}
								}
							});
							editor.subscribe(AceEditor.CLOSE, new EventHandler<Event>() {
								@Override
								public void handle(Event arg0) {
									MainController.getInstance().close(id);
								}
							});
							editor.subscribe(AceEditor.CHANGE, new EventHandler<Event>() {
								@Override
								public void handle(Event arg0) {
									if (!newTab.getText().endsWith("*")) {
										newTab.setText(newTab.getText() + " *");
									}
								}
							});
							editor.setWrap(true);
							buttons.getChildren().addAll(save);
							vbox.getChildren().addAll(properties, editor.getWebView(), buttons);
							VBox.setVgrow(editor.getWebView(), Priority.ALWAYS);
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
			
			MenuItem delete = new MenuItem("Delete");
			delete.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
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
			delete.setGraphic(MainController.loadGraphic("edit-delete.png"));
			
			menu.getItems().addAll(edit, delete);
			return menu;
		}
		return null;
	}
	
}
