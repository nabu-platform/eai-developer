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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.base.BaseComponent;
import be.nabu.eai.developer.managers.util.RemoveTreeContextMenu;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ExtensibleEntry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.eai.server.ServerConnection;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.clipboard.ClipboardHandler;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDragListener;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.utils.mime.impl.FormatException;

/**
 * TODO: the ArtifactGUIManager assumes that a TreeItem is given though it would be better to simply give the entry
 * Currently you need to roundtrip the tree and no one actually needs the tree entry, only ever the actual entry
 */
public class RepositoryBrowser extends BaseComponent<MainController, Tree<Entry>> {

	private Logger logger = LoggerFactory.getLogger(getClass());
	private ServerConnection server;
	
	public RepositoryBrowser(ServerConnection server) {
		this.server = server;
	}

	@Override
	protected void initialize(final Tree<Entry> tree) {
		RemoveTreeContextMenu.removeOnHide(tree);
		tree.rootProperty().set(new RepositoryTreeItem(getController(), null, getController().getRepository().getRoot(), false));
		for (TreeItem<Entry> child : tree.rootProperty().get().getChildren()) {			
			tree.getTreeCell(child).collapseAll();
		}
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		// if you select a node, there may be properties that require updating
		tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeCell<Entry>>() {
			@Override
			public void changed(ObservableValue<? extends TreeCell<Entry>> observable, TreeCell<Entry> oldValue, TreeCell<Entry> newValue) {
				if (newValue != null) {
					Entry entry = newValue.getItem().itemProperty().get();
					// for a service, we can only set additional properties if it has a node.xml to store them in which should be for all resource entries
					// this means we can _not_ set additional properties on memory based entries like wsdl services and the like, if you want that you will need to wrap them unfortunately
					if (entry instanceof ResourceEntry && entry.isNode() && Service.class.isAssignableFrom(entry.getNode().getArtifactClass())) {
						
					}
				}
			}
		});
		tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				if (arg0.getCode() == KeyCode.F5) {
					// do a reload first to pick up any changes, use ctrl to skip reload and just refresh the tree
					if (!arg0.isControlDown()) {
						for (TreeCell<Entry> selected : tree.getSelectionModel().getSelectedItems()) {
							getController().getRepository().reload(selected.getItem().itemProperty().get().getId());
						}
						getController().refresh();
					}
					for (TreeCell<Entry> child : tree.getSelectionModel().getSelectedItems()) {
						child.refresh();
						// attempt remote refresh
						try {
							server.getRemote().reload(child.getItem().itemProperty().get().getId());
						}
						catch (Exception e) {
							logger.error("Could not refresh " + child.getItem().itemProperty().get().getId() + " remotely", e);
						}
					}
				}
			}
		});
		tree.setClipboardHandler(new ClipboardHandler() {
			@Override
			public ClipboardContent getContent() {
				List<Artifact> artifacts = new ArrayList<Artifact>();
				for (TreeCell<Entry> entry : tree.getSelectionModel().getSelectedItems()) {
					if (entry.getItem().leafProperty().get() && entry.getItem().itemProperty().get().isNode()) {
						try {
							artifacts.add(entry.getItem().itemProperty().get().getNode().getArtifact());
						}
						catch (IOException e) {
							logger.error("Could not copy item", e);
						}
						catch (ParseException e) {
							logger.error("Could not copy item", e);
						}
					}
				}
				return MainController.buildClipboard(artifacts.toArray());
			}
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void setClipboard(Clipboard clipboard) {
				// no paste capabilities atm
				Object content = clipboard.getContent(DataFormat.PLAIN_TEXT);
				if (content instanceof String) {
					TreeCell<Entry> selectedItem = tree.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						while (selectedItem.getItem().itemProperty().get().isLeaf()) {
							selectedItem = selectedItem.getParent();
							if (selectedItem == null) {
								break;
							}
						}
						if (selectedItem != null && selectedItem.getItem().itemProperty().get() instanceof ExtensibleEntry) {
							TreeItem<Entry> resolved = tree.resolve(((String) content).replace('.', '/'), false);
							if (resolved != null && resolved.itemProperty().get() instanceof ResourceEntry && resolved.itemProperty().get().isNode()) {
								Entry entryToCopy = resolved.itemProperty().get();
								ExtensibleEntry parent = (ExtensibleEntry) selectedItem.getItem().itemProperty().get();
								int counter = 1;
								String name = resolved.getName();
								while (parent.getChild(name) != null) {
									if (counter == 1) {
										name = name + "_copy" + counter++;
									}
									else {
										name = name.replaceFirst("[0-9]+$", "" + counter++);
									}
								}
								try {
									ArtifactManager artifactManager = entryToCopy.getNode().getArtifactManager().newInstance();
									RepositoryEntry targetEntry = ((ExtensibleEntry) parent).createNode(name, artifactManager, false);
									// first copy all the files from the source
									for (Resource resource : ((ResourceEntry) entryToCopy).getContainer()) {
										if (!(resource instanceof ResourceContainer) || targetEntry.getRepository().isInternal((ResourceContainer<?>) resource)) {
											ResourceUtils.copy(resource, (ManageableContainer<?>) targetEntry.getContainer(), resource.getName(), false, true);
										}
									}
									// then resave the artifact (which may have merged values)
									artifactManager.save(targetEntry, entryToCopy.getNode().getArtifact());
									MainController.getInstance().getRepository().reload(parent.getId());
									// trigger refresh in tree
									TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(parent.getId().replace('.', '/'), false);
									if (resolve != null) {
										resolve.refresh();
									}
									// reload remotely
									MainController.getInstance().getServer().getRemote().reload(targetEntry.getId());
								}
								catch (Exception e) {
									throw new RuntimeException(e);
								}
							}
						}
					}
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
						open(getController(), selected);
					}
				}
			}

		});
		TreeDragDrop.makeDraggable(tree, new TreeDragListener<Entry>() {
			@Override
			public boolean canDrag(TreeCell<Entry> arg0) {
				return arg0.getParent() != null;
//				return arg0.getItem().leafProperty().get();
			}
			@Override
			public void drag(TreeCell<Entry> arg0) {
				// do nothing
			}
			@Override
			public String getDataType(TreeCell<Entry> arg0) {
				return arg0.getItem().leafProperty().get()
					? RepositoryBrowser.getDataType(arg0.getItem().itemProperty().get().getNode().getArtifactClass())
					: "folder";
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
	
	public static void open(MainController controller, List<TreeCell<Entry>> selected) {
		for (TreeCell<Entry> entry : selected) {
			entry.getItem().itemProperty().get().refresh(true);
			open(controller, entry.getItem());
		}
	}
	
	public static void open(MainController controller, TreeItem<Entry> treeItem) {
		ArtifactGUIManager<?> manager = controller.getGUIManager(treeItem.itemProperty().get().getNode().getArtifactClass());
		try {
			manager.view(controller, treeItem);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
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
		getControl().getTreeCell(getControl().rootProperty().get()).refresh();
//		for (TreeItem<Entry> child : getControl().rootProperty().get().getChildren()) {
//			getControl().getTreeCell(child).refresh();
//		}
	}
	
	public static class RepositoryTreeItem implements TreeItem<Entry>, RemovableTreeItem<Entry> {
		
		private Logger logger = LoggerFactory.getLogger(getClass());
		
		private ObjectProperty<Entry> itemProperty;
		private BooleanProperty editableProperty, leafProperty;
		private TreeItem<Entry> parent;
		private ObservableList<TreeItem<Entry>> children;
		private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
		private boolean isNode = false;
		private MainController controller;
		private boolean isModule = false;
		
		public RepositoryTreeItem(MainController controller, TreeItem<Entry> parent, Entry entry, boolean isNode) {
			this.controller = controller;
			this.parent = parent;
			itemProperty = new SimpleObjectProperty<Entry>(entry);
			editableProperty = new SimpleBooleanProperty(entry.isEditable() && entry instanceof ResourceEntry);
			// if this is the "node view" of the entry, it's always a leaf (folder is created with children if necessary)
			leafProperty = new SimpleBooleanProperty(entry.isLeaf() || isNode);
			this.isNode = isNode;
			this.isModule = !isNode && entry instanceof ResourceEntry && ((ResourceEntry) entry).getContainer().getChild("module.xml") != null; 
			if (isNode) {
				HBox box = new HBox();
				box.setAlignment(Pos.CENTER);
				box.setMinWidth(25);
				box.setMaxWidth(25);
				box.setPrefWidth(25);
				box.getChildren().add(controller.getGUIManager(entry.getNode().getArtifactClass()).getGraphic());
				graphicProperty.set(box);
			}
			else if (isModule) {
				graphicProperty.set(MainController.loadGraphic("folder-module.png"));
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
			TreeUtils.refreshChildren(this, loadChildren());
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
		
		@Override
		public String toString() {
			return TreeDragDrop.getPath(this);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (isNode ? 1231 : 1237);
			result = prime * result
					+ ((itemProperty.get() == null) ? 0 : itemProperty.get().hashCode());
			return result;
		}

		public boolean equals(Object object) {
			return object instanceof RepositoryTreeItem 
				&& ((RepositoryTreeItem) object).isNode == isNode
				&& ((RepositoryTreeItem) object).itemProperty.get().equals(itemProperty.get());
		}

		@Override
		public boolean remove() {
			if (itemProperty.get() instanceof ResourceEntry) {
				ResourceEntry entry = (ResourceEntry) itemProperty.get();
				Confirm.confirm(ConfirmType.QUESTION, "Delete " + entry.getId(), "Are you sure you want to delete: " + entry.getId() + "?", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						controller.closeAll(entry.getId());
						controller.getRepository().unload(itemProperty.get().getId());
						try {
							((ManageableContainer<?>) entry.getContainer().getParent()).delete(entry.getName());
							controller.getRepository().reload(entry.getParent().getId());
							controller.getTree().refresh();
						}
						catch (IOException e) {
							logger.error("Could not delete entry " + entry.getId(), e);
						}
						try {
							controller.getServer().getRemote().unload(entry.getId());
						}
						catch (IOException e) {
							logger.error("Could not remotely unload entry " + entry.getId(), e);
						}
						catch (FormatException e) {
							logger.error("Could not remotely unload entry " + entry.getId(), e);
						}
						catch (ParseException e) {
							logger.error("Could not remotely unload entry " + entry.getId(), e);
						}
					}
				});
				return true;
			}
			return false;
		}

	}
}
