package be.nabu.eai.developer.managers.util;

import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.types.structure.Structure;

public class RootElementWithPush extends RootElement {

	public RootElementWithPush(Structure type, boolean allowNameChange, Value<?>...values) {
		super(type);
		setProperty(values);
		if (!allowNameChange) {
			getBlockedProperties().add(new NameProperty());
		}
	}

	@Override
	public Value<?>[] getProperties() {
		return getType().getProperties();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <S> Value<S> getProperty(Property<S> property) {
		for (Value<?> value : getProperties()) {
			if (value.getProperty().equals(property)) {
				return (Value<S>) value;
			}
		}
		return null;
	}

	@Override
	public void setProperty(Value<?>...properties) {
		if (properties.length > 0) {
			((Structure) getType()).setProperty(properties);
		}
	}
	
}
