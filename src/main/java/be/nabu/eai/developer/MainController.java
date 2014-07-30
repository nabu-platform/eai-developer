package be.nabu.eai.developer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.managers.JDBCPoolGUIManager;
import be.nabu.eai.developer.managers.JDBCServiceGUIManager;
import be.nabu.eai.developer.managers.ServiceGUIManager;
import be.nabu.eai.developer.managers.StructureGUIManager;
import be.nabu.eai.developer.managers.TypeGUIManager;
import be.nabu.eai.developer.managers.VMServiceGUIManager;
import be.nabu.eai.developer.managers.WSDLClientGUIManager;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.converter.api.Converter;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.DefinedTypeResolver;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.structure.SuperTypeProperty;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import be.nabu.libs.validator.api.Validator;

/**
 * TODO: apparantly the panes are not scrollable by default, need to add it?
 * http://docs.oracle.com/javafx/2/ui_controls/scrollbar.htm
 * Wtf happened to setScrollable(true) ?
 */
public class MainController implements Initializable, Controller {

	@FXML
	private AnchorPane ancLeft, ancMiddle, ancProperties, ancPipeline;
	
	@FXML
	private TabPane tabArtifacts;
	
	@FXML
	private ListView<String> lstNotifications;
	
	@FXML
	private MenuItem mniClose, mniSave;
	
	private DefinedTypeResolver typeResolver = DefinedTypeResolverFactory.getInstance().getResolver();
	
	private Converter converter = ConverterFactory.getInstance().getConverter();
	
	private List<ArtifactGUIInstance> artifacts = new ArrayList<ArtifactGUIInstance>();
	
	private Map<String, Component<MainController, ?>> components = new HashMap<String, Component<MainController, ?>>();
	
	private EAIResourceRepository repository;

	private Stage stage;
	
