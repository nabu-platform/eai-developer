package be.nabu.eai.developer.api;

import be.nabu.libs.property.api.Property;

public interface EnvironmentAwareProperty<T> extends Property<T> {
	public boolean isEnvironmentSpecific();
}
