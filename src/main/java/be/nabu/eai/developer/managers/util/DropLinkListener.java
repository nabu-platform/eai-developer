package be.nabu.eai.developer.managers.util;

import javafx.scene.input.TransferMode;
import be.nabu.eai.developer.controllers.VMServiceController;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDropListener;
import be.nabu.libs.services.vm.Invoke;
import be.nabu.libs.services.vm.Link;
import be.nabu.libs.services.vm.Map;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.services.vm.VMService;
import be.nabu.libs.types.ParsedPath;
import be.nabu.libs.types.api.Element;

public class DropLinkListener implements TreeDropListener<Element<?>> {
	private final VMService service;
	private final VMServiceController serviceController;
	private final Tree<Step> serviceTree;
	private java.util.Map<Link, Mapping> mappings;

	public DropLinkListener(java.util.Map<Link, Mapping> mappings, VMService service, VMServiceController serviceController, Tree<Step> serviceTree) {
		this.mappings = mappings;
		this.service = service;
		this.serviceController = serviceController;
		this.serviceTree = serviceTree;
	}

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
			ParsedPath from = new ParsedPath(TreeDragDrop.getPath(dragged.getItem()));
			// you are dragging something from an invoke output
			if (dragged.getTree().get("invoke") != null) {
				// it has to come from the output
				if (!from.getName().equals("output")) {
					throw new RuntimeException("Expecting an output path");	
				}
				// update the unnecessary "output" with the actual name of the invoke as it is mapped to the pipeline
				from.setName(((Invoke) dragged.getTree().get("invoke")).getResultName());
			}
			else {
				if (!from.getName().equals("pipeline")) {
					throw new RuntimeException("Expecting a pipeline path");
				}
				from = from.getChildPath();
			}
			ParsedPath to = new ParsedPath(TreeDragDrop.getPath(target.getItem()));
			// you are dragging it to an invoke input
			if (target.getTree().get("invoke") != null) {
				if (!to.getName().equals("input")) {
					throw new RuntimeException("Expecting an input path");
				}
				// don't need the "input" leadin
				to = to.getChildPath();
			}
			else {
				if (!to.getName().equals("pipeline")) {
					throw new RuntimeException("Expecting a pipeline path");
				}
				// don't need the "pipeline" bit
				to = to.getChildPath();
			}
			setDefaultIndexes(from, (TreeItem<Element<?>>) dragged.getTree().rootProperty().get());
			setDefaultIndexes(to, target.getTree().rootProperty().get());
			Link link = new Link(from.toString(), to.toString());
			// if the target is an invoke, the mapping has to be done inside the invoke
			if (target.getTree().get("invoke") != null) {
				link.setParent(((Invoke) target.getTree().get("invoke")));
				// add the link to the currently selected mapping
				((Invoke) target.getTree().get("invoke")).getChildren().add(link);
				// when you are mapping an input to an invoke, we also have to recalculate the invocation order for the mapping
				// it could be that you are mapping from another invoke which means this one has to be invoked after that
				((Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get()).recalculateInvocationOrder();
			}
			// else link it to the map
			else {
				link.setParent(((Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get()));
				// add the link to the currently selected mapping
				((Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get()).getChildren().add(link);
			}
			mappings.put(link, mapping);
		}
	}
	
	private static void setDefaultIndexes(ParsedPath path, TreeItem<Element<?>> parent) {
		for (TreeItem<Element<?>> child : parent.getChildren()) {
			if (child.getName().equals(path.getName())) {
				// if it's a list, set a default index
				if (child.itemProperty().get().getType().isList(child.itemProperty().get().getProperties())) {
					path.setIndex("0");
				}
				// recurse
				if (path.getChildPath() != null) {
					setDefaultIndexes(path.getChildPath(), parent);
				}
			}
		}
	}

}

