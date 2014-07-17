package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.VMServiceGUIManager;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.services.vm.StepGroup;

public class StepTreeItem implements TreeItem<Step> {
	private StepTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Step> itemProperty = new SimpleObjectProperty<Step>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Step>> children;
	
	public StepTreeItem(Step step, StepTreeItem parent, boolean isEditable) {
		this.itemProperty.set(step);
		this.parent = parent;
		editableProperty.set(isEditable);
		leafProperty.set(!(step instanceof StepGroup));
		graphicProperty.set(MainController.loadGraphic(VMServiceGUIManager.getIcon(step)));
	}

	@Override
	public BooleanProperty editableProperty() {
		return editableProperty;
	}

	@Override
	public ObservableList<TreeItem<Step>> getChildren() {
		if (children == null) {
			children = FXCollections.observableArrayList(loadChildren());
		}
		return children;
	}

	private List<TreeItem<Step>> loadChildren() {
		List<TreeItem<Step>> children = new ArrayList<TreeItem<Step>>();
		if (itemProperty.get() instanceof StepGroup) {
			for (Step child : ((StepGroup) itemProperty.get()).getChildren()) {
				children.add(new StepTreeItem(child, this, editableProperty.get()));
			}
		}
		return children;
	}
	
	public void refresh() {
		getChildren().clear();
		getChildren().addAll(loadChildren());
	}
	
	@Override
	public String getName() {
		return itemProperty.get().getClass().getSimpleName();
	}

	@Override
	public TreeItem<Step> getParent() {
		return parent;
	}

	@Override
	public ObjectProperty<Node> graphicProperty() {
		return graphicProperty;
	}

	@Override
	public ObjectProperty<Step> itemProperty() {
		return itemProperty;
	}

	@Override
	public BooleanProperty leafProperty() {
		return leafProperty;
	}

}
