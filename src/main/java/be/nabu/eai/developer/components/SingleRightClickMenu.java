package be.nabu.eai.developer.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashSet;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseEvent;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.components.RepositoryBrowser.RepositoryTreeItem;
import be.nabu.eai.developer.managers.JDBCServiceGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class SingleRightClickMenu {
	
	public ContextMenu buildMenu(final MainController controller, final TreeItem<Entry> entry) {
		ContextMenu menu = new ContextMenu();
		if (((RepositoryTreeItem) entry).isNode()) {
			
		}
		// only make the repository entries editable
		else if (entry.itemProperty().get() instanceof RepositoryEntry) {
			Menu create = new Menu("Create");
			
			// hardcoded for directory
			MenuItem createDirectory = new MenuItem("Folder");
			createDirectory.setGraphic(MainController.loadGraphic("folder2.png"));
			createDirectory.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(
						new SimpleProperty<String>("Name", String.class)
					)));
					JDBCServiceGUIManager.buildPopup(controller, updater, "New Folder", new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent arg0) {
							String name = updater.getValue("Name");
							try {
								((RepositoryEntry) entry.itemProperty().get()).createDirectory(name);
								controller.getRepositoryBrowser().refresh();
							}
							catch (IOException e) {
								controller.notify(new ValidationMessage(Severity.ERROR, "Cannot create a directory by the name of '" + name + "': " + e.getMessage()));
							}
						}
					});
				}
			});
			create.getItems().addAll(createDirectory, new SeparatorMenuItem());
			
			for (final ArtifactGUIManager<?> handler : controller.getGUIManagers()) {
				if (handler.getArtifactManager() == null) {
					continue;
				}
				MenuItem item = new MenuItem(handler.getArtifactName());
				item.setGraphic(handler.getGraphic());
				item.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						try {
							controller.register(handler.create(controller, entry));
						}
						catch (IOException e) {
							controller.notify(new ValidationMessage(Severity.ERROR, "Cannot create the node: " + e.getMessage()));
						}
					}
				});
				create.getItems().add(item);
			}
			menu.getItems().add(create);
		}
		return menu;
	}
}
