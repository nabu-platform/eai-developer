package be.nabu.eai.developer.managers.base;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.ComplexContentEditor;
import be.nabu.eai.developer.ComplexContentEditor.AddHandler;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.java.BeanInstance;

abstract public class BaseJAXBComplexGUIManager<C, T extends JAXBArtifact<C>> extends BasePortableGUIManager<T, BaseArtifactGUIInstance<T>> {

	private List<Property<?>> properties;

	public BaseJAXBComplexGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager, Class<C> configurationClass) {
		super(name, artifactClass, artifactManager);
		load(configurationClass);
	}

	private void load(Class<C> configurationClass) {
		properties = new ArrayList<Property<?>>();
		properties.addAll(BaseConfigurationGUIManager.createProperties(configurationClass));
	}
	
	@Override
	public void display(MainController controller, AnchorPane pane, T artifact) throws IOException, ParseException {
		C config = artifact.getConfig();
		ComplexContentEditor editor = new ComplexContentEditor(new BeanInstance<C>(config), true, artifact.getRepository());
		List<AddHandler> addHandlers = getAddHandlers();
		if (addHandlers != null) {
			editor.addHandler(addHandlers.toArray(new AddHandler[addHandlers.size()]));
		}
		
		ScrollPane scroll = new ScrollPane();
		scroll.setContent(editor.getTree());
		pane.getChildren().add(scroll);
		AnchorPane.setLeftAnchor(scroll, 0d);
		AnchorPane.setRightAnchor(scroll, 0d);
		AnchorPane.setTopAnchor(scroll, 0d);
		AnchorPane.setBottomAnchor(scroll, 0d);
		
		editor.getTree().prefWidthProperty().bind(scroll.widthProperty());
	}
	
	protected List<AddHandler> getAddHandlers() {
		return null;
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected BaseArtifactGUIInstance<T> newGUIInstance(Entry entry) {
		return new BaseArtifactGUIInstance<T>(this, entry);
	}

	@Override
	protected void setEntry(BaseArtifactGUIInstance<T> guiInstance, ResourceEntry entry) {
		guiInstance.setEntry(entry);
	}

	@Override
	protected void setInstance(BaseArtifactGUIInstance<T> guiInstance, T instance) {
		guiInstance.setArtifact(instance);
	}

}
