package be.nabu.eai.developer;

import be.nabu.eai.developer.api.CollectionAction;
import be.nabu.eai.developer.api.EntryAcceptor;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;

public class CollectionActionImpl implements CollectionAction {

	private Node node;
	private EventHandler<ActionEvent> handler;
	private EntryAcceptor acceptor;
	
	public CollectionActionImpl() {
		// auto
	}
	
	public CollectionActionImpl(Node node, EventHandler<ActionEvent> handler) {
		this(node, handler, null);
	}

	public CollectionActionImpl(Node node, EventHandler<ActionEvent> handler, EntryAcceptor acceptor) {
		this.node = node;
		this.handler = handler;
		this.acceptor = acceptor;
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

	@Override
	public EntryAcceptor getEntryAcceptor() {
		return acceptor;
	}

	public void setEntryAcceptor(EntryAcceptor acceptor) {
		this.acceptor = acceptor;
	}

}
