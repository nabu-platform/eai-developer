package be.nabu.eai.developer.managers.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.controllers.VMServiceController;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.vm.step.Invoke;
import be.nabu.libs.services.vm.step.Link;
import be.nabu.libs.services.vm.step.Map;
import be.nabu.libs.services.vm.api.Step;
import be.nabu.libs.services.vm.api.StepGroup;
import be.nabu.libs.services.vm.api.VMService;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;

public class InvokeWrapper {
	
	private Invoke invoke;
	private Pane target;
	private java.util.Map<Link, Mapping> mappings;
	private Tree<Step> serviceTree;
	private VMService service;
	private VMServiceController serviceController;
	private Tree<Element<?>> input, output;
	private MainController controller;

	public InvokeWrapper(MainController controller, Invoke invoke, Pane target, VMService service, VMServiceController serviceController, Tree<Step> serviceTree, java.util.Map<Link, Mapping> mappings) {
		this.controller = controller;
		this.invoke = invoke;
		this.target = target;
		this.service = service;
		this.serviceTree = serviceTree;
		this.serviceController = serviceController;
		this.mappings = mappings;
	}
	
	public Pane getComponent() {
		// use an anchorpane, because if you set the vbox to unmanaged, things go...wrong
		final AnchorPane pane = new AnchorPane();
		pane.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.DELETE) {
					// the input is mapped inside the invoke itself so from the object perspective they don't have to be specifically removed
					// however we do need to remove the lines that were drawn
					for (Step child : invoke.getChildren()) {
						if (child instanceof Link) {
							Link link = (Link) child;
							Mapping mapping = mappings.get(link);
							if (mapping != null) {
								mappings.remove(link);
								mapping.remove();
							}
						}
					}
					// remove anyone who has mapped an output from this invoke
					removeInGroup(invoke.getParent());
					invoke.getParent().getChildren().remove(invoke);
					((Pane) pane.getParent()).getChildren().remove(pane);
					event.consume();
					MainController.getInstance().setChanged();
				}
				else if (event.getCode() == KeyCode.L && event.isControlDown()) {
					Tree<Entry> tree = MainController.getInstance().getTree();
					TreeItem<Entry> resolve = tree.resolve(invoke.getServiceId().replace(".", "/"));
					if (resolve != null) {
						TreeCell<Entry> treeCell = tree.getTreeCell(resolve);
						treeCell.show();
						treeCell.select();
						tree.autoscroll();
					}
					event.consume();
				}
			}
		});
		pane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				pane.requestFocus();
				SimpleProperty<Integer> invocationProperty = new SimpleProperty<Integer>("invocationOrder", Integer.class, true);
				HashSet<Property<?>> hashSet = new HashSet<Property<?>>(Arrays.asList(invocationProperty));
				PropertyUpdater updater = new PropertyUpdater() {
					@Override
					public Set<Property<?>> getSupportedProperties() {
						return hashSet;
					}
					@Override
					public Value<?>[] getValues() {
						return new Value<?> [] { new ValueImpl<Integer>(invocationProperty, invoke.getInvocationOrder()) };
					}
					@Override
					public boolean canUpdate(Property<?> property) {
						return true;
					}
					@Override
					public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
						if (value instanceof Integer && property.equals(invocationProperty)) {
							invoke.setInvocationOrder((Integer) value);
						}
						return invoke.getParent() instanceof Map ? ((Map) invoke.getParent()).calculateInvocationOrder() : null;
					}
					@Override
					public boolean isMandatory(Property<?> property) {
						return true;
					}
				};
				controller.showProperties(updater);
			}
		});
		VBox vbox = new VBox();
		HBox name = new HBox();
		name.getChildren().add(new Label(invoke.getServiceId()));
		vbox.getChildren().add(name);
		// the input & output should not be scrollable but should resize on demand
		Service service = invoke.getService(controller.getRepository().getServiceContext());
		vbox.getStyleClass().add("service");
		if (service != null) {
			input = new Tree<Element<?>>(new ElementMarshallable());
			input.setClipboardHandler(new ElementClipboardHandler(input, false));
			input.set("invoke", invoke);
			input.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getInputDefinition(), "input"), null, false, false));
			input.getTreeCell(input.rootProperty().get()).expandedProperty().set(false);
			TreeDragDrop.makeDroppable(input, new DropLinkListener(controller, mappings, this.service, serviceController, serviceTree));
			
			output = new Tree<Element<?>>(new ElementMarshallable());
			output.setClipboardHandler(new ElementClipboardHandler(output, false));
			output.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getOutputDefinition(), "output"), null, false, false));
			output.getTreeCell(output.rootProperty().get()).expandedProperty().set(false);
			output.set("invoke", invoke);
			TreeDragDrop.makeDraggable(output, new ElementLineConnectListener(target));
		
			HBox iface = new HBox();
			iface.getChildren().addAll(input, output);
			vbox.getChildren().add(iface);
			vbox.getStyleClass().add("existent");

			input.resize();
			output.resize();
			
			// the initial resize just won't work...
			input.setPrefWidth(100);
			output.setPrefWidth(100);
		}
		else {
			vbox.getStyleClass().add("nonExistent");
		}
		pane.getChildren().add(vbox);
		pane.setManaged(false);
		pane.setLayoutX(invoke.getX());
		pane.setLayoutY(invoke.getY());
		return pane;
	}
	
	private void removeInGroup(StepGroup group) {
		List<Step> children = group.getChildren();
		for (int i = children.size() - 1; i >= 0; i--) {
			Step child = children.get(i);
			if (child instanceof Link) {
				Link link = (Link) child;
				if (link.getFrom().startsWith(invoke.getResultName() + "/")) {
					group.getChildren().remove(i);
					Mapping mapping = mappings.get(link);
					if (mapping != null) {
						mappings.remove(link);
						mapping.remove();
					}
				}
			}
			else if (child instanceof Invoke) {
				removeInGroup((Invoke) child);
			}
		}
	}
	
	public Tree<Element<?>> getInput() {
		return input;
	}

	public Tree<Element<?>> getOutput() {
		return output;
	}
}
