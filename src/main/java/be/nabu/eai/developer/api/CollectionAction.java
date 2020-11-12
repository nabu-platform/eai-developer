package be.nabu.eai.developer.api;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

public interface CollectionAction {
	public Node getNode();
	public EventHandler<ActionEvent> getEventHandler();
}
