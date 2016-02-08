package be.nabu.eai.developer.util;

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
import be.nabu.jfx.control.tree.MovableTreeItem;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.TreeUtils.TreeItemCreator;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.ModifiableTypeInstance;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.StringMapCollectionHandlerProvider;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.java.BeanType;
import be.nabu.libs.types.properties.CollectionHandlerProviderProperty;
import be.nabu.libs.types.properties.HiddenProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class ElementTreeItem implements RemovableTreeItem<Element<?>>, MovableTreeItem<Element<?>> {
	
	public static final String UNNAMED = "unnamed";
	public static final String DATA_TYPE_DEFINED = "type";
	public static final String DATA_TYPE_ELEMENT = "element";

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

	public static String getIcon(Type type, Value<?>...values) {
		// shortcut for maps
		CollectionHandlerProvider<?, ?> value = ValueUtils.getValue(new CollectionHandlerProviderProperty(), values);
		if (value != null && value.getClass().equals(StringMapCollectionHandlerProvider.class)) {
			return "types/map.gif";
		}
		
		String image;
		if (type instanceof SimpleType) {
			SimpleType<?> simpleType = (SimpleType<?>) type;
			String simpleName = simpleType.getInstanceClass().equals(byte[].class) 
				? "bytes" 
				: simpleType.getInstanceClass().getSimpleName().toLowerCase();
			image = "types/" + simpleName + ".gif";
		}
		else {
			if (type instanceof BeanType && ((BeanType<?>) type).getBeanClass().equals(Object.class)) {
				image = "types/object.gif";
			}
			else if (type.getSuperType() != null) {
				image = "types/structureextension.gif";
			}
			else {
				image = type instanceof DefinedType ? "types/definedstructure.gif" : "types/structure.gif";
			}
		}
		Integer maxOccurs = ValueUtils.getValue(new MaxOccursProperty(), values);
		if (maxOccurs != null && maxOccurs != 1) {
			image = image.replace(".gif", "list.gif");
		}
		return image;
	}
	
	private void refresh(boolean includeChildren) {
		leafProperty.set(!(itemProperty.get().getType() instanceof ComplexType) || (itemProperty.get().getType() instanceof BeanType && ((BeanType<?>) itemProperty.get().getType()).getBeanClass().equals(Object.class)));		
		HBox graphicBox = new HBox();
		graphicBox.getChildren().add(MainController.loadGraphic(getIcon(itemProperty.get().getType(), itemProperty.get().getProperties())));
		Integer minOccurs = ValueUtils.contains(MinOccursProperty.getInstance(), itemProperty.get().getProperties()) 
			? ValueUtils.getValue(MinOccursProperty.getInstance(), itemProperty.get().getProperties()) 
			: null;
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
			Boolean value = ValueUtils.getValue(HiddenProperty.getInstance(), next.getProperties());
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
			MainController.getInstance().setChanged();
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
					MainController.getInstance().setChanged();
				break;
				default:
					// do nothing
			}
		}
	}
	
	public static boolean rename(MainController controller, TreeItem<Element<?>> cell, String name) {
		if (!cell.itemProperty().get().getSupportedProperties().contains(new NameProperty())) {
			controller.notify(new ValidationMessage(Severity.ERROR, "Can not update this name"));
		}
		else if (!isValidName(name)) {
			controller.notify(new ValidationMessage(Severity.ERROR, "The name '" + name + "' is not a valid field name"));
		}
		else {
			Element<?> existingChild = cell.getParent() == null ? null : ((ComplexType)(cell.getParent().itemProperty().get().getType())).get(name);
			if (existingChild == null) {
				cell.itemProperty().get().setProperty(new ValueImpl<String>(new NameProperty(), name));
				controller.setChanged();
				return true;
			}
			else {
				controller.notify(new ValidationMessage(Severity.ERROR, "There is already an element with the name " + name));
			}
		}
		return false;
	}
	
	
	private static boolean isValidName(String name) {
		// the full name must be a word and the first character must be a letter
		return name.matches("^[\\w]+$") && name.substring(0, 1).matches("[a-zA-Z]");
	}
	
	public static int getLastCounter(ComplexType type) {
		int last = -1;
		for (Element<?> child : TypeUtils.getAllChildren(type)) {
			if (child.getName().matches("^" + UNNAMED + "[0-9]+$")) {
				int childNumber = new Integer(child.getName().replace(UNNAMED, ""));
				if (childNumber > last) {
					last = childNumber;
				}
			}
		}
		return last + 1;
	}
}
