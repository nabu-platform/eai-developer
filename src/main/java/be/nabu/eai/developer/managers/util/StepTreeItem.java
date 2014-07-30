package be.nabu.eai.developer.managers.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.VMServiceGUIManager;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.TreeUtils.TreeItemCreator;
import be.nabu.libs.services.vm.Map;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.services.vm.StepGroup;

public class StepTreeItem implements RemovableTreeItem<Step> {
	private StepTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Step> itemProperty = new SimpleObjectProperty<Step>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Step>> children = FXCollections.observableArrayList();
	
	public StepTreeItem(Step step, StepTreeItem parent, boolean isEditable) {
		this.itemProperty.set(step);
		this.parent = parent;
		editableProperty.set(isEditable);
		refresh();
	}

	@Override
	public BooleanProperty editableProperty() {
		return editableProperty;
	}

	@Override
	public ObservableList<TreeItem<Step>> getChildren() {
		return children;
	}
	
	@Override
	public void refresh() {
		leafProperty.set(!(itemProperty.get() instanceof StepGroup) || itemProperty.get() instanceof Map);
		graphicProperty.set(MainController.loadGraphic(VMServiceGUIManager.getIcon(itemProperty.get())));
		if (!leafProperty.get()) {
			TreeUtils.refreshChildren(new TreeItemCreator<Step>() {
				@Override
				public TreeItem<Step> create(TreeItem<Step> parent, Step child) {
					return new StepTreeItem(child, (StepTreeItem) parent, editableProperty.get());	
				}
			}, this, ((StepGroup) itemProperty.get()).getChildren());
		}
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

	@Override
	public boolean remove() {
		if (itemProperty().get().getParent() != null) {
			itemProperty().get().getParent().getChildren().remove(itemProperty().get());
			return true;
		}
		return false;
	}

}
