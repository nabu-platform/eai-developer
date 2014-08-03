package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javafx.scene.input.KeyCode;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.developer.controllers.VMServiceController;
import be.nabu.eai.developer.managers.util.DropLinkListener;
import be.nabu.eai.developer.managers.util.ElementLineConnectListener;
import be.nabu.eai.developer.managers.util.ElementMarshallable;
import be.nabu.eai.developer.managers.util.ElementSelectionListener;
import be.nabu.eai.developer.managers.util.ElementTreeItem;
import be.nabu.eai.developer.managers.util.FixedValue;
import be.nabu.eai.developer.managers.util.InvokeWrapper;
import be.nabu.eai.developer.managers.util.LinkPropertyUpdater;
import be.nabu.eai.developer.managers.util.Mapping;
import be.nabu.eai.developer.managers.util.MovablePane;
import be.nabu.eai.developer.managers.util.RemoveLinkListener;
import be.nabu.eai.developer.managers.util.RootElementWithPush;
import be.nabu.eai.developer.managers.util.StepPropertyProvider;
import be.nabu.eai.developer.managers.util.StepTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.managers.VMServiceManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDragListener;
import be.nabu.jfx.control.tree.drag.TreeDropListener;
import be.nabu.libs.services.SimpleServiceRuntime;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.vm.Catch;
import be.nabu.libs.services.vm.Finally;
import be.nabu.libs.services.vm.For;
import be.nabu.libs.services.vm.Invoke;
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
import be.nabu.libs.types.ParsedPath;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class VMServiceGUIManager implements ArtifactGUIManager<VMService> {

	@Override
	public ArtifactManager<VMService> getArtifactManager() {
		return new VMServiceManager();
	}
	
	private java.util.Map<Class<? extends Step>, Button> addButtons = new HashMap<Class<? extends Step>, Button>();
	private java.util.Map<Link, Mapping> mappings = new LinkedHashMap<Link, Mapping>();
	private java.util.Map<Link, FixedValue> fixedValues = new LinkedHashMap<Link, FixedValue>();
	private Tree<Element<?>> inputTree;
	private Tree<Element<?>> outputTree;
	private Tree<Element<?>> leftTree;
	private Tree<Element<?>> rightTree;
	private java.util.Map<String, InvokeWrapper> invokeWrappers;

	@Override
	public String getArtifactName() {
		return "Service";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("vmservice.png");
	}

	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<Entry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create Service", true);
		final NameOnlyCreateController createController = loader.getController();
		final VMServiceGUIInstance instance = new VMServiceGUIInstance();
		createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				try {
					String name = createController.getTxtName().getText();
					RepositoryEntry entry = ((RepositoryEntry) target.itemProperty().get()).createNode(name, getArtifactManager());
					VMService service = new SimpleVMServiceDefinition(new Pipeline(new Structure(), new Structure()));
					getArtifactManager().save(entry, service);
					controller.getRepositoryBrowser().refresh();
					createController.close();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					ServiceGUIManager.makeRunnable(tab, service, controller);
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
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		ServiceGUIManager.makeRunnable(tab, (VMService) target.itemProperty().get().getNode().getArtifact(), controller);
		return new VMServiceGUIInstance(target.itemProperty().get(), display(controller, pane, target.itemProperty().get()));
	}
	
	public static TreeItem<Element<?>> find(TreeItem<Element<?>> parent, ParsedPath path) {
		for (TreeItem<Element<?>> child : parent.getChildren()) {
			if (child.getName().equals(path.getName())) {
				return path.getChildPath() == null ? child : find(child, path.getChildPath());
			}
		}
		return null;
	}
	
	private VMService display(final MainController controller, Pane pane, Entry entry) throws IOException, ParseException {
		FXMLLoader loader = controller.load("vmservice.fxml", "Service", false);
		final VMServiceController serviceController = loader.getController();
		
		final VMService service = (VMService) entry.getNode().getArtifact();
		
		// the top part is the service, the bottom is a tabpane with input/output & mapping
		SplitPane splitPane = new SplitPane();
		
		AnchorPane top = new AnchorPane();
		splitPane.getItems().add(top);
		final Tree<Step> serviceTree = new Tree<Step>(new StepMarshallable());
		serviceTree.rootProperty().set(new StepTreeItem(service.getRoot(), null, false));
		// disable map tab
		serviceController.getTabMap().setDisable(true);
		
		serviceTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeCell<Step>>() {
			@Override
			public void changed(ObservableValue<? extends TreeCell<Step>> arg0, TreeCell<Step> arg1, TreeCell<Step> arg2) {
				if (arg2 != null) {
					controller.showProperties(new StepPropertyProvider(arg2));
				}
			}
		});
		serviceTree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.F12) {
					TreeCell<Step> selectedItem = serviceTree.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						Boolean current = ((StepTreeItem) selectedItem.getItem()).disableProperty().get();
						((StepTreeItem) selectedItem.getItem()).disableProperty().set(!current);
					}
				}
			}
		});
				
		serviceController.getHbxButtons().getChildren().add(createAddButton(serviceTree, Sequence.class));
		serviceController.getHbxButtons().getChildren().add(createAddButton(serviceTree, Map.class));
		serviceController.getHbxButtons().getChildren().add(createAddButton(serviceTree, For.class));
		serviceController.getHbxButtons().getChildren().add(createAddButton(serviceTree, Switch.class));		
		serviceController.getHbxButtons().getChildren().add(createAddButton(serviceTree, Catch.class));
		serviceController.getHbxButtons().getChildren().add(createAddButton(serviceTree, Finally.class));
		serviceController.getHbxButtons().getChildren().add(createAddButton(serviceTree, Throw.class));

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
		inputTree = structureManager.display(controller, input, new RootElementWithPush(
			(Structure) service.getPipeline().get(Pipeline.INPUT).getType(), 
			false,
			service.getPipeline().get(Pipeline.INPUT).getProperties()
		), true, false);
		serviceController.getPanInput().getChildren().add(input);
		
		VBox output = new VBox();
		outputTree = structureManager.display(controller, output, new RootElementWithPush(
			(Structure) service.getPipeline().get(Pipeline.OUTPUT).getType(), 
			false,
			service.getPipeline().get(Pipeline.OUTPUT).getProperties()
		), true, false);
		serviceController.getPanOutput().getChildren().add(output);
		
		leftTree = buildLeftPipeline(controller, serviceController, service.getRoot());
		rightTree = buildRightPipeline(controller, service, serviceTree, serviceController, service.getRoot());

		// if we select a map step, we have to show the mapping screen
		serviceTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeCell<Step>>() {
			@Override
			public void changed(ObservableValue<? extends TreeCell<Step>> arg0, TreeCell<Step> arg1, TreeCell<Step> arg2) {
				Step step = arg2.getItem().itemProperty().get();
				
				// enable/disable buttons depending on the selection
				// first just disable all buttons
				for (Button button : addButtons.values()) {
					button.disableProperty().set(true);
				}
				// for a stepgroup, reenable some or all buttons
				if (step instanceof StepGroup) {
					for (Class<? extends Step> supported : step instanceof LimitedStepGroup ? ((LimitedStepGroup) step).getAllowedSteps() : addButtons.keySet()) {
						if (addButtons.containsKey(supported)) {
							addButtons.get(supported).disableProperty().set(false);
						}
					}
				}
				
				// if the new selection is not a map, or not the same map, clear it
				if (!(step instanceof Map) || !arg2.equals(arg1)) {
					serviceController.getTabMap().setDisable(true);
					// remove all the current lines
					for (Mapping mapping : mappings.values()) {
						mapping.remove();
					}
					mappings.clear();
					// remove all the set values
					for (FixedValue fixedValue : fixedValues.values()) {
						fixedValue.remove();
					}
					fixedValues.clear();
					// clear left & right & center
					serviceController.getPanLeft().getChildren().clear();
					serviceController.getPanRight().getChildren().clear();
					serviceController.getPanMiddle().getChildren().clear();
				}
				// if the new selection is a map, draw everything
				if (arg2.getItem().itemProperty().get() instanceof Map) {
					leftTree = buildLeftPipeline(controller, serviceController, (Map) arg2.getItem().itemProperty().get());
					rightTree = buildRightPipeline(controller, service, serviceTree, serviceController, (Map) arg2.getItem().itemProperty().get());
					serviceController.getTabMap().setDisable(false);
					// first draw all the invokes and build a map of temporary result mappings
					invokeWrappers = new HashMap<String, InvokeWrapper>();
					for (final Step child : ((Map) arg2.getItem().itemProperty().get()).getChildren()) {
						if (child instanceof Invoke) {
							drawInvoke(controller, (Invoke) child, invokeWrappers, serviceController, service, serviceTree);
						}
					}
					Iterator<Step> iterator = ((Map) arg2.getItem().itemProperty().get()).getChildren().iterator();
					// loop over the invoke again but this time to draw links
					while (iterator.hasNext()) {
						Step child = iterator.next();
						if (child instanceof Invoke) {
							for (Step linkChild : ((Invoke) child).getChildren()) {
								final Link link = (Link) linkChild;
								if (link.isFixedValue()) {
									// must be mapped to the input of an invoke
									Tree<Element<?>> tree = invokeWrappers.get(((Invoke) child).getResultName()).getInput();
									FixedValue fixedValue = buildFixedValue(controller, tree, link);
									if (fixedValue == null) {
										controller.notify(new ValidationMessage(Severity.ERROR, "The fixed value to " + link.getTo() + " is no longer valid"));
									}
									else {
										fixedValues.put(link, fixedValue);
									}
								}
								else {
									// a link in an invoke always maps to that invoke, so substitute the right tree for the input of this invoke
									Mapping mapping = buildMapping(
										link, 
										serviceController.getPanMap(), 
										leftTree, 
										invokeWrappers.get(((Invoke) child).getResultName()).getInput(), 
										invokeWrappers
									);
									if (mapping == null) {
										controller.notify(new ValidationMessage(Severity.ERROR, "The mapping from " + ((Link) link).getFrom() + " to " + ((Link) link).getTo() + " is no longer valid, it will be removed"));
										link.getParent().getChildren().remove(link);
									}
									else {
										mappings.put(link, mapping);
										mapping.getLine().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
											@Override
											public void handle(MouseEvent arg0) {
												controller.showProperties(new LinkPropertyUpdater(link));
											}
										});
									}
								}
							}
						}
					}
					// draw all the links from the mappings
					iterator = ((Map) arg2.getItem().itemProperty().get()).getChildren().iterator();
					while (iterator.hasNext()) {
						Step child = iterator.next();
						if (child instanceof Link) {
							final Link link = (Link) child;
							if (link.isFixedValue()) {
								FixedValue fixedValue = buildFixedValue(controller, rightTree, link);
								if (fixedValue == null) {
									controller.notify(new ValidationMessage(Severity.ERROR, "The fixed value to " + link.getTo() + " is no longer valid"));
								}
								else {
									fixedValues.put(link, fixedValue);
								}
							}
							else {
								Mapping mapping = buildMapping(link, serviceController.getPanMap(), leftTree, rightTree, invokeWrappers);
								// don't remove the mapping alltogether, the user might want to fix it or investigate it
								if (mapping == null) {
									controller.notify(new ValidationMessage(Severity.ERROR, "The mapping from " + link.getFrom() + " to " + link.getTo() + " is no longer valid"));
									iterator.remove();
								}
								else {
									mappings.put(link, mapping);
									mapping.getLine().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
										@Override
										public void handle(MouseEvent arg0) {
											controller.showProperties(new LinkPropertyUpdater(link));
										}
									});
								}
							}
						}
					}

				}
			}
		});
		
		// the service controller resizes the scroll pane based on this pane
		// so bind it to the the tree
		serviceController.getPanLeft().prefWidthProperty().bind(leftTree.widthProperty());
		serviceController.getPanRight().prefWidthProperty().bind(rightTree.widthProperty());
		
		serviceController.getPanMiddle().addEventHandler(DragEvent.DRAG_OVER, new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get() instanceof Map) {
					Dragboard dragboard = event.getDragboard();
					if (dragboard != null) {
						String id = (String) dragboard.getContent(TreeDragDrop.getDataFormat("tree"));
						// the drag happened from a tree
						if (id != null && id.equals("repository")) {
							Object content = dragboard.getContent(TreeDragDrop.getDataFormat(RepositoryBrowser.getDataType(DefinedService.class)));
							// this will be the path in the tree
							if (content != null) {
								String serviceId = controller.getRepositoryBrowser().getControl().resolve((String) content).itemProperty().get().getId();
								if (serviceId != null) {
									event.acceptTransferModes(TransferMode.MOVE);
									event.consume();
								}
							}
						}
					}
				}
			}
		});
		serviceController.getPanMiddle().addEventHandler(DragEvent.DRAG_DROPPED, new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				if (serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get() instanceof Map) {
					Map target = (Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get();
					Dragboard dragboard = event.getDragboard();
					if (dragboard != null) {
						String id = (String) dragboard.getContent(TreeDragDrop.getDataFormat("tree"));
						// the drag happened from a tree
						if (id != null && id.equals("repository")) {
							Object content = dragboard.getContent(TreeDragDrop.getDataFormat(RepositoryBrowser.getDataType(DefinedService.class)));
							// this will be the path in the tree
							if (content != null) {
								String serviceId = controller.getRepositoryBrowser().getControl().resolve((String) content).itemProperty().get().getId();
								if (serviceId != null) {
									Invoke invoke = new Invoke();
									invoke.setParent(target);
									invoke.setServiceId(serviceId);
									invoke.setX(event.getSceneX());
									invoke.setY(event.getSceneY());
									target.getChildren().add(invoke);
									drawInvoke(controller, invoke, invokeWrappers, serviceController, service, serviceTree);
								}
							}
						}
					}
				}
			}
		});
		
		return service;
	}
	
	private InvokeWrapper drawInvoke(MainController controller, final Invoke invoke, java.util.Map<String, InvokeWrapper> invokeWrappers, VMServiceController serviceController, VMService service, Tree<Step> serviceTree) {
		InvokeWrapper invokeWrapper = new InvokeWrapper(controller, invoke, serviceController.getPanMap(), service, serviceController, serviceTree, mappings);
		invokeWrappers.put(invoke.getResultName(), invokeWrapper);
		Pane pane = invokeWrapper.getComponent();
		serviceController.getPanMiddle().getChildren().add(pane);
		MovablePane movable = MovablePane.makeMovable(pane);
		movable.xProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				invoke.setX(arg2.doubleValue());
			}
		});
		movable.yProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
				invoke.setY(arg2.doubleValue());
			}
		});
		FixedValue.allowFixedValue(controller, fixedValues, serviceTree, invokeWrapper.getInput());
		return invokeWrapper;
	}
	
	private Button createAddButton(Tree<Step> serviceTree, Class<? extends Step> clazz) {
		Button button = new Button();
		button.setGraphic(MainController.loadGraphic(getIcon(clazz)));
		button.addEventHandler(ActionEvent.ACTION, new ServiceAddHandler(serviceTree, clazz));
		addButtons.put(clazz, button);
		return button;
	}
	
	private Tree<Element<?>> buildRightPipeline(MainController controller, VMService service, Tree<Step> serviceTree, VMServiceController serviceController, StepGroup step) {
		// remove listeners
		if (rightTree != null) {
			inputTree.removeRefreshListener(rightTree.getTreeCell(rightTree.rootProperty().get()));
			outputTree.removeRefreshListener(rightTree.getTreeCell(rightTree.rootProperty().get()));
		}
		final VBox right = new VBox();
		StructureGUIManager structureManager = new StructureGUIManager();
		try {
			Tree<Element<?>> rightTree = structureManager.display(controller, right, new RootElementWithPush(
				(Structure) step.getPipeline(new SimpleServiceRuntime.SimpleServiceContext()), 
				false,
				step.getPipeline(new SimpleServiceRuntime.SimpleServiceContext()).getProperties()
			), true, true);
		
			// make sure the "input" & "output" are not editable
			for (TreeItem<Element<?>> item : rightTree.rootProperty().get().getChildren()) {
				if (item.itemProperty().get().getName().equals("input") || item.itemProperty().get().getName().equals("output")) {
					item.editableProperty().set(false);
				}
				else {
					item.editableProperty().set(true);
				}
			}
			serviceController.getPanRight().getChildren().add(right);
			
			// make sure the left & right trees are refreshed if the input/output is updated
			inputTree.addRefreshListener(rightTree.getTreeCell(rightTree.rootProperty().get()));
			outputTree.addRefreshListener(rightTree.getTreeCell(rightTree.rootProperty().get()));
			
			TreeDragDrop.makeDroppable(rightTree, new DropLinkListener(controller, mappings, service, serviceController, serviceTree));
			FixedValue.allowFixedValue(controller, fixedValues, serviceTree, rightTree);
			
			return rightTree;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
		catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	private Tree<Element<?>> buildLeftPipeline(MainController controller, VMServiceController serviceController, StepGroup step) {
		if (leftTree != null) {
			inputTree.removeRefreshListener(leftTree.getTreeCell(leftTree.rootProperty().get()));
			outputTree.removeRefreshListener(leftTree.getTreeCell(leftTree.rootProperty().get()));
		}
		Tree<Element<?>> leftTree = new Tree<Element<?>>(new ElementMarshallable());
		leftTree.rootProperty().set(new ElementTreeItem(new RootElementWithPush(
			(Structure) step.getPipeline(new SimpleServiceRuntime.SimpleServiceContext()),
			false,
			step.getPipeline(new SimpleServiceRuntime.SimpleServiceContext()).getProperties()
		), null, false, false));
		// show properties if selected
		leftTree.getSelectionModel().selectedItemProperty().addListener(new ElementSelectionListener(controller, false));
		// add first to get parents right
		serviceController.getPanLeft().getChildren().add(leftTree);
		TreeDragDrop.makeDraggable(leftTree, new ElementLineConnectListener(serviceController.getPanMap()));
		inputTree.addRefreshListener(leftTree.getTreeCell(leftTree.rootProperty().get()));
		outputTree.addRefreshListener(leftTree.getTreeCell(leftTree.rootProperty().get()));
		return leftTree;
	}
	
	private FixedValue buildFixedValue(MainController controller, Tree<Element<?>> tree, Link link) {
		TreeItem<Element<?>> target = VMServiceGUIManager.find(tree.rootProperty().get(), new ParsedPath(link.getTo()));
		if (target == null) {
			return null;
		}
		return new FixedValue(controller, tree.getTreeCell(target), link);
	}
	
	private Mapping buildMapping(Link link, Pane target, Tree<Element<?>> left, Tree<Element<?>> right, java.util.Map<String, InvokeWrapper> invokeWrappers) {
		ParsedPath from = new ParsedPath(link.getFrom());
		TreeItem<Element<?>> fromElement;
		Tree<Element<?>> fromTree;
		// this means you are mapping it from another invoke, use that output tree to find the element
		if (invokeWrappers.containsKey(from.getName())) {
			fromElement = find(invokeWrappers.get(from.getName()).getOutput().rootProperty().get(), from.getChildPath());
			fromTree = invokeWrappers.get(from.getName()).getOutput();
		}
		// otherwise, it's from the pipeline
		else {
			fromElement = find(left.rootProperty().get(), from);
			fromTree = left;
		}
		ParsedPath to = new ParsedPath(link.getTo());
		TreeItem<Element<?>> toElement;
		Tree<Element<?>> toTree;
		if (invokeWrappers.containsKey(to.getName())) {
			toElement = find(invokeWrappers.get(to.getName()).getInput().rootProperty().get(), to);
			toTree = invokeWrappers.get(to.getName()).getInput();
		}
		// otherwise, it's from the pipeline
		else {
			toElement = find(right.rootProperty().get(), to);
			toTree = right;
		}
		
		if (fromElement == null || toElement == null) {
			return null;
		}
		else {
			Mapping mapping = new Mapping(target, fromTree.getTreeCell(fromElement), toTree.getTreeCell(toElement));
			mapping.setRemoveMapping(new RemoveLinkListener(link));
			return mapping;
		}
	}
			
	private final class StepMarshallable implements Marshallable<Step> {
		@Override
		public String marshal(Step step) {
			String specific = "";
			if (step instanceof For) {
				specific = " each " + ((For) step).getVariable() + " in " + ((For) step).getQuery();
			}
			else if (step instanceof Switch) {
				String query = ((Switch) step).getQuery();
				if (query != null) {
					specific = " on " + query;
				}
			}
			else if (step instanceof Throw) {
				if (((Throw) step).getMessage() != null) {
					specific = ((Throw) step).getMessage();
				}
			}
			String label = step.getLabel() != null ? step.getLabel() + ": " : "";
			// if the label is empty inside a switch, it is the default option
			if (label.isEmpty() && step.getParent() instanceof Switch) {
				label = "$default: ";
			}
			return label + step.getClass().getSimpleName() + specific + (step.getComment() != null ? " (" + step.getComment() + ")" : "");
		}
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
							Step instance = step.newInstance();
							instance.setParent((StepGroup) selectedItem.getItem().itemProperty().get());
							((StepGroup) selectedItem.getItem().itemProperty().get()).getChildren().add(instance);
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
		return "step/" + clazz.getSimpleName().toLowerCase() + ".png";
	}
	public static String getIcon(Step step) {
		return getIcon(step.getClass());
	}

	@Override
	public Class<VMService> getArtifactClass() {
		return getArtifactManager().getArtifactClass();
	}

}
