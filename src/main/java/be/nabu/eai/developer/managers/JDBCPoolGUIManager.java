package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.managers.JDBCPoolManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.jdbc.JDBCPool;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.converter.api.Converter;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.base.ValueImpl;

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
					Tab tab = controller.newTab(entry.getId(), instance);
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
		Tab tab = controller.newTab(target.itemProperty().get().getId(), instance);
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		instance.setPool(display(controller, pane, target.itemProperty().get()));
		return instance;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private JDBCPool display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		final JDBCPool pool = (JDBCPool) entry.getNode().getArtifact();
		
		Set<Property<?>> supported = new LinkedHashSet<Property<?>>();
		supported.add(new SimpleProperty<String>("driverClassName", String.class, true));
		supported.add(new SimpleProperty<String>("jdbcUrl", String.class, true));
		supported.add(new SimpleProperty<String>("username", String.class, true));
		supported.add(new SimpleProperty<String>("password", String.class, true));
		supported.add(new SimpleProperty<Long>("connectionTimeout", Long.class, false));
		supported.add(new SimpleProperty<Long>("idleTimeout", Long.class, false));
		supported.add(new SimpleProperty<Integer>("maxPoolSize", Integer.class, false));
		supported.add(new SimpleProperty<Integer>("minIdle", Integer.class, false));
		supported.add(new SimpleProperty<Boolean>("autoCommit", Boolean.class, false));
		
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
					if (change.wasUpdated() || change.wasReplaced()) {
						for (Value<?> value : change.getList()) {
							pool.getConfig().setProperty(value.getProperty().getName(), converter.convert(value.getValue(), String.class));
						}
					}
				}
			}
		});
		
		controller.showProperties(propertyUpdater, pane, false);
		return pool;
	}
}
