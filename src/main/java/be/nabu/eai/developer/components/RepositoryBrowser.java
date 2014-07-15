package be.nabu.eai.developer.components;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.base.BaseComponent;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;

public class RepositoryBrowser extends BaseComponent<MainController, Tree<RepositoryEntry>> {

	@Override
	protected void initialize(final Tree<RepositoryEntry> tree) {
		tree.rootProperty().set(new RepositoryTreeItem(getController(), null, getController().getRepository().getRoot(), false));
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tree.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				List<TreeCell<RepositoryEntry>> selected = tree.getSelectionModel().getSelectedItems();
				if (event.getButton().equals(MouseButton.SECONDARY)) {
					// if you have selected one, show the contextual menu for that type
					if (selected.size() == 1) {
						ContextMenu menu = new SingleRightClickMenu().buildMenu(getController(), selected.get(0).getItem());
						tree.setContextMenu(menu);
						tree.getContextMenu().show(getController().getStage(), event.getScreenX(), event.getScreenY());
						// need to actually _remove_ the context menu on action
						// otherwise by default (even if not in this if), the context menu will be shown if you right click
						// this means if you select a folder, right click, you get this menu, you then select a non-folder and right click, you don't enter this code but still see the context menu!
						tree.getContextMenu().addEventHandler(ActionEvent.ACTION, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								tree.setContextMenu(null);
							}
						});
					}
					// otherwise, show the contextual menu for multiple operations
					else {
						
					}
				}
				else if (event.getClickCount() == 2 && selected.size() > 0) {
					if (!getController().activate(selected.get(0).getItem().itemProperty().get().getId())) {
						ArtifactGUIManager<?> manager = getController().getGUIManager(selected.get(0).getItem().itemProperty().get().getNode().getArtifactClass());
						try {
							getController().register(manager.view(getController(), selected.get(0).getItem()));
						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}
						catch (ParseException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		});

	}
	
	public void refresh() {
		((RepositoryTreeItem) getControl().rootProperty().get()).refresh();
	}
	
	public static class RepositoryTreeItem implements TreeItem<RepositoryEntry> {
		
		private ObjectProperty<RepositoryEntry> itemProperty;
		private BooleanProperty editableProperty, leafProperty;
		private TreeItem<RepositoryEntry> parent;
		private ObservableList<TreeItem<RepositoryEntry>> children;
		private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
		private boolean isNode = false;
		private MainController controller;
		
		public RepositoryTreeItem(MainController controller, TreeItem<RepositoryEntry> parent, RepositoryEntry entry, boolean isNode) {
			this.controller = controller;
			this.parent = parent;
			itemProperty = new SimpleObjectProperty<RepositoryEntry>(entry);
			editableProperty = new SimpleBooleanProperty(entry.isEditable());
			leafProperty = new SimpleBooleanProperty(entry.isLeaf());
			this.isNode = isNode;
			if (isNode) {
				graphicProperty.set(controller.getGUIManager(entry.getNode().getArtifactClass()).getGraphic());
			}
			else {
				graphicProperty.set(MainController.loadGraphic("folder.png"));
			}
		}
		
		@Override
		public BooleanProperty editableProperty() {
			return editableProperty;
		}

		@Override
		public String getName() {
			return itemProperty.get().getName();
		}

		@Override
		public ObjectProperty<Node> graphicProperty() {
			return graphicProperty;
		}

		@Override
		public BooleanProperty leafProperty() {
			return leafProperty;
		}

		@Override
		public ObservableList<TreeItem<RepositoryEntry>> getChildren() {
			if (children == null) {
				children = FXCollections.observableArrayList(loadChildren());
			}
			return children;
		}
		
		private List<TreeItem<RepositoryEntry>> loadChildren() {
			List<TreeItem<RepositoryEntry>> items = new ArrayList<TreeItem<RepositoryEntry>>();
			for (RepositoryEntry entry : itemProperty.get()) {
				// if the non-leaf is a repository, it will not be shown as a dedicated map
				if (!entry.isLeaf() && (!entry.isNode() || !Repository.class.isAssignableFrom(entry.getNode().getArtifactClass()))) {
					items.add(new RepositoryTreeItem(controller, this, entry, false));
				}
				// for nodes we add two entries: one for the node, and one for the folder
				if (entry.isNode()) {
					items.add(new RepositoryTreeItem(controller, this, entry, true));	
				}
			}
			Collections.sort(items, new Comparator<TreeItem<RepositoryEntry>>() {
				@Override
				public int compare(TreeItem<RepositoryEntry> arg0, TreeItem<RepositoryEntry> arg1) {
					RepositoryTreeItem item1 = (RepositoryTreeItem) arg0;
					RepositoryTreeItem item2 = (RepositoryTreeItem) arg1;
					if (item1.isNode && !item2.isNode) {
						return 1;
					}
					else if (!item1.isNode && item2.isNode) {
						return -1;
					}
					else {
						return item1.getName().compareTo(item2.getName());
					}
				}
			});
			return items;
		}
		public void refresh() {
			getChildren().clear();
			getChildren().addAll(loadChildren());
			for (TreeItem<RepositoryEntry> child : getChildren()) {
				((RepositoryTreeItem) child).refresh();
			}
		}

		@Override
		public TreeItem<RepositoryEntry> getParent() {
			return parent;
		}

		@Override
		public ObjectProperty<RepositoryEntry> itemProperty() {
			return itemProperty;
		}

		public boolean isNode() {
			return isNode;
		}
	}
}
