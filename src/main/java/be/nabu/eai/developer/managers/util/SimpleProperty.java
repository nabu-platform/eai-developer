package be.nabu.eai.developer.managers.util;

import java.util.Collection;

import be.nabu.eai.developer.api.EnvironmentAwareProperty;
import be.nabu.eai.developer.api.EvaluatableProperty;
import be.nabu.eai.developer.api.MandatoryProperty;
import be.nabu.libs.property.api.Filter;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.validator.api.Validator;

public class SimpleProperty<T> implements Property<T>, Filter<T>, EvaluatableProperty<T>, EnvironmentAwareProperty<T>, MandatoryProperty<T> {

	private String name;
	private Class<T> clazz;
	private boolean isMandatory;
	private Filter<T> filter;
	private boolean isList, isFixedList;
	private boolean evaluatable;
	private boolean environmentSpecific;

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
	public boolean isList() {
		return isList;
	}
	public void setList(boolean isList) {
		this.isList = isList;
	}
	public boolean isFixedList() {
		return isFixedList;
	}
	public void setFixedList(boolean isFixedList) {
		this.isFixedList = isFixedList;
	}
	
	@Override
	public boolean isEvaluatable() {
		return evaluatable;
	}
	public void setEvaluatable(boolean evaluatable) {
		this.evaluatable = evaluatable;
	}
	
	@Override
	public boolean isEnvironmentSpecific() {
		return environmentSpecific;
	}
	public void setEnvironmentSpecific(boolean environmentSpecific) {
		this.environmentSpecific = environmentSpecific;
	}
	public void setMandatory(boolean isMandatory) {
		this.isMandatory = isMandatory;
	}
	
}