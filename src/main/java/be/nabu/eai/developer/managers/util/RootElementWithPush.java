package be.nabu.eai.developer.managers.util;

import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.types.properties.ValidateProperty;

public class RootElementWithPush extends RootElement {

	// you can push properties set on this root element to another target
	private Element<?> pushTarget;
	
	public RootElementWithPush(ComplexType type, boolean allowNameChange, Value<?>...values) {
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
		// push them in the element as well to reset any caches
		super.setProperty(properties);
		if (properties.length > 0) {
			for (Value<?> value : properties) {
				if (getType() instanceof ModifiableComplexType && !ValidateProperty.getInstance().equals(value.getProperty())) {
					((ModifiableComplexType) getType()).setProperty(value);
				}
			}
			// this was added for the restrict property
			// we wrap a rootelementwithpush around the input _type_, ignoring the original wrapper element that wraps the input type in the pipeline
			// if we just write the property to the type (which is what the push did), it does not get persisted however
			// if we just push it to the element, it does not take effect
			// so we need to push it to both
			if (pushTarget != null) {
				pushTarget.setProperty(properties);
			}
		}
	}

	public Element<?> getPushTarget() {
		return pushTarget;
	}

	public void setPushTarget(Element<?> pushTarget) {
		this.pushTarget = pushTarget;
	}
}
