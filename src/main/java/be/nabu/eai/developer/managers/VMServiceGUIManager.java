package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;

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
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.developer.controllers.VMServiceController;
import be.nabu.eai.developer.managers.util.ElementMarshallable;
import be.nabu.eai.developer.managers.util.ElementSelectionListener;
import be.nabu.eai.developer.managers.util.ElementTreeItem;
import be.nabu.eai.developer.managers.util.Mapping;
import be.nabu.eai.developer.managers.util.RootElementWithPush;
import be.nabu.eai.developer.managers.util.StepTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.managers.VMServiceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.line.Line;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDragListener;
import be.nabu.jfx.control.tree.drag.TreeDropListener;
import be.nabu.libs.services.vm.For;
import be.nabu.libs.services.vm.LimitedStepGroup;
import be.nabu.libs.services.vm.Link;
import be.nabu.libs.services.vm.Map;
import be.nabu.libs.services.vm.Pipeline;
import be.nabu.libs.services.vm.Sequence;
import be.nabu.libs.services.vm.SimpleVMServiceDefinition;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.services.vm.StepGroup;
import be.nabu.libs.services.vm.Switch;
import be.nabu.libs.services.vm.Throw;
import be.nabu.libs.services.vm.VMService;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.structure.Structure;

public class VMServiceGUIManager implements ArtifactGUIManager<VMService> {

	@Override
	public ArtifactManager<VMService> getArtifactManager() {
		return new VMServiceManager();
	}
	
	private java.util.Map<Link, Mapping> mappings = new LinkedHashMap<Link, Mapping>();

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
					VMService service = new SimpleVMServiceDefinition(new Pipeline(new Structure(), new Structure()));
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
		
		final VMService service = (VMService) entry.getNode().getArtifact();
		
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

		serviceController.getPanService().getChildren().add(serviceTree);

		Parent parent = loader.getRoot();
		pane.getChildren().add(parent);
		// make sure it is full size
		AnchorPane.setTopAnchor(parent, 0d);
		AnchorPane.setBottomAnchor(parent, 0d);
		AnchorPane.setLeftAnchor(parent, 0d);
		AnchorPane.setRightAnchor(parent, 0d);

		TreeDragDrop.makeDraggable(serviceTree, new TreeDragListener<Step>() {
			@Override
			public boolean canDrag(TreeCell<Step> arg0) {
				return arg0.getItem().getParent() != null;
			}
			@Override
			public void drag(TreeCell<Step> arg0) {
				// do nothing
			}
			@Override
			public String getDataType(TreeCell<Step> arg0) {
				return "vmservice-step";
			}
			@Override
			public TransferMode getTransferMode() {
				return TransferMode.MOVE;
			}
			@Override
			public void stopDrag(TreeCell<Step> arg0, boolean arg1) {
				// do nothing
			}
		});
		TreeDragDrop.makeDroppable(serviceTree, new TreeDropListener<Step>() {
			@Override
			public boolean canDrop(String dataType, TreeCell<Step> target, TreeCell<?> dragged, TransferMode transferMode) {
				if (!dataType.equals("vmservice-step")) {
					return false;
				}
				else if (target.getItem().itemProperty().get() instanceof StepGroup) {
					// not to itself
					if (target.getItem().equals(dragged.getParent())) {
						return false;
					}
					// if it's a limited group, check the type
					if (target.getItem().itemProperty().get() instanceof LimitedStepGroup) {
						return ((LimitedStepGroup) target.getItem().itemProperty().get()).getAllowedSteps().contains(dragged.getItem().itemProperty().get().getClass());
					}
					else {
						return true;
					}
				}
				return false;
			}
			@SuppressWarnings("unchecked")
			@Override
			public void drop(String dataType, TreeCell<Step> target, TreeCell<?> dragged, TransferMode transferMode) {
				StepGroup newParent = (StepGroup) target.getItem().itemProperty().get();
				TreeCell<Step> draggedElement = (TreeCell<Step>) dragged;
				StepGroup originalParent = (StepGroup) draggedElement.getItem().getParent().itemProperty().get();
				if (originalParent.getChildren().remove(draggedElement.getItem().itemProperty().get())) {
					newParent.getChildren().add(draggedElement.getItem().itemProperty().get());
				}
				// refresh both
				((StepTreeItem) target.getItem()).refresh();
				((StepTreeItem) dragged.getParent().getItem()).refresh();	
			}
		});
		
		// show the input & output
		StructureGUIManager structureManager = new StructureGUIManager();
		VBox input = new VBox();
		structureManager.display(controller, input, new RootElementWithPush(
			(Structure) service.getPipeline().get(Pipeline.INPUT).getType(), 
			false,
			service.getPipeline().get(Pipeline.INPUT).getProperties()
		), true);
		serviceController.getPanInput().getChildren().add(input);
		
