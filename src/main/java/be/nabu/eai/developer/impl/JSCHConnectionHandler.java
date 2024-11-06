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

package be.nabu.eai.developer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ConnectionTarget;
import be.nabu.eai.developer.api.ConnectionTunnel;
import be.nabu.eai.developer.api.TunnelableConnectionHandler;
import be.nabu.eai.developer.base.ConnectionTunnelBase;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;

public class JSCHConnectionHandler implements TunnelableConnectionHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public class JSCHConnection extends ConnectionTunnelBase {
		private Session session;
		
		@Override
		public void connect() {
			disconnect();
			try {
				JSch jsch = new JSch();
				ConnectionTarget target = getTarget();
				if (target.getKey() != null) {
					jsch.addIdentity(target.getKey());
				}
				session = jsch.getSession(
						target.getUsername(), 
						target.getHost(), 
						target.getPort() == null ? 22 : target.getPort());
				
				if (target.getPassword() != null) {
					session.setPassword(target.getPassword());
				}
		        session.setConfig("StrictHostKeyChecking", "no");
	
		        logger.info("Creating ssh connection: " + target.getUsername() + "@" + target.getHost() + ":" + (target.getPort() == null ? 22 : target.getPort()));
				session.connect();
	
				// set to 5 minutes, the connection was dropping when unused for a while
				session.setServerAliveInterval(300000);
				
				logger.info("Creating ssh tunnel from port " + getLocalPort() + " to " + getRemoteHost() + ":" + getRemotePort());
				int assignedPort = session.setPortForwardingL(getLocalPort(), getRemoteHost(), getRemotePort());
				if (assignedPort != getLocalPort()) {
					logger.warn("Tunnel created on different local port: " + assignedPort);
				}
				MainController.getInstance().getStage().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent arg0) {
						if (session.isConnected()) {
							session.disconnect();
						}
					}
				});
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		@Override
		public void disconnect() {
			if (session != null && session.isConnected()) {
				session.disconnect();
				session = null;
			}
		}
		@Override
		public boolean isConnected() {
			return session != null && session.isConnected();
		}
	}
	
	@Override
	public ConnectionTunnel newTunnel(ConnectionTarget target, String localHost, int localPort, String remoteHost, int remotePort) {
		JSCHConnection tunnel = new JSCHConnection();
		tunnel.setRemoteHost(remoteHost);
		tunnel.setLocalHost(localHost);
		tunnel.setRemotePort(remotePort);
		tunnel.setLocalPort(localPort);
		tunnel.setTarget(target);
		return tunnel;
	}

}
