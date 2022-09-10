package be.nabu.eai.developer.impl;

import java.util.Arrays;
import java.util.HashSet;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.Main.Protocol;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.artifacts.api.TunnelableArtifact;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.base.ValueImpl;

public class TunnelContextMenu implements EntryContextMenuProvider {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	@Override
	public MenuItem getContext(Entry entry) {
		if (entry.isNode() && Protocol.SSH.equals(MainController.getInstance().getProfile().getProtocol())) {
			if (TunnelableArtifact.class.isAssignableFrom(entry.getNode().getArtifactClass())) {
				try {
					TunnelableArtifact artifact = (TunnelableArtifact) entry.getNode().getArtifact();
					
					if (MainController.getInstance().isTunneled(entry.getId())) {
						MenuItem item = new MenuItem("Remove SSH Tunnel (" + MainController.getInstance().getTunnelPort(entry.getId()) + ")");
						item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								MainController.getInstance().untunnel(entry.getId());
							}
						});
						return item;
					}
					else if (artifact.getTunnelHost() != null && artifact.getTunnelPort() != null) {
						MenuItem item = new MenuItem("Create SSH Tunnel");
						item.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent arg0) {
								SimpleProperty<Integer> portProperty = new SimpleProperty<Integer>("Local Port", Integer.class, true);
								SimplePropertyUpdater simplePropertyUpdater = new SimplePropertyUpdater(true, new HashSet<Property<?>>(Arrays.asList(portProperty)), new ValueImpl<Integer>(portProperty, artifact.getTunnelPort()));
								EAIDeveloperUtils.buildPopup(MainController.getInstance(), simplePropertyUpdater, "Create SSH Tunnel", new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										Integer localPort = simplePropertyUpdater.getValue("Local Port");
										if (localPort == null) {
											localPort = artifact.getTunnelPort();
										}
										MainController.getInstance().tunnel(entry.getId(), localPort, true);
									}
								});
							}
						});
						return item;
					}
				}
				catch (Exception e) {
					logger.error("Could not parse item: " + entry.getId(), e);
				}
			}
		}
		return null;
	}


}
