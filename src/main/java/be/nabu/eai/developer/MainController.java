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
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import java.util.concurrent.ForkJoinPool;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
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
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import javax.imageio.ImageIO;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.Main.Developer;
import be.nabu.eai.developer.Main.Protocol;
import be.nabu.eai.developer.Main.Reconnector;
import be.nabu.eai.developer.Main.ServerProfile;
import be.nabu.eai.developer.Main.ServerTunnel;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIInstanceWithChildren;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.api.ClipboardProvider;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.eai.developer.api.DeveloperPlugin;
import be.nabu.eai.developer.api.EvaluatableProperty;
import be.nabu.eai.developer.api.FindFilter;
import be.nabu.eai.developer.api.MainMenuEntry;
import be.nabu.eai.developer.api.NodeContainer;
import be.nabu.eai.developer.api.PortableArtifactGUIManager;
import be.nabu.eai.developer.api.RedrawableArtifactGUIInstance;
import be.nabu.eai.developer.api.RefresheableArtifactGUIInstance;
import be.nabu.eai.developer.api.ValidatableArtifactGUIInstance;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.events.ArtifactMoveEvent;
import be.nabu.eai.developer.impl.AsynchronousRemoteServer;
import be.nabu.eai.developer.impl.CustomTooltip;
import be.nabu.eai.developer.impl.NotificationHandler;
import be.nabu.eai.developer.impl.StageNodeContainer;
import be.nabu.eai.developer.impl.TabNodeContainer;
import be.nabu.eai.developer.managers.ServiceGUIManager;
import be.nabu.eai.developer.managers.ServiceInterfaceGUIManager;
import be.nabu.eai.developer.managers.SimpleTypeGUIManager;
import be.nabu.eai.developer.managers.TypeGUIManager;
import be.nabu.eai.developer.managers.util.EnumeratedSimpleProperty;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.CollaborationClient;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.developer.util.ContentTreeItem;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.ElementTreeItem;
import be.nabu.eai.developer.util.Find;
import be.nabu.eai.developer.util.RepositoryValidatorService;
import be.nabu.eai.developer.util.RunService;
import be.nabu.eai.developer.util.StringComparator;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.Notification;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.BrokenReferenceArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.events.NodeEvent;
import be.nabu.eai.repository.events.NodeEvent.State;
import be.nabu.eai.repository.logger.NabuLogMessage;
import be.nabu.eai.server.CollaborationListener.User;
import be.nabu.eai.server.ServerConnection;
import be.nabu.eai.server.rest.ServerREST;
import be.nabu.jfx.control.date.DatePicker;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.StringMarshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeCellValue;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.Updateable;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDropListener;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.artifacts.api.TunnelableArtifact;
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
import be.nabu.libs.resources.alias.AliasResourceResolver;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.resources.api.ReadableResource;
import be.nabu.libs.resources.api.Resource;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedSimpleType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.DefinedTypeResolver;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.Duration;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.StringMapCollectionHandlerProvider;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.binding.BindingProviderFactory;
import be.nabu.libs.types.binding.api.BindingProvider;
import be.nabu.libs.types.binding.api.MarshallableBinding;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.java.BeanType;
import be.nabu.libs.types.properties.CollectionHandlerProviderProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.simple.UUID;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.types.structure.SuperTypeProperty;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import be.nabu.libs.validator.api.Validator;
import be.nabu.utils.io.ContentTypeMap;
import be.nabu.utils.io.IOUtils;
import be.nabu.utils.io.api.ByteBuffer;
import be.nabu.utils.io.api.ReadableContainer;
import be.nabu.utils.mime.impl.FormatException;

import com.jcraft.jsch.Session;

/**
 * TODO: i may need to further optimize the classloading, the mavenclassloader already shortcuts to parent loading for internal namespaces
 * additionally it keeps a list of misses to prevent double scanning
 * while the gui managers have been cached as it had to rescan all the maven classloaders to build the list otherwise
 * The biggest problem is actually the @Interface annotation which is resolved against the DefinedServiceInterfaceResolverFactory
 * That factory however has no context awareness and as such will search all maven classloaders, it doesn't know where it was defined
 *  
 * currently there is still a delay when you open a cell in the tree
 */
public class MainController implements Initializable, Controller {

	private static Developer configuration;
	private Reconnector reconnector;
	private boolean leftAlignLabels = true;
	private Stage lastFocused;
	private NotificationHandler notificationHandler;
	public static BooleanProperty expertMode = new SimpleBooleanProperty();
	
	private Map<String, StringProperty> locks = new HashMap<String, StringProperty>();
	private Map<String, BooleanProperty> isLocked = new HashMap<String, BooleanProperty>();
	
	private boolean showHidden = Boolean.parseBoolean(System.getProperty("show.hidden", "false"));

	public boolean isShowHidden() {
		return showHidden;
	}
	
	public static File getHomeDir() {
		String property = System.getProperty("user.home");
		File file = property == null ? new File(".nabu") : new File(property, ".nabu");
		if (!file.exists()) {
			file.mkdirs();
		}
		return file;
	}
	
	public static Developer getConfiguration() {
		if (configuration == null) {
			try {
				File file = new File(getHomeDir(), "nabu-developer.xml");
				if (file.exists()) {
					Unmarshaller unmarshaller = JAXBContext.newInstance(Developer.class).createUnmarshaller();
					configuration = (Developer) unmarshaller.unmarshal(file);
				}
				else {
					configuration = new Developer();
				}
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return configuration;
	}
	
	public static void saveConfiguration() {
		if (configuration != null) {
			try {
				File file = new File(getHomeDir(), "nabu-developer.xml");
				Marshaller marshaller = JAXBContext.newInstance(Developer.class).createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.marshal(configuration, file);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private double[] previousDividerPositions;
	
	public static final String DATA_TYPE_NODE = "repository-node";
	
	@FXML
	private VBox root;
	
	@FXML
	private AnchorPane ancLeft, ancMiddle, ancProperties, ancPipeline, ancRight;
	
	@FXML
	private TabPane tabArtifacts, tabBrowsers;
	
	@FXML
	private TabPane tabMisc;
	
	@FXML
	private MenuItem mniClose, mniSave, mniCloseAll, mniCloseOther, mniSaveAll, mniRebuildReferences, mniLocate, mniFind, mniUpdateReference, mniGrep, mniRun, mniReconnectSsh, mniServerLog, mniDetach, mniMaximize;
	
	@FXML
	private ScrollPane scrLeft, scrPipeline;
	
	@FXML
	private SplitPane splMain;
	
	@FXML
	private MenuBar mnbMain;
	
	@FXML
	private Tab tabPipeline, tabRepository;
	
	private boolean scrLeftFocused;
	
	private Map<NodeContainer<?>, ArtifactGUIInstance> managers = new HashMap<NodeContainer<?>, ArtifactGUIInstance>();
	
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
	
	private StringProperty remoteServerMessage = new SimpleStringProperty();
	
	/**
	 * Keep track of the last directory used to select a file from, set it as default
	 */
	private File lastDirectoryUsed;
	
	/**
	 * The id that was active when the validations were generated, it can probably find them again
	 */
	private String validationsId;
	
	private Set<KeyCode> activeKeys = new HashSet<KeyCode>();

	private AsynchronousRemoteServer asynchronousRemoteServer;

	private ServerProfile profile;
	
	private Map<String, Session> tunnels = new HashMap<String, Session>();
	
	private BooleanProperty connected = new SimpleBooleanProperty(false);
	
	private ObservableList<User> users = FXCollections.observableArrayList();
	
	private Find<?> currentFind;
	
	private Map<String, Stage> stages = new HashMap<String, Stage>();
	
	public boolean isTunneled(String id) {
		return tunnels.containsKey(id) && tunnels.get(id).isConnected();
	}
	
	public VBox getRoot() {
		return root;
	}
	
	public void untunnel(String id) {
		if (tunnels.containsKey(id) && tunnels.get(id).isConnected()) {
			tunnels.get(id).disconnect();
			tunnels.remove(id);
			
			if (profile.getTunnels() != null) {
				ServerTunnel current = null;
				for (ServerTunnel tunnel : profile.getTunnels()) {
					if (tunnel.getId().equals(id)) {
						current = tunnel;
						break;
					}
				}
				if (current != null) {
					profile.getTunnels().remove(current);
					saveConfiguration();
				}
			}
		}
	}
	
	public void tunnel(String id, Integer localPort, boolean save) {
		untunnel(id);
		Artifact resolve = getRepository().resolve(id);
		if (resolve instanceof TunnelableArtifact && ((TunnelableArtifact) resolve).getTunnelHost() != null && ((TunnelableArtifact) resolve).getTunnelPort() != null) {
			logger.info("Creating SSH tunnel to: " + ((TunnelableArtifact) resolve).getTunnelHost() + ":" + ((TunnelableArtifact) resolve).getTunnelPort());
			Session openTunnel = Main.openTunnel(this, profile, ((TunnelableArtifact) resolve).getTunnelHost(), ((TunnelableArtifact) resolve).getTunnelPort(), localPort == null ? ((TunnelableArtifact) resolve).getTunnelPort() : localPort);
			if (openTunnel != null) {
				tunnels.put(id, openTunnel);
				if (save) {
					saveTunnel(id, localPort);
				}
			}
		}
	}
	
	private void saveTunnel(String id, Integer localPort) {
		if (profile.getTunnels() == null) {
			profile.setTunnels(new ArrayList<ServerTunnel>());
		}
		ServerTunnel current = null;
		for (ServerTunnel tunnel : profile.getTunnels()) {
			if (tunnel.getId().equals(id)) {
				current = tunnel;
				break;
			}
		}
		if (current == null) {
			current = new ServerTunnel();
			current.setId(id);
			current.setLocalPort(localPort);
			profile.getTunnels().add(current);
		}
		else {
			current.setLocalPort(localPort);
		}
		saveConfiguration();
	}
	
	public void connect(ServerProfile profile, ServerConnection server) {
		new Themer().load();
		
		File restCache = new File(getHomeDir(), "rest-cache");
		try {
			logger.info("Setting rest cache to: " + restCache.getCanonicalPath());
			System.setProperty("resource.rest.cache", restCache.getCanonicalPath());
		}
		catch (IOException e2) {
			logger.error("Could not set resource cache location", e2);
		}
		String serverVersion;
		try {
			this.profile = profile;
			logger.info("Connecting to: " + server.getHost() + ":" + server.getPort());
			this.server = server;
			this.asynchronousRemoteServer = new AsynchronousRemoteServer(server.getRemote());
			// create repository
			serverVersion = server.getVersion();
			
			stageFocuser(stage);
			stage.setTitle("Nabu Developer: " + server.getName() + " (" + serverVersion + ")");
			stage.getIcons().add(loadImage("icon.png"));
			URI repositoryRoot = server.getRepositoryRoot();
			if (repositoryRoot.getScheme().equals("remote") || repositoryRoot.getScheme().equals("remotes")) {
				// timeout 5 minutes instead of the default 2
				long timeout = 1000l*60*5;
				repositoryRoot = new URI(repositoryRoot.toASCIIString() + "?remote=true&full=true&timeout=" + timeout);
			}
			Resource resourceRoot = ResourceFactory.getInstance().resolve(repositoryRoot, server.getPrincipal());
			if (resourceRoot == null) {
				throw new RuntimeException("Could not find the repository root: " + server.getRepositoryRoot());
			}
			// use the same pool so we make sure refreshes are always after actual persistence
//			else if (resourceRoot instanceof RemoteResource) {
//				logger.info("Registering asynchronous remote file system executor");
//				((RemoteResource) resourceRoot).setExecutor(new Executor() {
//					@Override
//					public void execute(Runnable command) {
//						asynchronousRemoteServer.getPool().submit(new Action("Saving file...", new Callable<Object>() {
//							@Override
//							public Object call() throws Exception {
//								command.run();
//								return null;
//							}
//						}));
//					}
//				});
//			}
			Resource mavenRoot = null;
			URI mavenRootUri = server.getMavenRoot();
			if (mavenRootUri != null) {
				mavenRoot = ResourceFactory.getInstance().resolve(mavenRootUri, server.getPrincipal());
				if (mavenRoot == null) {
					throw new RuntimeException("Could not find the maven root: " + server.getMavenRoot());
				}
			}
			repository = new EAIResourceRepository((ResourceContainer<?>) resourceRoot, (ResourceContainer<?>) mavenRoot);
			Thread.currentThread().setContextClassLoader(repository.getClassLoader());
			repository.getEventDispatcher().subscribe(NodeEvent.class, new be.nabu.libs.events.api.EventHandler<NodeEvent, Void>() {
				@Override
				public Void handle(NodeEvent event) {
					if (event.getState() == State.CREATE && event.isDone()) {
						try {
							if (event.getId().contains(".")) {
								getAsynchronousRemoteServer().reload(event.getId());
							}
						}
						catch (Exception e) {
							e.printStackTrace();
						}
					}
					return null;
				}
			});
			// mount them before the repository starts, artifacts may refer to the aliases
			Map<String, URI> aliases = server.getRemote().getAliases();
			for (String alias : aliases.keySet()) {
				logger.info("Mounting remote alias '" + alias + "': " + aliases.get(alias));
				AliasResourceResolver.alias(alias, aliases.get(alias));
			}
		}
		catch (Exception e) {
//			StringWriter writer = new StringWriter();
//			PrintWriter printer = new PrintWriter(writer);
//			e.printStackTrace(printer);
			Stage confirm = Confirm.confirm(ConfirmType.ERROR, "Connection Failed", "Could not connect to: " + profile.getName() + "\n\nMessage: " + e.getMessage(), null);
			confirm.setOnHidden(new EventHandler<WindowEvent>() {
				@Override
				public void handle(WindowEvent event) {
					Main.draw(MainController.this);			
				}
			});
			throw new RuntimeException(e);
		}
		repository.setServiceRunner(server.getRemote());
		logger.info("Loading repository...");
		Date date = new Date();
		
		AnchorPane pane = new AnchorPane();
		VBox content = new VBox();
		content.setAlignment(Pos.CENTER);
		ImageView loadGraphic = loadGraphic("icon.png");
		HBox graphicBox = new HBox();
		graphicBox.getChildren().add(loadGraphic);
		graphicBox.setPadding(new Insets(10));
		graphicBox.setAlignment(Pos.CENTER);
		final Label progressLabel = new Label("Loading repository...");
		progressLabel.setPadding(new Insets(10));
		progressLabel.setAlignment(Pos.CENTER);
		HBox.setHgrow(progressLabel, Priority.ALWAYS);
		ProgressIndicator progressIndicator = new ProgressIndicator();
		
		HBox progressBox = new HBox();
		progressBox.getChildren().add(progressIndicator);
		
		progressBox.setAlignment(Pos.CENTER);
		progressBox.setPadding(new Insets(10));
//		progressBox.setStyle("-fx-background-color: white; -fx-border-width: 1; -fx-border-color: #cccccc; -fx-border-style: solid none solid none");
		
		AnchorPane.setBottomAnchor(content, 0.0);
		AnchorPane.setLeftAnchor(content, 0.0);
		AnchorPane.setRightAnchor(content, 0.0);
		AnchorPane.setTopAnchor(content, 0.0);
		
		Button stop = new Button("Stop");
		stop.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.exit(0);
			}
		});
		HBox buttons = new HBox();
		buttons.setPadding(new Insets(10));
		buttons.setAlignment(Pos.CENTER);
		buttons.getChildren().add(stop);
		Label titleLabel = new Label("Nabu Developer");
		titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold");
		titleLabel.setPadding(new Insets(10));
		titleLabel.setAlignment(Pos.CENTER);
		
		Label versionLabel = new Label(new ServerREST().getVersion());
		versionLabel.setPadding(new Insets(0, 0, 20, 0));
		versionLabel.setAlignment(Pos.CENTER);
		
		content.getChildren().addAll(titleLabel, versionLabel, graphicBox, progressLabel, progressBox, buttons);
		pane.getChildren().add(content);
		
		VBox.setVgrow(pane, Priority.ALWAYS);
		pane.prefWidthProperty().bind(root.widthProperty());
		pane.minHeightProperty().bind(root.heightProperty());
		root.getChildren().add(0,pane);
		
//		AnchorPane.setBottomAnchor(pane, 0d);
//		AnchorPane.setRightAnchor(pane, 0d);
//		AnchorPane.setTopAnchor(pane, 0d);
//		AnchorPane.setLeftAnchor(pane, 0d);
//		ancMiddle.getChildren().add(pane);
//		final Stage progress = EAIDeveloperUtils.buildPopup("Connecting to " + server.getName() + "...", pane, stage, StageStyle.UNDECORATED, true);
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				repository.start();
				logger.info("Repository loaded in: " + ((new Date().getTime() - date.getTime()) / 1000) + "s");
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
//						progressLabel.setText("Repository loaded in: " + ((new Date().getTime() - date.getTime()) / 1000) + "s");
						progressLabel.setText("Constructing workspace...");
					}
				});
				// we create ssh tunnels if necessary
				if (Protocol.SSH.equals(profile.getProtocol()) && profile.getTunnels() != null) {
					for (ServerTunnel tunnel : profile.getTunnels()) {
						tunnel(tunnel.getId(), tunnel.getLocalPort(), false);
					}
				}
				
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						initializeButtons();
						
