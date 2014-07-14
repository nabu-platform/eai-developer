package be.nabu.eai.developer.api;

import java.io.IOException;

import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;

public interface ArtifactGUIManager<T extends Artifact> {
	public ArtifactManager<T> getArtifactManager();
	public String getArtifactName();
	public ImageView getGraphic();
	// the controller allows you to pop up the windows that you need
	// the repository entry allows you to create something new there
	public void create(MainController controller, TreeItem<RepositoryEntry> target) throws IOException;
	public void view(MainController controller, TreeItem<RepositoryEntry> target) throws IOException;
}
