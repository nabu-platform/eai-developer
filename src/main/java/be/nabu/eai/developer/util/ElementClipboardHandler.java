package be.nabu.eai.developer.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import be.nabu.eai.developer.MainController;
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
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.ValueImpl;

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
				boolean refresh = false;
				List<Map<String, Object>> elements = (List<Map<String, Object>>) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_SERIALIZED_ELEMENT_LIST));
				if (elements != null && !elements.isEmpty()) {
					for (Map<String, Object> properties : elements) {
						String typeName = (String) properties.get("$type");
						if (typeName == null) {
							typeName = ElementTreeItem.UNNAMED;
						}
						addElement(parent, properties, typeName);
					}
					refresh = true;
				}
				// old way...
				else {
					Map<String, Object> properties = (Map<String, Object>) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_SERIALIZED_ELEMENT));
					// let's check if there is a simple type element
					String typeName = properties == null ? (String) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_DEFINED)) : (String) properties.get("$type");
					// for now the element selection also boils down to the type
					if (typeName == null) {
						typeName = (String) arg0.getContent(TreeDragDrop.getDataFormat(ElementTreeItem.DATA_TYPE_ELEMENT));
					}
					if (typeName != null) {
						addElement(parent, properties, typeName);
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
	private void addElement(ComplexType parent, Map<String, Object> properties, String typeName) {
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
				if (properties != null) {
					for (String key : properties.keySet()) {
						if ("name".equals(key)) {
							continue;
						}
						Property<?> property = PropertyFactory.getInstance().getProperty(key);
						if (property != null) {
							element.setProperty(new ValueImpl(property, properties.get(key)));
						}
					}
				}
				((ModifiableComplexType) parent).add(element);
			}
		}
	}
}