	private Tree<Entry> tree;
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// create repository
		try {
			repository = new EAIResourceRepository((ManageableContainer<?>) ResourceFactory.getInstance().resolve(new URI("file:/home/alex/repository"), null));
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
		repository.load();
		
		tree = new Tree<Entry>(new Marshallable<Entry>() {
			@Override
			public String marshal(Entry arg0) {
				return arg0.getName();
			}
		});
		tree.setId("repository");
		ancLeft.getChildren().add(tree);
		// create the browser
		components.put(tree.getId(), new RepositoryBrowser().initialize(this, tree));
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
	
	public void register(ArtifactGUIInstance instance) {
		artifacts.add(instance);
	}
	
	public void save(String id) throws IOException {
		for (ArtifactGUIInstance instance : artifacts) {
			if (instance.isReady() && instance.getId().equals(id)) {
				if (instance.isEditable()) {
					instance.save();
				}
			}
		}
	}
	
	public Tab newTab(final String id) {
		final Tab tab = new Tab(id);
		tab.setId(id);
		tabArtifacts.getTabs().add(tab);
		tabArtifacts.selectionModelProperty().get().select(tab);
		tab.contentProperty().addListener(new ChangeListener<javafx.scene.Node>() {
			@Override
			public void changed(ObservableValue<? extends javafx.scene.Node> arg0, javafx.scene.Node arg1, javafx.scene.Node arg2) {
				arg2.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.S && event.isControlDown()) {
							try {
								save(id);
							}
							catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
						else if (event.getCode() == KeyCode.W && event.isControlDown()) {
							tabArtifacts.getTabs().remove(tab);
						}
					}
				});
			}
		});
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
			new WSDLClientGUIManager()
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
	public void notify(ValidationMessage... messages) {
		lstNotifications.getItems().clear();
		for (ValidationMessage message : messages) {
			lstNotifications.getItems().add(message.getMessage());
		}
	}
	
	public void showProperties(final PropertyUpdater updater) {
		showProperties(updater, ancProperties);
	}
	
	public void showProperties(final PropertyUpdater updater, final Pane target) {
		GridPane grid = new GridPane();
		int row = 0;
		for (final Property<?> property : updater.getSupportedProperties()) {
			Label name = new Label(property.getName());
			grid.add(name, 0, row);
			String superTypeName = null;
			boolean allowSuperType = true;
			if (property.equals(new SuperTypeProperty())) {
				Type superType = ValueUtils.getValue(new SuperTypeProperty(), updater.getValues());
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
			
			final String currentValue = property.equals(new SuperTypeProperty())
				? superTypeName
				: (originalValue instanceof String ? (String) originalValue : converter.convert(originalValue, String.class));

			// if we can't convert from a string to the property value, we can't show it
			if (updater.canUpdate(property) && ((property.equals(new SuperTypeProperty()) && allowSuperType) || !property.equals(new SuperTypeProperty()))) {
				final TextField textField = new TextField(currentValue);
				textField.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
					@Override
					public void handle(KeyEvent event) {
						if (event.getCode() == KeyCode.ENTER) {
							if (!parseAndUpdate(updater, property, textField.getText())) {
								textField.setText(currentValue);
							}
							else {
								// refresh basically, otherwise the final currentValue will keep pointing at the old one
								showProperties(updater, target);
							}
							event.consume();
						}
					}
				});
				grid.add(textField, 1, row);
			}
			else if (currentValue != null) {
				Label value = new Label(currentValue);
				grid.add(value, 1, row);
			}
			row++;
		}
		target.getChildren().clear();
		target.getChildren().add(grid);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private boolean parseAndUpdate(PropertyUpdater updater, Property<?> property, String value) {
		try {
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
					return false;
				}
			}
			updater.updateProperty(property, parsed);
		}
		catch (RuntimeException e) {
			e.printStackTrace();
			notify(new ValidationMessage(Severity.ERROR, "Could not parse the value '" + value + "'"));
		}
		return true;
	}
	
	public static interface PropertyUpdater {
		public Set<Property<?>> getSupportedProperties();
		public Value<?> [] getValues();
		public boolean canUpdate(Property<?> property);
		public List<ValidationMessage> updateProperty(Property<?> property, Object value);
	}
	
	public void showContent(ComplexContent content) {
		ancPipeline.getChildren().clear();
		ancPipeline.getChildren().add(buildContent(content));
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Pane buildContent(ComplexContent content) {
		VBox vbox = new VBox();
		if (content == null) {
			return vbox;
		}
		for (Element<?> child : content.getType()) {
			Object value = content.get(child.getName());
			if (value != null) {
				if (child.getType().isList(child.getProperties())) {
					CollectionHandlerProvider collectionHandler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
					for (Object index : collectionHandler.getIndexes(value)) {
						HBox hbox = new HBox();
						hbox.getChildren().add(loadGraphic(StructureGUIManager.getIcon(child.getType(), child.getProperties())));
						Label label = new Label(child.getName() + "[" + index + "]");
						label.getStyleClass().add("key");
						label.setPrefWidth(100);
						hbox.getChildren().add(label);
						Object single = collectionHandler.get(value, index);
						if (child.getType() instanceof ComplexType) {
							hbox.getChildren().add(buildContent(single instanceof ComplexContent ? (ComplexContent) single : new BeanInstance(single)));		
						}
						else if (child.getType() instanceof be.nabu.libs.types.api.Marshallable) {
							hbox.getChildren().add(new Label(((be.nabu.libs.types.api.Marshallable) child.getType()).marshal(single)));		
						}
						else {
							hbox.getChildren().add(new Label(child.getType().toString()));
						}
						vbox.getChildren().add(hbox);
					}
				}
				else {
					HBox hbox = new HBox();
					hbox.getChildren().add(loadGraphic(StructureGUIManager.getIcon(child.getType(), child.getProperties())));
					Label label = new Label(child.getName());
					label.getStyleClass().add("key");
					label.setPrefWidth(100);
					hbox.getChildren().add(label);
					if (child.getType() instanceof ComplexType) {
						hbox.getChildren().add(buildContent(value instanceof ComplexContent ? (ComplexContent) value : new BeanInstance(value)));
					}
					else if (child.getType() instanceof be.nabu.libs.types.api.Marshallable) {
						hbox.getChildren().add(new TextField(((be.nabu.libs.types.api.Marshallable) child.getType()).marshal(value)));
					}
					else {
						hbox.getChildren().add(new Label(child.getType().toString()));
					}
					vbox.getChildren().add(hbox);
				}
			}
		}
		return vbox;
	}
}