		VBox output = new VBox();
		structureManager.display(controller, output, new RootElementWithPush(
			(Structure) service.getPipeline().get(Pipeline.OUTPUT).getType(), 
			false,
			service.getPipeline().get(Pipeline.OUTPUT).getProperties()
		), true);
		serviceController.getPanOutput().getChildren().add(output);
		
		// the map step
		// the left part will be a custom tree because we don't need any of the editability of the structure manager
		Tree<Element<?>> leftTree = new Tree<Element<?>>(new ElementMarshallable());
		leftTree.rootProperty().set(new ElementTreeItem(new RootElementWithPush(
			service.getPipeline(), 
			false,
			service.getPipeline().getProperties()
		), null, false));
		// show properties if selected
		leftTree.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		
		// add first to get parents right
		serviceController.getPanLeft().getChildren().add(leftTree);
		
		TreeDragDrop.makeDraggable(leftTree, new TreeDragListener<Element<?>>() {
			private Line line;
			@Override
			public boolean canDrag(TreeCell<Element<?>> arg0) {
				return true;
			}
			@Override
			public void drag(TreeCell<Element<?>> dragged) {
				line = new Line();
				line.startXProperty().bind(dragged.rightAnchorXProperty());
				line.startYProperty().bind(dragged.rightAnchorYProperty());
				// move it by one pixel, otherwise it will keep triggering events on the line instead of what is underneath it
				line.endXProperty().bind(TreeDragDrop.getMouseLocation(dragged.getTree()).xProperty().subtract(1).subtract(serviceController.getPanMap().localToSceneTransformProperty().get().getTx()));
				line.endYProperty().bind(TreeDragDrop.getMouseLocation(dragged.getTree()).yProperty().subtract(dragged.getTree().localToSceneTransformProperty().get().getTy()));
				serviceController.getPanMap().getChildren().add(line);
			}
			@Override
			public String getDataType(TreeCell<Element<?>> arg0) {
				return "type";
			}
			@Override
			public TransferMode getTransferMode() {
				return TransferMode.LINK;
			}
			@Override
			public void stopDrag(TreeCell<Element<?>> arg0, boolean arg1) {
				((Pane) line.getParent()).getChildren().remove(line);
				line = null;
			}
		});
		
		VBox right = new VBox();
		Tree<Element<?>> rightTree = structureManager.display(controller, right, new RootElementWithPush(
			service.getPipeline(), 
			false,
			service.getPipeline().getProperties()
		), true);
		serviceController.getPanRight().getChildren().add(right);
		
		TreeDragDrop.makeDroppable(rightTree, new TreeDropListener<Element<?>>() {
			@SuppressWarnings("unchecked")
			@Override
			public boolean canDrop(String dataType, TreeCell<Element<?>> target, TreeCell<?> dragged, TransferMode transferMode) {
				// this listener is only interested in link attempts (drawing a line)
				if (transferMode != TransferMode.LINK) {
					return false;
				}
				else if (!dataType.equals("type")) {
					return false;
				}
				else {
					TreeCell<Element<?>> draggedElement = (TreeCell<Element<?>>) dragged;
					return service.isMappable(draggedElement.getItem().itemProperty().get(), target.getItem().itemProperty().get());
				}
			}
			@SuppressWarnings("unchecked")
			@Override
			public void drop(String arg0, TreeCell<Element<?>> target, TreeCell<?> dragged, TransferMode transferMode) {
				boolean alreadyMapped = false;
				for (Mapping mapping : mappings.values()) {
					if (mapping.getFrom().equals(dragged) && mapping.getTo().equals(target)) {
						alreadyMapped = true;
						break;
					}
				}
				if (!alreadyMapped) {
					Mapping mapping = new Mapping(serviceController.getPanMap(), (TreeCell<Element<?>>) dragged, target);
					System.out.println("Linking from " + TreeDragDrop.getPath(dragged.getItem()) + " to " + TreeDragDrop.getPath(target.getItem()));
					// TODO: need to take into account indexes!
					Link link = new Link(TreeDragDrop.getPath(dragged.getItem()), TreeDragDrop.getPath(target.getItem()));
					mappings.put(link, mapping);
				}
			}
		});
		
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
					if (!(selectedItem.getItem().itemProperty().get() instanceof LimitedStepGroup)
							|| ((LimitedStepGroup) selectedItem.getItem().itemProperty().get()).getAllowedSteps().contains(step)) {
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