						// subtract scrollbar
//						ancProperties.minWidthProperty().bind(ancRight.widthProperty().subtract(25));
//						ancProperties.setPadding(new Insets(10));
						
						mniReconnectSsh.setDisable(reconnector == null);
						mniReconnectSsh.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								reconnector.reconnect();
							}
						});
						
						tree = new Tree<Entry>(new Marshallable<Entry>() {
							@Override
							public String marshal(Entry entry) {
								if ((entry.isEditable() && entry.isLeaf()) || showExactName) {
									return entry.getName();
								}
								else {
									String name = entry.getName();
									return name.isEmpty() ? name : name.substring(0, 1).toLowerCase() + name.substring(1);
								}
							}
						}, new Updateable<Entry>() {
							@Override
							public Entry update(TreeCell<Entry> treeCell, String newName) {
								ResourceEntry entry = (ResourceEntry) treeCell.getItem().itemProperty().get();
								String oldId = entry.getId();
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
									getAsynchronousRemoteServer().reload(treeCell.getParent().getItem().itemProperty().get().getId());
									// reload the dependencies to pick up the new item
									for (String dependency : dependencies) {
										getAsynchronousRemoteServer().reload(dependency);
									}
									getCollaborationClient().updated(treeCell.getParent().getItem().itemProperty().get().getId(), "Renamed from: " + oldId);
								}
								catch (Exception e) {
									logger.error("Could not reload renamed items on server", e);
								}
								String newId = treeCell.getParent().getItem().itemProperty().get().getChild(newName).getId();
								getDispatcher().fire(new ArtifactMoveEvent(oldId, newId), tree);
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
								return !dragged.equals(target) && !target.getItem().itemProperty().get().isNode() && entry instanceof ResourceEntry && ((ResourceEntry) entry).getContainer() instanceof ManageableContainer
										// no item must exist with that name
										&& ((ResourceEntry) entry).getContainer().getChild(((TreeCell<Entry>) dragged).getItem().getName()) == null;
							}
							@SuppressWarnings("unchecked")
							@Override
							public void drop(String arg0, TreeCell<Entry> target, TreeCell<?> dragged, TransferMode arg3) {
								Entry original = ((TreeCell<Entry>) dragged).getItem().itemProperty().get();
								Confirm.confirm(ConfirmType.QUESTION, "Move " + original.getId(), "Are you sure you want to move: " + original.getId(), new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										try {
											List<String> dependencies = repository.getDependencies(original.getId());
											String originalParentId = ((TreeCell<Entry>) dragged).getParent().getItem().itemProperty().get().getId();
											closeAll(original.getId());
											repository.move(
													original.getId(), 
													target.getItem().itemProperty().get().getId() + "." + original.getName(), 
													true);
											// refresh the tree
											target.getParent().refresh();
											dragged.getParent().refresh();
											// reload remotely
											try {
												getAsynchronousRemoteServer().reload(originalParentId);
												getAsynchronousRemoteServer().reload(target.getItem().itemProperty().get().getId());
												// reload dependencies
												for (String dependency : dependencies) {
													getAsynchronousRemoteServer().reload(dependency);
												}
												getCollaborationClient().updated(originalParentId, "Moved (delete) " + original.getId());
												getCollaborationClient().updated(originalParentId, "Moved (create) " + target.getItem().itemProperty().get().getId() + "." + original.getName());
											}
											catch (Exception e) {
												logger.error("Could not reload moved items on server", e);
											}
											getDispatcher().fire(new ArtifactMoveEvent(original.getId(), target.getItem().itemProperty().get().getId() + "." + original.getName()), tree);
										}
										catch (IOException e) {
											logger.error("Could not move " + original.getId(), e);
										}						
									}
								});
							}
						});
						tree.setId("repository");
						ancLeft.getChildren().add(tree);
						
						if (Boolean.parseBoolean(System.getProperty("developer.fastScroll", "false"))) {
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
						}
						
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
						logger.info("Creating repository browser");
						components.put(tree.getId(), new RepositoryBrowser(server).initialize(MainController.this, tree));
						AnchorPane.setLeftAnchor(tree, 0d);
						AnchorPane.setRightAnchor(tree, 0d);
						AnchorPane.setTopAnchor(tree, 0d);
						AnchorPane.setBottomAnchor(tree, 0d);
						
						logger.info("Populating main menu");
						for (MainMenuEntry mainMenuEntry : ServiceLoader.load(MainMenuEntry.class)) {
							mainMenuEntry.populate(mnbMain);
						}
						
						repositoryValidatorService = new RepositoryValidatorService(repository, mnbMain);
						
						logger.info("Starting validation service");
						repositoryValidatorService.start();
						
						String developerVersion = new ServerREST().getVersion();
						if (!developerVersion.equals(serverVersion)) {
//			Confirm.confirm(ConfirmType.WARNING, "Version mismatch", "Your developer is version " + developerVersion + " but the server has version " + server.getVersion() + ".\n\nThis may cause issues.", null);
//			logDeveloperText("Your developer is version " + developerVersion + " but the server has version " + server.getVersion() + ".\n\nThis may cause issues.");
							logger.warn("Your developer is version " + developerVersion + " but the server has version " + server.getVersion() + ".\n\nThis may cause issues.");
						}
						
						remoteServerMessageProperty().addListener(new ChangeListener<String>() {
							@Override
							public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
								stage.setTitle(stage.getTitle().replaceAll("[\\s]*\\[.*?\\]", ""));
								if (arg2 != null && !arg2.trim().isEmpty()) {
									stage.setTitle(stage.getTitle() + " [" + arg2 + "]");
								}
							}
						});
						
//						mnbMain
						vbxServerLog = new VBox();
						vbxServerLog.setPadding(new Insets(10));
						vbxServerLog.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
							@Override
							public void handle(KeyEvent event) {
								if (event.getCode() == KeyCode.D && event.isControlDown()) {
									vbxServerLog.getChildren().clear();
								}
							}
						});
						vbxServerLog.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								vbxServerLog.requestFocus();
							}
						});
						mniServerLog.setGraphic(loadGraphic("log.png"));
						mniServerLog.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								String id = "Server Log (" + server.getName() + ")";
								Tab existingTab = getTab(id);
								if (existingTab != null) {
									tabArtifacts.getSelectionModel().select(existingTab);
								}
								else {
									Stage stage = getStage(id);
									if (stage != null) {
										stage.requestFocus();
									}
									else {
										Tab tab = new Tab(id);
										tab.setId(id);
//										decouplable(tab);
										ScrollPane scroll = new ScrollPane();
										scroll.setContent(vbxServerLog);
										if (vbxServerLog.minWidthProperty().isBound()) {
											vbxServerLog.minWidthProperty().unbind();
										}
										// subtract possible scrollbar
										vbxServerLog.minWidthProperty().bind(scroll.widthProperty().subtract(50));
										tab.setContent(scroll);
										tabArtifacts.getTabs().add(tab);
										tabArtifacts.getSelectionModel().select(tab);
									}
								}
							}
						});
//						mnbMain.getMenus().add();
						
						// set up the misc tabs
						Tab tab = new Tab("Developer");
						ScrollPane scroll = new ScrollPane();
						vbxDeveloperLog = new VBox();
						scroll.setContent(vbxDeveloperLog);
						// subtract possible scrollbar
						vbxDeveloperLog.prefWidthProperty().bind(scroll.widthProperty().subtract(50));
						tab.setContent(scroll);
						tabMisc.getTabs().add(tab);
						
						tab = new Tab("Notifications");
						scroll = new ScrollPane();
						vbxNotifications = new VBox();
						scroll.setContent(vbxNotifications);
						// subtract possible scrollbar
						vbxNotifications.prefWidthProperty().bind(tabMisc.widthProperty().subtract(50));
						tab.setContent(scroll);
						tabMisc.getTabs().add(tab);
						
						notificationHandler = new NotificationHandler(vbxNotifications);
						
						final Tab tabUsers = new Tab("Users");
						ListView<User> lstUser = new ListView<User>(users);
						lstUser.setCellFactory(new Callback<ListView<User>, ListCell<User>>() {
							@Override 
							public ListCell<User> call(ListView<User> list) {
								return new ListCell<User>() {
									@Override
									protected void updateItem(User arg0, boolean arg1) {
										super.updateItem(arg0, arg1);
										setText(arg0 == null ? null : arg0.getAlias());
									}
								};
							}
						});
						tabUsers.setContent(lstUser);
						tabMisc.getTabs().add(tabUsers);
						
						tabUsers.setGraphic(loadGraphic("connection/disconnected.png"));
						connected.addListener(new ChangeListener<Boolean>() {
							@Override
							public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
								tabUsers.setGraphic(loadGraphic(arg2 == null || !arg2 ? "connection/disconnected.png" : "connection/connected.png"));
								// if we disconnect, set all lock booleans to false
								if (arg2 == null || !arg2) {
									for (BooleanProperty bool : isLocked.values()) {
										bool.set(false);
									}
								}
								else {
									for (String key : isLocked.keySet()) {
										isLocked.get(key).set("$self".equals(locks.get(key).get()));
									}
								}
							}
						});
						
						collaborationClient = new CollaborationClient();
						collaborationClient.start();

						tabRepository.setGraphic(loadGraphic("folder.png"));
						
						// load plugins
						for (DeveloperPlugin plugin : ServiceLoader.load(DeveloperPlugin.class)) {
							plugin.initialize(MainController.this);
						}
						
//						progress.hide();
						root.getChildren().remove(pane);
						splMain.setVisible(true);
						mnbMain.setVisible(true);
						
