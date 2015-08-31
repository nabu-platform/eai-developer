package be.nabu.eai.developer.managers.util;

import java.util.Collection;

import be.nabu.libs.property.api.Filter;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.validator.api.Validator;

public class SimpleProperty<T> implements Property<T>, Filter<T> {

	private String name;
	private Class<T> clazz;
	private boolean isMandatory;
	private Filter<T> filter;

	public SimpleProperty(String name, Class<T> clazz, boolean isMandatory) {
		this.name = name;
		this.clazz = clazz;
		this.isMandatory = isMandatory;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Validator<T> getValidator() {
		return null;
	}

	@Override
	public Class<T> getValueClass() {
		return clazz;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object object) {
		return object != null
			&& object instanceof SimpleProperty	
			&& clazz.isAssignableFrom(((SimpleProperty<T>) object).getValueClass()) 
			&& ((SimpleProperty<T>) object).name.equals(name);
	}
	@Override
	public int hashCode() {
		return name.hashCode();
	}
	
	@Override
	public String toString() {
		return name;
	}
	public boolean isMandatory() {
		return isMandatory;
	}
	
	@Override
	public Collection<T> filter(Collection<T> collection) {
		return filter == null ? collection : filter.filter(collection);
	}
	public Filter<T> getFilter() {
		return filter;
	}
	public void setFilter(Filter<T> filter) {
		this.filter = filter;
	}
}