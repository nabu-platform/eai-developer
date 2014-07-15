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
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;

public class ElementTreeItem implements TreeItem<Element<?>> {

	private ElementTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Element<?>> itemProperty = new SimpleObjectProperty<Element<?>>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Element<?>>> children;
	
	public ElementTreeItem(Element<?> element, ElementTreeItem parent, boolean isEditable) {
		this.itemProperty.set(element);
		this.parent = parent;
		editableProperty.set(isEditable);
		leafProperty.set(!(element.getType() instanceof ComplexType));
	}
	
	@Override
	public BooleanProperty editableProperty() {
		return editableProperty;
	}

	@Override
	public ObservableList<TreeItem<Element<?>>> getChildren() {
		if (children == null) {
			children = FXCollections.observableArrayList(loadChildren());
		}
		return children;
	}
	
	public void refresh() {
		getChildren().clear();
		getChildren().addAll(loadChildren());
	}
	
	private List<TreeItem<Element<?>>> loadChildren() {
		List<TreeItem<Element<?>>> children = new ArrayList<TreeItem<Element<?>>>();
		if (itemProperty.get().getType() instanceof ComplexType) {
			for (Element<?> child : (ComplexType) itemProperty.get().getType()) {
				children.add(new ElementTreeItem(child, this, editableProperty.get() && (parent == null || !(itemProperty.get().getType() instanceof DefinedType))));
			}
		}
		return children;
	}

	@Override
	public String getName() {
		return itemProperty.get().getName();
	}

	@Override
	public TreeItem<Element<?>> getParent() {
		return parent;
	}

	@Override
	public ObjectProperty<Node> graphicProperty() {
		return graphicProperty;
	}

	@Override
	public ObjectProperty<Element<?>> itemProperty() {
		return itemProperty;
	}

	@Override
	public BooleanProperty leafProperty() {
		return leafProperty;
	}

}
