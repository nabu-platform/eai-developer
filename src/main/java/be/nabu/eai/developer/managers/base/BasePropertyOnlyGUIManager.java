package be.nabu.eai.developer.managers.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.ListChangeListener;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Repository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;

abstract public class BasePropertyOnlyGUIManager<T extends Artifact, I extends ArtifactGUIInstance> extends BasePortableGUIManager<T, I> {

	private SimplePropertyUpdater propertyUpdater;
	
	public BasePropertyOnlyGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager) {
		super(name, artifactClass, artifactManager);
	}

	@Override
	public void display(MainController controller, AnchorPane pane, T instance) {
		ScrollPane scroll = new ScrollPane();
		AnchorPane.setBottomAnchor(scroll, 0d);
		AnchorPane.setTopAnchor(scroll, 0d);
		AnchorPane.setLeftAnchor(scroll, 0d);
		AnchorPane.setRightAnchor(scroll, 0d);
		AnchorPane scrollRoot = new AnchorPane();
		// this does not work to autosize the anchorpane
//		scrollRoot.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
		scrollRoot.prefWidthProperty().bind(scroll.widthProperty());
		scroll.setContent(scrollRoot);
		display(instance, scrollRoot);
		pane.getChildren().add(scroll);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void display(T instance, Pane pane) {
		ListChangeListener<Value<?>> listChangeListener = new ListChangeListener<Value<?>>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Value<?>> change) {
				while (change.next()) {
					if (change.wasRemoved()) {
						for (Value<?> value : change.getRemoved()) {
							setValue(instance, value.getProperty(), null);
						}
					}
					if (change.wasAdded()) {
						for (Value value : change.getAddedSubList()) {
							setValue(instance, value.getProperty(), value.getValue());
						}
					}
					if (change.wasUpdated() || change.wasReplaced()) {
						for (Value value : change.getList()) {
							setValue(instance, value.getProperty(), value.getValue());
						}
					}
				}
			}
		};
		Accordion accordion = new Accordion();
		
		AnchorPane basicPane = new AnchorPane();
		basicPane.getStyleClass().add("configuration-pane");
		basicPane.getStyleClass().add("configuration-pane-basic");
		TitledPane basic = new TitledPane("Basic Configuration", basicPane);
		accordion.getPanes().add(basic);
		showProperties(instance, basicPane, listChangeListener, false);
		
		AnchorPane advancedPane = new AnchorPane();
		advancedPane.getStyleClass().add("configuration-pane");
		advancedPane.getStyleClass().add("configuration-pane-advanced");
		TitledPane advanced = new TitledPane("Advanced Configuration", advancedPane);
		if (showProperties(instance, advancedPane, listChangeListener, true)) {
			accordion.getPanes().add(advanced);
		}
		
		accordion.setExpandedPane(basic);
		
		pane.getChildren().add(accordion);
		AnchorPane.setBottomAnchor(accordion, 0d);
		AnchorPane.setRightAnchor(accordion, 0d);
		AnchorPane.setTopAnchor(accordion, 0d);
		AnchorPane.setLeftAnchor(accordion, 0d);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean showProperties(T instance, Pane pane, ListChangeListener<Value<?>> listChangeListener, boolean advanced) {
		Set<Property<?>> supported = new LinkedHashSet<Property<?>>(getModifiableProperties(instance));
		boolean hasCollection = false;
		List<Value<?>> values = new ArrayList<Value<?>>();
		Iterator<Property<?>> iterator = supported.iterator();
		boolean hasProperties = false;
		while (iterator.hasNext()) {
			Property<?> property = iterator.next();
			// only simple properties can expose the advanced boolean and appear there
			if (!(property instanceof SimpleProperty) && advanced) {
				iterator.remove();
				continue;
			}
			else if (property instanceof SimpleProperty && ((SimpleProperty<?>) property).isAdvanced() != advanced) {
				iterator.remove();
				continue;
			}
			Object value = getValue(instance, property);
			if (value != null) {
				values.add(new ValueImpl(property, value));
			}
			if (property instanceof SimpleProperty && ((SimpleProperty<?>) property).isList()) {
				hasCollection = true;
			}
			hasProperties = true;
		}
		propertyUpdater = new SimplePropertyUpdater(true, supported, values.toArray(new Value[values.size()]));
		propertyUpdater.setSourceId(instance.getId());
		propertyUpdater.setRepository(getRepository(instance));
		propertyUpdater.valuesProperty().addListener(listChangeListener);
		MainController.getInstance().showProperties(propertyUpdater, pane, hasCollection, MainController.getInstance().getRepository(), true);
		return hasProperties;
	}
	
	public SimplePropertyUpdater getPropertyUpdater() {
		return propertyUpdater;
	}

	abstract public Repository getRepository(T instance);
	abstract public Collection<Property<?>> getModifiableProperties(T instance);
	abstract public <V> V getValue(T instance, Property<V> property);
	abstract public <V> void setValue(T instance, Property<V> property, V value);
}