//						ancLeft.setStyle("-fx-control-inner-background: #333333 !important; -fx-background-color: #333333 !important; -fx-text-fill: white !important");
//						tree.setStyle("-fx-control-inner-background: #333333 !important; -fx-background-color: #333333 !important; -fx-text-fill: white !important");
					}
				});
				
			}
		}).start();
	}

	private void stageFocuser(Stage stage) {
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue != null && newValue) {
					lastFocused = stage;
				}
			}
		});
	}
	
	private void detach(Tab tab) {
		NodeContainer<?> nodeContainer = getNodeContainer(tab);
		ArtifactGUIInstance artifactGUIInstance = nodeContainer == null ? null : managers.get(nodeContainer);

		// no can do
		if (artifactGUIInstance == null || artifactGUIInstance.isDetachable()) {
			
			AnchorPane pane = new AnchorPane();
			VBox box = new VBox();
			MenuBar menuBar = new MenuBar();
			
			Menu menu = new Menu("File");
			MenuItem save = new MenuItem("Save");
			save.addEventHandler(ActionEvent.ANY, newSaveHandler());
			save.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
			
			MenuItem find = new MenuItem("Find");
			
			find.setAccelerator(new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN));
			
			MenuItem run = new MenuItem("Run");
			run.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
			
			MenuItem close = new MenuItem("Close");
			close.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN));
			
			MenuItem closeAll = new MenuItem("Close All");
			closeAll.setAccelerator(new KeyCodeCombination(KeyCode.W, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN));
			closeAll.addEventHandler(ActionEvent.ANY, newCloseAllHandler());
			
			MenuItem toTab = new MenuItem("Reattach");
			toTab.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
			
			menu.getItems().addAll(save, find);
			
			if (artifactGUIInstance != null) {
				menu.getItems().addAll(toTab);
			}
			if (artifactGUIInstance != null && artifactGUIInstance.getArtifact() instanceof Service) {
				menu.getItems().addAll(run);
			}
			
			menu.getItems().addAll(close, closeAll);
			menuBar.getMenus().add(menu);
			
			Node content = tab.getContent();
			content.setId("content");
			box.getChildren().add(menuBar);
	//				box.getChildren().add(content);
			VBox.setVgrow(menuBar, Priority.NEVER);
			
			// only artifacts need the properties side bar
			if (artifactGUIInstance != null && artifactGUIInstance.requiresPropertiesPane()) {
				SplitPane contentWrapper = new SplitPane();
				contentWrapper.setId("#content-wrapper");
				contentWrapper.setOrientation(Orientation.HORIZONTAL);
				contentWrapper.getItems().add(content);
				ScrollPane rightPane = new ScrollPane();
				AnchorPane propertiesPane = new AnchorPane();
				propertiesPane.setId("properties");
				propertiesPane.setPadding(new Insets(10));
				rightPane.setContent(propertiesPane);
				contentWrapper.getItems().add(rightPane);
				rightPane.setFitToWidth(true);
				rightPane.setHbarPolicy(ScrollBarPolicy.NEVER);
				
				VBox.setVgrow(contentWrapper, Priority.ALWAYS);
				box.getChildren().add(contentWrapper);
				// make sure we don't have stale properties, we can't be sure the properties are for this item
				ancProperties.getChildren().clear();
				rightPane.setPrefWidth(ancProperties.getWidth());
				MenuItem propertacher = new MenuItem("Toggle Properties");
				// this is "in sync" with the global key combination to toggle the full screen mode
				propertacher.setAccelerator(new KeyCodeCombination(KeyCode.SPACE, KeyCombination.CONTROL_DOWN));
				propertacher.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						int indexOf = box.getChildren().indexOf(content);
						// detached already
						if (indexOf >= 0) {
							box.getChildren().set(indexOf, contentWrapper);
							contentWrapper.getItems().set(0, content);
						}
						else {
							indexOf = box.getChildren().indexOf(contentWrapper);
							box.getChildren().set(indexOf, content);
							VBox.setVgrow(content, Priority.ALWAYS);
						}
					}
				});
				menu.getItems().add(propertacher);
			}
			else {
				box.getChildren().add(content);
				VBox.setVgrow(content, Priority.ALWAYS);
			}
			
			pane.getChildren().add(box);
			AnchorPane.setBottomAnchor(box, 0d);
			AnchorPane.setLeftAnchor(box, 0d);
			AnchorPane.setRightAnchor(box, 0d);
			AnchorPane.setTopAnchor(box, 0d);
			String id = tab.getId() == null ? tab.getText() : tab.getId();
			
			tabArtifacts.getTabs().remove(tab);
			Stage stage = EAIDeveloperUtils.buildPopup(id, pane, null, StageStyle.DECORATED, false);
			
			stageFocuser(stage);
			
			run.addEventHandler(ActionEvent.ANY, newRunHandler(stage));
			find.addEventHandler(ActionEvent.ANY, newFindHandler(stage, false));
			close.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					stage.close();
				}
			});
			toTab.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					Node content = stage.getScene().getRoot().lookup("#content");
					if (content != null) {
						Tab newTab = newTab(artifactGUIInstance.getId(), artifactGUIInstance);
						new TabNodeContainer(newTab, tabArtifacts).setChanged(new StageNodeContainer(stage).isChanged());
						newTab.setContent(content);
						stage.close();
						// try lock async
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								tryLock(artifactGUIInstance.getId(), null);
							}
						});
						MainController.this.stage.requestFocus();
						tabArtifacts.requestFocus();
						tabArtifacts.getSelectionModel().select(newTab);
						// make sure we clear the properties
						ancProperties.getChildren().clear();
					}
				}
			});
	//				
	//				// initial locked
			stage.getIcons().add(loadImage("icon.png"));
	//				
	//				hasLock.addListener(new ChangeListener<Boolean>() {
	//					@Override
	//					public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
	//						if (arg2 != null && arg2) {
	//							stage.getIcons().clear();
	//							stage.getIcons().add(MainController.loadImage("status/unlocked.png"));
	//						}
	//						else {
	//							stage.getIcons().clear();
	//							stage.getIcons().add(MainController.loadImage("status/locked.png"));
	//						}
	//					}
	//				});
			
			// the unlocking kicks in later, relock it after
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					tryLock(id, null);
				}
			});
			
			stage.show();
			
			if (artifactGUIInstance != null) {
				managers.put(new StageNodeContainer(stage), artifactGUIInstance);
			}
			
			// inherit stylesheets
			stage.getScene().getStylesheets().addAll(MainController.this.stage.getScene().getStylesheets());
			stage.setMinHeight(200);
			stage.setMinWidth(400);
			if (pane.minWidthProperty().isBound()) {
				pane.minWidthProperty().unbind();
			}
			pane.minWidthProperty().bind(stage.widthProperty());
			if (pane.minHeightProperty().isBound()) {
				pane.minHeightProperty().unbind();
			}
			pane.minHeightProperty().bind(stage.heightProperty());
			stage.setMaximized(true);
			synchronized(stages) {
				stages.put(id, stage);
			}
			stage.showingProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					if (newValue != null && !newValue) {
						synchronized(stages) {
							stages.remove(id);
							removeContainer(stage);
							MainController.getInstance().getCollaborationClient().unlock(id, "Closed");
						}
					}
				}
			});
			// inherit the changed property
			if (nodeContainer.isChanged()) {
				new StageNodeContainer(stage).setChanged(true);
			}
		}
	}
	
	private void decouplable(Tab tab) {
		MenuItem menu = new MenuItem("Detach");
		ContextMenu contextMenu = tab.getContextMenu() == null ? new ContextMenu() : tab.getContextMenu();
		contextMenu.getItems().add(menu);
		tab.setContextMenu(contextMenu);
		menu.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				detach(tab);
			}
		});
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
	
	public void showNotification(Severity severity, String title, String message) {
		if (trayIcon != null && !isMac()) {
			trayIcon.setToolTip("Nabu Developer");
			trayIcon.displayMessage(title, message, severity == Severity.ERROR || severity == Severity.CRITICAL ? MessageType.ERROR : MessageType.INFO);
		}
	}

	private boolean isMac() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("mac") || osName.contains("darwin");
	}
	
	public void offload(final Runnable runnable, boolean requestLockTab, final String message) {
//		Tab selectedItem = tabArtifacts.getSelectionModel().getSelectedItem();
		NodeContainer<?> selectedItem = getCurrent();
		// race condition at times where we don't have a tab yet when running a service, failing is usually uglier at this point than not locking it...
		if (selectedItem == null) {
			requestLockTab = false;
		}
		final boolean lockTab = requestLockTab;
		if (selectedItem != null || !lockTab) {
			if (lockTab) {
				selectedItem.getContent().setDisable(true);
			}
			if (trayIcon != null && !isMac()) {
				trayIcon.setToolTip("Nabu Developer - " + message);
			}
//			Object container = selectedItem.getContainer();
//			if (container instanceof Tab) {
//				((Tab) container).setGraphic(loadGraphic("status/running.png"));
//			}
			Runnable newRunnable = new Runnable() {
				public void run() {
					Exception exception = null;
					try {
						runnable.run();
					}
					catch (Exception e) {
						exception = e;
					}
					finally {
						final Exception exceptionFinal = exception;
						Platform.runLater(new Runnable() {
							public void run() {
								if (lockTab) {
									selectedItem.getContent().setDisable(false);
								}
								if (exceptionFinal == null) {
//									if (container instanceof Tab) {
//										((Tab) container).setGraphic(loadGraphic("status/success.png"));
//									}
									if (trayIcon != null && !isMac()) {
										trayIcon.displayMessage("Action Completed", message, MessageType.INFO);
										trayIcon.setToolTip("Nabu Developer");
									}
								}
								else {
//									if (container instanceof Tab) {
//										((Tab) container).setGraphic(loadGraphic("status/failed.png"));
//									}
									if (trayIcon != null && !isMac()) {
										trayIcon.displayMessage("Action Failed", message, MessageType.ERROR);
										trayIcon.setToolTip("Nabu Developer");
									}
									MainController.this.notify(exceptionFinal);
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
	
	public void runIn(Runnable runnable, long timeout) {
		if (timeout <= 0) {
			Platform.runLater(runnable);
		}
		else {
			ForkJoinPool.commonPool().submit(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(timeout);
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
					Platform.runLater(runnable);
				}
			});
		}
	}
	
	public void closeDragSource() {
		Stage stage = dragSource != null ? dragSource.get() : null;
		if (stage != null) {
			stage.close();
		}
	}
	
	private WeakReference<Stage> dragSource;
	
	private void removeContainer(Object container) {
		Iterator<NodeContainer<?>> iterator = managers.keySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getContainer().equals(container)) {
				iterator.remove();
			}
		}
	}
	
	private NodeContainer<?> getNodeContainer(Object current) {
		if (current != null) {
			for (NodeContainer<?> container : managers.keySet()) {
				if (container.getContainer().equals(current)) {
					return container;
				}
			}
		}
		return null;
	}
	
	public NodeContainer<?> getCurrent() {
		Object current = getCurrentSelected();
		return getNodeContainer(current);
	}

	private Object getCurrentUserData() {
		Object current = getCurrentSelected();
		if (current instanceof Tab) {
			return ((Tab) current).getUserData();
		}
		else if (current instanceof Stage) {
			return ((Stage) current).getUserData();
		}
		return null;
	}
	
	private Object getCurrentSelected() {
		Object current = null;
		if (this.stage.isFocused()) {
			current = this.tabArtifacts.getSelectionModel().getSelectedItem();
		}
		else {
			for (Stage stage : stages.values()) {
				if (stage.isFocused()) {
					current = stage;
					break;
				}
			}
		}
		// if we are using a popup (e.g. fixed value), noone has current focus so we need to go by the last one
		if (current == null && this.lastFocused != null) {
			if (this.lastFocused.equals(this.stage)) {
				current = this.tabArtifacts.getSelectionModel().getSelectedItem();
			}
			else {
				for (Stage stage : stages.values()) {
					if (lastFocused.equals(stage)) {
						current = stage;
						break;
					}
				}
			}
		}
		return current;
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		instance = this;
//		lstNotifications.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Validation<?>>() {
//			@Override
//			public void changed(ObservableValue<? extends Validation<?>> arg0, Validation<?> arg1, final Validation<?> arg2) {
//				if (validationsId != null && arg2 != null) {
//					for (final ArtifactGUIInstance instance : managers.values()) {
//						if (validationsId.equals(instance.getId())) {
//							if (instance instanceof ValidatableArtifactGUIInstance) {
//								Platform.runLater(new Runnable() {
//									public void run() {
//										((ValidatableArtifactGUIInstance) instance).locate(arg2);
//									}
//								});
//							}
//							break;
//						}
//					}
//				}
//			}
//		});
//		lstNotifications.setCellFactory(new Callback<ListView<Validation<?>>, ListCell<Validation<?>>>() {
//			@Override 
//			public ListCell<Validation<?>> call(ListView<Validation<?>> list) {
//				return new ListCell<Validation<?>>() {
//					@Override
//					protected void updateItem(Validation<?> arg0, boolean arg1) {
//						super.updateItem(arg0, arg1);
//						setText(arg0 == null ? null : arg0.getMessage());
//					}
//				};
//			}
//		});
		
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

	private void initializeButtons() {
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
		
		// if we close a tab, throw away all gui instances associated with it
		// otherwise, upon save, we loop through the instances and save all instances, even the ones that have been long closed
		tabArtifacts.getTabs().addListener(new ListChangeListener<Tab>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Tab> change) {
				while (change.next()) {
					if (change.wasRemoved()) {
						for (Tab tab : change.getRemoved()) {
							removeContainer(tab);
						}
					}
				}
			}
		});
		
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
		mniFind.addEventHandler(ActionEvent.ANY, newFindHandler(stage, true));
		mniRun.addEventHandler(ActionEvent.ANY, newRunHandler(stage));
		mniDetach.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Tab selectedItem = tabArtifacts.getSelectionModel().getSelectedItem();
				if (selectedItem != null) {
					detach(selectedItem);
				}
			}
		});
		mniMaximize.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double[] dividerPositions = splMain.getDividerPositions();
				// we were maximized, demaximize
				if (previousDividerPositions != null) {
					splMain.setDividerPositions(previousDividerPositions);
					previousDividerPositions = null;
				}
				else {
					previousDividerPositions = dividerPositions;
					splMain.setDividerPositions(0, 1, 0);
				}
			}
		});
		mniSave.addEventHandler(ActionEvent.ANY, newSaveHandler());
		mniSaveAll.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (!connected.get()) {
					showNotification(Severity.ERROR, "Disconnected", "Can not save while not connected to the server");
				}
				else {
					// see below...
//					tabArtifacts.requestFocus();
					List<String> saved = new ArrayList<String>();
					for (NodeContainer<?> tab : managers.keySet()) {
						ArtifactGUIInstance instance = managers.get(tab);
						if (instance.isReady() && hasLock(instance.getId()).get() & instance.isEditable() && instance.hasChanged()) {
							try {
								System.out.println("Saving " + instance.getId());
								instance.save();
								if (repositoryValidatorService != null) {
									repositoryValidatorService.clear(instance.getId());
								}
								tab.setChanged(false);
								instance.setChanged(false);
								saved.add(instance.getId());
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}
							try {
								getAsynchronousRemoteServer().reload(instance.getId());
								getCollaborationClient().updated(instance.getId(), "Saved");
							}
							catch (Exception e) {
								logger.error("Could not remotely reload: " + instance.getId(), e);
							}
						}
						if (instance instanceof ArtifactGUIInstanceWithChildren) {
							try {
								((ArtifactGUIInstanceWithChildren) instance).saveChildren();
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}	
						}
					}
					if (!saved.isEmpty()) {
						// redraw all tabs, there might be interdependent changes
						for (NodeContainer<?> container : managers.keySet()) {
							ArtifactGUIInstance guiInstance = managers.get(container);
							if (!guiInstance.hasChanged() && guiInstance.isReady() && guiInstance instanceof RefresheableArtifactGUIInstance && repository.getReferences(guiInstance.getId()).removeAll(saved)) {
								refreshContainer(container);
							}
						}
					}
				}
			}
		});
		mniLocate.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// see below...
