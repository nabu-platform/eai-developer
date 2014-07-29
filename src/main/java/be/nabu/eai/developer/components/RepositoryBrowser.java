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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.base.BaseComponent;
import be.nabu.eai.developer.managers.util.RemoveTreeContextMenu;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDragListener;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.api.DefinedType;

public class RepositoryBrowser extends BaseComponent<MainController, Tree<Entry>> {

	@Override
	protected void initialize(final Tree<Entry> tree) {
		RemoveTreeContextMenu.removeOnHide(tree);
		tree.rootProperty().set(new RepositoryTreeItem(getController(), null, getController().getRepository().getRoot(), false));
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				if (arg0.getCode() == KeyCode.F5) {
					tree.getTreeCell(tree.rootProperty().get()).refresh();
				}
			}
		});
		tree.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				List<TreeCell<Entry>> selected = tree.getSelectionModel().getSelectedItems();
				if (event.getButton().equals(MouseButton.SECONDARY)) {
					// if you have selected one, show the contextual menu for that type
					// currently only resource entries can be added to, so for example if you have a memory resource, you can't add to it
					if (selected.size() == 1 && selected.get(0).getItem().itemProperty().get() instanceof ResourceEntry) {
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
		TreeDragDrop.makeDraggable(tree, new TreeDragListener<Entry>() {
			@Override
			public boolean canDrag(TreeCell<Entry> arg0) {
				return arg0.getItem().leafProperty().get();
			}
			@Override
			public void drag(TreeCell<Entry> arg0) {
				// do nothing
			}
			@Override
			public String getDataType(TreeCell<Entry> arg0) {
				return RepositoryBrowser.getDataType(arg0.getItem().itemProperty().get().getNode().getArtifactClass());
			}
			@Override
			public TransferMode getTransferMode() {
				return TransferMode.MOVE;
			}
			@Override
			public void stopDrag(TreeCell<Entry> arg0, boolean arg1) {
				// do nothing
			}
		});
	}
	
	public static String getDataType(Class<? extends Artifact> clazz) {
		if (DefinedService.class.isAssignableFrom(clazz)) {
			clazz = DefinedService.class;
		}
		else if (DefinedType.class.isAssignableFrom(clazz)) {
			clazz = DefinedType.class;
		}
		return "repository-" + clazz.getName();
	}
	
	public void refresh() {
		((RepositoryTreeItem) getControl().rootProperty().get()).refresh();
	}
	
	public static class RepositoryTreeItem implements TreeItem<Entry> {
		
		private ObjectProperty<Entry> itemProperty;
		private BooleanProperty editableProperty, leafProperty;
		private TreeItem<Entry> parent;
		private ObservableList<TreeItem<Entry>> children;
		private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
		private boolean isNode = false;
		private MainController controller;
		
		public RepositoryTreeItem(MainController controller, TreeItem<Entry> parent, Entry entry, boolean isNode) {
			this.controller = controller;
			this.parent = parent;
			itemProperty = new SimpleObjectProperty<Entry>(entry);
			editableProperty = new SimpleBooleanProperty(entry.isEditable());
			// if this is the "node view" of the entry, it's always a leaf (folder is created with children if necessary)
			leafProperty = new SimpleBooleanProperty(entry.isLeaf() || isNode);
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
		public ObservableList<TreeItem<Entry>> getChildren() {
			if (children == null) {
				children = FXCollections.observableArrayList(loadChildren());
			}
			return children;
		}
		
		private List<TreeItem<Entry>> loadChildren() {
			List<TreeItem<Entry>> items = new ArrayList<TreeItem<Entry>>();
			// for nodes we have created a duplicate map entry so don't recurse!
			if (isNode) {
				return items;
			}
			for (Entry entry : itemProperty.get()) {
				// if the non-leaf is a repository, it will not be shown as a dedicated map
				if (!entry.isLeaf() && (!entry.isNode() || !Repository.class.isAssignableFrom(entry.getNode().getArtifactClass()))) {
					items.add(new RepositoryTreeItem(controller, this, entry, false));
				}
				// for nodes we add two entries: one for the node, and one for the folder
				if (entry.isNode()) {
					items.add(new RepositoryTreeItem(controller, this, entry, true));	
				}
			}
			Collections.sort(items, new Comparator<TreeItem<Entry>>() {
				@Override
				public int compare(TreeItem<Entry> arg0, TreeItem<Entry> arg1) {
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
		@Override
		public void refresh() {
			getChildren().clear();
			getChildren().addAll(loadChildren());
			for (TreeItem<Entry> child : getChildren()) {
				((RepositoryTreeItem) child).refresh();
			}
		}

		@Override
		public TreeItem<Entry> getParent() {
			return parent;
		}

		@Override
		public ObjectProperty<Entry> itemProperty() {
			return itemProperty;
		}

		public boolean isNode() {
			return isNode;
		}
	}
}
