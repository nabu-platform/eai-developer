package be.nabu.eai.developer.managers.base;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ConfigurableGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimpleProperty.HiddenCalculator;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Repository;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.evaluator.EvaluationException;
import be.nabu.libs.evaluator.PathAnalyzer;
import be.nabu.libs.evaluator.QueryParser;
import be.nabu.libs.evaluator.impl.VariableOperation;
import be.nabu.libs.evaluator.types.api.TypeOperation;
import be.nabu.libs.evaluator.types.operations.TypesOperationProvider;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.ServiceException;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.base.ValueImpl;

abstract public class BasePropertyOnlyGUIManager<T extends Artifact, I extends ArtifactGUIInstance> extends BasePortableGUIManager<T, I> implements ConfigurableGUIManager<T> {

	private SimplePropertyUpdater propertyUpdater;
	private Map<String, String> configuration;
	private static Map<String, TypeOperation> analyzedOperations = new HashMap<String, TypeOperation>();
	
	public BasePropertyOnlyGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager) {
		super(name, artifactClass, artifactManager);
	}
	
	private static TypeOperation getOperation(String query) throws ParseException {
		if (!analyzedOperations.containsKey(query)) {
			synchronized(analyzedOperations) {
				if (!analyzedOperations.containsKey(query))
					analyzedOperations.put(query, (TypeOperation) new PathAnalyzer<ComplexContent>(new TypesOperationProvider()).analyze(QueryParser.getInstance().parse(query)));
			}
		}
		return analyzedOperations.get(query);
	}
	public static Object getVariable(ComplexContent pipeline, String query) throws ServiceException {
		VariableOperation.registerRoot();
		try {
			return getOperation(query).evaluate(pipeline);
		}
		catch (EvaluationException e) {
			throw new ServiceException(e);
		}
		catch (ParseException e) {
			throw new ServiceException(e);
		}
		finally {
			VariableOperation.unregisterRoot();
		}
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
	
	protected void display(T instance, Pane pane) {
		displayWithAccordion(instance, pane);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Accordion displayWithAccordion(T instance, Pane pane) {
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
		TitledPane basic = new TitledPane("Basic", basicPane);
		// show the basic properties (no group and no advanced toggle)
		if (showProperties(instance, basicPane, listChangeListener, null)) {
			accordion.getPanes().add(basic);
		}
		
		for (String group : getPropertyGroups(instance)) {
			AnchorPane groupedPane = new AnchorPane();
			groupedPane.getStyleClass().add("configuration-pane");
			groupedPane.getStyleClass().add("configuration-pane-" + group);
			TitledPane grouped = new TitledPane(NamingConvention.UPPER_TEXT.apply(NamingConvention.UNDERSCORE.apply(group)), groupedPane);
			// show the basic properties (no group and no advanced toggle)
			if (showProperties(instance, groupedPane, listChangeListener, group)) {
				accordion.getPanes().add(grouped);
			}
		}
		
		AnchorPane advancedPane = new AnchorPane();
		advancedPane.getStyleClass().add("configuration-pane");
		advancedPane.getStyleClass().add("configuration-pane-advanced");
		TitledPane advanced = new TitledPane("Advanced", advancedPane);
		if (showProperties(instance, advancedPane, listChangeListener, "Advanced")) {
			accordion.getPanes().add(advanced);
		}
		
		accordion.setExpandedPane(basic);
		
		pane.getChildren().add(accordion);
		AnchorPane.setBottomAnchor(accordion, 0d);
		AnchorPane.setRightAnchor(accordion, 0d);
		AnchorPane.setTopAnchor(accordion, 0d);
		AnchorPane.setLeftAnchor(accordion, 0d);
		
		return accordion;
	}
	
	private List<String> getPropertyGroups(T instance) {
		List<String> groups = new ArrayList<String>();
		for (Property<?> property : getModifiableProperties(instance)) {
			if (property instanceof SimpleProperty) {
				String group = ((SimpleProperty<?>) property).getGroup();
				if (group != null && !groups.contains(group)) {
					groups.add(group);
				}
			}
		}
		return groups;
	}
	
	protected List<String> getBlacklistedProperties() {
		return new ArrayList<String>();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected boolean showProperties(T instance, Pane pane, boolean advanced) {
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
		return showProperties(instance, pane, listChangeListener, advanced ? "Advanced" : null);
	}
	
	protected String getDefaultValue(T instance, String property) {
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean showProperties(T instance, Pane pane, ListChangeListener<Value<?>> listChangeListener, String group) {
		boolean advanced = group != null && group.equalsIgnoreCase("Advanced");
		boolean basic = group == null;
		
		Set<Property<?>> supported = new LinkedHashSet<Property<?>>(getModifiableProperties(instance));
		boolean hasCollection = false;
		List<Value<?>> values = new ArrayList<Value<?>>();
		Iterator<Property<?>> iterator = supported.iterator();
		boolean hasProperties = false;
		List<String> blacklistedProperties = getBlacklistedProperties();
		ComplexContent complexContent = null;
		while (iterator.hasNext()) {
			Property<?> property = iterator.next();
			if (property instanceof SimpleProperty && (((SimpleProperty) property).getShow() != null || ((SimpleProperty) property).getHide() != null)) {
				String show = ((SimpleProperty) property).getShow();
				String hide = ((SimpleProperty) property).getHide();
				if (complexContent == null) {
					Object original = getArtifactConfiguration(instance);
					if (original == null) {
						original = instance;
					}
					complexContent = original instanceof ComplexContent 
						? (ComplexContent) original 
						: ComplexContentWrapperFactory.getInstance().getWrapper().wrap(original);
				}
				((SimpleProperty) property).setHiddenCalculator(newHiddenCalculator(show, hide, complexContent));
			}
			
			if (property instanceof SimpleProperty) {
				((SimpleProperty) property).setDefaultValue(getDefaultValue(instance, property.getName()));
			}
			
			// only simple properties can expose the advanced boolean and appear there
			if (!(property instanceof SimpleProperty) && advanced) {
				iterator.remove();
				continue;
			}
			// if we are using the old binary (basic/advanced) toggle, check the advanced boolean
			else if ((advanced || basic) && property instanceof SimpleProperty && ((SimpleProperty<?>) property).isAdvanced() != advanced) {
				iterator.remove();
				continue;
			}
			// in the new group system, it must match the group, which can only be the case for simple properties
			else if (!advanced && !basic && (!(property instanceof SimpleProperty) || !group.equalsIgnoreCase(((SimpleProperty) property).getGroup()))) {
				iterator.remove();
				continue;
			}
			else if ((advanced || basic) && property instanceof SimpleProperty && ((SimpleProperty<?>) property).getGroup() != null && !"Advanced".equalsIgnoreCase(((SimpleProperty<?>) property).getGroup())
					&& !"Basic".equalsIgnoreCase(((SimpleProperty<?>) property).getGroup())) {
				iterator.remove();
				continue;
			}
			else if (blacklistedProperties.indexOf(property.getName()) >= 0) {
				iterator.remove();
				continue;
			}
			// you can also blacklist parent
			int indexOf = property.getName().indexOf('/');
			if (indexOf > 0 && blacklistedProperties.indexOf(property.getName().substring(0, indexOf)) >= 0) {
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
		String actualId = getActualId(instance);
		// don't set the id if it is not correct
		if (actualId != null && !actualId.startsWith("$self")) {
			propertyUpdater.setSourceId(actualId);
		}
		propertyUpdater.setRepository(getRepository(instance));
		propertyUpdater.valuesProperty().addListener(listChangeListener);
//		MainController.getInstance().showProperties(propertyUpdater, pane, hasCollection, MainController.getInstance().getRepository(), true);
		// we always want to update now because we might have show/hide rules that need to be triggered
		MainController.getInstance().showProperties(propertyUpdater, pane, true, MainController.getInstance().getRepository(), true);
		pane.setPadding(new Insets(10, 10, 0, 10));
		return hasProperties;
	}

	public static HiddenCalculator newHiddenCalculator(String show, String hide, final ComplexContent finalContent) {
		return new HiddenCalculator() {
			@Override
			public boolean isHidden() {
				try {
					if (hide != null) {
						Object variable = getVariable(finalContent, hide);
						if (variable instanceof Boolean && (Boolean) variable) {
							return true;
						}
					}
					if (show != null) {
						Object variable = getVariable(finalContent, show);
						if (variable instanceof Boolean && !(Boolean) variable) {
							return true;
						}
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				return false;
			}
		};
	}
	
	public SimplePropertyUpdater getPropertyUpdater() {
		return propertyUpdater;
	}

	abstract public Repository getRepository(T instance);
	abstract public Collection<Property<?>> getModifiableProperties(T instance);
	abstract public <V> V getValue(T instance, Property<V> property);
	abstract public <V> void setValue(T instance, Property<V> property, V value);
	
	protected Object getArtifactConfiguration(T instance) {
		return null;
	}
	
	@Override
	public void setConfiguration(Map<String, String> configuration) {
		this.configuration = configuration;
	}
	
	public String getActualId(T artifact) {
		return this.configuration != null && this.configuration.containsKey("actualId") ? this.configuration.get("actualId") : artifact.getId();
	}
}
