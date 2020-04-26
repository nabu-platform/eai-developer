package be.nabu.eai.developer.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.components.RepositoryBrowser.RepositoryTreeItem;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;
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
				Collections.sort(nodeDependencies);
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
			// can't deal with null when sorting...
			if (nodeReferences != null) {
				Iterator<String> iterator = nodeReferences.iterator();
				while (iterator.hasNext()) {
					if (iterator.next() == null) {
						iterator.remove();
					}
				}
			}
			if (nodeReferences != null && !nodeReferences.isEmpty()) {
				Collections.sort(nodeReferences);
				final Set<String> brokenReferences = new TreeSet<String>();
				// hardcoded for references
				Menu references = new Menu("References");
				for (String nodeReference : nodeReferences) {
					if (nodeReference != null) {
						if (controller.isBrokenReference(nodeReference)) {
							brokenReferences.add(nodeReference);
						}
						final Tab tab = MainController.getInstance().getTab(nodeReference);
						MenuItem reference = new MenuItem((tab == null ? "" : "* ") + nodeReference);
						reference.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								if (tab == null) {
									TreeItem<Entry> resolve = controller.getTree().resolve(nodeReference.replace('.', '/'), false);
									// if we found the reference, open it in a new tab
									if (resolve != null) {
										RepositoryBrowser.open(controller, resolve);
									}
									else {
										controller.notify(new ValidationMessage(Severity.WARNING, "Could not find '" + nodeReference + "' in tree"));
									}
								}
								else {
									tab.getTabPane().getSelectionModel().select(tab);
								}
							}
						});
						references.getItems().add(reference);
					}
					else {
						System.out.println("Found null reference for " + entry.itemProperty().get().getId() + ": "  + nodeReferences);
					}
				}
				node.getItems().add(references);
				if (!brokenReferences.isEmpty()) {
					Menu broken = new Menu("Broken References");
					for (String reference : brokenReferences) {
						MenuItem item = new MenuItem(reference);
						broken.getItems().add(item);
					}
					node.getItems().add(broken);
					// allow for a tab to fix broken references
					MenuItem item = new MenuItem("Fix...");
					item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							Tab tab = controller.newTab("Fix: " + entry.itemProperty().get().getId());
							AnchorPane pane = new AnchorPane();
							List<Value<?>> values = new ArrayList<Value<?>>();
							Set<Property<?>> properties = new LinkedHashSet<Property<?>>();
							for (String reference : brokenReferences) {
								SimpleProperty<String> property = new SimpleProperty<String>(reference, String.class, true);
								properties.add(property);
								values.add(new ValueImpl<String>(property, reference));
							}
							VBox box = new VBox();
							SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties, values.toArray(new Value<?>[0]));
							MainController.getInstance().showProperties(updater, box, false);
							pane.getChildren().add(box);
							AnchorPane.setTopAnchor(box, 0d);
							AnchorPane.setRightAnchor(box, 0d);
							AnchorPane.setBottomAnchor(box, 0d);
							AnchorPane.setLeftAnchor(box, 0d);
							Button persist = new Button("Update References");
							persist.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									for (String reference : brokenReferences) {
										String value = updater.getValue(reference);
										if (value != null && !value.isEmpty()) {
											try {
												MainController.updateReference(entry.itemProperty().get(), reference, value);
											}
											catch (Exception e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
									persist.setDisable(true);
								}
							});
							box.getChildren().add(persist);
							tab.setContent(pane);
						}
					});
					node.getItems().add(item);
				}
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
			createDirectory.setGraphic(MainController.loadGraphic("folder.png"));
			createDirectory.addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(Arrays.asList(
						new SimpleProperty<String>("Name", String.class, true)
					)));
					EAIDeveloperUtils.buildPopup(controller, updater, "Create New Folder", new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							String name = updater.getValue("Name");
							if (name != null) {
								try {
									if (((RepositoryEntry) entry.itemProperty().get()).getContainer().getChild(name) != null) {
										throw new IOException("A folder or artifact with that name already exists");
									}
									RepositoryEntry newEntry = ((RepositoryEntry) entry.itemProperty().get()).createDirectory(name);
									
//									TreeItem<Entry> parentTreeItem = controller.getRepositoryBrowser().getControl().resolve(entry.itemProperty().get().getId().replace(".", "/"));
									// @optimize
									TreeCell<Entry> treeCell = controller.getRepositoryBrowser().getControl().getTreeCell(entry);
									if (treeCell != null) {
										treeCell.refresh();
									}
									else {
										controller.getRepositoryBrowser().refresh();
									}
									try {
										MainController.getInstance().getAsynchronousRemoteServer().reload(newEntry.getId());
									}
									catch (Exception e) {
										e.printStackTrace();
									}
									MainController.getInstance().getCollaborationClient().created(newEntry.getId(), "Created folder");
								}
								catch (IOException e) {
									controller.notify(new ValidationMessage(Severity.ERROR, "Cannot create a directory by the name of '" + name + "': " + e.getMessage()));
								}
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
				HBox box = new HBox();
				box.setAlignment(Pos.CENTER);
				box.setMinWidth(25);
				box.setMaxWidth(25);
				box.setPrefWidth(25);
				box.getChildren().add(handler.getGraphic());
				item.setGraphic(box);
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
				// remove the "root" menu, it should not be readded
				subMenus.remove(null);
				List<Menu> menus = new ArrayList<Menu>(subMenus.values());
				Collections.sort(menus, new Comparator<Menu>() {
					@Override
					public int compare(Menu o1, Menu o2) {
						return o1.getText().compareTo(o2.getText());
					}
				});
				for (Menu subMenu : menus) {
					sort(subMenu.getItems());
					create.getItems().add(subMenu);
				}
			}
			menu.getItems().add(create);
		}
		// load external menus
		try {
			for (Class<?> provider : EAIRepositoryUtils.getImplementationsFor(EntryContextMenuProvider.class)) {
				EntryContextMenuProvider newInstance = (EntryContextMenuProvider) provider.newInstance();
				MenuItem context = newInstance.getContext(entry.itemProperty().get());
				if (context != null) {
//					menu.getItems().add(context);
					// merge by name
					merge(menu.getItems(), context);
				}
			}
		}
		catch (Exception e) {
			logger.error("Could not load external context menus", e);
		}
		sort(menu.getItems());
		return menu;
	}
	
	private void merge(List<MenuItem> items, MenuItem item) {
		boolean found = false;
		for (MenuItem existing : items) {
			if (existing.getText().equals(item.getText()) && item instanceof Menu && existing instanceof Menu) {
				found = true;
				for (MenuItem childItem : ((Menu) item).getItems()) {
					merge(((Menu) existing).getItems(), childItem);
				}
			}
		}
		if (!found) {
			items.add(item);
			// if we are mixing multiple menus, reorder
			sort(items);
		}
	}

	private static void sort(List<MenuItem> items) {
		Collections.sort(items, new Comparator<MenuItem>() {
			@Override
			public int compare(MenuItem o1, MenuItem o2) {
				if (o1 == null || o1.getText() == null || o2 == null || o2.getText() == null) {
					return 0;
				}
				return o1.getText().compareTo(o2.getText());
			}
		});
	}
}
