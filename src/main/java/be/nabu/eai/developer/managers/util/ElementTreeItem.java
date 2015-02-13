package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.StructureGUIManager;
import be.nabu.jfx.control.tree.MovableTreeItem;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.TreeUtils.TreeItemCreator;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.services.vm.HiddenProperty;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.ModifiableTypeInstance;
import be.nabu.libs.types.properties.MinOccursProperty;

public class ElementTreeItem implements RemovableTreeItem<Element<?>>, MovableTreeItem<Element<?>> {

	private ElementTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Element<?>> itemProperty = new SimpleObjectProperty<Element<?>>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Element<?>>> children;
	
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
		refresh(false);
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
		if (children == null) {
			children = FXCollections.observableArrayList();
			refresh(true);
		}
		return children;
	}
	
	@Override
	public void refresh() {
		refresh(true);
	}

	private void refresh(boolean includeChildren) {
		leafProperty.set(!(itemProperty.get().getType() instanceof ComplexType));		
		HBox graphicBox = new HBox();
		graphicBox.getChildren().add(MainController.loadGraphic(StructureGUIManager.getIcon(itemProperty.get().getType(), itemProperty.get().getProperties())));
		Integer minOccurs = ValueUtils.getValue(new MinOccursProperty(), itemProperty.get().getProperties());
		if (minOccurs == null || minOccurs > 0) {
			graphicBox.getChildren().add(MainController.loadGraphic("types/mandatory.png"));
		}
		else {
			graphicBox.getChildren().add(MainController.loadGraphic("types/optional.png"));
		}
		graphicProperty.set(graphicBox);
		if (!leafProperty.get() && includeChildren) {
			TreeUtils.refreshChildren(new TreeItemCreator<Element<?>>() {
				@Override
				public TreeItem<Element<?>> create(TreeItem<Element<?>> parent, Element<?> child) {
					boolean isRemotelyDefined = parent.getParent() != null && itemProperty.get().getType() instanceof DefinedType;
					boolean isLocal = TypeUtils.getLocalChild((ComplexType) itemProperty.get().getType(), child.getName()) != null;
					return new ElementTreeItem(child, (ElementTreeItem) parent, (allowNonLocalModification || (isLocal && !isRemotelyDefined)) && (forceChildrenEditable || editableProperty.get()), allowNonLocalModification);	
				}
			}, this, filterTemporary(TypeUtils.getAllChildren((ComplexType) itemProperty.get().getType())));
		}
	}
	
	private Collection<Element<?>> filterTemporary(Collection<Element<?>> children) {
		Iterator<Element<?>> iterator = children.iterator();
		while (iterator.hasNext()) {
			Element<?> next = iterator.next();
			Boolean value = ValueUtils.getValue(new HiddenProperty(), next.getProperties());
			if (value != null && value) {
				iterator.remove();
			}
		}
		return children;
	}

	@Override
	public String getName() {
		return (itemProperty().get() instanceof Attribute ? "@" : "") + itemProperty.get().getName();
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

	@Override
	public void move(be.nabu.jfx.control.tree.MovableTreeItem.Direction direction) {
		if (editableProperty().get() && itemProperty().get().getParent() instanceof ModifiableComplexType) {
			switch(direction) {
				case UP:
				case DOWN:
					// get the local elements
					ModifiableComplexType target = (ModifiableComplexType) itemProperty().get().getParent();
					Iterator<Element<?>> iterator = target.iterator();
					List<Element<?>> currentChildren = new ArrayList<Element<?>>();
					while (iterator.hasNext()) {
						Element<?> next = iterator.next();
						if (TypeUtils.getLocalChild(target, next.getName()) != null) {
							currentChildren.add(next);
							iterator.remove();
						}
					}
					int index = currentChildren.indexOf(itemProperty().get());
					if (direction == Direction.UP && index > 0) {
						currentChildren.remove(index);
						currentChildren.add(index - 1, itemProperty().get());
					}
					else if (direction == Direction.DOWN && index < currentChildren.size() - 1) {
						currentChildren.remove(index);
						currentChildren.add(index + 1, itemProperty().get());
					}
					for (Element<?> element : currentChildren) {
						target.add(element);
					}
					getParent().refresh();
				break;
				default:
					// do nothing
			}
		}
	}
}
