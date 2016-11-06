package be.nabu.eai.developer;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.api.ClipboardProvider;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.eai.developer.api.EvaluatableProperty;
import be.nabu.eai.developer.api.MainMenuEntry;
import be.nabu.eai.developer.api.RefresheableArtifactGUIInstance;
import be.nabu.eai.developer.api.ValidatableArtifactGUIInstance;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.managers.ServiceGUIManager;
import be.nabu.eai.developer.managers.ServiceInterfaceGUIManager;
import be.nabu.eai.developer.managers.SimpleTypeGUIManager;
import be.nabu.eai.developer.managers.TypeGUIManager;
import be.nabu.eai.developer.managers.util.EnumeratedSimpleProperty;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.ContentTreeItem;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.ElementTreeItem;
import be.nabu.eai.developer.util.RepositoryValidatorService;
import be.nabu.eai.developer.util.StringComparator;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.BrokenReferenceArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.events.NodeEvent;
import be.nabu.eai.repository.events.NodeEvent.State;
import be.nabu.eai.server.ServerConnection;
import be.nabu.jfx.control.date.DatePicker;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeCellValue;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.Updateable;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDropListener;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.converter.api.Converter;
import be.nabu.libs.evaluator.EvaluationException;
import be.nabu.libs.evaluator.PathAnalyzer;
import be.nabu.libs.evaluator.QueryParser;
import be.nabu.libs.evaluator.types.api.TypeOperation;
import be.nabu.libs.evaluator.types.operations.TypesOperationProvider;
import be.nabu.libs.events.api.EventDispatcher;
import be.nabu.libs.events.impl.EventDispatcherImpl;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Enumerated;
import be.nabu.libs.property.api.Filter;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.DefinedSimpleType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.DefinedTypeResolver;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.properties.CollectionHandlerProviderProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.types.structure.SuperTypeProperty;
import be.nabu.libs.types.utils.DateTimeFormat;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import be.nabu.libs.validator.api.Validator;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.mime.impl.FormatException;

/**
 * TODO: apparently the panes are not scrollable by default, need to add it?
 * http://docs.oracle.com/javafx/2/ui_controls/scrollbar.htm
 * Wtf happened to setScrollable(true) ?
 * 
 * 
 * TODO: i may need to further optimize the classloading, the mavenclassloader already shortcuts to parent loading for internal namespaces
 * additionally it keeps a list of misses to prevent double scanning
 * while the gui managers have been cached as it had to rescan all the maven classloaders to build the list otherwise
 * The biggest problem is actually the @Interface annotation which is resolved against the DefinedServiceInterfaceResolverFactory
 * That factory however has no context awareness and as such will search all maven classloaders, it doesn't know where it was defined
 *  
 * currently there is still a delay when you open a cell in the tree
 */
public class MainController implements Initializable, Controller {

	public static final String DATA_TYPE_NODE = "repository-node";
	
	@FXML
	private AnchorPane ancLeft, ancMiddle, ancProperties, ancPipeline;
	
	@FXML
	private TabPane tabArtifacts;
	
	@FXML
	private ListView<Validation<?>> lstNotifications;
	
	@FXML
	private MenuItem mniClose, mniSave, mniCloseAll, mniCloseOther, mniSaveAll, mniRebuildReferences, mniLocate, mniFind, mniUpdateReference;
	
	@FXML
	private ScrollPane scrLeft;
	
	@FXML
	private MenuBar mnbMain;
	
	private boolean scrLeftFocused;
	
	private Map<Tab, ArtifactGUIInstance> managers = new HashMap<Tab, ArtifactGUIInstance>();
	
	private DefinedTypeResolver typeResolver = DefinedTypeResolverFactory.getInstance().getResolver();
	
	private Converter converter = ConverterFactory.getInstance().getConverter();
	
	private Map<String, Component<MainController, ?>> components = new HashMap<String, Component<MainController, ?>>();
	
	private EAIResourceRepository repository;

	private Stage stage;
	
	private Tree<Entry> tree;
	
	private static MainController instance;

	private ServerConnection server;
	
	private Logger logger = LoggerFactory.getLogger(getClass());
	
	private boolean showExactName = Boolean.parseBoolean(System.getProperty("show.exact.name", "false"));
	
	private Map<String, Object> state = new HashMap<String, Object>();
	
	private EventDispatcher dispatcher = new EventDispatcherImpl();
	
	/**
	 * Keep track of the last directory used to select a file from, set it as default
	 */
	private File lastDirectoryUsed;
	
	/**
	 * The id that was active when the validations were generated, it can probably find them again
	 */
	private String validationsId;
	
	private Set<KeyCode> activeKeys = new HashSet<KeyCode>();

	public void connect(ServerConnection server) {
		this.server = server;
		// create repository
		try {
			stage.setTitle("Nabu Developer: " + server.getName());
			Resource resourceRoot = ResourceFactory.getInstance().resolve(server.getRepositoryRoot(), null);
			if (resourceRoot == null) {
				throw new RuntimeException("Could not find the repository root: " + server.getRepositoryRoot());
			}
			Resource mavenRoot = ResourceFactory.getInstance().resolve(server.getMavenRoot(), null);
			if (mavenRoot == null) {
				throw new RuntimeException("Could not find the maven root: " + server.getMavenRoot());
			}
			repository = new EAIResourceRepository((ResourceContainer<?>) resourceRoot, (ResourceContainer<?>) mavenRoot);
			Thread.currentThread().setContextClassLoader(repository.getClassLoader());
			repository.getEventDispatcher().subscribe(NodeEvent.class, new be.nabu.libs.events.api.EventHandler<NodeEvent, Void>() {
				@Override
				public Void handle(NodeEvent event) {
					if (event.getState() == State.CREATE && event.isDone()) {
						try {
							if (event.getId().contains(".")) {
								getServer().getRemote().reload(event.getId());
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			});
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		repository.setServiceRunner(server.getRemote());
		repository.start();
		tree = new Tree<Entry>(new Marshallable<Entry>() {
			@Override
			public String marshal(Entry entry) {
				if ((entry.isEditable() && entry.isLeaf()) || showExactName) {
					return entry.getName();
				}
				else {
					String name = entry.getName();
					return name.substring(0, 1).toLowerCase() + name.substring(1);
				}
			}
		}, new Updateable<Entry>() {
			@Override
			public Entry update(TreeCell<Entry> treeCell, String newName) {
				ResourceEntry entry = (ResourceEntry) treeCell.getItem().itemProperty().get();
				// we need to reload the dependencies after the move is done as they will have their references updated
				List<String> dependencies = repository.getDependencies(entry.getId());
				closeAll(entry.getId());
				try {
					MainController.this.notify(repository.move(entry.getId(), entry.getId().replaceAll("[^.]+$", newName), true));
				}
				catch (IOException e1) {
					e1.printStackTrace();
					return treeCell.getItem().itemProperty().get();
				}
				treeCell.getParent().getItem().itemProperty().get().refresh(true);
				// reload the repository
				getRepository().reload(treeCell.getParent().getItem().itemProperty().get().getId());
				// refresh the tree
				treeCell.getParent().refresh();
				try {
					// reload the remote parent to pick up the new arrangement
					server.getRemote().reload(treeCell.getParent().getItem().itemProperty().get().getId());
					// reload the dependencies to pick up the new item
					for (String dependency : dependencies) {
						server.getRemote().reload(dependency);
					}
				}
				catch (Exception e) {
					logger.error("Could not reload renamed items on server", e);
				}
				return treeCell.getParent().getItem().itemProperty().get().getChild(newName);
			}
		});
		tree.setAutoscrollOnSelect(false);
		tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					TreeCell<Entry> selectedItem = tree.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						String id = selectedItem.getItem().itemProperty().get().getId();
						Tab tab = getTab(id);
						if (tab == null) {
							RepositoryBrowser.open(MainController.this, selectedItem.getItem());
						}
						else {
							tab.getTabPane().getSelectionModel().select(tab);
						}
						event.consume();
					}
				}
			}
		});
		// allow you to move items in the tree by drag/dropping them (drag is currently in RepositoryBrowser for legacy reasons
		TreeDragDrop.makeDroppable(tree, new TreeDropListener<Entry>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean canDrop(String dataType, TreeCell<Entry> target, TreeCell<?> dragged, TransferMode transferMode) {
				Entry entry = target.getItem().itemProperty().get();
				return !dragged.equals(target) && !target.getItem().leafProperty().get() && entry instanceof ResourceEntry && ((ResourceEntry) entry).getContainer() instanceof ManageableContainer
					// no item must exist with that name
					&& ((ResourceEntry) entry).getContainer().getChild(((TreeCell<Entry>) dragged).getItem().getName()) == null;
			}
			@SuppressWarnings("unchecked")
			@Override
			public void drop(String arg0, TreeCell<Entry> target, TreeCell<?> dragged, TransferMode arg3) {
				Entry original = ((TreeCell<Entry>) dragged).getItem().itemProperty().get();
				try {
					List<String> dependencies = repository.getDependencies(original.getId());
					String originalParentId = ((TreeCell<Entry>) dragged).getParent().getItem().itemProperty().get().getId();
					repository.move(
						original.getId(), 
						target.getItem().itemProperty().get().getId() + "." + original.getName(), 
						true);
					// refresh the tree
					System.out.println("Refreshing: " + target.getParent().getItem().getName() + " and " + dragged.getParent().getItem().getName());
					target.getParent().refresh();
					dragged.getParent().refresh();
					// reload remotely
					try {
						server.getRemote().reload(originalParentId);
						server.getRemote().reload(target.getItem().itemProperty().get().getId());
						// reload dependencies
						for (String dependency : dependencies) {
							server.getRemote().reload(dependency);
						}
					}
					catch (Exception e) {
						logger.error("Could not reload moved items on server", e);
					}
				}
				catch (IOException e) {
					logger.error("Could not move " + original.getId(), e);
				}
			}
		});
		tree.setId("repository");
		ancLeft.getChildren().add(tree);

