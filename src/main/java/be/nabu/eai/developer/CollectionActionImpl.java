/*
* Copyright (C) 2014 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

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
