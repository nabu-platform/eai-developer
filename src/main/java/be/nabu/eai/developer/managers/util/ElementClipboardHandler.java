package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.StructureGUIManager;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.clipboard.ClipboardHandler;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.types.DefinedTypeResolverFactory;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.SimpleElementImpl;

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
		List<Element<?>> elements = new ArrayList<Element<?>>();
		for (TreeCell<Element<?>> entry : tree.getSelectionModel().getSelectedItems()) {
			elements.add(entry.getItem().itemProperty().get());
		}
		return MainController.buildClipboard(elements.toArray());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setClipboard(Clipboard arg0) {
		if (allowPaste && !tree.getSelectionModel().getSelectedItems().isEmpty()) {
			TreeCell<Element<?>> target = tree.getSelectionModel().getSelectedItems().get(0);
			ComplexType parent = target.getItem().itemProperty().get().getType() instanceof ComplexType 
				? (ComplexType) target.getItem().itemProperty().get().getType()
				: target.getItem().itemProperty().get().getParent();
			if (parent instanceof ModifiableComplexType) {
				// let's check if there is a simple type element
				String typeName = (String) arg0.getContent(TreeDragDrop.getDataFormat(StructureGUIManager.DATA_TYPE_DEFINED));
				// for now the element selection also boils down to the type
				if (typeName == null) {
					typeName = (String) arg0.getContent(TreeDragDrop.getDataFormat(StructureGUIManager.DATA_TYPE_ELEMENT));
				}
				if (typeName != null) {
					DefinedType type = DefinedTypeResolverFactory.getInstance().getResolver().resolve(typeName);
					if (type != null) {
						String elementName = StructureGUIManager.UNNAMED + StructureGUIManager.getLastCounter(parent);
						if (type instanceof ComplexType) {
							((ModifiableComplexType) parent).add(new ComplexElementImpl(elementName, (ComplexType) type, parent));
						}
						else if (type instanceof SimpleType) {
							((ModifiableComplexType) parent).add(new SimpleElementImpl(elementName, (SimpleType) type, parent));
						}
						if (target.getParent() != null) {
							target.getParent().refresh();
						}
						else {
							target.refresh();
						}
					}
				}
			}
		}
	}
}
