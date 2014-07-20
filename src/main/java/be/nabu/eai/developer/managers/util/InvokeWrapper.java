package be.nabu.eai.developer.managers.util;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.controllers.VMServiceController;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.services.SimpleServiceRuntime;
import be.nabu.libs.services.api.Service;
import be.nabu.libs.services.vm.Invoke;
import be.nabu.libs.services.vm.Link;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.services.vm.VMService;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.structure.Structure;

public class InvokeWrapper {
	
	private Invoke invoke;
	private Pane target;
	private java.util.Map<Link, Mapping> mappings;
	private Tree<Step> serviceTree;
	private VMService service;
	private VMServiceController controller;
	private Tree<Element<?>> input, output;

	public InvokeWrapper(Invoke invoke, Pane target, VMService service, VMServiceController controller, Tree<Step> serviceTree, java.util.Map<Link, Mapping> mappings) {
		this.invoke = invoke;
		this.target = target;
		this.service = service;
		this.serviceTree = serviceTree;
		this.controller = controller;
		this.mappings = mappings;
	}
	
	public Pane getComponent() {
		VBox vbox = new VBox();
		HBox name = new HBox();
		name.getChildren().add(new Label(invoke.getServiceId()));
		vbox.getChildren().add(name);
		// the input & output should not be scrollable but should resize on demand
		Service service = invoke.getService(new SimpleServiceRuntime.SimpleServiceContext());
		vbox.getStyleClass().add("service");
		if (service != null) {
			SplitPane split = new SplitPane();
			split.setOrientation(Orientation.HORIZONTAL);

			AnchorPane leftPane = new AnchorPane();
			input = new Tree<Element<?>>(new ElementMarshallable());
			input.set("invoke", invoke);
			input.rootProperty().set(new ElementTreeItem(new RootElementWithPush((Structure) service.getServiceInterface().getInputDefinition(), false), null, false));
			input.getTreeCell(input.rootProperty().get()).expandedProperty().set(false);
			leftPane.getChildren().add(input);
			
			TreeDragDrop.makeDroppable(input, new DropLinkListener(mappings, this.service, controller, serviceTree));
//			TreeDragDrop.makeDroppable(input, new TreeDropListener<Element<?>>() {
//				@Override
//				public boolean canDrop(String arg0, TreeCell<Element<?>> arg1, TreeCell<?> arg2, TransferMode arg3) {
//					return false;
//				}
//				@Override
//				public void drop(String arg0, TreeCell<Element<?>> arg1,
//						TreeCell<?> arg2, TransferMode arg3) {
//					// TODO Auto-generated method stub
//					
//				}
//				
//			});
			
			AnchorPane rightPane = new AnchorPane();
			output = new Tree<Element<?>>(new ElementMarshallable());
			output.rootProperty().set(new ElementTreeItem(new RootElementWithPush((Structure) service.getServiceInterface().getOutputDefinition(), false), null, false));
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
		return vbox;
	}
	
	public Tree<Element<?>> getInput() {
		return input;
	}

	public Tree<Element<?>> getOutput() {
		return output;
	}
}