		// make the tree scroll faster
		scrLeft.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
			@Override
			public void handle(ScrollEvent scrollEvent) {
				double height = scrLeft.getHeight();
				// the deltay is in pixels, the vvalue is relative 0-1 range
				// apparently negative value means downwards..
				double move = scrLeft.getVvalue() - (scrollEvent.getDeltaY() * 2) / height;
				scrLeft.setVvalue(move);
				scrollEvent.consume();
			}
		});
		
		// for some reason on refocusing, the scrollbar jumps to the bottom, if the scrollbar is at the very top (vvalue = 0) nothing happens
		// if it is at vvalue > 0, it will jump to near the end everytime it gets focus
		// it is actually not the scrollpane in general getting focus, it is the tree
		// that's why we set a focus boolean if the tree is triggered so we can revert the jump in the scrollbar
		tree.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				scrLeftFocused = arg2 != null && arg2;
			}
		});
		scrLeft.vvalueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				if (scrLeftFocused) {
					scrLeftFocused = false;
					scrLeft.setVvalue(arg1.doubleValue());
				}
			}
		});
		// end hack to stop scrollbar jumping
		
		// create the browser
		components.put(tree.getId(), new RepositoryBrowser(server).initialize(this, tree));
		AnchorPane.setLeftAnchor(tree, 0d);
		AnchorPane.setRightAnchor(tree, 0d);
		AnchorPane.setTopAnchor(tree, 0d);
		AnchorPane.setBottomAnchor(tree, 0d);
		
		for (MainMenuEntry mainMenuEntry : ServiceLoader.load(MainMenuEntry.class)) {
			mainMenuEntry.populate(mnbMain);
		}

		repositoryValidatorService = new RepositoryValidatorService(repository, mnbMain);
		repositoryValidatorService.start();
	}
	
	public void setStatusMessage(String message) {
		Platform.runLater(new Runnable() {
			public void run() {
				String title = stage.getTitle().replaceAll(" - .*$", "");
				if (message != null) {
					title += " - " + message;
				}
				stage.setTitle(title);
			}
		});
	}
	
	public void setStatusMessage(String id, String message) {
		for (Tab tab : tabArtifacts.getTabs()) {
			if (id.equals(tab.getId())) {
				boolean isChanged = tab.getText().endsWith("*");
				String title = tab.getText().replaceAll(" - .*$", "").replaceAll("[\\s]*\\*$", "");
				if (message != null) {
					title += " - " + message;
				}
				if (isChanged) {
					title += " *";
				}
				tab.setText(title);
			}
		}
	}
	
	public void offload(final Runnable runnable, final boolean lockTab, final String message) {
		Tab selectedItem = tabArtifacts.getSelectionModel().getSelectedItem();
		if (selectedItem != null || !lockTab) {
			if (lockTab) {
				selectedItem.getContent().setDisable(true);
			}
			if (trayIcon != null) {
				trayIcon.setToolTip("Nabu Developer - " + message);
			}
			selectedItem.setGraphic(loadGraphic("status/running.png"));
			Runnable newRunnable = new Runnable() {
				public void run() {
					try {
						runnable.run();
					}
					finally {
						Platform.runLater(new Runnable() {
							public void run() {
								if (lockTab) {
									selectedItem.getContent().setDisable(false);
								}
								selectedItem.setGraphic(loadGraphic("status/success.png"));
								if (trayIcon != null) {
									trayIcon.displayMessage("Action Completed", message, MessageType.INFO);
									trayIcon.setToolTip("Nabu Developer");
								}
							}
						});
					}
				}
			};
			new Thread(newRunnable).start();
		}
		else {
			throw new RuntimeException("No tab found");
		}
	}
	
	public static MainController getInstance() {
		return instance;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		instance = this;
		if (SystemTray.isSupported()) {
			try {
				trayIcon = new TrayIcon(ImageIO.read(MainController.class.getClassLoader().getResource("icon.png")));
				trayIcon.setImageAutoSize(true);
				trayIcon.setToolTip("Nabu Developer");
				SystemTray.getSystemTray().add(trayIcon);
			}
			catch (Exception e) {
				logger.error("Could not load tray icon", e);
			}
		}
		tabArtifacts.setTabClosingPolicy(TabClosingPolicy.SELECTED_TAB);
		// ---------------------------- RESIZING ------------------------------
		// the anchorpane bindings make sure the tree resizes with the anchor pane
		// the anchorpane does not have a parent yet, but when it does, bind the width to the parent width
		ancLeft.parentProperty().addListener(new ChangeListener<Parent>() {
			@Override
			public void changed(ObservableValue<? extends Parent> arg0, Parent arg1, Parent newParent) {
				if (ancLeft.prefWidthProperty().isBound()) {
					ancLeft.prefWidthProperty().unbind();
				}
				ancLeft.prefWidthProperty().bind(((Pane) newParent).widthProperty());
			}
		});
		mniFind.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			private List<String> nodes;
			private void populate(Entry entry) {
				if (entry.isNode()) {
					nodes.add(entry.getId());
				}
				for (Entry child : entry) {
					populate(child);
				}
			}
			private List<String> getNodes() {
				nodes = new ArrayList<String>();
				populate(repository.getRoot());
				return nodes;
			}
			@Override
			public void handle(ActionEvent arg0) {
				VBox box = new VBox();
				TextField field = new TextField();
				ListView<String> list = new ListView<String>();
				list.addEventHandler(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						String selectedItem = list.getSelectionModel().getSelectedItem();
						if (selectedItem != null) {
							Artifact resolve = getRepository().resolve(selectedItem);
							ClipboardContent clipboard = new ClipboardContent();
							Dragboard dragboard = list.startDragAndDrop(TransferMode.MOVE);
							DataFormat format = TreeDragDrop.getDataFormat(RepositoryBrowser.getDataType(resolve.getClass()));
							// it resolves it against the tree itself
							clipboard.put(format, resolve.getId().replace(".", "/"));
							dragboard.setContent(clipboard);
							event.consume();
						}
					}
				});
				list.getItems().addAll(getNodes());
				box.getChildren().addAll(field, list);
				final Stage stage = EAIDeveloperUtils.buildPopup("Find artifact", box);
				field.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.ENTER) {
							String selectedItem = list.getSelectionModel().getSelectedItem();
							if (selectedItem != null) {
								locate(selectedItem);
								stage.close();
							}
							event.consume();
						}
						else if (event.getCode() == KeyCode.DOWN) {
							list.getSelectionModel().selectNext();
							event.consume();
						}
						else if (event.getCode() == KeyCode.UP) {
							list.getSelectionModel().selectPrevious();
							event.consume();
						}
					}
				});
				field.textProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
						if (arg2 == null || arg1 == null || !arg2.startsWith(arg1)) {
							list.getItems().clear();
							list.getItems().addAll(getNodes());
						}
						// filter current list
						if (arg2 != null && !arg2.trim().isEmpty()) {
							Iterator<String> iterator = list.getItems().iterator();
							arg2 = arg2.toLowerCase().replace("*", ".*");
							if (!arg2.startsWith("^")) {
								arg2 = ".*" + arg2 + ".*";
							}
							while(iterator.hasNext()) {
								if (!iterator.next().matches("(?i)" + arg2)) {
									iterator.remove();
								}
							}
						}
					}
				});
				list.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						String selectedItem = list.getSelectionModel().getSelectedItem();
						locate(selectedItem);
						if (event.getClickCount() > 1) {
							if (selectedItem != null) {
								stage.close();
							}
							event.consume();
						}
					}
				});
			}
		});
		mniSave.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// see below...
				tabArtifacts.requestFocus();
				if (tabArtifacts.getSelectionModel().selectedItemProperty().isNotNull().get()) {
					Tab selected = tabArtifacts.getSelectionModel().getSelectedItem();
					ArtifactGUIInstance instance = managers.get(selected);
					if (instance != null && instance.isReady() && instance.isEditable() && instance.hasChanged()) {
						try {
							System.out.println("Saving " + selected.getId());
							instance.save();
							if (repositoryValidatorService != null) {
								repositoryValidatorService.clear(selected.getId());
							}
							String text = selected.getText();
							selected.setText(text.replaceAll("[\\s]*\\*$", ""));
							instance.setChanged(false);
							// check all the open tabs, if they are somehow dependent on this item and have no pending edits, refresh
							for (Tab tab : managers.keySet()) {
								ArtifactGUIInstance guiInstance = managers.get(tab);
								// IMPORTANT: we only check _direct_ references. it could be you depend on it indirectly but then it shouldn't affect your display!
								if (!instance.equals(guiInstance) && !guiInstance.hasChanged() && guiInstance.isReady() && guiInstance instanceof RefresheableArtifactGUIInstance && repository.getReferences(guiInstance.getId()).contains(instance.getId())) {
									refreshTab(tab);
								}
							}
						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}
						try {
							server.getRemote().reload(instance.getId());
						}
						catch (IOException e) {
							logger.error("Could not remotely reload: " + instance.getId(), e);
						}
						catch (FormatException e) {
							logger.error("Could not remotely reload: " + instance.getId(), e);
						}
						catch (ParseException e) {
							logger.error("Could not remotely reload: " + instance.getId(), e);
						}
					}
				}
			}
		});
		mniSaveAll.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// see below...
				tabArtifacts.requestFocus();
				List<String> saved = new ArrayList<String>();
				for (Tab tab : managers.keySet()) {
					ArtifactGUIInstance instance = managers.get(tab);
					if (instance.isReady() && instance.isEditable() && instance.hasChanged()) {
						try {
							System.out.println("Saving " + instance.getId());
							instance.save();
							if (repositoryValidatorService != null) {
								repositoryValidatorService.clear(instance.getId());
							}
							String text = tab.getText();
							tab.setText(text.replaceAll("[\\s]*\\*$", ""));
							instance.setChanged(false);
							saved.add(instance.getId());
						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}
						try {
							server.getRemote().reload(instance.getId());
						}
						catch (IOException e) {
							logger.error("Could not remotely reload: " + instance.getId(), e);
						}
						catch (FormatException e) {
							logger.error("Could not remotely reload: " + instance.getId(), e);
						}
						catch (ParseException e) {
							logger.error("Could not remotely reload: " + instance.getId(), e);
						}
					}
				}
				if (!saved.isEmpty()) {
					// redraw all tabs, there might be interdependent changes
					for (Tab tab : managers.keySet()) {
						ArtifactGUIInstance guiInstance = managers.get(tab);
						if (!guiInstance.hasChanged() && guiInstance.isReady() && guiInstance instanceof RefresheableArtifactGUIInstance && repository.getReferences(guiInstance.getId()).removeAll(saved)) {
							refreshTab(tab);
						}
					}
				}
			}
		});
		mniLocate.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// see below...
				tabArtifacts.requestFocus();
				if (tabArtifacts.getSelectionModel().selectedItemProperty().isNotNull().get()) {
					Tab selected = tabArtifacts.getSelectionModel().getSelectedItem();
					if (managers.containsKey(selected)) {
						locate(managers.get(selected).getId());
					}
				}
				event.consume();
			}

		});
		mniClose.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// important (2016-01-27): upon closing the tab, the focus would (sometimes) jump back to the tree on the left
				// for some reason this refocus triggers a reposition of the scrollpane making it scroll down which is very annoying
				// i can not find any reason for the autoscroll but basically making sure the tabartifacts has the focus seems to preempt the focus switch
				tabArtifacts.requestFocus();
				if (tabArtifacts.getSelectionModel().selectedItemProperty().isNotNull().get()) {
					Tab selected = tabArtifacts.getSelectionModel().getSelectedItem();
					if (managers.containsKey(selected)) {
						managers.remove(selected);
					}
					tabArtifacts.getTabs().remove(selected);
				}
				event.consume();
			}
		});
		mniCloseAll.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				tabArtifacts.requestFocus();
				managers.clear();
				tabArtifacts.getTabs().clear();
				event.consume();
			}
		});
		mniCloseOther.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				tabArtifacts.requestFocus();
				Tab selected = tabArtifacts.getSelectionModel().getSelectedItem();
				// nothing selected
				if (selected != null) {
					ArtifactGUIInstance artifactGUIInstance = managers.get(selected);
					managers.clear();
					managers.put(selected, artifactGUIInstance);
					tabArtifacts.getTabs().retainAll(selected);
				}
			}
		});
		mniRebuildReferences.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				lstNotifications.getItems().clear();
				// the root is special as the visual display does not match an actual root
				// also if you refresh the root, no need to refresh anything else
				if (tree.getSelectionModel().getSelectedItems().contains(tree.getRootCell())) {
					for (String reference : repository.rebuildReferences(null, true)) {
						lstNotifications.getItems().add(new ValidationMessage(Severity.INFO, reference));
					}
				}
				else {
					for (TreeCell<Entry> selected : tree.getSelectionModel().getSelectedItems()) {
						for (String reference : repository.rebuildReferences(selected.getItem().itemProperty().get().getId(), true)) {
							lstNotifications.getItems().add(new ValidationMessage(Severity.INFO, reference));
						}
					}
				}
			}
		});
		mniUpdateReference.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void handle(ActionEvent arg0) {
				final List<Entry> artifacts = new ArrayList<Entry>();
				Set<String> references = new TreeSet<String>(); 
				for (TreeCell<Entry> selectedItem : getRepositoryBrowser().getControl().getSelectionModel().getSelectedItems()) {
					Entry entry = selectedItem.getItem().itemProperty().get();
					if (entry.isNode() && entry instanceof ResourceEntry) {
						references.addAll(entry.getNode().getReferences());
						artifacts.add(entry);
						MainController.this.closeAll(entry.getId());
					}
				}
				if (!references.isEmpty()) {
					EnumeratedSimpleProperty<String> oldReferenceProperty = new EnumeratedSimpleProperty<String>("Old Reference", String.class, true);
					oldReferenceProperty.addEnumeration(references);
					SimpleProperty<String> newReferenceProperty = new SimpleProperty<String>("New Reference", String.class, true);
					final SimplePropertyUpdater updater = new SimplePropertyUpdater(
						true, 
						new LinkedHashSet(Arrays.asList(new Property [] { oldReferenceProperty, newReferenceProperty }))
					);
					EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Update Reference", new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent arg0) {
							String oldReference = updater.getValue("Old Reference");
							String newReference = updater.getValue("New Reference");
							if (oldReference != null && newReference != null) {
								List<ValidationMessage> validations = new ArrayList<ValidationMessage>();
								for (Entry entry : artifacts) {
									try {
										validations.addAll(updateReference(entry, oldReference, newReference));
									}
									catch (Exception e) {
										e.printStackTrace();
										validations.add(new ValidationMessage(Severity.ERROR, "Could not update: " + entry.getId()));
									}
								}
								MainController.this.notify(validations);
							}
						}

					});
				}
			}
		});
		
		lstNotifications.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Validation<?>>() {
			@Override
			public void changed(ObservableValue<? extends Validation<?>> arg0, Validation<?> arg1, final Validation<?> arg2) {
				if (validationsId != null && arg2 != null) {
					for (final ArtifactGUIInstance instance : managers.values()) {
						if (validationsId.equals(instance.getId())) {
							if (instance instanceof ValidatableArtifactGUIInstance) {
								Platform.runLater(new Runnable() {
									public void run() {
										((ValidatableArtifactGUIInstance) instance).locate(arg2);
									}
								});
							}
							break;
						}
					}
				}
			}
		});
		lstNotifications.setCellFactory(new Callback<ListView<Validation<?>>, ListCell<Validation<?>>>() {
			@Override 
			public ListCell<Validation<?>> call(ListView<Validation<?>> list) {
				return new ListCell<Validation<?>>() {
					@Override
					protected void updateItem(Validation<?> arg0, boolean arg1) {
						super.updateItem(arg0, arg1);
						setText(arg0 == null ? null : arg0.getMessage());
					}
				};
			}
		});
		
		File styles = new File("styles");
		if (styles != null && styles.exists()) {
			for (File style : styles.listFiles()) {
				if (style.getName().endsWith(".css")) {
					try {
						registerStyleSheet(style.toURI().toURL().toString());
					}
					catch (MalformedURLException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<ValidationMessage> updateReference(Entry entry, String oldReference, String newReference) throws InstantiationException, IllegalAccessException, IOException, FormatException, ParseException {
		List<ValidationMessage> validations = new ArrayList<ValidationMessage>();
		ArtifactManager artifactManager = entry.getNode().getArtifactManager().newInstance();
		try {
			Artifact artifact = entry.getNode().getArtifact();
			validations.addAll(artifactManager.updateReference(artifact, oldReference, newReference));
			artifactManager.save((ResourceEntry) entry, artifact);
			getInstance().getServer().getRemote().reload(artifact.getId());
		}
		catch (Exception e) {
			if (artifactManager instanceof BrokenReferenceArtifactManager) {
				validations.addAll(((BrokenReferenceArtifactManager) artifactManager).updateBrokenReference(((ResourceEntry) entry).getContainer(), oldReference, newReference));
				getInstance().getRepository().reload(entry.getId());
				getInstance().getServer().getRemote().reload(entry.getId());
			}
			else {
				throw e;
			}
		}
		return validations;
	}

	
	public boolean isBrokenReference(String reference) {
		boolean found = getRepository().getEntry(reference) != null && getRepository().getEntry(reference).isNode();
		if (!found) {
			try {
				found = getRepository().getClassLoader().loadClass(reference) != null;
			}
			catch (ClassNotFoundException e) {
				// do nothing
			}
		}
		return !found;
	}
	
	private void locate(String selectedId) {
		TreeItem<Entry> resolved = tree.resolve(selectedId.replace('.', '/'));
		if (resolved != null) {
			TreeCell<Entry> treeCell = tree.getTreeCell(resolved);
			treeCell.select();
			treeCell.show();
			tree.autoscroll();
		}
	}
	
	
	public void refresh(String id) {
		for (Tab tab : managers.keySet()) {
			if (id.equals(tab.getId())) {
				refreshTab(tab);
			}
		}
	}
	
	public void refreshTab(Tab tab) {
		ArtifactGUIInstance guiInstance = managers.get(tab);
		if (guiInstance != null) {
			try {
				AnchorPane pane = new AnchorPane();
				((RefresheableArtifactGUIInstance) guiInstance).refresh(pane);
				tab.setContent(pane);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static boolean isRepositoryTree(Tree<?> tree) {
		return tree.getId() != null && tree.getId().equals("repository");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <C extends Controller, T extends Control> Component<C, T> getComponent(String id) {
		return (Component<C, T>) components.get(id);
	}
	
	public RepositoryBrowser getRepositoryBrowser() {
		return (RepositoryBrowser) components.get("repository");
	}
	
	public void save() throws IOException {
		if (tabArtifacts.getSelectionModel().getSelectedItem() != null) {
			save(tabArtifacts.getSelectionModel().getSelectedItem().getId());
		}
	}
	
	public void save(String id) throws IOException {
		for (ArtifactGUIInstance instance : managers.values()) {
			if (instance.isReady() && instance.getId().equals(id)) {
				if (instance.isEditable()) {
					instance.save();
					if (repositoryValidatorService != null) {
						repositoryValidatorService.clear(id);
					}
					for (Tab tab : managers.keySet()) {
						if (id.equals(tab.getId()) && tab.getText().endsWith("*")) {
							tab.setText(tab.getText().replace(" *", ""));
						}
					}
					try {
						server.getRemote().reload(instance.getId());
					} 
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
	}
	
	/**
	 * Set the current element to changed
	 */
	public void setChanged() {
		if (tabArtifacts.getSelectionModel().getSelectedItem() != null) {
			ArtifactGUIInstance instance = managers.get(tabArtifacts.getSelectionModel().getSelectedItem());
			if (instance != null) {
				instance.setChanged(true);
				String text = tabArtifacts.getSelectionModel().getSelectedItem().getText();
				if (!text.endsWith("*")) {
					tabArtifacts.getSelectionModel().getSelectedItem().setText(text + " *");
				}
			}
		}
	}
	
	public void setChanged(String id) {
		for (Tab tab : managers.keySet()) {
			if (id.equals(tab.getId())) {
				ArtifactGUIInstance instance = managers.get(tab);
				if (instance != null) {
					instance.setChanged(true);
					String text = tab.getText();
					if (!text.endsWith("*")) {
						tab.setText(text + " *");
					}
				}
			}
		}
	}
	
	public Tab newTab(String title) {
		Tab tab = new Tab(title);
		tab.setId(title);
		tabArtifacts.getTabs().add(tab);
		tabArtifacts.selectionModelProperty().get().select(tab);
		return tab;
	}
	
	public Tab newTab(final String id, final ArtifactGUIInstance instance) {
		final Tab tab = new Tab(id);
		tab.setId(id);
		tab.getStyleClass().add(id.replace('.', '_'));
		Entry entry = getRepository().getEntry(id);
		if (entry != null && entry.isNode()) {
			tab.getStyleClass().add(entry.getNode().getArtifactClass().getName().replace('.', '_'));
		}
		tabArtifacts.getTabs().add(tab);
		tabArtifacts.selectionModelProperty().get().select(tab);
		managers.put(tab, instance);
		tab.contentProperty().addListener(new ChangeListener<javafx.scene.Node>() {
			@Override
			public void changed(ObservableValue<? extends javafx.scene.Node> arg0, javafx.scene.Node arg1, javafx.scene.Node arg2) {
				if (arg2 != null) {
					arg2.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent arg0) {
							if (instance instanceof RefresheableArtifactGUIInstance && arg0.getCode() == KeyCode.F5) {
								if (tab.getText().endsWith("*")) {
									tab.setText(tab.getText().replaceAll("[\\s]*\\*$", ""));
								}
								refreshTab(tab);
							}
							else if (arg0.getCode() == KeyCode.F12) {
								setChanged();
							}
							else if (instance instanceof ValidatableArtifactGUIInstance && arg0.getCode() == KeyCode.F2) {
								MainController.this.notify(((ValidatableArtifactGUIInstance) instance).validate());
							}
						}
					});
				}
			}
		});
		return tab;
	}

	public EAIResourceRepository getRepository() {
		return repository;
	}
	
	/**
	 * IMPORTANT: this method was "quick fixed"
	 * In the beginning GUI managers were thought to be stateless but turns out they aren't. They keep state per instance they manage.
	 * Ideally I would've added an artifact gui manager factory but we needed a quick fix (there were already a _lot_ of gui managers and deadlines are approaching)
	 * Because all of the managers however did have an empty constructor, we went for this solution (@2015-12-01)
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<ArtifactGUIManager> getGUIManagers() {
		List<Class<? extends ArtifactGUIManager>> guiManagers = new ArrayList<Class<? extends ArtifactGUIManager>>();
		guiManagers.add(ServiceGUIManager.class);
		guiManagers.add(ServiceInterfaceGUIManager.class);
		guiManagers.add(SimpleTypeGUIManager.class);
		guiManagers.add(TypeGUIManager.class);
		for (Class<?> provided : EAIRepositoryUtils.getImplementationsFor(ArtifactGUIManager.class)) {
			guiManagers.add((Class<ArtifactGUIManager>) provided);
		}
		List<ArtifactGUIManager> newGuiManagers = new ArrayList<ArtifactGUIManager>();
		for (Class<? extends ArtifactGUIManager> manager : guiManagers) {
			try {
				newGuiManagers.add(manager.newInstance());
			}
			catch (Exception e) {
				logger.error("Could not instantiate: " + manager, e);
			}
		}
		return newGuiManagers;
	}
	
	public ArtifactGUIManager<?> getGUIManager(Class<?> type) {
		ArtifactGUIManager<?> closest = null;
		for (ArtifactGUIManager<?> manager : getGUIManagers()) {
			if (manager.getArtifactClass().isAssignableFrom(type)) {
				if (closest == null || closest.getArtifactClass().isAssignableFrom(manager.getArtifactClass())) {
					closest = manager;
				}
			}
		}
		if (closest == null) {
			throw new IllegalArgumentException("No gui manager for type " + type + " in: " + getGUIManagers());
		}
		else {
			return closest;
		}
	}
	
	public static Node loadFixedSizeGraphic(String name) {
		return loadFixedSizeGraphic(name, 25);
	}
	
	public static Node loadFixedSizeGraphic(String name, int size) {
		HBox box = new HBox();
		box.getChildren().add(loadGraphic(name));
		box.setAlignment(Pos.CENTER);
		box.setMinWidth(size);
		box.setMaxWidth(size);
		box.setPrefWidth(size);
		return box;
	}
	
	public static ImageView loadGraphic(String name) {
		return new ImageView(loadImage(name));
	}
	
	private static Map<String, Image> images = new HashMap<String, Image>();

	public static Image loadImage(String name) {
		if (!images.containsKey(name)) {
			InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
			// not found
			if (input == null) {
				// first try the repository classloader
				input = getInstance().getRepository().getClassLoader().getResourceAsStream(name);
				if (input == null) {
					input = Thread.currentThread().getContextClassLoader().getResourceAsStream("default-type.png");
					if (input == null)
						throw new RuntimeException("Can not find the icon for type '" + name + "' and the default is not present either");
				}
			}
			try {
				images.put(name, new Image(input));
			}
			finally {
				try {
					input.close();
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return images.get(name);
	}

	public void setStage(Stage stage) {
		this.stage = stage;
		stage.sceneProperty().addListener(new ChangeListener<Scene>() {
			@Override
			public void changed(ObservableValue<? extends Scene> arg0, Scene arg1, Scene arg2) {
				arg2.addEventHandler(KeyEvent.ANY, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (event.getEventType().equals(KeyEvent.KEY_PRESSED)) {
							activeKeys.add(event.getCode());
						}
						else if (event.getEventType().equals(KeyEvent.KEY_RELEASED)) {
							activeKeys.remove(event.getCode());
						}
					}
				});
			}
		});
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				activeKeys.clear();		
			}
		});
	}

	public Stage getStage() {
		return stage;
	}
	
	public FXMLLoader load(String name, String title, boolean newWindow) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Thread.currentThread().getContextClassLoader().getResource(name));
		loader.load();
		Controller controller = loader.getController();
		if (newWindow) {
			Stage stage = new Stage();
			controller.setStage(stage);
			Parent root = loader.getRoot();
			stage.setScene(new Scene(root));
			stage.setTitle(title);
			stage.initModality(Modality.WINDOW_MODAL);
			stage.initOwner(getStage());
			stage.show();
		}
		else {
			controller.setStage(getStage());
		}
		return loader;
	}

	public Tab getTab(String id) {
		for (Tab tab : tabArtifacts.getTabs()) {
			if (tab.getId().equals(id)) {
				return tab;
			}
		}
		return null;
	}
	
	public ArtifactGUIInstance getArtifactInstance(String id) {
		for (ArtifactGUIInstance instance : managers.values()) {
			if (instance.getId().equals(id)) {
				return instance;
			}
		}
		return null;
	}
	
	public boolean activate(String id) {
		Tab tab = getTab(id);
		if (tab != null) {
			tabArtifacts.selectionModelProperty().get().select(tab);
			return true;
		}
		else {
			return false;
		}
	}
	
	public void notify(Throwable throwable) {
		notify(new ValidationMessage(Severity.ERROR, throwable.getMessage()));
	}
	
	@Override
	public void notify(ValidationMessage...messages) {
		notify(Arrays.asList(messages));
	}
	
	public void notify(List<? extends Validation<?>> messages) {
		lstNotifications.getItems().clear();
		if (tabArtifacts.getSelectionModel().getSelectedItem() != null) {
			ArtifactGUIInstance instance = managers.get(tabArtifacts.getSelectionModel().getSelectedItem());
			validationsId = instance.getId();
		}
		if (messages != null) {
			lstNotifications.getItems().addAll(messages);
		}
	}
	
	public void showProperties(final PropertyUpdater updater) {
		showProperties(updater, ancProperties, true);
	}
	
	public Pane showProperties(final PropertyUpdater updater, final Pane target, final boolean refresh) {
		return showProperties(updater, target, refresh, getRepository());
	}
	
	private boolean isInTab(Pane target) {
		boolean isInTab = false;
		Parent parent = target.getParent();
		List<Node> tabContents = new ArrayList<Node>();
		for (Tab tab : tabArtifacts.getTabs()) {
			tabContents.add(tab.getContent());
		}
		while (parent != null) {
			if (tabContents.contains(parent)) {
				isInTab = true;
				break;
			}
			else {
				parent = parent.getParent();
			}
		}
		return isInTab;
	}
	
	public Pane showProperties(final PropertyUpdater updater, final Pane target, final boolean refresh, Repository repository) {
		return showProperties(updater, target, refresh, repository, isInTab(target));
	}
	
	public Pane showProperties(final PropertyUpdater updater, final Pane target, final boolean refresh, Repository repository, boolean updateChanged) {
		final GridPane grid = new GridPane();
		grid.getStyleClass().add("propertyPane");
		grid.setVgap(5);
		grid.setHgap(10);
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setMinWidth(150);
		grid.getColumnConstraints().add(column1);
		SinglePropertyDrawer gridDrawer = new SinglePropertyDrawer() {
			int row = 0;
			@Override
			public void draw(Node label, Node value, Node additional) {
				grid.add(label, 0, row);
				grid.add(value, 1, row);
				if (additional != null) {
					grid.add(additional, 2, row);	
				}
				GridPane.setHalignment(label, HPos.RIGHT);
				if (value instanceof TextInputControl) {
					GridPane.setHgrow(value, Priority.ALWAYS);
				}
				row++;
			}
		};
		PropertyRefresher refresher = new PropertyRefresher() {
			@Override
			public void refresh() {
				showProperties(updater, target, refresh, repository);
			}
		};
		for (final Property<?> property : updater.getSupportedProperties()) {
			drawSingleProperty(updater, property, refresh ? refresher : null, gridDrawer, repository, updateChanged);
		}
		boolean found = false;
		for (int i = 0; i < target.getChildren().size(); i++) {
			if (target.getChildren().get(i) instanceof GridPane) {
				target.getChildren().set(i, grid);
				found = true;
				break;
			}
		}
		if (!found) {
			target.getChildren().clear();
			target.getChildren().add(grid);
		}
		if (target instanceof AnchorPane) {
			AnchorPane.setLeftAnchor(grid, 0d);
			AnchorPane.setRightAnchor(grid, 0d);
		}
		return grid;
	}

	public static interface SinglePropertyDrawer {
		public void draw(Node label, Node value, Node additional);
	}
	
	public static interface PropertyRefresher {
		public void refresh();
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void drawSingleProperty(final PropertyUpdater updater, final Property<?> property, PropertyRefresher refresher, SinglePropertyDrawer drawer, Repository repository, boolean updateChanged) {
		Label name = new Label(property.getName() + ": " + (updater.isMandatory(property) ? " *" : ""));
		String superTypeName = null;
		boolean allowSuperType = true;
		if (property.equals(SuperTypeProperty.getInstance())) {
			Type superType = ValueUtils.getValue(SuperTypeProperty.getInstance(), updater.getValues());
			if (superType != null) {
				if (!(superType instanceof DefinedType)) {
					allowSuperType = false;
				}
				else {
					superTypeName = ((DefinedType) superType).getId();
				}
			}
		}
		Object originalValue = ValueUtils.getValue(property, updater.getValues());
		
		final String currentValue = property.equals(SuperTypeProperty.getInstance())
			? superTypeName
			: (originalValue instanceof String || originalValue instanceof File || originalValue instanceof byte[] ? originalValue.toString() : converter.convert(originalValue, String.class));

		// if we can't convert from a string to the property value, we can't show it
		if (updater.canUpdate(property) && ((property.equals(new SuperTypeProperty()) && allowSuperType) || !property.equals(new SuperTypeProperty()))) {
			if (File.class.equals(property.getValueClass())) {
				File current = (File) originalValue;
				Button choose = new Button("Choose File");
				final Label label = new Label();
				if (current != null) {
					label.setText(current.getAbsolutePath());
				}
				choose.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent arg0) {
						FileChooser fileChooser = new FileChooser();
						if (lastDirectoryUsed != null) {
							fileChooser.setInitialDirectory(lastDirectoryUsed);
						}
						File file = !(property instanceof SimpleProperty) || !((SimpleProperty) property).isInput() ? fileChooser.showSaveDialog(stage) : fileChooser.showOpenDialog(stage);
						if (file != null) {
							lastDirectoryUsed = file.isDirectory() ? file : file.getParentFile();
							updater.updateProperty(property, file);
							label.setText(file.getAbsolutePath());
							if (refresher != null) {
								refresher.refresh();
							}
						}
					}
				});
				HBox box = new HBox();
				box.getChildren().addAll(choose, label);
				drawer.draw(name, box, null);
			}
			else if (byte[].class.equals(property.getValueClass())) {
				Button choose = new Button("Choose File");
				final Label label = new Label("Empty");
				if (originalValue != null) {
					label.setText("Currently: " + ((byte[]) originalValue).length + " bytes");
				}
				choose.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent arg0) {
						FileChooser fileChooser = new FileChooser();
						if (lastDirectoryUsed != null) {
							fileChooser.setInitialDirectory(lastDirectoryUsed);
						}
						File file = fileChooser.showOpenDialog(stage);
						if (file != null) {
							lastDirectoryUsed = file.isDirectory() ? file : file.getParentFile();
							try {
								InputStream input = new BufferedInputStream(new FileInputStream(file));
								try {
									byte[] bytes = IOUtils.toBytes(IOUtils.wrap(input));
									updater.updateProperty(property, bytes);
									label.setText(file.getAbsolutePath() + ": " + bytes.length + " bytes");
									setChanged();
								}
								finally {
									input.close();
								}
							}
							catch (IOException e) {
								MainController.this.notify(new ValidationMessage(Severity.ERROR, "Failed to load file: " + e.getMessage()));
								logger.error("Could not load file", e);
							}
						}
					}
				});
				Button clear = new Button("Clear");
				clear.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent arg0) {
						if (ValueUtils.getValue(property, updater.getValues()) != null) {
							updater.updateProperty(property, null);
							setChanged();
							label.setText("Empty");
						}
					}
				});
				HBox box = new HBox();
				box.getChildren().addAll(choose, clear, label);
				drawer.draw(name, box, null);
			}
			else if (property instanceof Enumerated || Boolean.class.equals(property.getValueClass()) || Enum.class.isAssignableFrom(property.getValueClass()) || Artifact.class.isAssignableFrom(property.getValueClass())) {
				final ComboBox<String> comboBox = new ComboBox<String>();
				
				boolean sort = false;
				CheckBox filterByApplication = null;
				comboBox.setEditable(true);
				Collection<?> values;
				if (property instanceof Enumerated) {
					values = ((Enumerated<?>) property).getEnumerations();
				}
				else if (Boolean.class.equals(property.getValueClass())) {
					values = Arrays.asList(Boolean.TRUE, Boolean.FALSE);
				}
				else if (Artifact.class.isAssignableFrom(property.getValueClass())) {
					sort = true;
					Collection<Artifact> artifacts = repository.getArtifacts((Class<Artifact>) property.getValueClass());
					if (property instanceof Filter) {
						artifacts = ((Filter<Artifact>) property).filter(artifacts);
					}
					if (updater instanceof PropertyUpdaterWithSource && ((PropertyUpdaterWithSource) updater).getSourceId() != null) {
						filterByApplication = new CheckBox();
						filterByApplication.setSelected(true);
						filterByApplication.setTooltip(new Tooltip("Filter by application"));
					}
					values = artifacts;
				}
				else {
					values = Arrays.asList(property.getValueClass().getEnumConstants());
				}
				
				// if simple type, add the repository listing
				if (SimpleType.class.isAssignableFrom(property.getValueClass())) {
					List definedTypes = new ArrayList();
					for (Artifact artifact : repository.getArtifacts(DefinedSimpleType.class)) {
						definedTypes.add(artifact);
					}
					values = new ArrayList(values);
					values.addAll(definedTypes);
				}
				
				// add null to allow deselection
				comboBox.getItems().add(0, null);
				// always add the current value first (null is already added)
				if (currentValue != null) {
					comboBox.getItems().add(currentValue);
				}
				// and select it
				comboBox.getSelectionModel().select(currentValue);
				// fill it
				for (Object value : values) {
					if (value == null) {
						continue;
					}
					else if (!converter.canConvert(value.getClass(), String.class)) {
						throw new ClassCastException("Can not convert " + value.getClass() + " to string");
					}
					String converted = converter.convert(value, String.class);
					if (!converted.equals(currentValue)) {
						comboBox.getItems().add(converted);
					}
				}
				if (sort) {
					Collections.sort(comboBox.getItems(), new StringComparator());
				}
				
				if (filterByApplication != null) {
					final String sourceId = ((PropertyUpdaterWithSource) updater).getSourceId();
					final List<String> filteredArtifacts = new ArrayList<String>(getItemsToFilterByApplication(comboBox.getItems(), sourceId));
					filteredArtifacts.remove(currentValue);
					filterByApplication.selectedProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
							if (!arg2) {
								comboBox.getItems().addAll(filteredArtifacts);
//									Collections.sort(comboBox.getItems(), new StringComparator());
							}
							else {
								comboBox.getItems().removeAll(filteredArtifacts);
							}
						}
					});
					if (filterByApplication.isSelected()) {
						comboBox.getItems().removeAll(filteredArtifacts);
						comboBox.getSelectionModel().select(currentValue);
					}
				}

				comboBox.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<String>() {
					@Override
					public void changed(ObservableValue<? extends String> arg0, String arg1, String newValue) {
						if (!parseAndUpdate(updater, property, newValue, repository, updateChanged)) {
							comboBox.getSelectionModel().select(arg1);
						}
						else if (refresher != null) {
							refresher.refresh();
						}
					}
				});
				drawer.draw(name, comboBox, filterByApplication);
			}
			else if (Date.class.isAssignableFrom(property.getValueClass())) {
				DatePicker dateField = new DatePicker();
				dateField.setPrefWidth(300);
				if (currentValue != null) {
					try {
						((DatePicker) dateField).setDate(new DateTimeFormat().parse(currentValue));
					}
					catch (ParseException e) {
						notify(e);
					}
				}
				dateField.timestampProperty().addListener(new ChangeListener<Long>() {
					@Override
					public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
						updater.updateProperty(property, dateField.getDate());
						if (updateChanged) {
							setChanged();
						}
						if (refresher != null) {
							refresher.refresh();
						}
					}
				});
				drawer.draw(name, dateField, null);
			}
			else {
				final TextInputControl textField = (currentValue != null && currentValue.contains("\n")) || (property instanceof SimpleProperty && ((SimpleProperty) property).isLarge()) ? new TextArea(currentValue) : (property instanceof SimpleProperty && ((SimpleProperty) property).isPassword() ? new PasswordField() : new TextField(currentValue));
				System.out.println("DRAWING: " + property.getName() + " / " + textField + " / " + currentValue);
				try{throw new RuntimeException("foemp");}catch(Exception e){e.printStackTrace();}
				if (textField instanceof TextArea && currentValue != null) {
					((TextArea) textField).setPrefRowCount(Math.min(((TextArea) textField).getPrefRowCount(), currentValue.length() - currentValue.replace("\n", "").length() + 1));
				}
				ChangeListener<Boolean> changeListener = new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
						if (arg2 != null && !arg2) {
							if (!parseAndUpdate(updater, property, textField.getText(), repository, updateChanged)) {
								textField.setText(currentValue);
							}
							else if (refresher != null) {
								// refresh basically, otherwise the final currentValue will keep pointing at the old one
								refresher.refresh();
							}
						}
					}
				};
				textField.focusedProperty().addListener(changeListener);
				textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.ENTER && event.isShiftDown() && textField instanceof TextField) {
							if (parseAndUpdate(updater, property, textField.getText() + "\n", repository, updateChanged) && refresher != null) {
								textField.focusedProperty().removeListener(changeListener);
								refresher.refresh();
							}
						}
						else if (event.getCode() == KeyCode.ENTER && (textField instanceof TextField || event.isControlDown())) {
							if (!parseAndUpdate(updater, property, textField.getText(), repository, updateChanged)) {
								textField.setText(currentValue);
							}
							else if (refresher != null) {
								// refresh basically, otherwise the final currentValue will keep pointing at the old one
								textField.focusedProperty().removeListener(changeListener);
								refresher.refresh();
							}
							event.consume();
						}
						// we added an enter to a text area, resize it
						else if (event.getCode() == KeyCode.ENTER && textField instanceof TextArea) {
							((TextArea) textField).setPrefRowCount(textField.getText().length() - textField.getText().replace("\n", "").length() + 1);
						}
					}
				});
				// when we lose focus, set it as well
				drawer.draw(name, textField, null);
			}
		}
		else if (currentValue != null) {
			Label value = new Label(currentValue);
			drawer.draw(name, value, null);
		}
	}
	
	private static List<String> getItemsToFilterByApplication(List<String> entries, String sourceId) {
		String application = sourceId.replaceAll("\\..*$", "");
		List<String> filtered = new ArrayList<String>();
		for (String entry : entries) {
			if (entry != null && !entry.equals(application) && !entry.startsWith(application + ".")) {
				filtered.add(entry);
			}
		}
		return filtered;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean parseAndUpdate(PropertyUpdater updater, Property<?> property, String value, Repository repository, boolean updateChanged) {
		try {
			if (value != null && value.isEmpty()) {
				value = null;
			}
			Object parsed;
			// hardcoded exception for max occurs
			if (property.equals(new MaxOccursProperty()) && value.equals("unbounded")) {
				parsed = 0;
			}
			// hardcoded exception for superType
			else if (property.equals(new SuperTypeProperty()) && value != null) {
				parsed = typeResolver.resolve(value);
			}
			else if (property instanceof EvaluatableProperty && ((EvaluatableProperty<?>) property).isEvaluatable() && value != null && value.startsWith("=")) {
				// TODO: can try to validate the query
				parsed = value;
			}
			// the converter will use the "default" repository but we want to resolve with the specific repository so shortcut it here
			else if (Artifact.class.isAssignableFrom(property.getValueClass()) && value != null) {
				parsed = repository.resolve(value);
			}
			else if (Class.class.isAssignableFrom(property.getValueClass()) && value != null) {
				try {
					parsed = this.repository.getClassLoader().loadClass(value);
				}
				catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			else {
				parsed = converter.convert(value, property.getValueClass());
			}
			if (value != null && parsed == null) {
				notify(new ValidationMessage(Severity.ERROR, "There is no suitable converter for the target type " + property.getValueClass().getName()));
				return false;
			}
			Validator validator = property.getValidator();
			if (validator != null) {
				List<ValidationMessage> messages = validator.validate(parsed);
				if (messages.size() > 0) {
					notify(messages.toArray(new ValidationMessage[0]));
					// check if there are errors or only warnings
					for (ValidationMessage message : messages) {
						if (Severity.ERROR.equals(message.getSeverity())) {
							return false;
						}
					}
				}
			}
			Object currentValue = ValueUtils.getValue(property, updater.getValues());
			// only push an update if it's changed
			if ((currentValue == null && parsed != null) || (currentValue != null && !currentValue.equals(parsed))) {
				System.out.println("UPDATING: " + property + " = " + parsed);
				updater.updateProperty(property, parsed);
				if (updateChanged) {
					setChanged();
				}
			}
			return true;
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			notify(new ValidationMessage(Severity.ERROR, "Could not parse the value '" + value + "'"));
			return false;
		}
	}
	
	public static interface PropertyUpdater {
		public Set<Property<?>> getSupportedProperties();
		public Value<?> [] getValues();
		public boolean canUpdate(Property<?> property);
		public List<ValidationMessage> updateProperty(Property<?> property, Object value);
		public boolean isMandatory(Property<?> property);
	}
	
	public static interface PropertyUpdaterWithSource extends PropertyUpdater {
		public String getSourceId();
		public Repository getRepository();
	}
	
	public void showContent(ComplexContent content) {
		showContent(content, null);
	}
	
	
	private Map<String, TypeOperation> analyzedOperations = new HashMap<String, TypeOperation>();
	public TypeOperation getOperation(String query) {
		if (!analyzedOperations.containsKey(query)) {
			synchronized(analyzedOperations) {
				if (!analyzedOperations.containsKey(query)) {
					try {
						analyzedOperations.put(query, (TypeOperation) new PathAnalyzer<ComplexContent>(new TypesOperationProvider()).analyze(QueryParser.getInstance().parse(query)));
					}
					catch (ParseException e) {
						notify(new ValidationMessage(Severity.ERROR, "Could not parse: " + query));
						return null;
					}
				}
			}
		}
		return analyzedOperations.get(query);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void showContent(ComplexContent content, String query) {
		ancPipeline.getChildren().clear();
		final ComplexContent original = content;
		if (query != null) {
			TypeOperation operation = getOperation(query);
			if (operation != null) {
				try {
					Object evaluate = operation.evaluate(content);
					if (evaluate == null) {
						content = null;
					}
					else if (evaluate instanceof ComplexContent) {
						content = (ComplexContent) evaluate;
					}
					else {
						Structure structure = new Structure();
						structure.setName("query");
						Object toCheck = evaluate;
						if (evaluate instanceof Iterable) {
							Iterator iterator = ((Iterable) evaluate).iterator();
							toCheck = iterator.hasNext() ? iterator.next() : null;
						}
						boolean straightSet = true;
						if (toCheck instanceof ComplexContent) {
							ComplexContent tmp = (ComplexContent) toCheck;
							structure.add(new ComplexElementImpl("results", tmp.getType(), structure, new ValueImpl<Integer>(MaxOccursProperty.getInstance(), 0)));
						}
						else {
							DefinedSimpleType<? extends Object> wrap = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(toCheck.getClass());
							if (wrap != null) {
								structure.add(new SimpleElementImpl("results", wrap, structure, new ValueImpl<Integer>(MaxOccursProperty.getInstance(), 0)));
							}
							else {
								ComplexContent tmp = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(toCheck);
								structure.add(new ComplexElementImpl("results", tmp.getType(), structure, new ValueImpl<Integer>(MaxOccursProperty.getInstance(), 0)));
								straightSet = false;
							}
						}
						content = structure.newInstance();
						if (straightSet) {
							content.set("results", evaluate);
						}
						else {
							int index = 0;
							for (Object single : (Iterable) evaluate) {
								content.set("results[" + index++ + "]", ComplexContentWrapperFactory.getInstance().getWrapper().wrap(single));
							}
						}
					}
				}
				catch (EvaluationException e) {
					notify(new ValidationMessage(Severity.ERROR, "Could not evaluate: " + query));
					e.printStackTrace();
				}
			}
		}
		if (content != null) {
			Tree<Object> contentTree = new Tree<Object>(new Callback<TreeItem<Object>, TreeCellValue<Object>>() {
				@Override
				public TreeCellValue<Object> call(final TreeItem<Object> item) {
					return new TreeCellValue<Object>() {
						private ObjectProperty<TreeCell<Object>> cell = new SimpleObjectProperty<TreeCell<Object>>();
						private HBox hbox;
						@Override
						public ObjectProperty<TreeCell<Object>> cellProperty() {
							return cell;
						}
						@Override
						public Region getNode() {
							if (hbox == null) {
								hbox = new HBox();
								Label labelName = new Label(item.getName());
								labelName.getStyleClass().add("contentName");
								hbox.getChildren().add(labelName);
								if (item.leafProperty().get()) {
									ContentTreeItem contentTreeItem = (ContentTreeItem) item;
									Type type = contentTreeItem.getDefinition().getType();
									while (type != null) {
										if (type instanceof be.nabu.libs.types.api.Marshallable) {
											final Label value = new Label(
												((be.nabu.libs.types.api.Marshallable) type).marshal(item.itemProperty().get(), contentTreeItem.getDefinition().getProperties()
											));
											newTextContextMenu(value, value.getText());
											value.getStyleClass().add("contentValue");
											hbox.getChildren().add(value);
											break;
										}
										type = type.getSuperType();
									}
									// we never found a marshallable...
									if (type == null) {
										hbox.getChildren().add(new Label(contentTreeItem.itemProperty().get().getClass().getName()));
									}
								}
							}
							return hbox;
						}
						
						@Override
						public void refresh() {
							hbox = null;
						}
					};
				}
			});
			
			VBox vbox = new VBox();
			TextField field = new TextField(query == null ? "" : query);
			field.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						showContent(original, field.getText().trim().isEmpty() ? null : field.getText());
					}
				}
			});
			VBox.setVgrow(contentTree, Priority.ALWAYS);
			VBox.setVgrow(field, Priority.NEVER);
			vbox.getChildren().addAll(field, contentTree);
			contentTree.prefWidthProperty().bind(vbox.widthProperty());
			// resize everything
			AnchorPane.setLeftAnchor(vbox, 0d);
			AnchorPane.setRightAnchor(vbox, 0d);
			AnchorPane.setTopAnchor(vbox, 0d);
			AnchorPane.setBottomAnchor(vbox, 0d);
			if (!ancPipeline.prefWidthProperty().isBound()) {
				ancPipeline.prefWidthProperty().bind(((Pane) ancPipeline.getParent()).widthProperty()); 
			}
			contentTree.rootProperty().set(new ContentTreeItem(new RootElement(content.getType()), content, null, false, null));
//			contentTree.getTreeCell(contentTree.rootProperty().get()).collapseAll();
			contentTree.getTreeCell(contentTree.rootProperty().get()).expandedProperty().set(true);
			ancPipeline.getChildren().add(vbox);
		}
		else {
			ancPipeline.getChildren().add(new Label("null"));
		}
	}
	
	public static void newTextContextMenu(final Control target, String text) {
		ContextMenu contextMenu = new ContextMenu();
		CustomMenuItem item = new CustomMenuItem();
		final TextInputControl textField = text != null && text.contains("\n") ? new TextArea(text) : new TextField(text);
		textField.setEditable(false);
		// this prevents context menu from closing when you click on the text field (allowing you for example to select parts)
		textField.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
			}
		});
		// this prevents the context menu from gaining focus when you move over the text field
		textField.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				textField.requestFocus();
				event.consume();
			}
		});
		item.setContent(textField);
		contextMenu.getItems().add(item);
		target.setContextMenu(contextMenu);
		target.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				target.getContextMenu().show(target, Side.BOTTOM, 0, 0);
				textField.selectAll();
				textField.requestFocus();
				event.consume();
			}
		});
	}
	
	public AnchorPane getAncPipeline() {
		if (!ancPipeline.prefWidthProperty().isBound()) {
			ancPipeline.prefWidthProperty().bind(((Pane) ancPipeline.getParent()).widthProperty()); 
		}
		if (!ancPipeline.prefHeightProperty().isBound()) {
			ancPipeline.prefHeightProperty().bind(((Pane) ancPipeline.getParent()).heightProperty()); 
		}
		return ancPipeline;
	}

	public Tree<Entry> getTree() {
		return tree;
	}
	
	public void close(String id) {
		// close any tab that is a child of this because it will be out of sync
		Iterator<Tab> iterator = tabArtifacts.getTabs().iterator();
		while (iterator.hasNext()) {
			Tab tab = iterator.next();
			if (id.equals(tab.getId())) {
				managers.remove(tab);
				iterator.remove();
			}
		}
		closeAll(id);
	}
	
	public void closeAll(String idToClose) {
		// close any tab that is a child of this because it will be out of sync
		Iterator<Tab> iterator = tabArtifacts.getTabs().iterator();
		while (iterator.hasNext()) {
			Tab tab = iterator.next();
			String id = tab.getId();
			if (id.startsWith(idToClose + ".") || id.equals(idToClose)) {
				managers.remove(tab);
				iterator.remove();
			}
		}	
	}
	
	public static void copy(Object object) {
		ClipboardContent clipboard = buildClipboard(object);
		if (clipboard != null) {
			Clipboard.getSystemClipboard().setContent(clipboard);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ClipboardContent buildClipboard(Object...objects) {
		ClipboardContent clipboard = new ClipboardContent();
		for (Object object : objects) {
			DataFormat format = null;
			String stringRepresentation = null;
			
			boolean foundDedicated = false;
			if (object != null) {
				// check for type-specific handling
				for (ClipboardProvider provider : getInstance().getClipboardProviders()) {
					if (provider.getClipboardClass().isAssignableFrom(object.getClass())) {
						stringRepresentation = provider.serialize(object);
						object = stringRepresentation;
						format = TreeDragDrop.getDataFormat(provider.getDataType());
						foundDedicated = true;
						break;
					}
				}
			}
			if (!foundDedicated) {
				if (object instanceof DefinedType) {
					format = TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_DEFINED);
					stringRepresentation = ((DefinedType) object).getId();
					object = stringRepresentation;
				}
				else if (object instanceof TreeItem && ((TreeItem<?>) object).itemProperty().get() instanceof Element) {
					format = TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_ELEMENT);
					stringRepresentation = TreeUtils.getPath((TreeItem<?>) object);
					// remove the root if it is called pipeline as we always act on the root object
					if (stringRepresentation.startsWith("pipeline")) {
						stringRepresentation = stringRepresentation.replaceFirst("^[^/]+/", "");
					}
					TreeItem<Element<?>> item = (TreeItem<Element<?>>) object;
					Element<?> element = item.itemProperty().get();
					serializeElement(clipboard, element);
					object = element.getType() instanceof DefinedType ? ((DefinedType) element.getType()).getId() : stringRepresentation;
				}
				else if (object instanceof Element && ((Element<?>) object).getType() instanceof DefinedType) {
					serializeElement(clipboard, object);
					format = TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_ELEMENT);
					stringRepresentation = ((DefinedType) ((Element<?>) object).getType()).getId();
					object = stringRepresentation;
				}
				else if (object instanceof DefinedService) {
					format = TreeDragDrop.getDataFormat(ServiceGUIManager.DATA_TYPE_SERVICE);
					stringRepresentation = ((DefinedService) object).getId();
					object = stringRepresentation;
				}
				else if (object instanceof Artifact) {
					stringRepresentation = ((Artifact) object).getId();
				}
			}
			if (format != null) {
				clipboard.put(format, object);
			}
			if (stringRepresentation != null) {
				clipboard.put(DataFormat.PLAIN_TEXT, stringRepresentation);
			}
		}
		return clipboard.size() == 0 ? null : clipboard;
	}

	@SuppressWarnings("unchecked")
	private static void serializeElement(ClipboardContent clipboard, Object object) {
		try {
			Map<String, Object> element = new HashMap<String, Object>();
			element.put("$type", ((DefinedType) ((Element<?>) object).getType()).getId());
			List<Value<?>> values = new ArrayList<Value<?>>(Arrays.asList(((Element<?>) object).getProperties()));
			// remove properties from type, we are using defined types
			values.removeAll(Arrays.asList(((Element<?>) object).getType().getProperties()));
			for (Value<?> value : values) {
				if (value.getProperty().equals(CollectionHandlerProviderProperty.getInstance())) {
					continue;
				}
				element.put(value.getProperty().getName(), value.getValue());
			}
			clipboard.put(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_SERIALIZED_ELEMENT), element);
			DataFormat listFormat = TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_SERIALIZED_ELEMENT_LIST);
			if (clipboard.get(listFormat) == null) {
				clipboard.put(listFormat, new ArrayList<Map<String, Object>>());
			}
			((List<Map<String, Object>>) clipboard.get(listFormat)).add(element);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private List<ClipboardProvider<?>> clipboardProviders;
	
	public List<ClipboardProvider<?>> getClipboardProviders() {
		if (clipboardProviders == null) {
			clipboardProviders = new ArrayList<ClipboardProvider<?>>();
			for (Class<?> provider : EAIRepositoryUtils.getImplementationsFor(ClipboardProvider.class)) {
				try {
					clipboardProviders.add((ClipboardProvider<?>) provider.newInstance());
				}
				catch (Exception e) {
					logger.error("Could not create clipboard provider: " + provider, e);
				}
			}
		}
		return clipboardProviders;
	}
	
	public static Object paste(String dataType) {
		return Clipboard.getSystemClipboard().getContent(TreeDragDrop.getDataFormat(dataType));
	}

	public ServerConnection getServer() {
		return server;
	}

	public Map<String, Object> getState() {
		return state;
	}
	
	public Object getState(Class<?> clazz, String name) {
		return state.get(clazz.getName() + "." + name);
	}
	
	public void setState(Class<?> clazz, String name, Object value) {
		state.put(clazz.getName() + "." + name, value);
	}

	public void refresh() {
		// nothing atm
	}
	
	private static Properties properties;

	private TrayIcon trayIcon;

	private RepositoryValidatorService repositoryValidatorService;
	
	public static Properties getProperties() {
		if (properties == null) {
			properties = new Properties();
			File file = new File("developer.properties");
			if (file.exists()) {
				try {
					InputStream input = new BufferedInputStream(new FileInputStream(file));
					try {
						properties.load(input);
					}
					finally {
						input.close();
					}
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return properties;
	}
	
	public static void saveProperties() {
		Properties properties = getProperties();
		File file = new File("developer.properties");
		try {
			OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
			try {
				properties.store(output, "");
			}
			finally {
				output.close();
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void registerStyleSheet(String name) {
		if (!getInstance().getStage().getScene().getStylesheets().contains(name)) {
			getInstance().getStage().getScene().getStylesheets().add(name);
		}
	}
	
	public void close() {
		if (trayIcon != null) {
			SystemTray.getSystemTray().remove(trayIcon);
		}
	}

	public EventDispatcher getDispatcher() {
		return dispatcher;
	}
	
	public String getCurrentArtifactId() {
		Tab selectedItem = tabArtifacts.getSelectionModel().getSelectedItem();
		return selectedItem == null ? null : selectedItem.getId();
	}
	
	public ArtifactGUIInstance getCurrentInstance() {
		Tab selectedItem = tabArtifacts.getSelectionModel().getSelectedItem();
		return managers.get(selectedItem);
	}

	public boolean isKeyActive(KeyCode code) {
		return activeKeys.contains(code);
	}
}
