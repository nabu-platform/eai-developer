package be.nabu.eai.developer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import be.nabu.eai.developer.api.Component;
import be.nabu.eai.developer.api.Controller;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.components.SingleRightClickMenu;
import be.nabu.eai.developer.handlers.StructureGUIManager;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.libs.resources.ResourceFactory;
import be.nabu.libs.resources.api.ManageableContainer;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MainController implements Initializable, Controller {

	@FXML
	private AnchorPane ancLeft, ancMiddle, ancRight;
	
	@FXML
	private TabPane tabArtifacts;
	
	private Map<String, Component<MainController, ?>> components = new HashMap<String, Component<MainController, ?>>();
	
	private EAIResourceRepository repository;

	private Stage stage;
	
	private Tree<RepositoryEntry> tree;
	
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
		
		tree = new Tree<RepositoryEntry>(new Marshallable<RepositoryEntry>() {
			@Override
			public String marshal(RepositoryEntry arg0) {
				return arg0.getName();
			}
		});
		tree.setId("repository");
		tree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		ancLeft.getChildren().add(tree);
		tree.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				List<TreeCell<RepositoryEntry>> selected = tree.getSelectionModel().getSelectedItems();
				if (event.getButton().equals(MouseButton.SECONDARY)) {
					System.out.println(">>>>> " + SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(Class.class));
					// if you have selected one, show the contextual menu for that type
					if (selected.size() == 1) {
						ContextMenu menu = new SingleRightClickMenu().buildMenu(MainController.this, selected.get(0).getItem());
						tree.setContextMenu(menu);
						tree.getContextMenu().show(stage);
					}
					// otherwise, show the contextual menu for multiple operations
					else {
						
					}
				}
			}
		});
		// create the browser
		components.put(tree.getId(), new RepositoryBrowser().initialize(this, tree));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <C extends Controller, T extends Control> Component<C, T> getComponent(String id) {
		return (Component<C, T>) components.get(id);
	}
	
	public RepositoryBrowser getRepositoryBrowser() {
		return (RepositoryBrowser) components.get("repository");
	}
	
	public Tab newTab(String id) {
		Tab tab = new Tab(id);
		tabArtifacts.getTabs().add(tab);
		return tab;
	}

	public EAIResourceRepository getRepository() {
		return repository;
	}
	
	@SuppressWarnings("rawtypes")
	public List<ArtifactGUIManager> getNodeHandlers() {
		return Arrays.asList(new ArtifactGUIManager [] { new StructureGUIManager() });
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
	
	public FXMLLoader load(String name, String title) throws IOException {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(Thread.currentThread().getContextClassLoader().getResource(name));
		loader.load();
		Controller controller = loader.getController();
		Stage stage = new Stage();
		controller.setStage(stage);
		Parent root = loader.getRoot();
		stage.setScene(new Scene(root));
		stage.setTitle(title);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(getStage());
		stage.show();
		return loader;
	}
}
