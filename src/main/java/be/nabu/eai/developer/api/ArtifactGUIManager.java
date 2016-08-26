package be.nabu.eai.developer.api;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.image.ImageView;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.artifacts.api.Artifact;

// TODO: update create/view to work with Entry only instead of TreeItem<Entry>
// this can decouple it slightly further from however we get the entry
public interface ArtifactGUIManager<T extends Artifact> {
	public ArtifactManager<T> getArtifactManager();
	public String getArtifactName();
	public ImageView getGraphic();
	public Class<T> getArtifactClass();
	public ArtifactGUIInstance create(MainController controller, TreeItem<Entry> target) throws IOException;
	public ArtifactGUIInstance view(MainController controller, TreeItem<Entry> target) throws IOException, ParseException;
	default String getCategory() { return null; }
}
