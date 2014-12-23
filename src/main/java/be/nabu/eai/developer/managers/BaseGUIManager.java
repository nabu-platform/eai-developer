package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public abstract class BaseGUIManager<T extends Artifact> implements ArtifactGUIManager<T> {

	private String name;
	private ArtifactManager<T> artifactManager;
	private Class<T> artifactClass;

	public BaseGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager) {
		this.name = name;
		this.artifactClass = artifactClass;
		this.artifactManager = artifactManager;
	}
	
	@Override
	public ArtifactManager<T> getArtifactManager() {
		return artifactManager;
	}

	@Override
	public String getArtifactName() {
		return name;
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic(getArtifactName().toLowerCase().replaceAll("[^\\w]+", "") + ".png");
	}

	@Override
	public Class<T> getArtifactClass() {
		return artifactClass;
	}

	abstract protected List<Property<?>> getCreateProperties();
	abstract protected ArtifactGUIInstance newGUIInstance(ResourceEntry entry);
	abstract protected T newInstance(MainController controller, RepositoryEntry entry, Value<?>...values);
	abstract protected T display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException;
	abstract protected void setInstance(ArtifactGUIInstance guiInstance, T instance);
	
	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<Entry> target) throws IOException {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		properties.add(new SimpleProperty<String>("Name", String.class));
		properties.addAll(getCreateProperties());
		final ArtifactGUIInstance guiInstance = newGUIInstance((ResourceEntry) target.itemProperty().get());
		final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(properties));
		JDBCServiceGUIManager.buildPopup(controller, updater, getArtifactName(), new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				try {
					String name = updater.getValue("Name");
					RepositoryEntry entry = ((RepositoryEntry) target.itemProperty().get()).createNode(name, getArtifactManager());
					T instance = newInstance(controller, entry, updater.getValues());
					getArtifactManager().save(entry, instance);
					controller.getRepositoryBrowser().refresh();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					display(controller, pane, entry);
					setInstance(guiInstance, instance);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return null;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		ArtifactGUIInstance guiInstance = newGUIInstance((ResourceEntry) target.itemProperty().get());
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		setInstance(guiInstance, display(controller, pane, target.itemProperty().get()));
		return guiInstance;
	}
}