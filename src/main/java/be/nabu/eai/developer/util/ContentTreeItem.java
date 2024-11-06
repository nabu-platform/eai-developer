/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package be.nabu.eai.developer.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import be.nabu.eai.developer.MainController;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.java.BeanType;

public class ContentTreeItem implements TreeItem<Object> {

	private ContentTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Object> itemProperty = new SimpleObjectProperty<Object>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Object>> children = FXCollections.observableArrayList();
	private Element<?> definition;
	private Object index;
	private ComplexType complexType;
	
	@SuppressWarnings("unchecked")
	public ContentTreeItem(Element<?> definition, Object content, ContentTreeItem parent, boolean isEditable, Object index) {
		this.definition = definition;
		this.index = index;
		this.complexType = definition.getType() instanceof ComplexType ? (ComplexType) definition.getType() : null;
		// if we have a complex type definition that is an object, check what it is at runtime
		if (this.complexType instanceof BeanType && ((BeanType<?>) this.complexType).getBeanClass().equals(Object.class) && content != null) {
			ComplexContent complexContent;
			if (content instanceof ComplexContent) {
				complexContent = (ComplexContent) content;
			}
			else {
				complexContent = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(content);
			}
			if (complexContent instanceof ComplexContent) {
				content = complexContent;
				this.complexType = ((ComplexContent) complexContent).getType();
			}
		}
		this.itemProperty.set(content);
		this.parent = parent;
		editableProperty.set(isEditable);
		refresh();
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void refresh() {
		leafProperty.set(!(definition.getType() instanceof ComplexType));		
		graphicProperty.set(MainController.loadGraphic(ElementTreeItem.getIcon(definition.getType(), definition.getProperties())));
		if (definition.getType() instanceof ComplexType) {
			children.clear();
			ComplexContent complexContent = getComplexContent();
			for (Element<?> child : TypeUtils.getAllChildren(complexType)) {
				Object value = complexContent.get(child.getName());
				if (value == null && !child.getName().startsWith("@") && child instanceof Attribute) {
					value = complexContent.get("@" + child.getName());
				}
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
	
	@SuppressWarnings({ "unchecked" })
	private ComplexContent getComplexContent() {
		if (itemProperty().get() instanceof ComplexContent) {
			return (ComplexContent) itemProperty().get();
		}
		else {
			return ComplexContentWrapperFactory.getInstance().getWrapper().wrap(itemProperty().get());
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
