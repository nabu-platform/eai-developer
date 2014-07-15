package be.nabu.eai.developer.api;

import java.io.IOException;
import java.text.ParseException;

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
	public ArtifactGUIInstance create(MainController controller, TreeItem<RepositoryEntry> target) throws IOException;
	public ArtifactGUIInstance view(MainController controller, TreeItem<RepositoryEntry> target) throws IOException, ParseException;
}
