package be.nabu.eai.developer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

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
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.managers.BrokerClientGUIManager;
import be.nabu.eai.developer.managers.DefinedHTTPServerGUIManager;
import be.nabu.eai.developer.managers.JDBCPoolGUIManager;
import be.nabu.eai.developer.managers.JDBCServiceGUIManager;
import be.nabu.eai.developer.managers.KeyStoreGUIManager;
import be.nabu.eai.developer.managers.ProxyGUIManager;
import be.nabu.eai.developer.managers.ServiceGUIManager;
import be.nabu.eai.developer.managers.ServiceInterfaceGUIManager;
import be.nabu.eai.developer.managers.StructureGUIManager;
import be.nabu.eai.developer.managers.SubscriptionGUIManager;
import be.nabu.eai.developer.managers.TypeGUIManager;
import be.nabu.eai.developer.managers.UMLTypeRegistryGUIManager;
import be.nabu.eai.developer.managers.VMServiceGUIManager;
import be.nabu.eai.developer.managers.WSDLClientGUIManager;
import be.nabu.eai.developer.managers.WebArtifactGUIManager;
import be.nabu.eai.developer.managers.WebRestArtifactGUIManager;
import be.nabu.eai.developer.managers.XMLSchemaTypeRegistryGUIManager;
import be.nabu.eai.developer.managers.util.ContentTreeItem;
import be.nabu.eai.developer.util.StringComparator;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Node;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.VMServiceManager;
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
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Enumerated;
import be.nabu.libs.property.api.Filter;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.resources.api.ResourceRoot;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.vm.api.Step;
import be.nabu.libs.services.vm.step.Sequence;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.DefinedTypeResolver;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.structure.SuperTypeProperty;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import be.nabu.libs.validator.api.Validator;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.mime.impl.FormatException;

/**
 * TODO: apparently the panes are not scrollable by default, need to add it?
 * http://docs.oracle.com/javafx/2/ui_controls/scrollbar.htm
 * Wtf happened to setScrollable(true) ?
 */
public class MainController implements Initializable, Controller {

	public static final String DATA_TYPE_NODE = "repository-node";
	
	@FXML
	private AnchorPane ancLeft, ancMiddle, ancProperties, ancPipeline;
	
	@FXML
	private TabPane tabArtifacts;
	
	@FXML
	private ListView<String> lstNotifications;
	
	@FXML
	private MenuItem mniClose, mniSave, mniCloseAll, mniSaveAll, mniRebuildReferences;
	
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
	