//				tabArtifacts.requestFocus();
				NodeContainer<?> selected = getCurrent();
				if (selected != null) {
					if (managers.containsKey(selected)) {
						locate(managers.get(selected).getId());
					}
				}
				event.consume();
			}
		});
		mniGrep.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				Entry root = null;
				TreeCell<Entry> selectedItem = getRepositoryBrowser().getControl().getSelectionModel().getSelectedItem();
				if (selectedItem != null) {
					root = selectedItem.getItem().itemProperty().get();
				}
				// don't search if you have nothing selected
				// cause then we would need to use the root which is probably quite a bit of files to search, has to be an explicit choice, not default behavior
				if (root instanceof ResourceEntry) {
					Find<Entry> find = new Find<Entry>(new Marshallable<Entry>() {
						@Override
						public String marshal(Entry instance) {
							return instance.getId();
						}
					}, new FindFilter<Entry>() {
						@Override
						public boolean accept(Entry item, String newValue) {
							if (item instanceof ResourceEntry) {
								if (newValue == null || newValue.trim().isEmpty()) {
									return true;
								}
								Map<Entry, List<Resource>> map = new HashMap<Entry, List<Resource>>();
								try {
									boolean regex = !newValue.matches("[\\w\\s.:-]+");
									if (regex) {
										newValue = newValue.toLowerCase().replace("*", ".*");
									}
									grep(item, newValue, regex, map, false);
									return !map.isEmpty();
								}
								catch (IOException e) {
									// ignore
								}
							}
							return false;
						}
					});
//					find.selectedItemProperty().addListener(new ChangeListener<Entry>() {
//						@Override
//						public void changed(ObservableValue<? extends Entry> observable, Entry oldValue, Entry newValue) {
//							if (newValue != null) {
//								locate(newValue.getId());
//							}
//						}
//					});
					find.finalSelectedItemProperty().addListener(new ChangeListener<Entry>() {
						@Override
						public void changed(ObservableValue<? extends Entry> observable, Entry oldValue, Entry newValue) {
							if (newValue != null) {
								locate(newValue.getId());
								RepositoryBrowser.open(MainController.this, tree.getSelectionModel().getSelectedItem().getItem());
							}
						}
					});
					find.setHeavySearch(true);
					find.show(root.isLeaf() ? Arrays.asList(root) : flattenResourceEntries(root), "Find in Repository (content)");
				}
			}
		});
		mniClose.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// important (2016-01-27): upon closing the tab, the focus would (sometimes) jump back to the tree on the left
				// for some reason this refocus triggers a reposition of the scrollpane making it scroll down which is very annoying
				// i can not find any reason for the autoscroll but basically making sure the tabartifacts has the focus seems to preempt the focus switch
//				tabArtifacts.requestFocus();
				NodeContainer<?> selected = getCurrent();
				if (selected != null) {
					if (selected.isChanged()) {
						Confirm.confirm(ConfirmType.QUESTION, "Changes pending in " + selected.getId(), "Are you sure you want to discard the pending changes?", new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								selected.close();
							}
						});
					}
					else {
						selected.close();
					}
				}
				// if we have no container, just close the current tab
				else {
					Tab selectedItem = tabArtifacts.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						tabArtifacts.getTabs().remove(selectedItem);
					}
				}
				event.consume();
			}
		});
		mniCloseAll.addEventHandler(ActionEvent.ANY, newCloseAllHandler());
		mniCloseOther.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
