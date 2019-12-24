package be.nabu.eai.developer.impl;

import be.nabu.eai.developer.api.NodeContainer;
import javafx.scene.Node;
import javafx.stage.Stage;

public class StageNodeContainer implements NodeContainer {

	private Stage stage;

	public StageNodeContainer(Stage stage) {
		this.stage = stage;
	}
	
	@Override
	public void close() {
		stage.hide();
	}

	@Override
	public void activate() {
		stage.requestFocus();
	}

	@Override
	public Node getContent() {
		return stage.getScene().getRoot();
	}

	@Override
	public void setChanged(boolean changed) {
		if (changed) {
			if (!stage.getTitle().endsWith("*")) {
				stage.setTitle(stage.getTitle() + " *");
			}
		}
		else {
			if (stage.getTitle().endsWith("*")) {
				stage.setTitle(stage.getTitle().replaceAll("[\\s]*\\*$", ""));
			}
		}
	}

	@Override
	public boolean isFocused() {
		return stage.isFocused();
	}
	
}
