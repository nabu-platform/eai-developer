package be.nabu.eai.developer.api;

import javafx.scene.layout.AnchorPane;

public interface RedrawableArtifactGUIInstance extends ArtifactGUIInstance {
	/**
	 * Redraw into the given pane
	 */
	public void redraw(AnchorPane pane);
}
