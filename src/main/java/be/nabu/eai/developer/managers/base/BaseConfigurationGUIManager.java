package be.nabu.eai.developer.managers.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.java.BeanResolver;
import be.nabu.libs.types.java.BeanType;
import be.nabu.libs.types.properties.NillableProperty;

abstract public class BaseConfigurationGUIManager<T extends Artifact, C> extends BasePropertyOnlyGUIManager<T, BaseArtifactGUIInstance<T>> {

	private BeanType<C> beanType;
	private List<Property<?>> properties;

	public BaseConfigurationGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager, Class<C> configurationClass) {
		super(name, artifactClass, artifactManager);
		load(configurationClass);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void load(Class<C> configurationClass) {
		beanType = (BeanType<C>) BeanResolver.getInstance().resolve(configurationClass);
		properties = new ArrayList<Property<?>>();
		for (Element<?> element : TypeUtils.getAllChildren(beanType)) {
			if (element.getType() instanceof ComplexType) {
				// TODO
			}
			else {
				Value<Boolean> property = element.getProperty(new NillableProperty());
				properties.add(new SimpleProperty(
					element.getName(), 
					((SimpleType<?>) element.getType()).getInstanceClass(),
					property != null && !property.getValue()
				));
			}
		}
	}

	@Override
	public Collection<Property<?>> getModifiableProperties(T instance) {
		return properties;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> V getValue(T instance, Property<V> property) {
		ComplexContent content = new BeanInstance<C>(getConfiguration(instance));
		return (V) content.get(property.getName());
	}

	@Override
	public <V> void setValue(T instance, Property<V> property, V value) {
		ComplexContent content = new BeanInstance<C>(getConfiguration(instance));
		content.set(property.getName(), value);
	}

	@Override
	protected BaseArtifactGUIInstance<T> newGUIInstance(ResourceEntry entry) {
		return new BaseArtifactGUIInstance<T>(getArtifactManager(), entry);
	}

	@Override
	protected void setInstance(BaseArtifactGUIInstance<T> guiInstance, T instance) {
		guiInstance.setArtifact(instance);
	}
	
	@Override
	protected void setEntry(BaseArtifactGUIInstance<T> guiInstance, ResourceEntry entry) {
		guiInstance.setEntry(entry);
	}
	
	abstract public C getConfiguration(T instance);
}
