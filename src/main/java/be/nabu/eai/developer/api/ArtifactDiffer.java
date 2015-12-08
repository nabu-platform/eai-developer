package be.nabu.eai.developer.api;

import javafx.scene.layout.Pane;
import be.nabu.libs.artifacts.api.Artifact;

/**
 * This visualizes the difference between two artifacts in the target pane
 * The boolean return indicates whether there is even a diff to be shown or not.
 * If the items are in sync (however that is defined), you should return "false" and the pane will not be shown
 * 
 * So basically if return == true: show pane
 */
public interface ArtifactDiffer<T extends Artifact> {
	public boolean diff(T original, T other, Pane target);
}
