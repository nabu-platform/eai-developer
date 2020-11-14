package be.nabu.eai.developer.managers.base;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.managers.ServiceGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.services.api.Service;

public abstract class BaseGUIManager<T extends Artifact, I extends ArtifactGUIInstance> implements ArtifactGUIManager<T> {

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
	abstract protected I newGUIInstance(Entry entry);
	abstract protected void setEntry(I guiInstance, ResourceEntry entry);
	abstract protected T newInstance(MainController controller, RepositoryEntry entry, Value<?>...values) throws IOException;
	abstract protected T display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException;
	abstract protected void setInstance(I guiInstance, T instance);
	
	// a hook for when stuff gets saved
	protected void saved() {
		// do nothing
	}
	
	@Override
	public I create(final MainController controller, final TreeItem<Entry> target) throws IOException {
		List<Property<?>> properties = new ArrayList<Property<?>>();
		properties.add(new SimpleProperty<String>("Name", String.class, true));
		List<Property<?>> createProperties = getCreateProperties();
		if (createProperties != null) {
			properties.addAll(createProperties);
		}
		final I guiInstance = newGUIInstance((ResourceEntry) target.itemProperty().get());
		final SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(properties));
		EAIDeveloperUtils.buildPopup(controller, updater, "Create " + getArtifactName(), new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				RepositoryEntry entry = null;
				try {
					String name = updater.getValue("Name");
					String originalName = name;
					if (controller.usePrettyNamesInRepositoryProperty().get()) {
						name = NamingConvention.LOWER_CAMEL_CASE.apply(NamingConvention.UNDERSCORE.apply(name));
					}
					entry = ((RepositoryEntry) target.itemProperty().get()).createNode(name, getArtifactManager(), true);
					if (!originalName.equals(name)) {
						entry.getNode().setName(originalName);
						entry.saveNode();
					}
					T instance = newInstance(controller, entry, updater.getValues());
					getArtifactManager().save(entry, instance);
					
					TreeItem<Entry> parentTreeItem = controller.getRepositoryBrowser().getControl().resolve(target.itemProperty().get().getId().replace(".", "/"));
					// @optimize
					if (parentTreeItem != null) {
						controller.getRepositoryBrowser().getControl().getTreeCell(parentTreeItem).refresh();
					}
					else {
						controller.getRepositoryBrowser().refresh();
					}
					
					// reload stuff
					MainController.getInstance().getAsynchronousRemoteServer().reload(parentTreeItem.itemProperty().get().getId());
					MainController.getInstance().getCollaborationClient().created(entry.getId(), "Created");
					
					Tab tab = controller.newTab(entry.getId(), guiInstance);
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					if (Service.class.isAssignableFrom(artifactClass)) {
						ServiceGUIManager.makeRunnable(tab, (Service) instance, controller);
					}
					// make sure it has the "latest" version
					// the problem we had was when you have additional create properties, they are correctly set in the instance and saved in the above
					// however for some reason the node seemed to have an outdated copy of the artifact
					// simply setting the artifact here fixes that...
					entry.getNode().setArtifact(instance);
					
					setEntry(guiInstance, entry);
					setInstance(guiInstance, display(controller, pane, entry));
				}
				catch (Exception e) {
					// if we created the entry but it failed later on, remove it again
					if (entry != null) {
						entry.getParent().removeChildren(entry.getName());
					}
					MainController.getInstance().notify(e);
					throw new RuntimeException(e);
				}
			}
		});
		return guiInstance;
	}

	@SuppressWarnings("unchecked")
	public static <V> V getValue(String name, Class<V> clazz, Value<?>...values) {
		for (Value<?> value : values) {
			if (value.getProperty().getName().equals(name)) {
				return (V) value.getValue();
			}
		}
		return null;
	}
	
	@Override
	public I view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		return view(controller, target.itemProperty().get());
	}
	
	@Override
	public I view(MainController controller, Entry target) throws IOException, ParseException {
		I guiInstance = newGUIInstance(target);
		Tab tab = controller.newTab(target.getId(), guiInstance);
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		T display = display(controller, pane, target);
		if (Service.class.isAssignableFrom(artifactClass)) {
			ServiceGUIManager.makeRunnable(tab, (Service) display, controller);
		}
		setInstance(guiInstance, display);
		return guiInstance;
	}
}
