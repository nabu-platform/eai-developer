package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.managers.StructureGUIManager;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableType;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.types.structure.SuperTypeProperty;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class ElementSelectionListener implements ChangeListener<TreeCell<Element<?>>> {
	
	private final MainController controller;
	private boolean updatable;

	public ElementSelectionListener(MainController controller, boolean updatable) {
		this.controller = controller;
		this.updatable = updatable;
	}

	@Override
	public void changed(ObservableValue<? extends TreeCell<Element<?>>> arg0, TreeCell<Element<?>> arg1, final TreeCell<Element<?>> newElement) {
		controller.showProperties(new PropertyUpdater() {
			@Override
			public Set<Property<?>> getSupportedProperties() {
				return newElement.getItem().itemProperty().get().getSupportedProperties();
			}
			@Override
			public Value<?>[] getValues() {
				return newElement.getItem().itemProperty().get().getProperties();
			}
			@Override
			public boolean canUpdate(Property<?> property) {
				return updatable;
			}
			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
				if (property.equals(new NameProperty())) {
					StructureGUIManager.rename(controller, newElement.getItem(), (String) value);
				}
				// need to set this on the type instead of on the element
				else if (property.equals(new SuperTypeProperty())) {
					if (!(newElement.getItem().itemProperty().get().getType() instanceof ModifiableType)) {
						return Arrays.asList(new ValidationMessage(Severity.ERROR, "The type can not be modified"));
					}
					else {
						((ModifiableType) newElement.getItem().itemProperty().get().getType()).setProperty(new ValueImpl(property, value));
					}
				}
				else {
					newElement.getItem().itemProperty().get().setProperty(new ValueImpl(property, value));
				}
				if (newElement.getItem().getParent() != null) {
					newElement.getParent().refresh();
//						((ElementTreeItem) newElement.getItem().getParent()).refresh();
				}
				return new ArrayList<ValidationMessage>();
			}
		});
	}
}