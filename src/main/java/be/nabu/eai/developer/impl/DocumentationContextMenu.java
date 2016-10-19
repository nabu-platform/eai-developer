package be.nabu.eai.developer.impl;

import java.io.IOException;
import java.nio.charset.Charset;

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
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.jfx.control.ace.AceEditor;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.WritableResource;
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
				@Override
				public void handle(ActionEvent event) {
					try {
						boolean rebuildGraphic = false;
						ResourceContainer<?> container = ((ResourceEntry) entry).getContainer();
						ResourceContainer<?> protectedFolder = (ResourceContainer<?>) container.getChild(EAIResourceRepository.PROTECTED);
						if (protectedFolder == null) {
							protectedFolder = (ResourceContainer<?>) ((ManageableContainer<?>) container).create(EAIResourceRepository.PROTECTED, Resource.CONTENT_TYPE_DIRECTORY);
							rebuildGraphic = true;
						}
						ResourceContainer<?> documentationFolder = (ResourceContainer<?>) protectedFolder.getChild("documentation");
						if (documentationFolder == null) {
							documentationFolder = (ResourceContainer<?>) ((ManageableContainer<?>) protectedFolder).create("documentation", Resource.CONTENT_TYPE_DIRECTORY);
							rebuildGraphic = true;
						}
						Resource readme = documentationFolder.getChild("readme.md");
						if (readme == null) {
							readme = ((ManageableContainer<?>) documentationFolder).create("readme.md", "text/plain");
						}
						if (rebuildGraphic) {
							TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
							if (resolve instanceof RepositoryTreeItem) {
								((RepositoryTreeItem) resolve).documentedProperty().set(true);
							}
						}
						final String id = entry.getId() + " (readme.md)";
						Tab tab = MainController.getInstance().getTab(id);
						if (tab == null) {
							final Tab newTab = MainController.getInstance().newTab(id);
							AnchorPane pane = new AnchorPane();
							VBox vbox = new VBox();
							
							byte [] content;
							ReadableContainer<ByteBuffer> readable = ((ReadableResource) readme).getReadable();
							try {
								content = IOUtils.toBytes(readable);
							}
							finally {
								readable.close();
							}
							AceEditor editor = new AceEditor();
							editor.setContent("text/x-markdown", new String(content, "UTF-8"));
							
							HBox buttons = new HBox();
							Button save = new Button("Save");
							final Resource finalReadme = readme;
							save.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									save(finalReadme, editor.getContent());
									// remove trailing *
									if (newTab.getText().endsWith("*")) {
										newTab.setText(newTab.getText().replace(" *", ""));
									}
								}
							});
							editor.subscribe(AceEditor.SAVE, new EventHandler<Event>() {
								@Override
								public void handle(Event arg0) {
									save(finalReadme, editor.getContent());
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
							vbox.getChildren().addAll(editor.getWebView(), buttons);
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
	
	private static void save(Resource resource, String content) {
		try {
			WritableContainer<ByteBuffer> writable = ((WritableResource) resource).getWritable();
			try {
				writable.write(IOUtils.wrap(content.getBytes(Charset.forName("UTF-8")), true));
			}
			finally {
				writable.close();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
