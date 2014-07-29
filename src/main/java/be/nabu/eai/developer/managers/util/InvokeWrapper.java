package be.nabu.eai.developer.managers.util;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
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
		VBox vbox = new VBox();
		HBox name = new HBox();
		name.getChildren().add(new Label(invoke.getServiceId()));
		vbox.getChildren().add(name);
		// the input & output should not be scrollable but should resize on demand
		Service service = invoke.getService(controller.getRepository().getServiceContext());
		vbox.getStyleClass().add("service");
		if (service != null) {
			SplitPane split = new SplitPane();
			split.setOrientation(Orientation.HORIZONTAL);

			AnchorPane leftPane = new AnchorPane();
			input = new Tree<Element<?>>(new ElementMarshallable());
			input.set("invoke", invoke);
			input.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getInputDefinition(), "input"), null, false, false));
			input.getTreeCell(input.rootProperty().get()).expandedProperty().set(false);
			leftPane.getChildren().add(input);
			
			TreeDragDrop.makeDroppable(input, new DropLinkListener(controller, mappings, this.service, serviceController, serviceTree));
			
			AnchorPane rightPane = new AnchorPane();
			output = new Tree<Element<?>>(new ElementMarshallable());
			output.rootProperty().set(new ElementTreeItem(new RootElement(service.getServiceInterface().getOutputDefinition(), "output"), null, false, false));
			output.getTreeCell(output.rootProperty().get()).expandedProperty().set(false);
			output.set("invoke", invoke);
			TreeDragDrop.makeDraggable(output, new ElementLineConnectListener(target));
			rightPane.getChildren().add(output);
		
			input.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			ComparableAmountListener<Double> heightListener = new ComparableAmountListener<Double>(
				input.getTreeCell(input.rootProperty().get()).getItemContainer().heightProperty(), 
				output.getTreeCell(output.rootProperty().get()).getItemContainer().heightProperty()
			);
			ComparableAmountListener<Double> widthListener = new ComparableAmountListener<Double>(
				input.getTreeCell(input.rootProperty().get()).getItemContainer().widthProperty(), 
				output.getTreeCell(output.rootProperty().get()).getItemContainer().widthProperty()
			);
			
			split.getItems().addAll(leftPane, rightPane);
			
			vbox.getChildren().add(split);
			vbox.getStyleClass().add("existent");
			
			leftPane.prefHeightProperty().bind(heightListener.maxProperty());
			rightPane.prefHeightProperty().bind(heightListener.maxProperty());
			leftPane.prefWidthProperty().bind(widthListener.maxProperty());
			rightPane.prefWidthProperty().bind(widthListener.maxProperty());
			
			split.minHeightProperty().bind(heightListener.maxProperty());
			split.prefHeightProperty().bind(heightListener.maxProperty());
			split.maxHeightProperty().bind(heightListener.maxProperty());
			
			split.prefWidthProperty().bind(
				input.getTreeCell(input.rootProperty().get()).getItemContainer().widthProperty()
				.add(output.getTreeCell(output.rootProperty().get()).getItemContainer().widthProperty())
				.add(50)
			);
			split.minWidthProperty().bind(split.prefWidthProperty());
			split.maxWidthProperty().bind(split.prefWidthProperty());
			
			vbox.prefHeightProperty().bind(split.prefHeightProperty());
		}
		else {
			vbox.getStyleClass().add("nonExistent");
		}
//		vbox.setManaged(false);
		vbox.setLayoutX(invoke.getX());
		vbox.setLayoutY(invoke.getY());
		return vbox;
	}
	
	public Tree<Element<?>> getInput() {
		return input;
	}

	public Tree<Element<?>> getOutput() {
		return output;
	}
}
