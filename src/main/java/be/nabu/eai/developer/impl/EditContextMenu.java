package be.nabu.eai.developer.impl;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;

import java.util.Date;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.components.RepositoryBrowser.RepositoryTreeItem;
import be.nabu.eai.repository.EAINode;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeCellValueLabel;
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
			
			// only matters if you can persist it
			if (entry instanceof RepositoryEntry && entry.getNode() instanceof EAINode) {
				if (entry.getNode().getDeprecated() != null) {
					MenuItem undeprecate = new MenuItem("Undo Deprecate");
					undeprecate.setGraphic(MainController.loadFixedSizeGraphic("deprecated.png", 16));
					undeprecate.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							((EAINode) entry.getNode()).setDeprecated(null);
							((RepositoryEntry) entry).saveNode();
//							MainController.getInstance().getRepositoryBrowser().refresh();
							TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
							if (resolve instanceof RepositoryTreeItem) {
								((RepositoryTreeItem) resolve).deprecatedProperty().set(null);
							}
						}
					});
					menu.getItems().add(undeprecate);
				}
				else {
					MenuItem deprecate = new MenuItem("Deprecate");
					deprecate.setGraphic(MainController.loadFixedSizeGraphic("deprecated.png", 16));
					deprecate.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							((EAINode) entry.getNode()).setDeprecated(new Date());
							((RepositoryEntry) entry).saveNode();
//							MainController.getInstance().getRepositoryBrowser().refresh();
							TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
							if (resolve instanceof RepositoryTreeItem) {
								((RepositoryTreeItem) resolve).deprecatedProperty().set(new Date());
							}
						}
					});
					menu.getItems().add(deprecate);
				}
			}
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
			
			MenuItem rename = new MenuItem("Rename");
			rename.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
					if (resolve != null) {
						TreeCell<Entry> treeCell = MainController.getInstance().getTree().getTreeCell(resolve);
						if (treeCell != null && treeCell.getCellValue() instanceof TreeCellValueLabel) {
							((TreeCellValueLabel<?>) treeCell.getCellValue()).edit();
						}
					}
				}
			});
			rename.setGraphic(MainController.loadGraphic("edit-edit.png"));
			menu.getItems().add(rename);
		}
		return menu.getItems().isEmpty() ? null : menu;
	}

}