//				tabArtifacts.requestFocus();
				NodeContainer<?> selected = getCurrent();
				// nothing selected
				if (selected != null && selected.getContainer() instanceof Tab) {
					tabArtifacts.getTabs().clear();
					tabArtifacts.getTabs().retainAll((Tab) selected.getContainer());
				}
			}
		});
		mniRebuildReferences.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				// the root is special as the visual display does not match an actual root
				// also if you refresh the root, no need to refresh anything else
				if (tree.getSelectionModel().getSelectedItems().contains(tree.getRootCell())) {
					for (String reference : repository.rebuildReferences(null, true)) {
						logValidation(new ValidationMessage(Severity.INFO, reference));
					}
				}
				else {
					for (TreeCell<Entry> selected : tree.getSelectionModel().getSelectedItems()) {
						for (String reference : repository.rebuildReferences(selected.getItem().itemProperty().get().getId(), true)) {
							logValidation(new ValidationMessage(Severity.INFO, reference));
						}
					}
				}
			}
		});
		mniUpdateReference.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			private void buildReferences(Entry entry, Set<String> references, List<Entry> artifacts, List<ValidationMessage> validations) {
				if (entry instanceof ResourceEntry) {
					if (entry.isNode()) {
						try {
							references.addAll(entry.getNode().getReferences());
							artifacts.add(entry);
						}
						catch (Exception e) {
							validations.add(new ValidationMessage(Severity.ERROR, "Could not load: " + entry.getId()));
						}
						MainController.this.closeAll(entry.getId());
					}
					for (Entry child : entry) {
						buildReferences(child, references, artifacts, validations);
					}
				}
			}
			
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void handle(ActionEvent arg0) {
				final List<Entry> artifacts = new ArrayList<Entry>();
				Set<String> references = new TreeSet<String>();
				List<ValidationMessage> validations = new ArrayList<ValidationMessage>();
				for (TreeCell<Entry> selectedItem : getRepositoryBrowser().getControl().getSelectionModel().getSelectedItems()) {
					Entry entry = selectedItem.getItem().itemProperty().get();
					buildReferences(entry, references, artifacts, validations);
				}
				if (!validations.isEmpty()) {
					MainController.getInstance().notify(validations);
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
	}

	private EventHandler<ActionEvent> newCloseAllHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
//				tabArtifacts.requestFocus();
				tabArtifacts.getTabs().clear();
				for (Stage stage : new ArrayList<Stage>(stages.values())) {
					stage.close();
				}
				event.consume();
			}
		};
	}

	private EventHandler<ActionEvent> newRunHandler(Stage stage) {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				NodeContainer<?> selectedItem = getCurrent();
				Artifact resolve = null;
				if (selectedItem != null) {
					resolve = getRepository().resolve(selectedItem.getId());
				}
				// if it can not be resolved by id, we can't tell the server what to run!
//				else {
//					Object userData = getCurrentUserData();
//					resolve = userData instanceof Artifact ? (Artifact) userData : null;
//				}
				if (resolve instanceof DefinedService) {
					new RunService((Service) resolve).build(MainController.this, stage);		
				}
			}
		};
	}

	private EventHandler<ActionEvent> newFindHandler(final Stage stage, boolean locate) {
		return new EventHandler<ActionEvent>() {
			private List<String> nodes;
			private void populate(Entry entry) {
				if (entry.isNode() && (isShowHidden() || !entry.getNode().isHidden())) {
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
			public void handle(ActionEvent event) {
				if (currentFind != null) {
					currentFind.focus();
				}
				else {
					Find<String> find = new Find<String>(new StringMarshallable());
					ListView<String> list = find.getList();
					list.addEventHandler(MouseEvent.DRAG_DETECTED, new EventHandler<MouseEvent>() {
						@Override
						public void handle(MouseEvent event) {
							String selectedItem = list.getSelectionModel().getSelectedItem();
							if (selectedItem != null) {
								dragSource = new WeakReference<Stage>(find.getStage());
								
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
					EventHandler<KeyEvent> keyPressedEventHandler = new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent event) {
							if (event.getCode() == KeyCode.C && event.isControlDown()) {
								if (list.getSelectionModel().getSelectedItem() != null) {
									Artifact resolve = getRepository().resolve(list.getSelectionModel().getSelectedItem());
									if (resolve != null) {
										copy(resolve);
									}
									else {
										Entry entry = getRepository().getEntry(list.getSelectionModel().getSelectedItem());
										if (entry != null) {
											copy(entry);
										}
									}
								}
								event.consume();
							}
						}
					};
					list.addEventHandler(KeyEvent.KEY_PRESSED, keyPressedEventHandler);
					find.getField().addEventHandler(KeyEvent.KEY_PRESSED, keyPressedEventHandler);
//					find.selectedItemProperty().addListener(new ChangeListener<String>() {
//						@Override
//						public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
//							if (newValue != null) {
//								if (locate) {
//									locate(newValue);
//								}
//							}
//						}
//					});
					find.finalSelectedItemProperty().addListener(new ChangeListener<String>() {
						@Override
						public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
							if (newValue != null) {
								if (locate) {
									locate(newValue);
								}
								open(newValue);
//								RepositoryBrowser.open(MainController.this, tree.getSelectionModel().getSelectedItem().getItem());
							}
						}
					});
					find.show(getNodes(), "Find in Repository", stage);
					currentFind = find;
					find.getStage().showingProperty().addListener(new ChangeListener<Boolean>() {
						@Override
						public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
							if (newValue != null && !newValue) {
								currentFind = null;
								if (stage != null) {
									stage.requestFocus();
								}
								else {
									MainController.this.stage.requestFocus();
								}
							}
						}
					});
					event.consume();
				}
			}
		};
	}

	private EventHandler<ActionEvent> newSaveHandler() {
		return new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (!connected.get()) {
					showNotification(Severity.ERROR, "Disconnected", "Can not save while not connected to the server");
				}
				else {
					// see below...
//					tabArtifacts.requestFocus();
					NodeContainer<?> selected = getCurrent();
					if (selected != null) {
						ArtifactGUIInstance instance = managers.get(selected);
						if (instance != null && hasLock(instance.getId()).get() && instance.isReady() && instance.isEditable() && instance.hasChanged()) {
							try {
								System.out.println("Saving " + selected.getId());
								// this will save the instance, but also reload some stuff
								save(instance.getId());
//								instance.save();
								if (repositoryValidatorService != null) {
									repositoryValidatorService.clear(selected.getId());
								}
								selected.setChanged(false);
								instance.setChanged(false);
								// check all the open tabs, if they are somehow dependent on this item and have no pending edits, refresh
								for (NodeContainer<?> tab : managers.keySet()) {
									ArtifactGUIInstance guiInstance = managers.get(tab);
									// IMPORTANT: we only check _direct_ references. it could be you depend on it indirectly but then it shouldn't affect your display!
									if (!instance.equals(guiInstance) && !guiInstance.hasChanged() && guiInstance.isReady() && guiInstance instanceof RefresheableArtifactGUIInstance && repository.getReferences(guiInstance.getId()).contains(instance.getId())) {
										refreshContainer(tab);
									}
								}
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}
							try {
								getAsynchronousRemoteServer().reload(instance.getId());
								getCollaborationClient().updated(instance.getId(), "Saved");
							}
							catch (Exception e) {
								logger.error("Could not remotely reload: " + instance.getId(), e);
							}
						}
						if (instance instanceof ArtifactGUIInstanceWithChildren) {
							try {
								((ArtifactGUIInstanceWithChildren) instance).saveChildren();
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}	
						}
					}
				}
			}
		};
	}
	
	public void logDeveloperText(final String message) {
		Runnable doIt = new Runnable() {
			public void run() {
				vbxDeveloperLog.getChildren().add(0, new Label(message));
				// if it's too big, remove at the end
				while (vbxDeveloperLog.getChildren().size() > 1000) {
					vbxDeveloperLog.getChildren().remove(vbxDeveloperLog.getChildren().size() - 1);
				}
				
			}
		};
		if (Platform.isFxApplicationThread()) {
			doIt.run();
		}
		else {
			Platform.runLater(doIt);
		}
	}
	
	public void logServerText(NabuLogMessage message) {
		logServerText(message, false);
	}
	private void logServerText(NabuLogMessage message, boolean notification) {
		SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, HH:mm:ss");
		HBox box = new HBox();
		Label timestamp = new Label(formatter.format(message.getTimestamp()));
		Label severity = new Label(message.getSeverity() == null ? Severity.INFO.toString() : message.getSeverity().toString());
		Label context = new Label(message.getContext().toString());
		Label text = new Label(message.getMessage());
		if (message.getSeverity() != null && message.getSeverity().equals(Severity.ERROR)) {
			severity.setStyle("-fx-text-fill: red;-fx-font-weight: bold;");
			text.setStyle("-fx-text-fill: red");
			timestamp.setStyle("-fx-text-fill: red;");
			context.setStyle("-fx-text-fill: red;");
		}
		else {
			timestamp.setStyle("-fx-text-fill: #888;");
			context.setStyle("-fx-text-fill: #888;");
			severity.setStyle("-fx-font-weight: bold;");
		}
		timestamp.setPadding(new Insets(2, 5, 2, 0));
		severity.setPadding(new Insets(2, 5, 2, 5));
		text.setPadding(new Insets(2, 5, 2, 5));
		context.setPadding(new Insets(2, 5, 2, 5));
		box.getChildren().addAll(timestamp, severity, context, text);
		MenuItem item = new MenuItem("Copy to clipboard");
		item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				String text = formatter.format(message.getTimestamp()) + " [" + message.getSeverity() + "] " + message.getContext() + ": " + message.getMessage();
				if (message.getDescription() != null) {
					text += "\n" + message.getDescription();
				}
				copy(text);
				showNotification(Severity.INFO, "Copied", "Copied description to clipboard");
			}
		});
		ContextMenu menu = new ContextMenu();
		menu.getItems().add(item);
		EventHandler<ContextMenuEvent> eventHandler = new EventHandler<ContextMenuEvent>() {
			@Override
			public void handle(ContextMenuEvent event) {
				menu.show(box.getScene().getWindow(), event.getScreenX(), event.getScreenY());
			}
		};
		if (message.getDescription() != null) {
			VBox vbox = new VBox();
			vbox.setOnContextMenuRequested(eventHandler);
			Label description = new Label(message.getDescription());
			if (message.getSeverity().equals(Severity.ERROR)) {
				description.setStyle("-fx-text-fill: red;");
			}
			description.setPadding(new Insets(2, 0, 5, 15));
			vbox.getChildren().addAll(box, description);
			if (notification) {
				vbox.setStyle("-fx-background-color: #fafafa; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-border-color:#cccccc;");
				vbox.setPadding(new Insets(10));
				VBox.setMargin(vbox, new Insets(3, 0, 3, 0));
			}
			vbxServerLog.getChildren().add(0, vbox);
		}
		else {
			box.setOnContextMenuRequested(eventHandler);
			if (notification) {
				box.setStyle("-fx-background-color: #fafafa; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-border-color:#cccccc;");
				box.setPadding(new Insets(10));
				VBox.setMargin(box, new Insets(3, 0, 3, 0));
			}
			vbxServerLog.getChildren().add(0, box);
		}
		// if it's too big, remove at the end
		while (vbxServerLog.getChildren().size() > 500) {
			vbxServerLog.getChildren().remove(vbxServerLog.getChildren().size() - 1);
		}
	}
	
	public void logNotification(Notification notification) {
		NabuLogMessage message = new NabuLogMessage();
		message.setSeverity(notification.getSeverity());
		message.setDescription(notification.getDescription());
		message.setContext(notification.getContext());
		message.setTimestamp(notification.getCreated());
		message.setErrorCode(notification.getCode());
		logServerText(message, true);
	}
	
	private void logValidation(Validation<?> message) {
		logDeveloperText("[" + message.getSeverity() + "] " + message.getMessage());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static List<ValidationMessage> updateReference(Entry entry, String oldReference, String newReference) throws InstantiationException, IllegalAccessException, IOException, FormatException, ParseException {
		List<ValidationMessage> validations = new ArrayList<ValidationMessage>();
		ArtifactManager artifactManager = entry.getNode().getArtifactManager().newInstance();
		try {
			Artifact artifact = entry.getNode().getArtifact();
			validations.addAll(artifactManager.updateReference(artifact, oldReference, newReference));
			artifactManager.save((ResourceEntry) entry, artifact);
			getInstance().getAsynchronousRemoteServer().reload(artifact.getId());
			getInstance().getCollaborationClient().updated(artifact.getId(), "Updated references");
		}
		catch (Exception e) {
			if (artifactManager instanceof BrokenReferenceArtifactManager) {
				validations.addAll(((BrokenReferenceArtifactManager) artifactManager).updateBrokenReference(((ResourceEntry) entry).getContainer(), oldReference, newReference));
				getInstance().getRepository().reload(entry.getId());
				getInstance().getAsynchronousRemoteServer().reload(entry.getId());
				getInstance().getCollaborationClient().updated(entry.getId(), "Updated broken references");
			}
			else {
				throw e;
			}
		}
		return validations;
	}

	
	public boolean isBrokenReference(String reference) {
		return EAIRepositoryUtils.isBrokenReference(repository, reference);
	}
	
	public TreeItem<Entry> getTreeEntry(String id) {
		return tree.resolve(id.replace('.', '/'));
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
		for (NodeContainer<?> container : managers.keySet()) {
			if (id.equals(container.getId())) {
				refreshContainer(container);
			}
		}
	}
	
	// a refresh will refresh the latest version and redraw it
	// a redraw will simply redraw it, allowing for in-memory adaptations
	public void redraw(String id) {
		for (NodeContainer<?> container : managers.keySet()) {
			if (id.equals(container.getId())) {
				redrawContainer(container);
			}
		}
	}
	
	private void redrawContainer(NodeContainer<?> container) {
		ArtifactGUIInstance guiInstance = managers.get(container);
		if (guiInstance instanceof RedrawableArtifactGUIInstance) {
			try {
				AnchorPane pane = new AnchorPane();
				((RedrawableArtifactGUIInstance) guiInstance).redraw(pane);
				// only redraw the pane if it has content
				// might have refreshed in situ (this came later)
				if (!pane.getChildren().isEmpty()) {
					container.setContent(pane);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Can not refresh: " + container.getId());
		}
	}
	
	private void refreshContainer(NodeContainer<?> container) {
		ArtifactGUIInstance guiInstance = managers.get(container);
		if (guiInstance instanceof RefresheableArtifactGUIInstance) {
			try {
				AnchorPane pane = new AnchorPane();
				((RefresheableArtifactGUIInstance) guiInstance).refresh(pane);
				// only redraw the pane if it has content
				// might have refreshed in situ (this came later)
				if (!pane.getChildren().isEmpty()) {
					container.setContent(pane);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Can not refresh: " + container.getId());
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
		if (!connected.get()) {
			showNotification(Severity.ERROR, "Disconnected", "Can not save while not connected to the server");
		}
		else {
			for (ArtifactGUIInstance instance : managers.values()) {
				if (instance.isReady() && instance.getId().equals(id)) {
					if (instance.isEditable()) {
						instance.save();
						if (repositoryValidatorService != null) {
							repositoryValidatorService.clear(id);
						}
						for (NodeContainer<?> container : managers.keySet()) {
							if (id.equals(container.getId()) && container.isChanged()) {
								container.setChanged(false);
							}
						}
						try {
							// reload locally
							getRepository().reload(instance.getId());
							TreeItem<Entry> resolve = getRepositoryBrowser().getControl().resolve(instance.getId().replace(".", "/"));
							TreeCell<Entry> treeCell = getTree().getTreeCell(resolve);
//							resolve.refresh(true);
							treeCell.refresh(true);
							getAsynchronousRemoteServer().reload(instance.getId());
							getCollaborationClient().updated(instance.getId(), "Saved");
						} 
						catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}
	}
	
	public void updated(String id) {
		try {
			// reload locally
			getRepository().reload(id);
			getAsynchronousRemoteServer().reload(id);
			getCollaborationClient().updated(id, "Updated");
			
			TreeItem<Entry> resolve = getRepositoryBrowser().getControl().resolve(id.replace(".", "/"));
			Entry entry = getRepository().getEntry(id);
			// reload the entry always
			resolve.refresh(true);
			// if we have an artifact that is not a leaf, it probably has generated children, we need harder refresh
			// not working yet...
//			if (!entry.isLeaf()) {
//				TreeCell<Entry> parent = getTree().getTreeCell(resolve.getParent());
//				Platform.runLater(new Runnable() {
//					public void run() {
//						parent.refresh(false);
//						for (TreeCell<Entry> child : parent.getChildren()) {
//							System.out.println("reloading " + child.getItem().getName() + ": " + child.getItem().getName().equals(entry.getName()));
//							if (child.getItem().getName().equals(entry.getName())) {
//								child.refresh(false);
//							}
//						}
//						
//					}
//				});
//			}
		} 
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		refresh(id);
	}
	
	/**
	 * Set the current element to changed
	 */
	public void setChanged() {
		NodeContainer<?> selected = getCurrent();
		if (selected != null) {
			ArtifactGUIInstance instance = managers.get(selected);
			if (instance != null) {
				instance.setChanged(true);
				selected.setChanged(true);
			}
		}
	}
	
	public void setChanged(String id) {
		for (NodeContainer<?> container : managers.keySet()) {
			if (id.equals(container.getId())) {
				ArtifactGUIInstance instance = managers.get(container);
				if (instance != null) {
					instance.setChanged(true);
					container.setChanged(true);
				}
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void show(Artifact artifact) {
		ArtifactGUIManager<?> guiManager = getGUIManager(artifact.getClass());
		if (guiManager instanceof PortableArtifactGUIManager) {
			final Tab tab = new Tab(artifact.getId() + " (Read-only)");
			tab.setId(artifact.getId());
			tab.setGraphic(MainController.loadGraphic("status/locked.png"));
			AnchorPane pane = new AnchorPane();
			try {
				((PortableArtifactGUIManager) guiManager).display(this, pane, artifact);
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			tab.setContent(pane);
			tabArtifacts.getTabs().add(tab);
			tabArtifacts.selectionModelProperty().get().select(tab);
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
		
		Entry entry = getRepository().getEntry(id);

		// initially locked
		tab.setGraphic(MainController.loadGraphic("status/locked.png"));
		
		BooleanProperty hasLock = hasLock(id);
		
		if (entry != null) {
			MenuItem menu = new MenuItem("Request Lock");
			menu.disableProperty().bind(hasLock);
			ContextMenu contextMenu = new ContextMenu(menu);
			tab.setContextMenu(contextMenu);
			menu.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					if (lock(id).get() == null) {
						getCollaborationClient().lock(id, "Locking");
					}
					else {
						System.out.println("Requesting lock for: " + id);
						getCollaborationClient().requestLock(id);
					}
				}
			});
		}
		
		hasLock.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) {
				if (arg2 != null && arg2) {
					tab.setGraphic(MainController.loadGraphic("status/unlocked.png"));
				}
				else {
					tab.setGraphic(MainController.loadGraphic("status/locked.png"));
					Tooltip tooltip = new Tooltip();
					tooltip.textProperty().bind(lock(id));
					tab.setTooltip(tooltip);
				}
			}
		});
		
		// can create race conditions where multiple clients try to get the lock once someone releases it
		// if you can't get it on open, just leave it until you explicitly request it
		/*
		tryLock(id, new SimpleBooleanProperty() {
			@Override
			public boolean get() {
				return tabArtifacts.getTabs().contains(tab);
			}
		});
		*/
		
		tryLock(id, null);
		
		tab.getStyleClass().add(id.replace('.', '_'));
		if (entry != null && entry.isNode()) {
			tab.getStyleClass().add(entry.getNode().getArtifactClass().getName().replace('.', '_'));
		}
		tabArtifacts.getTabs().add(tab);
		tabArtifacts.selectionModelProperty().get().select(tab);
		TabNodeContainer container = new TabNodeContainer(tab, tabArtifacts);
		managers.put(container, instance);
		tab.contentProperty().addListener(new ChangeListener<javafx.scene.Node>() {
			@Override
			public void changed(ObservableValue<? extends javafx.scene.Node> arg0, javafx.scene.Node arg1, javafx.scene.Node arg2) {
				if (arg2 != null) {
					arg2.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
						@Override
						public void handle(KeyEvent arg0) {
							if (instance instanceof RefresheableArtifactGUIInstance && arg0.getCode() == KeyCode.F5) {
								refreshContainer(container);
								container.setChanged(false);
							}
							else if (arg0.getCode() == KeyCode.F11) {
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
		
		if (entry instanceof ResourceEntry) {
			tabArtifacts.getTabs().addListener(new ListChangeListener<Tab>() {
				@Override
				public void onChanged(javafx.collections.ListChangeListener.Change<? extends Tab> change) {
					while (change.next()) {
						if (change.wasRemoved()) {
							if (change.getRemoved().contains(tab)) {
								MainController.getInstance().getCollaborationClient().unlock(id, "Closed");
								tabArtifacts.getTabs().removeListener(this);
							}
						}
					}
				}
			});
		}
		
//		decouplable(tab);
		return tab;
	}

	public EAIResourceRepository getRepository() {
		return repository;
	}
	
	/**
	 * IMPORTANT: this method was "quick fixed"
	 * In the beginning GUI managers were thought to be stateless but turns out they aren't. They keep state per instance they manage.
	 * Ideally I would've added an artifact gui manager factory but we needed a quick fix (there were already a _lot_ of gui managers and deadlines are approaching)
	 * Because all of the managers however di)d have an empty constructor, we went for this solution (@2015-12-01)
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
		return loadFixedSizeGraphic(name, size, size);
	}
	
	public static Node loadFixedSizeGraphic(String name, int size, int containerSize) {
		HBox box = new HBox();
		ImageView loadGraphic = loadGraphic(name);
		if (loadGraphic.getImage().getWidth() > size) {
			loadGraphic.setPreserveRatio(true);
			loadGraphic.setFitWidth(size);
		}
		if (loadGraphic.getImage().getHeight() > size) {
			loadGraphic.setPreserveRatio(true);
			loadGraphic.setFitHeight(size);
		}
		box.getChildren().add(loadGraphic);
		box.setAlignment(Pos.CENTER);
		box.setMinWidth(containerSize);
		box.setMaxWidth(containerSize);
		box.setPrefWidth(containerSize);
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
	
	public Stage getActiveStage() {
		if (stage.isFocused()) {
			return stage;
		}
		for (Stage stage : stages.values()) {
			if (stage.isFocused()) {
				return stage;
			}
		}
		return lastFocused;
	}
	
	public NodeContainer<?> getContainer(String id) {
		Tab tab = getTab(id);
		if (tab != null) {
			return new TabNodeContainer(tab, tabArtifacts);
		}
		Stage stage = getStage(id);
		if (stage != null) {
			return new StageNodeContainer(stage);
		}
		return null;
	}
	
	public NodeContainer<?> newContainer(String id, Node content) {
		Tab newTab = newTab(id);
		newTab.setContent(content);
		return new TabNodeContainer(newTab, tabArtifacts);
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

	public boolean activate(String id) {
		Tab tab = getTab(id);
		if (tab != null) {
			tabArtifacts.getSelectionModel().select(tab);
			return true;
		}
		Stage stage = getStage(id);
		if (stage != null) {
			stage.requestFocus();
			return true;
		}
		return false;
	}
	
	public Tab getTab(String id) {
		if (tabArtifacts != null && tabArtifacts.getTabs() != null) {
			for (Tab tab : tabArtifacts.getTabs()) {
				if (tab.getId().equals(id)) {
					return tab;
				}
			}
		}
		return null;
	}
	
	public Stage getStage(String id) {
		Set<java.util.Map.Entry<String, Stage>> entrySet = stages.entrySet();
		Iterator<java.util.Map.Entry<String, Stage>> iterator = entrySet.iterator();
		while (iterator.hasNext()) {
			java.util.Map.Entry<String, Stage> next = iterator.next();
			if (!next.getValue().isShowing()) {
				iterator.remove();
			}
			else if (next.getKey().equals(id)) {
				return next.getValue();
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
	
	public void notify(Throwable throwable) {
		throwable.printStackTrace();
//		notificationHandler.notify(throwable.getMessage(), 5000l, Severity.ERROR);
		notify(new ValidationMessage(Severity.ERROR, throwable.getMessage()));
	}
	
	@Override
	public void notify(ValidationMessage...messages) {
		notify(Arrays.asList(messages));
	}
	
	public void notify(List<? extends Validation<?>> messages) {
		NodeContainer<?> selected = getCurrent();
		if (selected != null) {
			ArtifactGUIInstance instance = managers.get(selected);
			if (instance != null) {
				validationsId = instance.getId();
			}
		}
		if (messages != null) {
			for (Validation<?> message : messages) {
				logValidation(message);
			}
		}
	}
	
	public void showProperties(final PropertyUpdater updater) {
		Pane target = null;
		if (updater instanceof PropertyUpdaterWithSource) {
			String sourceId = ((PropertyUpdaterWithSource) updater).getSourceId();
			// if it is drawn in a separate stage, check if it has a properties pane
			if (stages.containsKey(sourceId)) {
				Stage stage = stages.get(sourceId);
				Node lookup = stage.getScene().lookup("#properties");
				if (lookup instanceof Pane) {
					target = (Pane) lookup;
				}
			}
		}
		if (target == null) {
			target = ancProperties;
		}
		showProperties(updater, target, true);
	}
	
	public AnchorPane getAncProperties() {
		return ancProperties;
	}

	public Pane showProperties(final PropertyUpdater updater, final Pane target, final boolean refresh) {
		return showProperties(updater, target, refresh, getRepository());
	}
	
	public boolean isInContainer(Pane target) {
		boolean isInTab = false;
		Parent parent = target.getParent();
		List<Node> tabContents = new ArrayList<Node>();
		for (Tab tab : tabArtifacts.getTabs()) {
			tabContents.add(tab.getContent());
		}
		for (Stage stage : stages.values()) {
			tabContents.add(stage.getScene().getRoot());
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
		return showProperties(updater, target, refresh, repository, isInContainer(target));
	}
	
	public Pane showProperties(final PropertyUpdater updater, final Pane target, final boolean refresh, Repository repository, boolean updateChanged) {
		return showProperties(updater, target, refresh, repository, updateChanged, leftAlignLabels);
	}
	
	public Pane showProperties(final PropertyUpdater updater, final Pane target, final boolean refresh, Repository repository, boolean updateChanged, boolean leftAlignLabels) {
		final GridPane grid = new GridPane();
		grid.getStyleClass().add("propertyPane");
		grid.setVgap(5);
		grid.setHgap(10);
		ColumnConstraints column1 = new ColumnConstraints();
		column1.setMinWidth(150);
		grid.getColumnConstraints().add(column1);
		
		ColumnConstraints column2 = new ColumnConstraints();
		column2.setHgrow(Priority.ALWAYS);
		grid.getColumnConstraints().add(column2);
		
		SinglePropertyDrawer gridDrawer = new SinglePropertyDrawer() {
			int row = 0;
			@Override
			public void draw(Node label, Node value, Node additional) {
				Label labelToStyle = null;
				if (label instanceof Label) {
					labelToStyle = (Label) label;
				}
				else {
					labelToStyle = (Label) label.lookup("#property-name");
				}
				if (labelToStyle != null) {
					String originalText = ((Label) labelToStyle).getText();
					// we want to get rid of the properties bit, this indicates property maps
					if (originalText.startsWith("properties[") && originalText.trim().endsWith("]:")) {
						originalText = originalText.trim().substring("properties[".length(), originalText.length() - 2);
					}
					String newText = NamingConvention.UPPER_TEXT.apply(originalText);
					// replace numbers at the end, this indicates array syntax
					newText = newText.replaceAll("(.*[\\s]+)([0-9]+)$", "$1 ($2)");
					((Label) labelToStyle).setText(newText);
					if (leftAlignLabels) {
						labelToStyle.setStyle("-fx-text-fill: #666666");
					}
					if (!((Label) labelToStyle).getText().endsWith(":")) {
						((Label) labelToStyle).setText(((Label) labelToStyle).getText() + ":");
					}
					if (originalText.endsWith("*")) {
						if (leftAlignLabels) {
							((Label) labelToStyle).setText("* " + ((Label) labelToStyle).getText());
						}
						else {
							((Label) labelToStyle).setText(((Label) labelToStyle).getText() + " *");
						}
					}
				}
				grid.add(label, 0, row);
				grid.add(value, 1, row);
				if (additional != null) {
					grid.add(additional, 2, row);	
				}
				if (!leftAlignLabels) {
					GridPane.setHalignment(label, HPos.RIGHT);
				}
				RowConstraints constraints = new RowConstraints();
//				if (value instanceof TextInputControl) {
				if (!(value instanceof Label)) {
					GridPane.setHgrow(value, Priority.ALWAYS);
				}
				// read only
				else if (value instanceof Label) {
					constraints.setMinHeight(25);
//					value.setStyle("-fx-font-weight: bold");
				}
				grid.getRowConstraints().add(constraints);
				row++;
			}
		};
		PropertyRefresher refresher = new PropertyRefresher() {
			@Override
			public void refresh() {
				showProperties(updater, target, refresh, repository, updateChanged, leftAlignLabels);
			}
		};
		for (final Property<?> property : updater.getSupportedProperties()) {
			if (!(property instanceof SimpleProperty) || !((SimpleProperty<?>) property).isHidden()) {
				drawSingleProperty(updater, property, refresh ? refresher : null, gridDrawer, repository, updateChanged);
			}
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
//		grid.prefWidthProperty().bind(target.widthProperty());
		if (target instanceof AnchorPane) {
			AnchorPane.setLeftAnchor(grid, 0d);
			AnchorPane.setRightAnchor(grid, 0d);
		}
		return grid;
	}
	
	public void open(String id) {
		NodeContainer<?> container = getContainer(id);
		if (container != null) {
			container.activate();
		}
		else {
			TreeItem<Entry> resolve = tree.resolve(id.replace(".", "/"));
			if (resolve != null) {
				RepositoryBrowser.open(this, resolve);
			}
		}
	}

	public static interface SinglePropertyDrawer {
		public void draw(Node label, Node value, Node additional);
	}
	
	public static interface PropertyRefresher {
		public void refresh();
	}
	
	public Stage getStageFor(String artifactId) {
		NodeContainer<?> container = getContainer(artifactId);
		if (container == null) {
			return stage;
		}
		Object container2 = container.getContainer();
		if (container2 instanceof Stage) {
			return (Stage) container2;
		}
		else {
			return stage;
		}
	}
	
	public void attachTooltip(Label label, String description) {
		Node loadGraphic = loadFixedSizeGraphic("info2.png", 10, 16);
		CustomTooltip customTooltip = new CustomTooltip(description);
		customTooltip.install(loadGraphic);
		customTooltip.setMaxWidth(400d);
		label.setGraphic(loadGraphic);
		label.setContentDisplay(ContentDisplay.RIGHT);
	}
	
	private Boolean leftAlignComboBox = Boolean.parseBoolean(System.getProperty("combobox-left", "false"));
	
	public Boolean getLeftAlignComboBox() {
		return leftAlignComboBox;
	}

	public void setLeftAlignComboBox(Boolean leftAlignComboBox) {
		this.leftAlignComboBox = leftAlignComboBox;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void drawSingleProperty(final PropertyUpdater updater, final Property<?> property, PropertyRefresher refresher, SinglePropertyDrawer drawer, Repository repository, boolean updateChanged) {
		Node name = new Label(property.getName() + ": " + (updater.isMandatory(property) ? " *" : ""));
		name.setId("property-name");
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
			: (originalValue instanceof String || originalValue instanceof File || originalValue instanceof byte[] ? originalValue.toString() : stringify(originalValue));
		
		if (property instanceof SimpleProperty && ((SimpleProperty) property).getTitle() != null) {
			Node loadGraphic = loadFixedSizeGraphic("info2.png", 10, 16);
			CustomTooltip customTooltip = new CustomTooltip(((SimpleProperty) property).getTitle());
			customTooltip.install(loadGraphic);
			customTooltip.setMaxWidth(400d);
			((Label) name).setGraphic(loadGraphic);
			if (leftAlignLabels) {
				((Label) name).setContentDisplay(ContentDisplay.RIGHT);
			}
		}
//		if (property instanceof SimpleProperty && ((SimpleProperty) property).getTitle() != null) {
//			HBox box = new HBox();
//			((Label) name).setTooltip(new Tooltip(((SimpleProperty) property).getTitle()));
//			box.getChildren().add(name);
//			Button button = new Button();
//			button.setGraphic(loadGraphic("help.png"));
//			box.getChildren().add(button);
//			String description = ((SimpleProperty) property).getDescription();
//			final String content = ((SimpleProperty) property).getTitle() + (description != null ? "\n\n" + description : "");
//			button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
//				@Override
//				public void handle(ActionEvent arg0) {
//					Confirm.confirm(ConfirmType.INFORMATION, "Description for: " + property.getName(), content, null);
//				}
//			});
//			box.setAlignment(Pos.CENTER_RIGHT);
//			name = box;
//		}
		
		// if we can't convert from a string to the property value, we can't show it
		if (updater.canUpdate(property) && ((property.equals(new SuperTypeProperty()) && allowSuperType) || !property.equals(new SuperTypeProperty()))) {
			String sourceId = updater instanceof PropertyUpdaterWithSource ? ((PropertyUpdaterWithSource) updater).getSourceId() : null;
			// backwards compatibility for container artifacts
			if (sourceId != null && sourceId.startsWith("$self")) {
				sourceId = null;
			}
			// if at this point the source id has a ":", it is pointing to a fragment, let's see if you have the lock for the overarching thing
			else if (sourceId != null && sourceId.contains(":")) {
				sourceId = sourceId.split(":")[0];
			}
			BooleanProperty hasLock = sourceId != null
					? hasLock(sourceId) 
					: new SimpleBooleanProperty(true);
			BooleanBinding doesNotHaveLock = hasLock.not();
			
			if (File.class.equals(property.getValueClass())) {
				File current = (File) originalValue;
				Button choose = new Button("Choose File");
				choose.disableProperty().bind(doesNotHaveLock);
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
				choose.disableProperty().bind(doesNotHaveLock);
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
									if (updateChanged) {
										setChanged();
									}
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
							if (updateChanged) {
								setChanged();
							}
							label.setText("Empty");
						}
					}
				});
				HBox box = new HBox();
				box.getChildren().addAll(choose, clear, label);
				drawer.draw(name, box, null);
			}
			else if ((!(property instanceof SimpleProperty) || !((SimpleProperty)property).isDisableSuggest()) && (property instanceof Enumerated || Boolean.class.equals(property.getValueClass()) || Enum.class.isAssignableFrom(property.getValueClass()) || Artifact.class.isAssignableFrom(property.getValueClass()) || Entry.class.isAssignableFrom(property.getValueClass()))) {
				final ComboBox<String> comboBox = new ComboBox<String>();
				comboBox.setId(((Label) name).getText().replaceAll("[^\\w]+", ""));
				if (property instanceof SimpleProperty) {
					comboBox.setPromptText(((SimpleProperty) property).getDefaultValue());
				}
				comboBox.setEditable(true);
				comboBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
						if (newValue != null && newValue) {
							// @19-06-2020: too annoying!
							//comboBox.show();
						}
					}
				});
				boolean sort = false;
				CheckBox filterByApplication = null;
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
						filterByApplication.disableProperty().bind(doesNotHaveLock);
						filterByApplication.setSelected(true);
						filterByApplication.setTooltip(new Tooltip("Filter by application"));
					}
					String regex = "\\[[^\\]]+\\]";
					for (Value<?> value : updater.getValues()) {
						if (value.getProperty().getName().replaceAll(regex, "").equals(property.getName().replaceAll(regex, ""))) {
							artifacts.remove(value.getValue());
						}
					}
					values = artifacts;
				}
				else if (Entry.class.isAssignableFrom(property.getValueClass())) {
					sort = true;
					throw new UnsupportedOperationException("Currently not supported for entries because they are hierarchic, flattening them might be too much overhead");
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
				
				List<String> serialized = new ArrayList<String>();
				// add null to allow deselection
				serialized.add(null);
				// always add the current value first (null is already added)
				if (currentValue != null) {
					serialized.add(currentValue);
				}
				// fill it
				for (Object value : values) {
					if (value == null) {
						continue;
					}
//					else if (!converter.canConvert(value.getClass(), String.class)) {
//						throw new ClassCastException("Can not convert " + value.getClass() + " to string");
//					}
//					String converted = converter.convert(value, String.class);
					String converted = stringify(value);
					// failed to convert property, the canConvert surfaced as a bottleneck in certain developer scenarios
					// this is a more expedient version of the same check
					if (converted == null) {
						throw new ClassCastException("Can not convert " + value.getClass() + " to string");
					}
					if (!converted.equals(currentValue)) {
						serialized.add(converted);
					}
				}
				if (sort) {
					Collections.sort(serialized, new StringComparator());
				}
				comboBox.getItems().addAll(serialized);
				// and select it
				comboBox.getSelectionModel().select(currentValue);
				
				if (leftAlignComboBox) {
					comboBox.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
					comboBox.getEditor().setStyle("-fx-alignment: baseline-right");
					comboBox.getStyleClass().add("reverse-oriented");
				}
				
				if (filterByApplication != null && sourceId != null) {
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
				HBox box = new HBox();
				box.getChildren().add(comboBox);
				if (Artifact.class.isAssignableFrom(property.getValueClass()) && repository.equals(MainController.getInstance().getRepository())) {
					String selectedItem = comboBox.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						// TODO: button to open the artifact in question
						Button link = new Button();
						link.setGraphic(MainController.loadFixedSizeGraphic("right-chevron.png", 12));
						link.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								String selectedItem = comboBox.getSelectionModel().getSelectedItem();
								if (selectedItem != null) {
									RepositoryBrowser.open(MainController.getInstance(), repository.getEntry(selectedItem));
								}
							}
						});
						box.getChildren().add(link);
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
				// we can't seem to set the margin via css?
				// this is not ideal but hey...
				HBox.setMargin(comboBox, new Insets(0, 2, 0, 0));
				
				// need to explicitly set this or it won't resize
				comboBox.setMaxWidth(Double.MAX_VALUE);
				HBox.setHgrow(comboBox, Priority.ALWAYS);
				box.setAlignment(Pos.CENTER_LEFT);

				comboBox.disableProperty().bind(doesNotHaveLock);
//				comboBox.editableProperty().bind(hasLock);
				
//				drawer.draw(name, box, filterByApplication);
				if (filterByApplication != null) {
					box.getChildren().add(filterByApplication);
				}
				drawer.draw(name, box, null);
			}
			// if we have an equation, don't show it in a datefield
			else if (Date.class.isAssignableFrom(property.getValueClass()) && (currentValue == null || !currentValue.startsWith("="))) {
				DatePicker dateField = new DatePicker();
				dateField.disableProperty().bind(doesNotHaveLock);
				dateField.setPrefWidth(300);
				if (currentValue != null) {
					be.nabu.libs.types.simple.Date date = new be.nabu.libs.types.simple.Date();
					try {
						Value<?> [] properties = property instanceof SimpleProperty ? (Value[]) ((SimpleProperty) property).getAdditional().toArray(new Value[0]) : new Value[0];
						Date unmarshal = date.unmarshal(currentValue, properties);
						dateField.timestampProperty().set(unmarshal.getTime());
					}
					catch (Exception e) {
						notify(e);
					}
				}
				else {
					dateField.setDate(null);
				}
				dateField.timestampProperty().addListener(new ChangeListener<Long>() {
					@Override
					public void changed(ObservableValue<? extends Long> arg0, Long arg1, Long arg2) {
						updater.updateProperty(property, arg2 == null ? null : new Date(arg2));
						if (updateChanged) {
							setChanged();
						}
						if (refresher != null) {
							refresher.refresh();
						}
					}
				});
				
				// need a way to enter a formula instead of a fixed string
				MenuItem item = new MenuItem("Switch to formula");
				item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						updater.updateProperty(property, "=");
						if (refresher != null) {
							refresher.refresh();
						}
					}
				});
				ContextMenu menu = new ContextMenu();
				menu.getItems().add(item);
				dateField.setContextMenu(menu);
				
				drawer.draw(name, dateField, null);
			}
			else {
				final TextInputControl textField = (currentValue != null && currentValue.contains("\n")) || (property instanceof SimpleProperty && ((SimpleProperty) property).isLarge()) ? new TextArea(currentValue) : (property instanceof SimpleProperty && ((SimpleProperty) property).isPassword() ? new PasswordField() : new TextField(currentValue));
				
				if (property instanceof SimpleProperty) {
					textField.setPromptText(((SimpleProperty) property).getDefaultValue());
				}
				textField.setId(((Label) name).getText().replaceAll("[^\\w]+", ""));
				
				MenuItem copy = new MenuItem("Copy");
				copy.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						ClipboardContent content = new ClipboardContent();
						content.putString(textField.getSelectedText());
						Clipboard.getSystemClipboard().setContent(content);
					}
				});
				MenuItem paste = new MenuItem("Paste");
				paste.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						String content = (String) Clipboard.getSystemClipboard().getContent(DataFormat.PLAIN_TEXT);
						if (content != null) {
							textField.replaceSelection(content);
						}
					}
				});
				ContextMenu menu = new ContextMenu();
				menu.getItems().addAll(copy, paste);
				textField.setContextMenu(menu);
				
				if (textField instanceof TextArea && currentValue != null) {
					((TextArea) textField).setPrefRowCount(Math.min(((TextArea) textField).getPrefRowCount(), currentValue.length() - currentValue.replace("\n", "").length() + 1));
				}
				textField.editableProperty().bind(hasLock);
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
				
				// add a way to switch
				if (textField instanceof TextField && refresher != null) {
					if (refresher != null) {
						MenuItem toArea = new MenuItem("Switch to large editor");
						toArea.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								if (parseAndUpdate(updater, property, (textField.getText() == null ? "" : textField.getText()) + "\n", repository, updateChanged) && refresher != null) {
									textField.focusedProperty().removeListener(changeListener);
									refresher.refresh();
								}
							}
						});
						textField.getContextMenu().getItems().addAll(new SeparatorMenuItem(), toArea);
					}
				}
				else if (textField instanceof TextArea && refresher != null) {
					MenuItem toSingle = new MenuItem("Switch to single line editor");
					toSingle.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							String result = textField.getText().replaceAll("[\\n\\r]+", " ").trim();
							System.out.println("the result is: " + result);
							if (parseAndUpdate(updater, property, result, repository, updateChanged) && refresher != null) {
								textField.focusedProperty().removeListener(changeListener);
								refresher.refresh();
							}
						}
					});
					textField.getContextMenu().getItems().addAll(new SeparatorMenuItem(), toSingle);
				}
				
				textField.focusedProperty().addListener(changeListener);
				textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (hasLock.get()) {
							if (event.getCode() == KeyCode.ENTER && event.isControlDown() && textField instanceof TextField) {
								if (parseAndUpdate(updater, property, (textField.getText() == null ? "" : textField.getText()) + "\n", repository, updateChanged) && refresher != null) {
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
					}
				});
				// attempt to add a margin...
				HBox.setMargin(textField, new Insets(0, 2, 0, 0));
				VBox.setMargin(textField, new Insets(0, 2, 0, 0));
				GridPane.setMargin(textField, new Insets(0, 2, 0, 0));
				// when we lose focus, set it as well
				drawer.draw(name, textField, null);
			}
		}
		else if (currentValue != null) {
			TextField lockedTextField = new TextField(currentValue);
			lockedTextField.setEditable(false);
			drawer.draw(name, lockedTextField, null);
//			Label value = new Label(currentValue);
//			drawer.draw(name, value, null);
		}
	}

	private String stringify(Object value) {
		return value instanceof DefinedSimpleType 
			&& (
				((DefinedSimpleType<?>) value).getId().startsWith("java.")
				// hardcoded exception for byte array
				|| ((DefinedSimpleType<?>) value).getId().equals("[B")
				// an exception for the custom duration class
				|| Duration.class.getName().equals(((DefinedSimpleType<?>) value).getId())
			) ? ((DefinedSimpleType<?>) value).getName() : converter.convert(value, String.class);
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
			else if (value != null && value.startsWith("=") && property instanceof EvaluatableProperty) {
				updater.updateProperty(property, value);
				if (updateChanged) {
					setChanged();
				}
				return true;
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
	
	private void showContent(ComplexContent content, String query) {
		this.showContent(this.ancPipeline, content, query);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void showContent(Pane ancPipeline, ComplexContent content, String query) {
		// make sure it is selected
		tabMisc.getSelectionModel().select(tabPipeline);
		ancPipeline.getChildren().clear();
		final ComplexContent original = content;
		if (query != null) {
			TypeOperation operation = getOperation(query);
			if (operation != null) {
				try {
					Object evaluate = operation.evaluate(content);
					// if we found nothing, return an empty structure instance
					if (evaluate == null) {
						Structure structure = new Structure();
						structure.setName("empty");
						content = structure.newInstance();
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
							// might be no results!
							if (toCheck == null) {
								Structure tmp = new Structure();
								tmp.setName("empty");
								toCheck = tmp.newInstance();
							}
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
								if (((ContentTreeItem) item).getDefinition().getType() instanceof SimpleType) {
									ContentTreeItem contentTreeItem = (ContentTreeItem) item;
									Type type = contentTreeItem.getDefinition().getType();
									while (type != null) {
										if (type instanceof be.nabu.libs.types.api.Marshallable) {
											Object object = item.itemProperty().get();
											// we want to marshal the simple value if we have a simple complex type
											if (contentTreeItem.getDefinition().getType() instanceof ComplexType && object instanceof ComplexContent) {
												object = ((ComplexContent) object).get(ComplexType.SIMPLE_TYPE_VALUE);
											}
											final Label value = new Label(
												((be.nabu.libs.types.api.Marshallable) type).marshal(object, contentTreeItem.getDefinition().getProperties()
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
								else if (((ContentTreeItem) item).getDefinition().getType() instanceof BeanType && ((BeanType<?>) ((ContentTreeItem) item).getDefinition().getType()).getBeanClass().equals(Object.class)) {
									Object object = item.itemProperty().get();
									if (object instanceof BeanInstance) {
										object = ((BeanInstance) object).getUnwrapped();
									}
									if (object != null) {
										Type type = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(object.getClass());
										while (type != null) {
											if (type instanceof be.nabu.libs.types.api.Marshallable) {
												// we want to marshal the simple value if we have a simple complex type
												final Label value = new Label(
													((be.nabu.libs.types.api.Marshallable) type).marshal(object, ((ContentTreeItem) item).getDefinition().getProperties()
												));
												newTextContextMenu(value, value.getText());
												value.getStyleClass().add("contentValue");
												hbox.getChildren().add(value);
												break;
											}
											type = type.getSuperType();
										}
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
						showContent(ancPipeline, original, field.getText().trim().isEmpty() ? null : field.getText());
					}
				}
			});
			HBox exports = new HBox();
			final ComplexContent finalContent = content;
			for (BindingProvider provider : BindingProviderFactory.getInstance().getProviders()) {
				String extension = ContentTypeMap.getInstance().getExtensionFor(provider.getContentType());
				Button button = new Button("As " + extension);
				button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						SimpleProperty<File> fileProperty = new SimpleProperty<File>("File", File.class, true);
						Set properties = new LinkedHashSet(Arrays.asList(new Property [] { fileProperty }));
						final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, properties, new ValueImpl<File>(fileProperty, new File("export." + extension)));
						EAIDeveloperUtils.buildPopup(MainController.getInstance(), updater, "Export as " + extension, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								File file = updater.getValue("File");
								if (file != null) {
									MarshallableBinding binding = provider.getMarshallableBinding(finalContent.getType(), Charset.forName("UTF-8"));
									try {
										OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
										try {
											binding.marshal(output, finalContent);
										}
										catch (IOException e) {
											getInstance().notify(e);
										}
										finally {
											output.close();
										}
									}
									catch (IOException e) {
										getInstance().notify(e);
									}
								}
							}
						});
					}
				});
				exports.getChildren().add(button);
			}
			
//			VBox.setVgrow(contentTree, Priority.ALWAYS);
			HBox fieldBox = new HBox();
			fieldBox.setPadding(new Insets(10));
			Label fieldLabel = new Label("Query: ");
			fieldLabel.setPadding(new Insets(4, 10, 0, 5));
			HBox.setHgrow(field, Priority.ALWAYS);
			fieldBox.getChildren().addAll(fieldLabel, field);
			VBox.setVgrow(fieldBox, Priority.NEVER);
			
			// only show this if we are in expert mode
			fieldBox.visibleProperty().bind(expertMode);
			fieldBox.managedProperty().bind(expertMode);
			
			Button asTab = new Button("In tab");
			asTab.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					AnchorPane pane = new AnchorPane();
					showContent(pane, original, query);
					Tab newTab = newTab("Result Viewer");
					newTab.setContent(pane);
				}
			});
			exports.getChildren().add(0, asTab);
			
			if (!exports.getChildren().isEmpty()) {
				exports.setPadding(new Insets(10));
				exports.setAlignment(Pos.CENTER);
				Label exportLabel = new Label("Export result: ");
				exportLabel.setPadding(new Insets(4, 10, 0, 5));
//				exports.getChildren().add(0, exportLabel);
				vbox.getChildren().add(exports);
			}
			
			vbox.getChildren().addAll(fieldBox);

			int contentSet = 0;
			ComplexContent masked = null;
			// any lists of table-like structures we want to show as a table instead, we do this by removing them from the content
			for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
				boolean isObject = child.getType() instanceof BeanType && ((BeanType) child.getType()).getBeanClass().equals(Object.class);
				// a list of elements
				if (child.getType() instanceof ComplexType && child.getType().isList(child.getProperties()) && !isObject) {
					// and it must have no complex children of its own and no lists
					boolean isPlain = true;
					for (Element<?> secondChild : TypeUtils.getAllChildren((ComplexType) child.getType())) {
						if (secondChild.getType() instanceof ComplexType || secondChild.getType().isList(secondChild.getProperties())) {
							isPlain = false;
							break;
						}
					}
					// if it is a plain child, we remove it from the content and display it in its own tableview
					if (isPlain && !(content.getType() instanceof BeanType)) {
						if (masked == null) {
							// mask as itself so we can throw away content
							masked = content.getType().newInstance();
							// actual masking proves too problematic in these circumstances?
							// e.g. be.nabu.eai.module.services.crud.Page is not an interface
							// we probably have to fix these issues anyway, but not now, we need a shallow copy only
							for (Element<?> element : TypeUtils.getAllChildren(content.getType())) {
								Object value = content.get(element.getName());
								if (value != null) {
									masked.set(element.getName(), value);
									contentSet++;
								}
							}
						}
						Object object = masked.get(child.getName());
						// only if we actually have an object
						if (object != null) {
							TableView table = new TableView();
							VBox.setMargin(table, new Insets(10, 0, 10, 0));
							table.getStyleClass().add("result-table");
							// add a table column for very field
							for (Element<?> secondChild : TypeUtils.getAllChildren((ComplexType) child.getType())) {
								TableColumn<ComplexContent, String> column = new TableColumn<ComplexContent, String>(NamingConvention.UPPER_TEXT.apply(secondChild.getName(), NamingConvention.LOWER_CAMEL_CASE));
								column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ComplexContent,String>, ObservableValue<String>>() {
									@Override
									public ObservableValue<String> call(CellDataFeatures<ComplexContent, String> param) {
										Object value = param.getValue() == null ? null : param.getValue().get(secondChild.getName());
										if (value != null) {
											if (secondChild.getType() instanceof be.nabu.libs.types.api.Marshallable) {
												value = ((be.nabu.libs.types.api.Marshallable) secondChild.getType()).marshal(value, secondChild.getProperties());
											}
										}
										return new SimpleStringProperty(value == null ? null : value.toString());
									}
								});
//								column.setCellFactory(new Callback<TableColumn<ComplexContent,String>, TableCell<ComplexContent,String>>() {
//									@Override
//									public TableCell<ComplexContent, String> call(TableColumn<ComplexContent, String> param) {
//										return new TableCell<ComplexContent, String>() {
//											@Override
//											protected void updateItem(String item, boolean empty) {
//												super.updateItem(item, empty);
//											}
//										};
//									}
//								});
								column.setCellFactory(TextFieldTableCell.forTableColumn());
								if (UUID.class.isAssignableFrom(((SimpleType) secondChild.getType()).getInstanceClass())) {
									// make it very small if it's a uuid
									column.setPrefWidth(20);
								}
								table.getColumns().add(column);
								column.setEditable(true);
							}
							CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(object.getClass());
							if (handler != null) {
								ObservableList<Object> list = FXCollections.observableArrayList();
								for (Object single : handler.getAsCollection(object)) {
									if (single != null && !(single instanceof ComplexContent)) {
										single = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(single);
									}
									list.add(single);
								}
								table.setItems(list);
							}
							vbox.getChildren().add(table);
							// allows you to copy value easily
							table.setEditable(true);
							table.prefWidthProperty().bind(vbox.widthProperty());
							
							// unset it so we don't display it twice
							masked.set(child.getName(), null);
							contentSet--;
						}
					}
				}
			}
			
			vbox.getChildren().add(contentTree);
			
			contentTree.prefWidthProperty().bind(vbox.widthProperty());
			// resize everything
			AnchorPane.setLeftAnchor(vbox, 0d);
			AnchorPane.setRightAnchor(vbox, 0d);
			AnchorPane.setTopAnchor(vbox, 0d);
			AnchorPane.setBottomAnchor(vbox, 0d);
			if (!ancPipeline.prefWidthProperty().isBound() && ancPipeline.getParent() != null) {
				ancPipeline.prefWidthProperty().bind(((Pane) ancPipeline.getParent()).widthProperty()); 
			}
			// only set content if we have any left
			if (masked == null || contentSet > 0) {
				contentTree.rootProperty().set(new ContentTreeItem(new RootElement(content.getType()), masked == null ? content : masked, null, false, null));
//				contentTree.getTreeCell(contentTree.rootProperty().get()).collapseAll();
				contentTree.getTreeCell(contentTree.rootProperty().get()).expandedProperty().set(true);
			}
			ancPipeline.getChildren().add(vbox);
			
			field.requestFocus();
		}
		else {
			Label label = new Label("No content available");
			label.setPadding(new Insets(10));
			ancPipeline.getChildren().add(label);
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
	
	public void showText(String text) {
		TextArea area = new TextArea();
		area.setEditable(false);
		area.setText(text);
		MainController.getInstance().getAncPipeline().getChildren().clear();
		MainController.getInstance().getAncPipeline().getChildren().add(area);
		AnchorPane.setBottomAnchor(area, 0d);
		AnchorPane.setLeftAnchor(area, 0d);
		AnchorPane.setRightAnchor(area, 0d);
		AnchorPane.setTopAnchor(area, 0d);
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
	
	public void close(Tab tab) {
		tabArtifacts.getTabs().remove(tab);
	}
	
	public void close(String id) {
		NodeContainer<?> container = getContainer(id);
		// close any tab that is a child of this because it will be out of sync
		if (container != null) {
			if (container.isChanged()) {
				Confirm.confirm(ConfirmType.QUESTION, "Changes pending in " + id, "Are you sure you want to discard the pending changes?", new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						container.close();
					}
				});
			}
			else {
				container.close();
			}
		}
//		closeAll(id);
	}
	
	public void closeAll(String idToClose) {
		// close any tab that is a child of this because it will be out of sync
		Iterator<NodeContainer<?>> iterator = managers.keySet().iterator();
		List<NodeContainer<?>> toClose = new ArrayList<NodeContainer<?>>();
		while (iterator.hasNext()) {
			NodeContainer<?> container = iterator.next();
			String id = container.getId();
			if (id.startsWith(idToClose + ".") || id.equals(idToClose)) {
				toClose.add(container);
			}
		}
		for (NodeContainer<?> container : toClose) {
			container.close();
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
						String id = object instanceof Artifact ? ((Artifact) object).getId() : null; 
						object = provider.serialize(object);
						// keep the string representation as an id if it is defined
						stringRepresentation = id == null ? object.toString() : id;
						format = TreeDragDrop.getDataFormat(provider.getDataType());
						foundDedicated = true;
						break;
					}
				}
			}

			if (object instanceof Artifact) {
				Entry entry = getInstance().getRepository().getEntry(((Artifact) object).getId());
				if (entry instanceof ResourceEntry) {
					try {
						clipboard.put(TreeDragDrop.getDataFormat("entry-binary"), EAIRepositoryUtils.zipSingleEntry((ResourceEntry) entry));
					}
					catch (Exception e) {
						getInstance().notify(e);
					}
				}
			}
			if (object instanceof ResourceEntry) {
				try {
					clipboard.put(TreeDragDrop.getDataFormat("entry-binary"), EAIRepositoryUtils.zipFullEntry((ResourceEntry) object));
				}
				catch (Exception e) {
					getInstance().notify(e);
				}
			}
			
			// if we have an element that represents an undefined complex type, allow for copy pasting it
			if (object instanceof Element || (object instanceof TreeItem && ((TreeItem<?>) object).itemProperty().get() instanceof Element)) {
				Element<?> element = object instanceof Element ? (Element<?>) object : ((TreeItem<Element<?>>) object).itemProperty().get();
				// we have generic copy/pasting of complex types
				if (!(element.getType() instanceof DefinedType) && element.getType() instanceof ComplexType) {
					for (ClipboardProvider provider : getInstance().getClipboardProviders()) {
						if (ComplexType.class.isAssignableFrom(provider.getClipboardClass())) {
							String serialized = provider.serialize(element.getType());
							stringRepresentation = object instanceof TreeItem ? getStringRepresentation((TreeItem<?>) object) : serialized;
							object = serialized;
							format = TreeDragDrop.getDataFormat(provider.getDataType());
							foundDedicated = true;
							break;
						}
					}
				}
			}
			
			if (object instanceof Image) {
				clipboard.putImage((Image) object);
				foundDedicated = true;
			}
//			if (object instanceof WritableImage) {
//				object = SwingFXUtils.fromFXImage((WritableImage) object, null);
//			}
//			if (object instanceof RenderedImage) {
//				ByteArrayOutputStream output = new ByteArrayOutputStream();
//				try {
//					ImageIO.write((RenderedImage) object, "png", output);
//				}
//				catch (IOException e) {
//					MainController.getInstance().notify(e);
//				}
//			}
			
			if (!foundDedicated) {
				if (object instanceof DefinedType) {
					format = TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_DEFINED);
					stringRepresentation = ((DefinedType) object).getId();
					object = stringRepresentation;
				}
				else if (object instanceof TreeItem && ((TreeItem<?>) object).itemProperty().get() instanceof Element) {
					format = TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_ELEMENT);
					stringRepresentation = getStringRepresentation((TreeItem<?>) object);
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
				else if (object instanceof Entry) {
					stringRepresentation = ((Entry) object).getId();
				}
			}
			if (format != null) {
				clipboard.put(format, object);
			}
			if (stringRepresentation == null && object instanceof String) {
				stringRepresentation = (String) object;
			}
			if (stringRepresentation != null) {
				clipboard.put(DataFormat.PLAIN_TEXT, stringRepresentation);
			}
		}
		return clipboard.size() == 0 ? null : clipboard;
	}

	private static String getStringRepresentation(TreeItem<?> object) {
		String stringRepresentation;
		stringRepresentation = TreeUtils.getPath(object);
		// remove the root if it is called pipeline as we always act on the root object
		if (stringRepresentation.startsWith("pipeline")) {
			stringRepresentation = stringRepresentation.replaceFirst("^[^/]+/", "");
		}
		return stringRepresentation;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void serializeElement(ClipboardContent clipboard, Object object) {
		try {
			Map<String, Object> element = new HashMap<String, Object>();
			Value<CollectionHandlerProvider> property = ((Element<?>) object).getProperty(CollectionHandlerProviderProperty.getInstance());
			List<Value<?>> values = new ArrayList<Value<?>>(Arrays.asList(((Element<?>) object).getProperties()));
			if (property != null && property.getValue() instanceof StringMapCollectionHandlerProvider) {
				element.put("$type", "java.util.Map");
				// remove properties that belong to the type
				values.removeAll(Arrays.asList(((Element<?>) object).getType().getProperties()));
			}
			else if (((Element<?>) object).getType() instanceof DefinedType) {
				DefinedType definedType = (DefinedType) ((Element<?>) object).getType();
				// if it is not a globally accessible type, keep going up until you find one
				while (DefinedTypeResolverFactory.getInstance().getResolver().resolve(definedType.getId()) == null && definedType.getSuperType() instanceof DefinedType) {
					definedType = (DefinedType) definedType.getSuperType();
				}
				element.put("$type", definedType.getId());
				// remove properties from type, we are using defined types
				values.removeAll(Arrays.asList(definedType.getProperties()));
			}
			for (Value<?> value : values) {
				if (value.getProperty().equals(CollectionHandlerProviderProperty.getInstance())) {
					if (value instanceof StringMapCollectionHandlerProvider) {
						element.put(value.getProperty().getName(), "stringMap");
					}
					continue;
				}
				// don't want maxoccurs for a map
				if (value.getProperty().equals(MaxOccursProperty.getInstance()) && "java.util.Map".equals(element.get("$type"))) {
					continue;
				}
				// don't serialize the super type, especially for the ones we unwound
				else if (value.getProperty().equals(SuperTypeProperty.getInstance())) {
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

	private CollaborationClient collaborationClient;

	private VBox vbxDeveloperLog;

	private VBox vbxServerLog;

	private VBox vbxNotifications;
	
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
		NodeContainer<?> current = getCurrent();
		return current == null ? null : managers.get(current);
	}

	public boolean isKeyActive(KeyCode code) {
		return activeKeys.contains(code);
	}
	
	public StringProperty remoteServerMessageProperty() {
		return remoteServerMessage;
	}

	public AsynchronousRemoteServer getAsynchronousRemoteServer() {
		return asynchronousRemoteServer;
	}

	public ServerProfile getProfile() {
		return profile;
	}

	public void setProfile(ServerProfile profile) {
		this.profile = profile;
	}
	
	public BooleanProperty connectedProperty() {
		return connected;
	}

	public ObservableList<User> getUsers() {
		return users;
	}

	public void setUsers(ObservableList<User> users) {
		this.users = users;
	}

	public CollaborationClient getCollaborationClient() {
		return collaborationClient;
	}
	
	public StringProperty lock(String name) {
		if (!locks.containsKey(name)) {
			synchronized(locks) {
				if (!locks.containsKey(name)) {
					locks.put(name, new SimpleStringProperty());
				}
			}
		}
		return locks.get(name);
	}
	
	public List<String> getOwnLocks() {
		List<String> locks = new ArrayList<String>();
		for (String id : this.locks.keySet()) {
			if ("$self".equals(this.locks.get(id).get())) {
				locks.add(id);
			}
		}
		return locks;
	}
	
	public BooleanProperty hasLock(String name) {
		if (!isLocked.containsKey(name)) {
			synchronized(isLocked) {
				if (!isLocked.containsKey(name)) {
					BooleanProperty bool = new SimpleBooleanProperty();
					StringProperty lock = lock(name);
					bool.set("$self".equals(lock.get()));
					lock.addListener(new ChangeListener<String>() {
						@Override
						public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
							bool.set("$self".equals(arg2) && connected.get());
						}
					});
					isLocked.put(name, bool);
				}
			}
		}
		return isLocked.get(name);
	}
	
	public BooleanProperty hasLock() {
		NodeContainer<?> current = getCurrent();
		if (current != null) {
			ArtifactGUIInstance instance = managers.get(current);
			if (instance != null) {
				return hasLock(instance.getId());
			}
		}
		return new SimpleBooleanProperty(false);
	}
	
	public void tryLock(String lockId, ReadOnlyBooleanProperty wantLock) {
		StringProperty lock = lock(lockId);
		if (lock.get() == null) {
			MainController.getInstance().getCollaborationClient().lock(lockId, "Opened");
		}
		else if (wantLock != null) {
			final ChangeListener<String> changeListener = new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> arg0, String arg1, String arg2) {
					if (arg2 == null) {
						if (wantLock.get()) {
							MainController.getInstance().getCollaborationClient().lock(lockId, "Locked");
						}
						lock.removeListener(this);
					}
				}
			};
			lock.addListener(changeListener);
		}
	}
	
	public void unlockFor(String name) {
		synchronized(locks) {
			for (StringProperty lock : locks.values()) {
				if (name.equals(lock.get())) {
					lock.set(null);
				}
			}
		}
	}
	
	private static void grep(Entry entry, String toFind, boolean regex, Map<Entry, List<Resource>> map, boolean recursive) throws IOException {
		if (entry instanceof ResourceEntry) {
			List<Resource> resources = new ArrayList<Resource>();
			ResourceContainer<?> container = ((ResourceEntry) entry).getContainer();
			// search the root directory
			resources.addAll(grep(container, toFind, regex, false));
			ResourceContainer<?> publicFolder = (ResourceContainer<?>) container.getChild(EAIResourceRepository.PUBLIC);
			ResourceContainer<?> privateFolder = (ResourceContainer<?>) container.getChild(EAIResourceRepository.PRIVATE);
			ResourceContainer<?> protectedFolder = (ResourceContainer<?>) container.getChild(EAIResourceRepository.PROTECTED);
			
			if (publicFolder != null) {
				resources.addAll(grep(publicFolder, toFind, regex, true));	
			}
			if (privateFolder != null) {
				resources.addAll(grep(privateFolder, toFind, regex, true));	
			}
			if (protectedFolder != null) {
				resources.addAll(grep(protectedFolder, toFind, regex, true));	
			}
			if (!resources.isEmpty()) {
				map.put(entry, resources);
			}
			
			if (recursive) {
				for (Entry child : entry) {
					grep(child, toFind, regex, map, recursive);
				}
			}
		}
	}
	
	public static List<Resource> grep(ResourceContainer<?> container, String toFind, boolean regex, boolean recursive) throws IOException {
		List<Resource> resources = new ArrayList<Resource>();
		for (Resource child : container) {
			if (child instanceof ReadableResource) {
				if (grep((ReadableResource) child, toFind, regex)) {
					resources.add(child);
				}
			}
			if (child instanceof ResourceContainer && recursive) {
				resources.addAll(grep((ResourceContainer<?>) child, toFind, regex, recursive));
			}
		}
		return resources;
	}
	
	public static boolean grep(ReadableResource resource, String toFind, boolean regex) throws IOException {
		ReadableContainer<ByteBuffer> readable = ((ReadableResource) resource).getReadable();
		try {
			byte[] bytes = IOUtils.toBytes(readable);
			String string = new String(bytes, "UTF-8");
			if (regex && string.matches("(?i).*" + toFind + ".*")) {
				return true;
			}
			else if (!regex && string.toLowerCase().indexOf(toFind.toLowerCase()) >= 0) {
				return true;
			}
		}
		catch (Exception e) {
			// suppress
		}
		finally {
			readable.close();
		}
		return false;
	}
	
	private static List<Entry> flattenResourceEntries(Entry entry) {
		List<Entry> entries = new ArrayList<Entry>();
		for (Entry child : entry) {
			if (child instanceof ResourceEntry) {
				entries.add(child);
			}
			entries.addAll(flattenResourceEntries(child));
		}
		return entries;
	}

	public Reconnector getReconnector() {
		return reconnector;
	}

	public void setReconnector(Reconnector reconnector) {
		this.reconnector = reconnector;
	}

	public NotificationHandler getNotificationHandler() {
		return notificationHandler;
	}

	public TabPane getTabBrowsers() {
		return tabBrowsers;
	}
	
}
