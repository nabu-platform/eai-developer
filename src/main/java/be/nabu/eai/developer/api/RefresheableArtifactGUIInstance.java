package be.nabu.eai.developer.api;

import javafx.scene.layout.AnchorPane;

public interface RefresheableArtifactGUIInstance extends ArtifactGUIInstance {
	/**
	 * Redraw into the given pane
	 */
	public void refresh(AnchorPane pane);
}
