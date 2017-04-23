package be.nabu.eai.developer.managers.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import be.nabu.libs.property.api.Enumerated;

public class EnumeratedSimpleProperty<T> extends SimpleProperty<T> implements Enumerated<T> {

	private Set<T> enumerations = new LinkedHashSet<T>();
	
	public EnumeratedSimpleProperty(String name, Class<T> clazz, boolean isMandatory) {
		super(name, clazz, isMandatory);
	}

	@Override
	public Set<T> getEnumerations() {
		return enumerations;
	}

	public void addEnumeration(Collection<T> values) {
		enumerations.addAll(values);
	}

	@SuppressWarnings("unchecked")
	public void addAll(T...values) {
		addEnumeration(Arrays.asList(values));
	}
	
	@Override
	public EnumeratedSimpleProperty<T> clone() {
		EnumeratedSimpleProperty<T> property = new EnumeratedSimpleProperty<T>(getName(), getValueClass(), isMandatory());
		property.setInput(isInput());
		property.setPassword(isPassword());
		property.setLarge(isLarge());
		property.setEvaluatable(isEvaluatable());
		property.setFixedList(isFixedList());
		property.setEnvironmentSpecific(isEnvironmentSpecific());
		property.setFilter(getFilter());
		property.addEnumeration(enumerations);
		return property;
	}
}
