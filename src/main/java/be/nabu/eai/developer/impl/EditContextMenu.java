package be.nabu.eai.developer.impl;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeItem;

public class EditContextMenu implements EntryContextMenuProvider {

	@Override
	public MenuItem getContext(Entry entry) {
		Menu menu = new Menu("Edit");
		
		if (entry.isNode()) {
			MenuItem open = new MenuItem("Open");
			open.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					RepositoryBrowser.open(MainController.getInstance(), entry);
				}
			});
			open.setGraphic(MainController.loadGraphic("edit-open.png"));
			menu.getItems().add(open);
			MenuItem copy = new MenuItem("Copy");
			copy.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						MainController.copy(entry.getNode().getArtifact());
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			copy.setGraphic(MainController.loadGraphic("edit-copy.png"));
			menu.getItems().add(copy);
		}
		else if (entry instanceof ResourceEntry) {
			MenuItem paste = new MenuItem("Paste");
			paste.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						MainController.getInstance().getTree().getClipboardHandler().setClipboard(Clipboard.getSystemClipboard());
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			paste.setGraphic(MainController.loadGraphic("edit-paste.png"));
			menu.getItems().add(paste);
		}
		
		if (entry.getParent() != null && entry instanceof ResourceEntry) {
			MenuItem delete = new MenuItem("Delete");
			delete.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
					if (resolve instanceof RemovableTreeItem) {
						((RemovableTreeItem<Entry>) resolve).remove();
					}
				}
			});
			delete.setGraphic(MainController.loadGraphic("edit-delete.png"));
			menu.getItems().add(delete);
		}
		return menu.getItems().isEmpty() ? null : menu;
	}

}
