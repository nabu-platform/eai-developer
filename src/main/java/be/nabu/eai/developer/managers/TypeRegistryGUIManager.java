package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.types.api.TypeRegistry;

public class TypeRegistryGUIManager<T extends TypeRegistry & Artifact> implements ArtifactGUIManager<T> {

	private ArtifactManager<T> artifactManager;
	private String name;

	TypeRegistryGUIManager(ArtifactManager<T> artifactManager, String artifactName) {
		this.artifactManager = artifactManager;
		this.name = artifactName;
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
		return MainController.loadGraphic(name.replaceAll("[^\\w]+", "").toLowerCase() + ".png");
	}

	@Override
	public Class<T> getArtifactClass() {
		return artifactManager.getArtifactClass();
	}

	@Override
	public ArtifactGUIInstance create(MainController controller, TreeItem<Entry> target) throws IOException {
		return null;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException {
		return new ReadOnlyGUIInstance(target.itemProperty().get().getId());
	}

}
