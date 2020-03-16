package be.nabu.eai.developer.components;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

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

import javax.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.base.BaseComponent;
import be.nabu.eai.developer.impl.CustomTooltip;
import be.nabu.eai.developer.managers.util.RemoveTreeContextMenu;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.repository.EAINode;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.BrokenReferenceArtifactManager;
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
import be.nabu.libs.resources.ResourceReadableContainer;
import be.nabu.libs.resources.ResourceUtils;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.features.CacheableResource;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;

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
		tree.getRootCell().expandedProperty().set(true);
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
					// if we press control, we do a hard refresh of the file system as well
					if (arg0.isControlDown()) {
						for (TreeCell<Entry> selected : tree.getSelectionModel().getSelectedItems()) {
							Entry entry = selected.getItem().itemProperty().get();
							if (entry instanceof ResourceEntry) {
								ResourceContainer<?> container = ((ResourceEntry) entry).getContainer();
								if (container instanceof CacheableResource) {
									try {
										((CacheableResource) container).resetCache();
									}
									catch (IOException e) {
										MainController.getInstance().notify(e);
									}
								}
							}
						}
					}
					for (TreeCell<Entry> selected : tree.getSelectionModel().getSelectedItems()) {
						getController().getRepository().reload(selected.getItem().itemProperty().get().getId());
					}
					getController().refresh();
					for (TreeCell<Entry> child : tree.getSelectionModel().getSelectedItems()) {
						child.refresh();
						// attempt remote refresh
						try {
							MainController.getInstance().getAsynchronousRemoteServer().reload(child.getItem().itemProperty().get().getId());
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
				List<Object> contents = new ArrayList<Object>();
				for (TreeCell<Entry> entry : tree.getSelectionModel().getSelectedItems()) {
					if (entry.getItem().itemProperty().get().isNode()) {
						try {
							contents.add(entry.getItem().itemProperty().get().getNode().getArtifact());
						}
						catch (IOException e) {
							logger.error("Could not copy item", e);
						}
						catch (ParseException e) {
							logger.error("Could not copy item", e);
						}
					}
					else if (entry.getItem().itemProperty().get() instanceof ResourceEntry) {
						contents.add(entry.getItem().itemProperty().get());
					}
				}
				return MainController.buildClipboard(contents.toArray());
			}
			@SuppressWarnings({ "rawtypes", "unchecked" })
			@Override
			public void setClipboard(Clipboard clipboard) {
				// no paste capabilities atm
				Object content = clipboard.getContent(DataFormat.PLAIN_TEXT);
				if (content instanceof String) {
					TreeCell<Entry> selectedItem = tree.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						// some nodes are not leafs (e.g. a jdbc service), we still don't want to paste in them
						while (selectedItem.getItem().itemProperty().get().isLeaf() || selectedItem.getItem().itemProperty().get().isNode()) {
							selectedItem = selectedItem.getParent();
							if (selectedItem == null) {
								break;
							}
						}
						if (selectedItem != null && selectedItem.getItem().itemProperty().get() instanceof ExtensibleEntry) {
							// if we put a binary entry in the clipboard, we want to paste it
							Object binary = clipboard.getContent(TreeDragDrop.getDataFormat("entry-binary"));
							if (binary instanceof byte[]) {
								Entry entry = selectedItem.getItem().itemProperty().get();
								if (entry instanceof ResourceEntry) {
									ResourceContainer<?> container = ((ResourceEntry) entry).getContainer();
									String name = content.toString().replaceAll("^.*\\.([^.]+)$", "$1");
									int counter = 1;
									while (entry.getChild(name) != null) {
										if (counter == 1) {
											name = name + "_copy" + counter++;
										}
										else {
											name = name.replaceFirst("[0-9]+$", "" + counter++);
										}
									}
									try {
										ResourceContainer<?> create = (ResourceContainer<?>) ((ManageableContainer) container).create(name, Resource.CONTENT_TYPE_DIRECTORY);
										ResourceUtils.unzip(new ZipInputStream(new ByteArrayInputStream((byte[]) binary)), create);

										String newLocation = entry.getId() + "." + create.getName();
										// if we copied it to a different location, we need to update references so they match
										// we can only use the broken reference updating at this point
										if (!content.equals(newLocation)) {
											Map<String, String> relinks = new HashMap<String, String>();
											getRelinks(create, (String) content, newLocation, relinks);
											System.out.println("Relinking " + content + " to " + newLocation + ": " + relinks);
											relink(create, relinks);
										}
										
										MainController.getInstance().getRepository().reload(entry.getId());
										
										// we need to rebuild references so they are up to date with rewrites
										if (!content.equals(newLocation)) {
											MainController.getInstance().getRepository().rebuildReferences(newLocation, true);
										}
										
										// trigger refresh in tree
										TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace('.', '/'), false);
										if (resolve != null) {
											resolve.refresh();
										}
										// reload remotely
										MainController.getInstance().getAsynchronousRemoteServer().reload(entry.getId() + "." + name);
										MainController.getInstance().getCollaborationClient().created(entry.getId() + "." + name, "Copied " + content + " into " + newLocation);
									}
									catch (Exception e) {
										MainController.getInstance().notify(e);
									}
								}
							}
							// otherwise we are hoping it's a local copy so we can get it from the tree
							else {
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
			}
		});
		// this preempts default behavior
		tree.setClickHandler(new EventHandler<MouseEvent>() {
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
					// if it is a node, try to activate/open it
					if (selected.get(0).getItem().itemProperty().get().isNode()) {
						if (!getController().activate(selected.get(0).getItem().itemProperty().get().getId())) {
							open(getController(), selected);
						}
					}
					if (!selected.get(0).getItem().leafProperty().get() && (!selected.get(0).getItem().itemProperty().get().isNode() || event.isAltDown())) {
						// if it is merely a folder, we always toggle the expanded
						// if it is also an artifact, we only open it, as you might have meant to open the details of the artifact 
//						selected.get(0).expandedProperty().set(selected.get(0).getItem().itemProperty().get().isNode() ? true : !selected.get(0).expandedProperty().get());
						// because we now added the requirement of control down for artifacts, we can explicitly toggle expanded as well
						selected.get(0).expandedProperty().set(!selected.get(0).expandedProperty().get());
					}
					event.consume();
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
				return arg0.getItem().itemProperty().get().isNode()
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
			@Override
			public String getAsText(TreeCell<Entry> cell) {
				return cell.getItem().itemProperty().get().getId();
			}
		});
	}
	
	private void getRelinks(ResourceContainer<?> container, String originalLocation, String newLocation, Map<String, String> relinks) {
		Resource child = container.getChild("node.xml");
		// it is a node, add it to the relink map
		if (child != null) {
			relinks.put(originalLocation, newLocation);
		}
		for (Resource resource : container) {
			// don't recurse internal folders
			if (resource instanceof ResourceContainer && !EAIResourceRepository.getInstance().isInternal((ResourceContainer<?>) resource)) {
				String originalChildLocation = originalLocation + "." + resource.getName();
				String newChildLocation = newLocation + "." + resource.getName();
				getRelinks((ResourceContainer<?>) resource, originalChildLocation, newChildLocation, relinks);
			}
		}
	}
	
	private void relink(ResourceContainer<?> container, Map<String, String> relinks) {
		Resource child = container.getChild("node.xml");
		// if we have a node, get the artifact manager
		if (child != null) {
			try {
				ReadableContainer<ByteBuffer> readable = new ResourceReadableContainer((ReadableResource) child);
				try {
					EAINode node = (EAINode) JAXBContext.newInstance(EAINode.class).createUnmarshaller().unmarshal(IOUtils.toInputStream(readable));
					ArtifactManager<?> manager = node.getArtifactManager().newInstance();
					if (manager instanceof BrokenReferenceArtifactManager) {
						for (String key : relinks.keySet()) {
							((BrokenReferenceArtifactManager<?>) manager).updateBrokenReference(container, key, relinks.get(key));
						}
					}
				}
				finally {
					readable.close();
				}
			}
			catch (Exception e) {
				logger.error("Could not rewrite: " + container);
			}
		}
		// recurse
		for (Resource resource : container) {
			// don't recurse internal folders
			if (resource instanceof ResourceContainer && !EAIResourceRepository.getInstance().isInternal((ResourceContainer<?>) resource)) {
				relink((ResourceContainer<?>) resource, relinks);
			}
		}
	}
	
	public static void open(MainController controller, List<TreeCell<Entry>> selected) {
		for (TreeCell<Entry> entry : selected) {
			open(controller, entry.getItem());
		}
	}
	
	public static void open(MainController controller, Entry treeItem) {
		TreeItem<Entry> resolved = controller.getTree().resolve(treeItem.getId().replace(".",  "/"));
		if (resolved != null) {
			open(controller, resolved);
		}
	}
	
	public static void open(MainController controller, TreeItem<Entry> treeItem) {
		treeItem.itemProperty().get().refresh(true);
		ArtifactGUIManager<?> manager = controller.getGUIManager(treeItem.itemProperty().get().getNode().getArtifactClass());
		try {
			manager.view(controller, treeItem);
		}
		catch (IOException e) {
			controller.notify(e);
			throw new RuntimeException(e);
		}
		catch (ParseException e) {
			controller.notify(e);
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
		private BooleanProperty editableProperty, leafProperty, documentedProperty;
		private TreeItem<Entry> parent;
		private ObservableList<TreeItem<Entry>> children;
		private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
		private ObjectProperty<Date> deprecatedProperty = new SimpleObjectProperty<Date>();
		private boolean isNode = false;
		private MainController controller;
		private boolean isModule = false;
		
		public RepositoryTreeItem(MainController controller, TreeItem<Entry> parent, Entry entry, boolean isNode) {
			this.controller = controller;
			this.parent = parent;
			itemProperty = new SimpleObjectProperty<Entry>(entry);
			editableProperty = new SimpleBooleanProperty(entry.isEditable() && entry instanceof ResourceEntry);
			leafProperty = new SimpleBooleanProperty(entry.isLeaf());
			this.isNode = isNode;
			this.isModule = !isNode && entry instanceof ResourceEntry && 
				// if there is a pom file, it is the unzipped version
				(((ResourceEntry) entry).getContainer().getChild("pom.xml") != null
					// the zipped version
					|| (((ResourceEntry) entry).getContainer().getName() != null && ((ResourceEntry) entry).getContainer().getName().endsWith(".nar")));
		
			documentedProperty = new SimpleBooleanProperty(entry instanceof ResourceEntry
				&& ((ResourceEntry) entry).getContainer().getChild(EAIResourceRepository.PROTECTED) != null
				&& ((ResourceContainer<?>) ((ResourceEntry) entry).getContainer().getChild(EAIResourceRepository.PROTECTED)).getChild("documentation") != null
			);
			deprecatedProperty.set(entry.isNode() ? entry.getNode().getDeprecated() : null);
			buildGraphic(controller, entry, isNode);
			// rebuild graphic if documentation is added/removed
			documentedProperty.addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
					buildGraphic(controller, entry, isNode);
				}
			});
			deprecatedProperty.addListener(new ChangeListener<Date>() {
				@Override
				public void changed(ObservableValue<? extends Date> arg0, Date arg1, Date arg2) {
					buildGraphic(controller, entry, isNode);
				}
			});
		}

		private void buildGraphic(MainController controller, Entry entry, boolean isNode) {
			HBox box = new HBox();
			box.setAlignment(Pos.CENTER);
			// 25 of icon and marker of 3
			box.setMinWidth(28);
			box.setMaxWidth(28);
			box.setPrefWidth(28);
			if (deprecatedProperty.get() != null) {
				box.setMaxWidth(28 * 2);
				box.setMinWidth(28 * 2);
				Node loadFixedSizeGraphic = MainController.loadFixedSizeGraphic("deprecated.png", 16);
				box.getChildren().add(loadFixedSizeGraphic);
				new CustomTooltip("Please be careful when using this, it has been deprecated since: " + deprecatedProperty.get() + ". It may be removed in a future version.").install(loadFixedSizeGraphic);
			}
			if (isNode) {
				box.getChildren().add(controller.getGUIManager(entry.getNode().getArtifactClass()).getGraphic());
			}
			else if (isModule) {
				box.getChildren().add(MainController.loadGraphic("folder-module.png"));
			}
			else {
				box.getChildren().add(MainController.loadGraphic("folder.png"));
			}
			if (documentedProperty.get()) {
				box.getChildren().add(MainController.loadGraphic("types/optional.png"));
			}
			else {
				box.getChildren().add(MainController.loadGraphic("types/mandatory.png"));
			}
			graphicProperty.set(box);
		}
		
		public BooleanProperty documentedProperty() {
			return documentedProperty;
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

		public ObjectProperty<Date> deprecatedProperty() {
			return deprecatedProperty;
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
			if (itemProperty.get().isLeaf()) {
				return items;
			}
			for (Entry entry : itemProperty.get()) {
				// if the non-leaf is a repository, it will not be shown as a dedicated map
				if (!entry.isLeaf() && (!entry.isNode() || !Repository.class.isAssignableFrom(entry.getNode().getArtifactClass()))) {
					items.add(new RepositoryTreeItem(controller, this, entry, entry.isNode()));
				}
				// for nodes we add two entries: one for the node, and one for the folder
				if (entry.isNode() && entry.isLeaf() && (MainController.getInstance().isShowHidden() || !entry.getNode().isHidden())) {
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
						List<String> dependencies = controller.getRepository().getDependencies(entry.getId());
						// self-references don't matter
						dependencies.remove(entry.getId());
						if (dependencies.isEmpty()) {
							delete(entry);
						}
						else {
							StringBuilder builder = new StringBuilder();
							builder.append("Removing ").append(entry.getId()).append(" will break these dependencies: \n\n");
							for (String dependency : dependencies) {
								builder.append("- ").append(dependency).append("\n");
							}
							builder.append("\nAre you sure you want to proceed?");
							Confirm.confirm(ConfirmType.WARNING, "Broken dependencies for " + entry.getId(), builder.toString(), new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									delete(entry);									
								}
							});
						}
					}

				});
				return true;
			}
			return false;
		}

		private void delete(ResourceEntry entry) {
			controller.closeAll(entry.getId());
			try {
				// unload synchronously in server before deleting
				controller.getServer().getRemote().unload(entry.getId());
				// unload in own repository
				controller.getRepository().unload(itemProperty.get().getId());
				// @optimize
				if (entry.getParent() instanceof ExtensibleEntry) {
					((ExtensibleEntry) entry.getParent()).deleteChild(entry.getName(), true);
				}
				else {
					((ManageableContainer<?>) entry.getContainer().getParent()).delete(entry.getName());
					controller.getRepository().reload(entry.getParent().getId());
				}
				controller.getCollaborationClient().deleted(entry.getId(), "Deleted");
				controller.getTree().refresh();
			}
			catch (Exception e) {
				logger.error("Could not delete entry " + entry.getId(), e);
			}
			controller.getAsynchronousRemoteServer().unload(entry.getId());
		}

	}
}
