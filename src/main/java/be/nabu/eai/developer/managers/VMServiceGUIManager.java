package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.developer.controllers.VMServiceController;
import be.nabu.eai.developer.managers.util.StepTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.managers.VMServiceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.services.vm.For;
import be.nabu.libs.services.vm.Map;
import be.nabu.libs.services.vm.Pipeline;
import be.nabu.libs.services.vm.Sequence;
import be.nabu.libs.services.vm.SimpleVMServiceDefinition;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.services.vm.StepGroup;
import be.nabu.libs.services.vm.Switch;
import be.nabu.libs.services.vm.Throw;
import be.nabu.libs.services.vm.VMService;

public class VMServiceGUIManager implements ArtifactGUIManager<VMService> {

	@Override
	public ArtifactManager<VMService> getArtifactManager() {
		return new VMServiceManager();
	}

	@Override
	public String getArtifactName() {
		return "Service";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("step/sequence.gif");
	}

	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<RepositoryEntry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create Service", true);
		final NameOnlyCreateController createController = loader.getController();
		final VMServiceGUIInstance instance = new VMServiceGUIInstance();
		createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				try {
					String name = createController.getTxtName().getText();
					RepositoryEntry entry = target.itemProperty().get().createNode(name, getArtifactManager());
					VMService service = new SimpleVMServiceDefinition(new Pipeline());
					getArtifactManager().save(entry, service);
					controller.getRepositoryBrowser().refresh();
					createController.close();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					display(controller, pane, entry);
					instance.setEntry(entry);
					instance.setService(service);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return instance;

	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<RepositoryEntry> target) throws IOException, ParseException {
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		return new VMServiceGUIInstance(target.itemProperty().get(), display(controller, pane, target.itemProperty().get()));
	}
	
	private VMService display(final MainController controller, Pane pane, RepositoryEntry entry) throws IOException, ParseException {
		
		FXMLLoader loader = controller.load("vmservice.fxml", "Service", false);
		final VMServiceController serviceController = loader.getController();
		
		VMService service = (VMService) entry.getNode().getArtifact();
		
		// the top part is the service, the bottom is a tabpane with input/output & mapping
		SplitPane splitPane = new SplitPane();
		
		AnchorPane top = new AnchorPane();
		splitPane.getItems().add(top);
		final Tree<Step> serviceTree = new Tree<Step>(new Marshallable<Step>() {
			@Override
			public String marshal(Step step) {
				return step.getClass().getSimpleName();
			}
		});
		serviceTree.rootProperty().set(new StepTreeItem(service.getRoot(), null, false));
		
		Button newFor = new Button();
		newFor.setGraphic(MainController.loadGraphic(getIcon(For.class)));
		newFor.addEventHandler(ActionEvent.ACTION, new ServiceAddHandler(serviceTree, For.class));
		serviceController.getHbxButtons().getChildren().add(newFor);
		
		Button newSequence = new Button();
		newSequence.setGraphic(MainController.loadGraphic(getIcon(Sequence.class)));
		newSequence.addEventHandler(ActionEvent.ACTION, new ServiceAddHandler(serviceTree, Sequence.class));
		serviceController.getHbxButtons().getChildren().add(newSequence);
		
		Button newMap = new Button();
		newMap.setGraphic(MainController.loadGraphic(getIcon(Map.class)));
		newMap.addEventHandler(ActionEvent.ACTION, new ServiceAddHandler(serviceTree, Map.class));
		serviceController.getHbxButtons().getChildren().add(newMap);
		
		Button newSwitch = new Button();
		newSwitch.setGraphic(MainController.loadGraphic(getIcon(Switch.class)));
		newSwitch.addEventHandler(ActionEvent.ACTION, new ServiceAddHandler(serviceTree, Switch.class));
		serviceController.getHbxButtons().getChildren().add(newSwitch);
		
		Button newThrow = new Button();
		newThrow.setGraphic(MainController.loadGraphic(getIcon(Throw.class)));
		newThrow.addEventHandler(ActionEvent.ACTION, new ServiceAddHandler(serviceTree, Throw.class));
		serviceController.getHbxButtons().getChildren().add(newThrow);
		
		serviceController.getVbxService().getChildren().add(serviceTree);
		
		Parent parent = loader.getRoot();
		pane.getChildren().add(parent);
		AnchorPane.setTopAnchor(parent, 0d);
		AnchorPane.setBottomAnchor(parent, 0d);
		AnchorPane.setLeftAnchor(parent, 0d);
		AnchorPane.setRightAnchor(parent, 0d);
		
		return service;
	}
	
	private class ServiceAddHandler implements EventHandler<Event> {
		private Tree<Step> tree;
		private Class<? extends Step> step;
		
		public ServiceAddHandler(Tree<Step> tree, Class<? extends Step> step) {
			this.tree = tree;
			this.step = step;
		}

		@Override
		public void handle(Event arg0) {
			TreeCell<Step> selectedItem = tree.getSelectionModel().getSelectedItem();
			if (selectedItem != null) {
				// add an element in it
				if (selectedItem.getItem().itemProperty().get() instanceof StepGroup) {
					try {
						((StepGroup) selectedItem.getItem().itemProperty().get()).getChildren().add(step.newInstance());
					}
					catch (InstantiationException e) {
						throw new RuntimeException(e);
					}
					catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				((StepTreeItem) selectedItem.getItem()).refresh();
				// add an element next to it
				// TODO
			}
		}
	}
	
	public static String getIcon(Class<? extends Step> clazz) {
		return "step/" + clazz.getSimpleName().toLowerCase() + ".gif";
	}
	public static String getIcon(Step step) {
		return getIcon(step.getClass());
	}

}
