package be.nabu.eai.developer.api;

import be.nabu.libs.property.api.Property;

public interface EvaluatableProperty<T> extends Property<T> {
	public boolean isEvaluatable();
}
