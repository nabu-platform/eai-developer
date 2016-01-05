package be.nabu.eai.developer.api;

import java.io.IOException;
import java.text.ParseException;

import javafx.scene.layout.AnchorPane;
import be.nabu.eai.developer.MainController;
import be.nabu.libs.artifacts.api.Artifact;

public interface PortableArtifactGUIManager<T extends Artifact> extends ArtifactGUIManager<T> {
	public void display(MainController controller, AnchorPane pane, T artifact) throws IOException, ParseException;
}
