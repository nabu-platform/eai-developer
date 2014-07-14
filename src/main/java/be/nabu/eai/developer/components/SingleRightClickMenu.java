package be.nabu.eai.developer.components;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.components.RepositoryBrowser.RepositoryTreeItem;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;

public class SingleRightClickMenu {
	
	public ContextMenu buildMenu(final MainController controller, final TreeItem<RepositoryEntry> entry) {
		ContextMenu menu = new ContextMenu();
		if (((RepositoryTreeItem) entry).isNode()) {
			
		}
		else {
			Menu create = new Menu("Create");
			for (final ArtifactGUIManager<?> handler : controller.getNodeHandlers()) {
				MenuItem item = new MenuItem(handler.getArtifactName());
				item.setGraphic(handler.getGraphic());
				item.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						try {
							handler.create(controller, entry);
						}
						catch (IOException e) {
							throw new RuntimeException(e);
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
