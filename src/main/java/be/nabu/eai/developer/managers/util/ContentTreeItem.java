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
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.java.BeanInstance;

public class ContentTreeItem implements TreeItem<Object> {

	private ContentTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Object> itemProperty = new SimpleObjectProperty<Object>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Object>> children = FXCollections.observableArrayList();
	private Element<?> definition;
	private Object index;
	
	public ContentTreeItem(Element<?> definition, Object content, ContentTreeItem parent, boolean isEditable, Object index) {
		this.definition = definition;
		this.index = index;
		this.itemProperty.set(content);
		this.parent = parent;
		editableProperty.set(isEditable);
		refresh();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void refresh() {
		leafProperty.set(!(definition.getType() instanceof ComplexType));		
		graphicProperty.set(MainController.loadGraphic(StructureGUIManager.getIcon(definition.getType(), definition.getProperties())));
		if (!leafProperty.get()) {
			children.clear();
			for (Element<?> child : TypeUtils.getAllChildren((ComplexType) definition.getType())) {
				Object value = getComplexContent().get(child.getName());
				if (value != null) {
					if (child.getType().isList(child.getProperties())) {
						CollectionHandlerProvider collectionHandler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
						for (Object index : collectionHandler.getIndexes(value)) {
							Object childValue = collectionHandler.get(value, index);
							if (childValue != null) {
								children.add(new ContentTreeItem(child, childValue, this, false, index));
							}
						}
					}
					else {
						children.add(new ContentTreeItem(child, value, this, false, null));
					}
				}
			}
		}
	}
	
	@SuppressWarnings("rawtypes")
	private ComplexContent getComplexContent() {
		if (itemProperty().get() instanceof ComplexContent) {
			return (ComplexContent) itemProperty().get();
		}
		else {
			return new BeanInstance(itemProperty().get());
		}
	}

	@Override
	public BooleanProperty editableProperty() {
		return editableProperty();
	}

	@Override
	public ObservableList<TreeItem<Object>> getChildren() {
		return children;
	}

	@Override
	public String getName() {
		return definition.getName() + (index != null ? "[" + index.toString() + "]" : "");
	}

	@Override
	public TreeItem<Object> getParent() {
		return parent;
	}

	@Override
	public ObjectProperty<Node> graphicProperty() {
		return graphicProperty;
	}

	@Override
	public ObjectProperty<Object> itemProperty() {
		return itemProperty;
	}

	@Override
	public BooleanProperty leafProperty() {
		return leafProperty;
	}

	public Element<?> getDefinition() {
		return definition;
	}
}
