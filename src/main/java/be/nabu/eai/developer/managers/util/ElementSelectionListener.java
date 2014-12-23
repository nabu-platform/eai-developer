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
import be.nabu.libs.property.api.Enumerated;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableType;
import be.nabu.libs.types.api.ModifiableTypeInstance;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.properties.ActualTypeProperty;
import be.nabu.libs.types.properties.BaseProperty;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.types.structure.SuperTypeProperty;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import be.nabu.libs.validator.api.Validator;

// TODO: when changing the simple type, remove properties that are not supported!
public class ElementSelectionListener implements ChangeListener<TreeCell<Element<?>>> {
	
	private final MainController controller;
	private boolean updatable;
	private boolean canUpdateType;
	private boolean forceAllowUpdate = false;

	public ElementSelectionListener(MainController controller, boolean updatable) {
		this.controller = controller;
		this.updatable = updatable;
		this.canUpdateType = updatable;
	}
	
	public ElementSelectionListener(MainController controller, boolean updatable, boolean canUpdateType) {
		this.controller = controller;
		this.updatable = updatable;
		this.canUpdateType = canUpdateType;
	}

	public boolean isForceAllowUpdate() {
		return forceAllowUpdate;
	}

	public void setForceAllowUpdate(boolean forceAllowUpdate) {
		this.forceAllowUpdate = forceAllowUpdate;
	}

	@Override
	public void changed(ObservableValue<? extends TreeCell<Element<?>>> arg0, TreeCell<Element<?>> arg1, final TreeCell<Element<?>> newElement) {
		controller.showProperties(new PropertyUpdater() {
			@Override
			public Set<Property<?>> getSupportedProperties() {
				Set<Property<?>> supportedProperties = newElement.getItem().itemProperty().get().getSupportedProperties();
				if (canUpdateType && newElement.getItem().itemProperty().get().getType() instanceof SimpleType) {
					supportedProperties.add(new TypeProperty());
				}
				return supportedProperties;
			}
			@Override
			public Value<?>[] getValues() {
				List<Value<?>> properties = new ArrayList<Value<?>>(Arrays.asList(newElement.getItem().itemProperty().get().getProperties()));
				if (canUpdateType && newElement.getItem().itemProperty().get().getType() instanceof SimpleType) {
					properties.add(new ValueImpl<SimpleType<?>>(new TypeProperty(), (SimpleType<?>) newElement.getItem().itemProperty().get().getType()));
				}
				return properties.toArray(new Value[properties.size()]);
			}
			@Override
			public boolean canUpdate(Property<?> property) {
				if (property.equals(new TypeProperty())) {
					return canUpdateType && (forceAllowUpdate || newElement.getItem().editableProperty().get());
				}
				else {
					return updatable && (forceAllowUpdate || newElement.getItem().editableProperty().get());
				}
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
				else if (property.equals(new TypeProperty())) {
					if (!(newElement.getItem().itemProperty().get() instanceof ModifiableTypeInstance)) {
						return Arrays.asList(new ValidationMessage(Severity.ERROR, "The element can not be modified"));
					}
					else {
						ModifiableTypeInstance modifiable = (ModifiableTypeInstance) newElement.getItem().itemProperty().get();
						SimpleType simpleType = (SimpleType) value;
						modifiable.setType(simpleType);
						// we need to remove any properties that exist in the element but are not compatible with the type
						List<Value<?>> valuesToUnset = new ArrayList<Value<?>>();
						Set<Property<?>> supportedProperties = simpleType.getSupportedProperties(modifiable.getProperties());
						for (Value<?> modifiableValue : modifiable.getProperties()) {
							if (!supportedProperties.contains(modifiableValue.getProperty())) {
								valuesToUnset.add(new ValueImpl(modifiableValue.getProperty(), null));
							}
						}
						modifiable.setProperty(valuesToUnset.toArray(new Value[valuesToUnset.size()]));
					}
				}
				else {
					newElement.getItem().itemProperty().get().setProperty(new ValueImpl(property, value));
				}
				if (newElement.getItem().getParent() != null) {
					newElement.getParent().refresh();
//						((ElementTreeItem) newElement.getItem().getParent()).refresh();
				}
				else {
					newElement.refresh();
				}
				return new ArrayList<ValidationMessage>();
			}
		});
	}
	
	private static class TypeProperty extends BaseProperty<SimpleType<?>> implements Enumerated<SimpleType<?>> {
		@Override
		public String getName() {
			return "simpleType";
		}

		@Override
		public Validator<SimpleType<?>> getValidator() {
			return null;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Class getValueClass() {
			return SimpleType.class;
		}

		@Override
		public Set<SimpleType<?>> getEnumerations() {
			return new ActualTypeProperty().getEnumerations();
		}
	}
}