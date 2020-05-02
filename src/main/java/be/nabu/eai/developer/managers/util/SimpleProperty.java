package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.api.EnvironmentAwareProperty;
import be.nabu.eai.developer.api.EvaluatableProperty;
import be.nabu.eai.developer.api.MandatoryProperty;
import be.nabu.libs.property.api.Filter;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.validator.api.Validator;

public class SimpleProperty<T> implements Property<T>, Filter<T>, EvaluatableProperty<T>, EnvironmentAwareProperty<T>, MandatoryProperty<T> {

	private String name;
	private Class<T> clazz;
	private boolean isMandatory;
	private Filter<T> filter;
	private boolean isList, isFixedList, isPassword, isLarge, isAdvanced, isDisableSuggest;
	public String title, description;
	private boolean evaluatable;
	private boolean environmentSpecific;
	private String show, hide;
	private HiddenCalculator hiddenCalculator;
	
	public static interface HiddenCalculator {
		public boolean isHidden();
	}
	
	private List<Value<?>> additional = new ArrayList<Value<?>>();
	
	/**
	 * Added for example for file selection, the file can be selected for input or output
	 * By default it assumes "output" for legacy reasons
	 */
	private boolean isInput;

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
	public boolean isInput() {
		return isInput;
	}
	public void setInput(boolean isInput) {
		this.isInput = isInput;
	}
	public boolean isPassword() {
		return isPassword;
	}
	public void setPassword(boolean isPassword) {
		this.isPassword = isPassword;
	}
	public boolean isLarge() {
		return isLarge;
	}
	public void setLarge(boolean isLarge) {
		this.isLarge = isLarge;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SimpleProperty<T> clone() {
		SimpleProperty<T> property = new SimpleProperty<T>(name, clazz, isMandatory);
		property.setInput(isInput);
		property.setPassword(isPassword);
		property.setLarge(isLarge);
		property.setEvaluatable(evaluatable);
		property.setFixedList(isFixedList);
		property.setEnvironmentSpecific(environmentSpecific);
		property.setFilter(filter);
		property.setDisableSuggest(isDisableSuggest);
		return property;
	}
	public List<Value<?>> getAdditional() {
		return additional;
	}
	public void setAdditional(List<Value<?>> additional) {
		this.additional = additional;
	}
	public boolean isAdvanced() {
		return isAdvanced;
	}
	public void setAdvanced(boolean isAdvanced) {
		this.isAdvanced = isAdvanced;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isDisableSuggest() {
		return isDisableSuggest;
	}
	public void setDisableSuggest(boolean isDisableSuggest) {
		this.isDisableSuggest = isDisableSuggest;
	}
	public boolean isHidden() {
		return hiddenCalculator != null && hiddenCalculator.isHidden();
	}
	public HiddenCalculator getHiddenCalculator() {
		return hiddenCalculator;
	}
	public void setHiddenCalculator(HiddenCalculator hiddenCalculator) {
		this.hiddenCalculator = hiddenCalculator;
	}
	public String getShow() {
		return show;
	}
	public void setShow(String show) {
		this.show = show;
	}
	public String getHide() {
		return hide;
	}
	public void setHide(String hide) {
		this.hide = hide;
	}
}