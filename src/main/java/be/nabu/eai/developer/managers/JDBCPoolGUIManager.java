package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.JDBCPoolManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.jdbc.JDBCPool;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.converter.api.Converter;
import be.nabu.libs.property.api.ModifiableValue;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.Validator;

public class JDBCPoolGUIManager implements ArtifactGUIManager<JDBCPool> {
	
	private Converter converter = ConverterFactory.getInstance().getConverter();
	
	@Override
	public ArtifactManager<JDBCPool> getArtifactManager() {
		return new JDBCPoolManager();
	}

	@Override
	public String getArtifactName() {
		return "JDBC Pool";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("jdbcpool.png");
	}

	@Override
	public Class<JDBCPool> getArtifactClass() {
		return JDBCPool.class;
	}

	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<Entry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create JDBC Pool", true);
		final NameOnlyCreateController createController = loader.getController();
		final JDBCPoolGUIInstance instance = new JDBCPoolGUIInstance((ResourceEntry) target.itemProperty().get());
		createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				try {
					String name = createController.getTxtName().getText();
					RepositoryEntry entry = ((RepositoryEntry) target.itemProperty().get()).createNode(name, getArtifactManager());
					JDBCPool pool = new JDBCPool(target.itemProperty().get().getId());
					getArtifactManager().save(entry, pool);
					controller.getRepositoryBrowser().refresh();
					createController.close();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					display(controller, pane, entry);
					instance.setPool(pool);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return instance;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		JDBCPoolGUIInstance instance = new JDBCPoolGUIInstance((ResourceEntry) target.itemProperty().get());
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		instance.setPool(display(controller, pane, target.itemProperty().get()));
		return instance;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JDBCPool display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		final JDBCPool pool = (JDBCPool) entry.getNode().getArtifact();
		
		Set<Property<?>> supported = new LinkedHashSet<Property<?>>();
		supported.add(new SimpleProperty<String>("driverClassName", String.class));
		supported.add(new SimpleProperty<String>("driverClassName", String.class));
		supported.add(new SimpleProperty<String>("jdbcUrl", String.class));
		supported.add(new SimpleProperty<String>("username", String.class));
		supported.add(new SimpleProperty<String>("password", String.class));
		supported.add(new SimpleProperty<Long>("connectionTimeout", Long.class));
		supported.add(new SimpleProperty<Long>("idleTimeout", Long.class));
		supported.add(new SimpleProperty<Integer>("maxPoolSize", Integer.class));
		supported.add(new SimpleProperty<Integer>("minIdle", Integer.class));
		supported.add(new SimpleProperty<Boolean>("autoCommit", Boolean.class));
		
		List<Value<?>> values = new ArrayList<Value<?>>();
		for (Property<?> property : supported) {
			String value = pool.getConfig().getProperty(property.getName());
			if (value != null) {
				values.add(new ValueImpl(property, converter.convert(value, property.getValueClass())));
			}
		}
		
		SimplePropertyUpdater propertyUpdater = new SimplePropertyUpdater(true, supported, values.toArray(new Value[0]));
		
		propertyUpdater.valuesProperty().addListener(new ListChangeListener<Value<?>>() {
			@Override
			public void onChanged(ListChangeListener.Change<? extends Value<?>> change) {
				while (change.next()) {
					if (change.wasRemoved()) {
						for (Value<?> value : change.getRemoved()) {
							pool.getConfig().remove(value.getProperty().getName());
						}
					}
					if (change.wasAdded()) {
						for (Value<?> value : change.getAddedSubList()) {
							pool.getConfig().setProperty(value.getProperty().getName(), converter.convert(value.getValue(), String.class));
						}
					}
				}
			}
		});
		
		controller.showProperties(propertyUpdater, pane);
		return pool;
	}
	
	public static class SimpleProperty<T> implements Property<T> {

		private String name;
		private Class<T> clazz;

		public SimpleProperty(String name, Class<T> clazz) {
			this.name = name;
			this.clazz = clazz;
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
	}
	
	public static class SimplePropertyUpdater implements PropertyUpdater {

		private Set<Property<?>> supported;
		private ObservableList<Value<?>> values;
		private boolean updatable;

		public SimplePropertyUpdater(boolean updatable, Set<Property<?>> supported, Value<?>...values) {
			this.updatable = updatable;
			this.supported = supported;
			this.values = FXCollections.observableArrayList(values);
		}
		@Override
		public Set<Property<?>> getSupportedProperties() {
			return supported;
		}

		@Override
		public Value<?>[] getValues() {
			return values.toArray(new Value[0]);
		}

		@Override
		public boolean canUpdate(Property<?> property) {
			return updatable;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
			boolean found = false;
			for (Value<?> current : values) {
				if (current.getProperty().equals(property)) {
					((ModifiableValue) current).setValue(value);
					found = true;
					break;
				}
			}
			if (!found) {
				values.add(new ValueImpl(property, value));
			}
			return new ArrayList<ValidationMessage>();
		}
		
		public ObservableList<Value<?>> valuesProperty() {
			return values;
		}
	}
}