	public void connect(ServerConnection server) {
		this.server = server;
		// create repository
		try {
			ResourceRoot resourceRoot = ResourceFactory.getInstance().resolve(server.getRepositoryRoot(), null);
			if (resourceRoot == null) {
				throw new RuntimeException("Could not find the repository root: " + server.getRepositoryRoot());
			}
			ResourceRoot mavenRoot = ResourceFactory.getInstance().resolve(server.getMavenRoot(), null);
			if (mavenRoot == null) {
				throw new RuntimeException("Could not find the maven root: " + server.getMavenRoot());
			}
			repository = new EAIResourceRepository((ResourceContainer<?>) resourceRoot, (ResourceContainer<?>) mavenRoot);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		repository.setServiceRunner(server.getRemote());
		repository.start();
		tree = new Tree<Entry>(new Marshallable<Entry>() {
			@Override
			public String marshal(Entry entry) {
				if (entry.isEditable() || showExactName) {
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
				closeAll(entry.getId());
				try {
					MainController.this.notify(repository.move(entry.getId(), entry.getId().replaceAll("[^.]+$", newName), true));
				}
				catch (IOException e1) {
					e1.printStackTrace();
					return treeCell.getItem().itemProperty().get();
				}
				treeCell.getParent().getItem().itemProperty().get().refresh();
				// reload the repository
				getRepository().reload(treeCell.getParent().getItem().itemProperty().get().getId());
				// refresh the tree
				treeCell.getParent().refresh();
				try {
					server.getRemote().reload(treeCell.getParent().getItem().itemProperty().get().getId());
				}
				catch (Exception e) {
					logger.error("Could not reload renamed items on server", e);
				}
				return treeCell.getParent().getItem().itemProperty().get().getChild(newName);
			}
		});
		// allow you to move items in the tree by drag/dropping them (drag is currently in RepositoryBrowser for legacy reasons
		TreeDragDrop.makeDroppable(tree, new TreeDropListener<Entry>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean canDrop(String dataType, TreeCell<Entry> target, TreeCell<?> dragged, TransferMode transferMode) {
				Entry entry = target.getItem().itemProperty().get();
				return entry instanceof ResourceEntry && ((ResourceEntry) entry).getContainer() instanceof ManageableContainer
					// no item must exist with that name
					&& ((ResourceEntry) entry).getContainer().getChild(((TreeCell<Entry>) dragged).getItem().getName()) == null;
			}
			@SuppressWarnings("unchecked")
			@Override
			public void drop(String arg0, TreeCell<Entry> target, TreeCell<?> dragged, TransferMode arg3) {
				Entry original = ((TreeCell<Entry>) dragged).getItem().itemProperty().get();
				try {
					String originalParentId = ((TreeCell<Entry>) dragged).getParent().getItem().itemProperty().get().getId();
					repository.move(
						original.getId(), 
						target.getItem().itemProperty().get().getId() + "." + original.getName(), 
						true);
					// refresh the tree
					target.getParent().refresh();
					dragged.getParent().refresh();
					// reload remotely
					try {
						server.getRemote().reload(originalParentId);
						server.getRemote().reload(target.getItem().itemProperty().get().getId());
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
		// create the browser
		components.put(tree.getId(), new RepositoryBrowser(server).initialize(this, tree));
		AnchorPane.setLeftAnchor(tree, 0d);
		AnchorPane.setRightAnchor(tree, 0d);
		AnchorPane.setTopAnchor(tree, 0d);
		AnchorPane.setBottomAnchor(tree, 0d);
	}
	
	public static MainController getInstance() {
		return instance;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		instance = this;
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
		mniSave.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (tabArtifacts.getSelectionModel().selectedItemProperty().isNotNull().get()) {
					Tab selected = tabArtifacts.getSelectionModel().getSelectedItem();
					ArtifactGUIInstance instance = managers.get(selected);
					if (instance == null) {
						throw new RuntimeException("This tab is not managed");
					}
					if (instance.isReady() && instance.isEditable() && instance.hasChanged()) {
						try {
							System.out.println("Saving " + selected.getId());
							instance.save();
							String text = selected.getText();
							selected.setText(text.replaceAll("[\\s]*\\*$", ""));
							instance.setChanged(false);
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
				for (Tab tab : managers.keySet()) {
					ArtifactGUIInstance instance = managers.get(tab);
					if (instance.isReady() && instance.isEditable() && instance.hasChanged()) {
						try {
							System.out.println("Saving " + instance.getId());
							instance.save();
							String text = tab.getText();
							tab.setText(text.replaceAll("[\\s]*\\*$", ""));
							instance.setChanged(false);
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
		mniClose.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (tabArtifacts.getSelectionModel().selectedItemProperty().isNotNull().get()) {
					Tab selected = tabArtifacts.getSelectionModel().getSelectedItem();
					if (managers.containsKey(selected)) {
						managers.remove(selected);
						tabArtifacts.getTabs().remove(selected);
					}
				}
			}
		});
		mniCloseAll.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				managers.clear();
				tabArtifacts.getTabs().clear();
			}
		});
		mniRebuildReferences.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				lstNotifications.getItems().clear();
				// the root is special as the visual display does not match an actual root
				// also if you refresh the root, no need to refresh anything else
				if (tree.getSelectionModel().getSelectedItems().contains(tree.getRootCell())) {
					lstNotifications.getItems().addAll(repository.rebuildReferences(null, true));
				}
				else {
					for (TreeCell<Entry> selected : tree.getSelectionModel().getSelectedItems()) {
						lstNotifications.getItems().addAll(repository.rebuildReferences(selected.getItem().itemProperty().get().getId(), true));
					}
				}
			}
		});
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
	
	public void save(String id) throws IOException {
		for (ArtifactGUIInstance instance : managers.values()) {
			if (instance.isReady() && instance.getId().equals(id)) {
				if (instance.isEditable()) {
					instance.save();
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
	
	public Tab newTab(final String id, ArtifactGUIInstance instance) {
		final Tab tab = new Tab(id);
		tab.setId(id);
		tabArtifacts.getTabs().add(tab);
		tabArtifacts.selectionModelProperty().get().select(tab);
		managers.put(tab, instance);
		return tab;
	}

	public EAIResourceRepository getRepository() {
		return repository;
	}
	
	@SuppressWarnings("rawtypes")
	public List<ArtifactGUIManager> getGUIManagers() {
		return Arrays.asList(new ArtifactGUIManager [] { 
			new StructureGUIManager(),
			new VMServiceGUIManager(),
			new JDBCServiceGUIManager(),
			new ServiceGUIManager(), 
			new TypeGUIManager(),
			new JDBCPoolGUIManager(),
			new WSDLClientGUIManager(),
			new KeyStoreGUIManager(),
			new BrokerClientGUIManager(),
			new SubscriptionGUIManager(),
			new DefinedHTTPServerGUIManager(),
			new WebArtifactGUIManager(),
			new WebRestArtifactGUIManager(),
			new ProxyGUIManager(),
			new UMLTypeRegistryGUIManager(),
			new ServiceInterfaceGUIManager(),
			new XMLSchemaTypeRegistryGUIManager()
		});
	}
	
	public ArtifactGUIManager<?> getGUIManager(Class<?> type) {
		for (ArtifactGUIManager<?> manager : getGUIManagers()) {
			if (manager.getArtifactClass().isAssignableFrom(type)) {
				return manager;
			}
		}
		throw new IllegalArgumentException("No gui manager for type " + type);
	}
	
	public static ImageView loadGraphic(String name) {
		return new ImageView(loadImage(name));
	}
	
	public static Image loadImage(String name) {
		InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
		// not found
		if (input == null) {
			input = Thread.currentThread().getContextClassLoader().getResourceAsStream("default-type.png");
			if (input == null)
				throw new RuntimeException("Can not find the icon for type '" + name + "' and the default is not present either");
		}
		try {
			return new Image(input);
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

	public void setStage(Stage stage) {
		this.stage = stage;
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
	
	@Override
	public void notify(ValidationMessage...messages) {
		System.out.println(Arrays.asList(messages));
		notify(Arrays.asList(messages));
	}
	
	public void notify(List<? extends Validation<?>> messages) {
		lstNotifications.getItems().clear();
		if (messages != null) {
			for (Validation<?> message : messages) {
				lstNotifications.getItems().add(message.getMessage());
			}
		}
	}
	
	public void showProperties(final PropertyUpdater updater) {
		showProperties(updater, ancProperties, true);
	}
	
	@SuppressWarnings("unchecked")
	public void showProperties(final PropertyUpdater updater, final Pane target, final boolean refresh) {
		GridPane grid = new GridPane();
		grid.setVgap(5);
		grid.setHgap(10);
		int row = 0;
		for (final Property<?> property : updater.getSupportedProperties()) {
			Label name = new Label(property.getName() + ": " + (updater.isMandatory(property) ? " *" : ""));
			grid.add(name, 0, row);
			GridPane.setHalignment(name, HPos.RIGHT);
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
				: (originalValue instanceof String ? (String) originalValue : converter.convert(originalValue, String.class));

			// if we can't convert from a string to the property value, we can't show it
			if (updater.canUpdate(property) && ((property.equals(new SuperTypeProperty()) && allowSuperType) || !property.equals(new SuperTypeProperty()))) {
				if (property instanceof Enumerated || Boolean.class.equals(property.getValueClass()) || Enum.class.isAssignableFrom(property.getValueClass()) || Artifact.class.isAssignableFrom(property.getValueClass())) {
					final ComboBox<String> comboBox = new ComboBox<String>();
					
					CheckBox filterByApplication = null;
					if (property instanceof Enumerated) {
						comboBox.setEditable(true);
					}
					Collection<?> values;
					if (property instanceof Enumerated) {
						values = ((Enumerated<?>) property).getEnumerations();
					}
					else if (Boolean.class.equals(property.getValueClass())) {
						values = Arrays.asList(Boolean.TRUE, Boolean.FALSE);
					}
					else if (Artifact.class.isAssignableFrom(property.getValueClass())) {
						Collection<Artifact> artifacts = new ArrayList<Artifact>();
						for (Node node : getRepository().getNodes((Class<Artifact>) property.getValueClass())) {
							try {
								artifacts.add(node.getArtifact());
							}
							catch (IOException e) {
								e.printStackTrace();
							}
							catch (ParseException e) {
								e.printStackTrace();
							}
						}
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
					Collections.sort(comboBox.getItems(), new StringComparator());
					
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
						}
					}

					comboBox.selectionModelProperty().get().selectedItemProperty().addListener(new ChangeListener<String>() {
						@Override
						public void changed(ObservableValue<? extends String> arg0, String arg1, String newValue) {
							if (!parseAndUpdate(updater, property, newValue)) {
								comboBox.getSelectionModel().select(arg1);
							}
							else if (refresh) {
								showProperties(updater, target, refresh);
							}
						}
					});
					grid.add(comboBox, 1, row);
					if (filterByApplication != null) {
						grid.add(filterByApplication, 2, row);
					}
				}
				else {
					final TextField textField = new TextField(currentValue);
					textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent event) {
							if (event.getCode() == KeyCode.ENTER) {
								if (!parseAndUpdate(updater, property, textField.getText())) {
									textField.setText(currentValue);
								}
								else if (refresh) {
									// refresh basically, otherwise the final currentValue will keep pointing at the old one
									showProperties(updater, target, refresh);
								}
								event.consume();
							}
						}
					});
					// when we lose focus, set it as well
					textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
							if (arg2 != null && !arg2) {
								if (!parseAndUpdate(updater, property, textField.getText())) {
									textField.setText(currentValue);
								}
								else if (refresh) {
									// refresh basically, otherwise the final currentValue will keep pointing at the old one
									showProperties(updater, target, refresh);
								}
							}
						}
					});
					grid.add(textField, 1, row);
				}
			}
			else if (currentValue != null) {
				Label value = new Label(currentValue);
				grid.add(value, 1, row);
			}
			row++;
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
	private boolean parseAndUpdate(PropertyUpdater updater, Property<?> property, String value) {
		try {
			if (value != null && value.trim().isEmpty()) {
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
				updater.updateProperty(property, parsed);
				setChanged();
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
	}
	
	public void showContent(ComplexContent content) {
		ancPipeline.getChildren().clear();
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
						@SuppressWarnings({ "unchecked", "rawtypes" })
						@Override
						public Region getNode() {
							if (hbox == null) {
								hbox = new HBox();
								Label labelName = new Label(item.getName());
								labelName.getStyleClass().add("contentName");
								hbox.getChildren().add(labelName);
								if (item.leafProperty().get()) {
									ContentTreeItem contentTreeItem = (ContentTreeItem) item;
									if (contentTreeItem.getDefinition().getType() instanceof be.nabu.libs.types.api.Marshallable) {
										final Label value = new Label(
											((be.nabu.libs.types.api.Marshallable) contentTreeItem.getDefinition().getType()).marshal(item.itemProperty().get(), contentTreeItem.getDefinition().getProperties()
										));
										ContextMenu contextMenu = new ContextMenu();
										CustomMenuItem item = new CustomMenuItem();
										final TextField textField = new TextField(value.getText());
										textField.setEditable(false);
										item.setContent(textField);
										contextMenu.getItems().add(item);
										value.setContextMenu(contextMenu);
										value.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
											@Override
											public void handle(MouseEvent event) {
												value.getContextMenu().show(value, Side.BOTTOM, 0, 0);
												textField.selectAll();
												textField.requestFocus();
												event.consume();
											}
										});
										value.getStyleClass().add("contentValue");
										hbox.getChildren().add(value);
									}
									else {
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
			
			// resize everything
			AnchorPane.setLeftAnchor(contentTree, 0d);
			AnchorPane.setRightAnchor(contentTree, 0d);
			AnchorPane.setTopAnchor(contentTree, 0d);
			AnchorPane.setBottomAnchor(contentTree, 0d);
			if (!ancPipeline.prefWidthProperty().isBound()) {
				ancPipeline.prefWidthProperty().bind(((Pane) ancPipeline.getParent()).widthProperty()); 
			}
			contentTree.rootProperty().set(new ContentTreeItem(new RootElement(content.getType()), content, null, false, null));
//			contentTree.getTreeCell(contentTree.rootProperty().get()).collapseAll();
			contentTree.getTreeCell(contentTree.rootProperty().get()).expandedProperty().set(true);
			ancPipeline.getChildren().add(contentTree);
		}
		else {
			ancPipeline.getChildren().add(new Label("null"));
		}
	}
	
	public Tree<Entry> getTree() {
		return tree;
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

	@SuppressWarnings("unchecked")
	public static ClipboardContent buildClipboard(Object...objects) {
		ClipboardContent clipboard = new ClipboardContent();
		for (Object object : objects) {
			DataFormat format = null;
			String stringRepresentation = null;
			if (object instanceof DefinedType) {
				format = TreeDragDrop.getDataFormat(StructureGUIManager.DATA_TYPE_DEFINED);
				stringRepresentation = ((DefinedType) object).getId();
				object = stringRepresentation;
			}
			else if (object instanceof TreeItem && ((TreeItem<?>) object).itemProperty().get() instanceof Element) {
				format = TreeDragDrop.getDataFormat(StructureGUIManager.DATA_TYPE_ELEMENT);
				stringRepresentation = TreeUtils.getPath((TreeItem<?>) object);
				// remove the root as we always act on the root object
				stringRepresentation = stringRepresentation.replaceFirst("^.*/", "");
				TreeItem<Element<?>> item = (TreeItem<Element<?>>) object;
				Element<?> element = item.itemProperty().get();
				object = element.getType() instanceof DefinedType ? ((DefinedType) element.getType()).getId() : stringRepresentation;
			}
			else if (object instanceof Element && ((Element<?>) object).getType() instanceof DefinedType) {
				format = TreeDragDrop.getDataFormat(StructureGUIManager.DATA_TYPE_ELEMENT);
				stringRepresentation = ((DefinedType) ((Element<?>) object).getType()).getId();
				object = stringRepresentation;
			}
			else if (object instanceof Step) {
				format = TreeDragDrop.getDataFormat(VMServiceGUIManager.DATA_TYPE_STEP);
				Sequence sequence = new Sequence();
				sequence.getChildren().add((Step) object);
				ByteBuffer buffer = IOUtils.newByteBuffer();
				try {
					VMServiceManager.formatSequence(buffer, sequence);
					stringRepresentation = new String(IOUtils.toBytes(buffer), "UTF-8");
				}
				catch (IOException e) {
					getInstance().notify(new ValidationMessage(Severity.ERROR, "Can not copy step"));
					// no can copy
					continue;
				}
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
			if (format != null) {
				clipboard.put(format, object);
			}
			if (stringRepresentation != null) {
				clipboard.put(DataFormat.PLAIN_TEXT, stringRepresentation);
			}
		}
		return clipboard.size() == 0 ? null : clipboard;
	}
	
	public static Object paste(String dataType) {
		return Clipboard.getSystemClipboard().getContent(TreeDragDrop.getDataFormat(dataType));
	}

	public ServerConnection getServer() {
		return server;
	}
}
