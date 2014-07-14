package be.nabu.eai.developer.components;

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
import javafx.scene.Node;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.base.BaseComponent;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeItem;

public class RepositoryBrowser extends BaseComponent<MainController, Tree<RepositoryEntry>> {

	@Override
	protected void initialize(Tree<RepositoryEntry> control) {
		control.rootProperty().set(new RepositoryTreeItem(getController(), null, getController().getRepository().getRoot(), false));
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
