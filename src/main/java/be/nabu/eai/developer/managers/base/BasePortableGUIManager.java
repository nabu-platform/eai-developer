package be.nabu.eai.developer.managers.base;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.PortableArtifactGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.artifacts.api.Artifact;

abstract public class BasePortableGUIManager<T extends Artifact, I extends ArtifactGUIInstance> extends BaseGUIManager<T, I> implements PortableArtifactGUIManager<T> {

	public BasePortableGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager) {
		super(name, artifactClass, artifactManager);
	}

	@SuppressWarnings("unchecked")
	final protected T display(MainController controller, AnchorPane pane, Entry entry) throws IOException, ParseException {
		T artifact = (T) entry.getNode().getArtifact();
		System.out.println("LOADING from: " + entry + ": " + artifact);
		display(controller, pane, artifact);
		return artifact;
	}
}
