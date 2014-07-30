package be.nabu.eai.developer.managers.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.StructureGUIManager;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.TreeUtils.TreeItemCreator;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.ModifiableTypeInstance;

public class ElementTreeItem implements RemovableTreeItem<Element<?>> {

	private ElementTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Element<?>> itemProperty = new SimpleObjectProperty<Element<?>>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Element<?>>> children = FXCollections.observableArrayList();
	
	/**
	 * You can set the editable to false on this one but force the children to still be editable
	 */
	private boolean forceChildrenEditable = false;
	private boolean allowNonLocalModification = false;
	
	public ElementTreeItem(Element<?> element, ElementTreeItem parent, boolean isEditable, boolean allowNonLocalModification) {
		this.allowNonLocalModification = allowNonLocalModification;
		this.itemProperty.set(element);
		this.parent = parent;
		editableProperty.set(isEditable);
		refresh();
	}
	
	public boolean isForceChildrenEditable() {
		return forceChildrenEditable;
	}
	public void setForceChildrenEditable(boolean forceChildrenEditable) {
		this.forceChildrenEditable = forceChildrenEditable;
	}

	@Override
	public BooleanProperty editableProperty() {
		return editableProperty;
	}

	@Override
	public ObservableList<TreeItem<Element<?>>> getChildren() {
		return children;
	}
	
	@Override
	public void refresh() {
		leafProperty.set(!(itemProperty.get().getType() instanceof ComplexType));		
		graphicProperty.set(MainController.loadGraphic(StructureGUIManager.getIcon(itemProperty.get().getType(), itemProperty.get().getProperties())));
		if (!leafProperty.get()) {
			TreeUtils.refreshChildren(new TreeItemCreator<Element<?>>() {
				@Override
				public TreeItem<Element<?>> create(TreeItem<Element<?>> parent, Element<?> child) {
					boolean isRemotelyDefined = parent.getParent() != null && itemProperty.get().getType() instanceof DefinedType;
					boolean isLocal = TypeUtils.getLocalChild((ComplexType) itemProperty.get().getType(), child.getName()) != null;
					return new ElementTreeItem(child, (ElementTreeItem) parent, (allowNonLocalModification || (isLocal && !isRemotelyDefined)) && (forceChildrenEditable || editableProperty.get()), allowNonLocalModification);	
				}
			}, this, TypeUtils.getAllChildren((ComplexType) itemProperty.get().getType()));
		}
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

	@Override
	public boolean remove() {
		if (itemProperty().get().getParent() != null && itemProperty().get().getParent() instanceof ModifiableComplexType) {
			ModifiableComplexType type = (ModifiableComplexType) itemProperty().get().getParent();
			type.remove(itemProperty.get());
			if (type.getSuperType() != null && getParent().itemProperty().get() instanceof ModifiableTypeInstance) {
				boolean allInherited = !type.iterator().hasNext();
				// if everything is inherited, replace with actual type
				if (allInherited) {
					((ModifiableTypeInstance) getParent().itemProperty().get()).setType(type.getSuperType());
				}
			}
			return true;
		}
		return false;
	}
}
