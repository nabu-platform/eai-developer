package be.nabu.eai.developer.managers.util;

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
import be.nabu.eai.developer.controllers.VMServiceController;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.vm.Invoke;
import be.nabu.libs.services.vm.Link;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.services.vm.VMService;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.RootElement;

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
					invoke.getParent().getChildren().remove(invoke);
					((Pane) pane.getParent()).getChildren().remove(pane);
					event.consume();
				}
			}
		});
		pane.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				pane.requestFocus();
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
			input.set("invoke", invoke);
			input.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getInputDefinition(), "input"), null, false, false));
			input.getTreeCell(input.rootProperty().get()).expandedProperty().set(false);
			TreeDragDrop.makeDroppable(input, new DropLinkListener(controller, mappings, this.service, serviceController, serviceTree));
			
			output = new Tree<Element<?>>(new ElementMarshallable());
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
	
	public Tree<Element<?>> getInput() {
		return input;
	}

	public Tree<Element<?>> getOutput() {
		return output;
	}
}
