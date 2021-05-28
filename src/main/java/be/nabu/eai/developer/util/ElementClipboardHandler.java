package be.nabu.eai.developer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ClipboardProvider;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.clipboard.ClipboardHandler;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.property.PropertyFactory;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.StringMapCollectionHandlerProvider;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.properties.CollectionHandlerProviderProperty;

public class ElementClipboardHandler implements ClipboardHandler {

	private Tree<Element<?>> tree;
	private boolean allowPaste;

	public ElementClipboardHandler(Tree<Element<?>> tree) {
		this(tree, true);
	}
	
	public ElementClipboardHandler(Tree<Element<?>> tree, boolean allowPaste) {
		this.tree = tree;
		this.allowPaste = allowPaste;
	}
	
	@Override
	public ClipboardContent getContent() {
		List<TreeItem<Element<?>>> elements = new ArrayList<TreeItem<Element<?>>>();
		for (TreeCell<Element<?>> entry : tree.getSelectionModel().getSelectedItems()) {
			elements.add(entry.getItem());
		}
		return MainController.buildClipboard(elements.toArray());
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void setClipboard(Clipboard arg0) {
		if (allowPaste && !tree.getSelectionModel().getSelectedItems().isEmpty()) {
			TreeCell<Element<?>> target = tree.getSelectionModel().getSelectedItems().get(0);
			ComplexType parent = target.getItem().itemProperty().get().getType() instanceof ComplexType 
				? (ComplexType) target.getItem().itemProperty().get().getType()
				: target.getItem().itemProperty().get().getParent();
			if (parent instanceof ModifiableComplexType) {
				// if we can't edit it, don't paste...
				if (target.getItem() instanceof ElementTreeItem) {
					if (!((ElementTreeItem) target.getItem()).editableProperty().get()) {
						return;
					}
				}
				boolean refresh = false;
				List<Map<String, Object>> elements = (List<Map<String, Object>>) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_SERIALIZED_ELEMENT_LIST));
				if (elements != null && !elements.isEmpty()) {
					refresh = true;
					for (Map<String, Object> properties : elements) {
						String typeName = (String) properties.get("$type");
						if (typeName == null) {
							typeName = ElementTreeItem.UNNAMED;
						}
						addElement(parent, properties, typeName);
					}
				}
				else {
					// first check if we have a defined type
					Map<String, Object> properties = (Map<String, Object>) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_SERIALIZED_ELEMENT));
					// let's check if there is a simple type element
					String typeName = properties == null ? (String) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_DEFINED)) : (String) properties.get("$type");
					// for now the element selection also boils down to the type
					if (typeName == null) {
						typeName = (String) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_ELEMENT));
					}
					// try to get the id
					if (typeName == null) {
						typeName = (String) arg0.getContent(DataFormat.PLAIN_TEXT);
					}
					if (typeName != null && addElement(parent, properties, typeName)) {
						refresh = true;
					}
					// if we could not find a typename, check for other ways
					else {
						for (ClipboardProvider<?> potential : MainController.getInstance().getClipboardProviders()) {
							if (Type.class.isAssignableFrom(potential.getClipboardClass())) {
								Object content = arg0.getContent(TreeDragDrop.getDataFormat(potential.getDataType()));
								if (content instanceof String) {
									content = potential.deserialize(content.toString());
									String nameToUse = content instanceof Type ? ((Type) content).getName() : "unnamed";
									String name = nameToUse + ElementTreeItem.getLastCounter(parent, nameToUse);
									if (content instanceof ComplexType) {
										((ModifiableComplexType) parent).add(new ComplexElementImpl(name, (ComplexType) content, parent));
										refresh = true;
									}
									else if (content instanceof SimpleType) {
										((ModifiableComplexType) parent).add(new SimpleElementImpl(name, (SimpleType<?>) content, parent));
										refresh = true;
									}
								}
							}
						}
					}
				}
				if (refresh) {
					if (target.getParent() != null) {
						target.getParent().expandedProperty().set(true);
						target.getParent().refresh();
					}
					else {
						target.expandedProperty().set(true);
						target.refresh();
					}
					MainController.getInstance().setChanged();
				}
			}
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean addElement(ComplexType parent, Map<String, Object> properties, String typeName) {
		DefinedType type = DefinedTypeResolverFactory.getInstance().getResolver().resolve(typeName);
		if (type != null) {
			Element<?> element = null;
			String elementName = properties == null || properties.get("name") == null ? ElementTreeItem.UNNAMED : (String) properties.get("name");
			if (parent.get(elementName) != null) {
				elementName += ElementTreeItem.getLastCounter(parent, elementName);
			}
			if (type instanceof ComplexType) {
				element = new ComplexElementImpl(elementName, (ComplexType) type, parent);
			}
			else if (type instanceof SimpleType) {
				element = new SimpleElementImpl(elementName, (SimpleType) type, parent);
			}
			if (element != null) {
				if ("java.util.Map".equals(typeName)) {
					element.setProperty(new ValueImpl(CollectionHandlerProviderProperty.getInstance(), new StringMapCollectionHandlerProvider()));
					// remove any max occurs definition
					if (properties != null) {
						properties.remove("maxOccurs");
					}
				}
				if (properties != null) {
					for (String key : properties.keySet()) {
						if ("name".equals(key)) {
							continue;
						}
						Property<?> property = PropertyFactory.getInstance().getProperty(key);
						if (property instanceof CollectionHandlerProviderProperty) {
							if ("stringMap".equals(properties.get(key))) {
								element.setProperty(new ValueImpl(CollectionHandlerProviderProperty.getInstance(), new StringMapCollectionHandlerProvider()));
							}
						}
						else if (property != null) {
							element.setProperty(new ValueImpl(property, properties.get(key)));
						}
					}
				}
				((ModifiableComplexType) parent).add(element);
			}
			return true;
		}
		return false;
	}
}
