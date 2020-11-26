package be.nabu.eai.developer.api;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
// add a "categorizer" or something? want to divide actions into categories? or the actions providers just need to make sure it is clear where the actions belong?
// can add sections (like connectors) that don't quite exist yet?
public interface CollectionAction {
	public Node getNode();
	public EventHandler<ActionEvent> getEventHandler();
}
