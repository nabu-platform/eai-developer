package be.nabu.eai.developer.managers.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import be.nabu.libs.property.api.Enumerated;

public class EnumeratedSimpleProperty<T> extends SimpleProperty<T> implements Enumerated<T> {

	private Set<T> enumerations = new HashSet<T>();
	
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
}
