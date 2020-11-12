package be.nabu.eai.developer;

import be.nabu.eai.developer.api.CollectionAction;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

public class CollectionActionImpl implements CollectionAction {

	private Node node;
	private EventHandler<ActionEvent> handler;
	
	public CollectionActionImpl() {
		// auto
	}
	
	public CollectionActionImpl(Node node, EventHandler<ActionEvent> handler) {
		this.node = node;
		this.handler = handler;
	}

	@Override
	public Node getNode() {
		return node;
	}

	@Override
	public EventHandler<ActionEvent> getEventHandler() {
		return handler;
	}

	public EventHandler<ActionEvent> getHandler() {
		return handler;
	}

	public void setHandler(EventHandler<ActionEvent> handler) {
		this.handler = handler;
	}

	public void setNode(Node node) {
		this.node = node;
	}
	
}
