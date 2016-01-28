package be.nabu.eai.developer.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
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
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public ContextMenu buildMenu(final MainController controller, final TreeItem<Entry> entry) {
		ContextMenu menu = new ContextMenu();
		if (((RepositoryTreeItem) entry).isNode()) {
//			Menu node = new Menu(controller.getGUIManager(((RepositoryTreeItem) entry).itemProperty().get().getNode().getArtifactClass()).getArtifactName());
			Menu node = new Menu("Links");
			List<String> nodeDependencies = controller.getRepository().getDependencies(entry.itemProperty().get().getId());
			if (nodeDependencies != null && !nodeDependencies.isEmpty()) {
				// hardcoded for dependencies
				Menu dependencies = new Menu("Dependencies");
				for (String nodeDependency : nodeDependencies) {
					final Tab tab = MainController.getInstance().getTab(nodeDependency);
					MenuItem dependency = new MenuItem((tab == null ? "" : "* ") + nodeDependency);
					dependency.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							if (tab == null) {
								RepositoryBrowser.open(controller, controller.getTree().resolve(nodeDependency.replace('.', '/')));
							}
							else {
								tab.getTabPane().getSelectionModel().select(tab);
							}
						}
					});
					dependencies.getItems().add(dependency);
				}
				node.getItems().add(dependencies);
			}
			List<String> nodeReferences = controller.getRepository().getReferences(entry.itemProperty().get().getId());
			if (nodeReferences != null && !nodeReferences.isEmpty()) {
				// hardcoded for references
				Menu references = new Menu("References");
				for (String nodeReference : nodeReferences) {
					final Tab tab = MainController.getInstance().getTab(nodeReference);
					MenuItem reference = new MenuItem((tab == null ? "" : "* ") + nodeReference);
					reference.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							if (tab == null) {
								RepositoryBrowser.open(controller, controller.getTree().resolve(nodeReference.replace('.', '/')));
							}
							else {
								tab.getTabPane().getSelectionModel().select(tab);
							}
						}
					});
					references.getItems().add(reference);
				}
				node.getItems().add(references);
			}
			if (!node.getItems().isEmpty()) {
				menu.getItems().add(node);
			}
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
						new SimpleProperty<String>("Name", String.class, true)
					)));
					JDBCServiceGUIManager.buildPopup(controller, updater, "Create New Folder", new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent arg0) {
							String name = updater.getValue("Name");
							try {
								RepositoryEntry newEntry = ((RepositoryEntry) entry.itemProperty().get()).createDirectory(name);
								controller.getRepositoryBrowser().refresh();
								try {
									MainController.getInstance().getServer().getRemote().reload(newEntry.getId());
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
							catch (IOException e) {
								controller.notify(new ValidationMessage(Severity.ERROR, "Cannot create a directory by the name of '" + name + "': " + e.getMessage()));
							}
						}
					});
				}
			});
			create.getItems().addAll(createDirectory, new SeparatorMenuItem());

			Map<String, Menu> subMenus = new HashMap<String, Menu>();
			subMenus.put(null, create);
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
							handler.create(controller, entry);
						}
						catch (IOException e) {
							controller.notify(new ValidationMessage(Severity.ERROR, "Cannot create the node: " + e.getMessage()));
						}
					}
				});
				if (!subMenus.containsKey(handler.getCategory())) {
					subMenus.put(handler.getCategory(), new Menu(handler.getCategory()));
				}
				subMenus.get(handler.getCategory()).getItems().add(item);
			}
			// after all the non-categorized (if any remain), add the categorized
			if (subMenus.size() > 1) {
				create.getItems().add(new SeparatorMenuItem());
				for (String category : subMenus.keySet()) {
					if (category == null) {
						continue;
					}
					create.getItems().add(subMenus.get(category));
				}
			}
			menu.getItems().add(create);
		}
		// load external menus
		try {
			for (Class<?> provider : MainController.getInstance().getRepository().getImplementationsFor(EntryContextMenuProvider.class)) {
				EntryContextMenuProvider newInstance = (EntryContextMenuProvider) provider.newInstance();
				MenuItem context = newInstance.getContext(entry.itemProperty().get());
				if (context != null) {
					menu.getItems().add(context);
				}
			}
		}
		catch (Exception e) {
			logger.error("Could not load external context menus", e);
		}
		Collections.sort(menu.getItems(), new Comparator<MenuItem>() {
			@Override
			public int compare(MenuItem o1, MenuItem o2) {
				return o1.getText().compareTo(o2.getText());
			}
		});
		
		return menu;
	}
}
