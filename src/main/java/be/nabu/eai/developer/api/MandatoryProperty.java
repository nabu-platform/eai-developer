package be.nabu.eai.developer.api;

import be.nabu.libs.property.api.Property;

public interface MandatoryProperty<T> extends Property<T> {
	public boolean isMandatory();
}
