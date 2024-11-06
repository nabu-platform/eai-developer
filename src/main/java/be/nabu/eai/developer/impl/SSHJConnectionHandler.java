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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.api.TunnelableConnectionHandler;
import be.nabu.eai.developer.base.ConnectionTunnelBase;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ConnectionTarget;
import be.nabu.eai.developer.api.ConnectionTunnel;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

public class SSHJConnectionHandler implements TunnelableConnectionHandler {

	private Logger logger = LoggerFactory.getLogger(getClass());
	
	public class SSHJConnectionTunnel extends ConnectionTunnelBase {
		
		private SSHClient client;
		private Thread thread;

		public void connect() {
			disconnect();
			try {
//				ConfigImpl config;
				DefaultConfig config = new DefaultConfig();
//				config.setVerifyHostKeyCertificates(false);
				ConnectionTarget target = getTarget();
				logger.info("Creating ssh connection: " + target.getUsername() + "@" + target.getHost() + ":" + (target.getPort() == null ? 22 : target.getPort()));
				client = new SSHClient(config);
				// allow all hosts...
				client.addHostKeyVerifier(new PromiscuousVerifier());
				client.connect(target.getHost(), target.getPort() == null ? 22 : target.getPort());
				if (target.getKey() != null) {
					logger.info("Identifying as " + target.getUsername() + " with key " + target.getKey());
					client.authPublickey(target.getUsername(), target.getKey());
				}
				if (target.getPassword() != null) {
					logger.info("Identifying as " + target.getUsername() + " with password");
					client.authPassword(target.getUsername(), target.getPassword());
				}
				ServerSocket serverSocket = new ServerSocket();
				serverSocket.setReuseAddress(true);
				serverSocket.bind(new InetSocketAddress("localhost", getLocalPort()));
				logger.info("Creating ssh tunnel from port " + getLocalPort() + " to " + getRemoteHost() + ":" + getRemotePort());
				LocalPortForwarder localPortForwarder = client.newLocalPortForwarder(new net.schmizz.sshj.connection.channel.direct.Parameters("localhost", getLocalPort(), getRemoteHost(), getRemotePort()), serverSocket);
				thread = new Thread(new Runnable() {
					public void run() {
						try {
							localPortForwarder.listen();
						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}, "ssh-forwarder-" + getRemoteHost() + ":" + getRemotePort());
				thread.start();
				MainController.getInstance().getStage().addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent arg0) {
						disconnect();
					}
				});
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public SSHClient getClient() {
			return client;
		}

		@Override
		public void disconnect() {
			try {
				if (thread != null) {
					thread.interrupt();
					client.disconnect();
					thread = null;
				}
			}
			catch (Exception e) {
				logger.error("Could not successfully close SSH tunnel to " + getRemoteHost(), e);
			}
		}

		@Override
		public boolean isConnected() {
			return client != null && client.isConnected();
		}

	}

	@Override
	public ConnectionTunnel newTunnel(ConnectionTarget target, String localHost, int localPort, String remoteHost, int remotePort) {
		SSHJConnectionTunnel tunnel = new SSHJConnectionTunnel();
		tunnel.setRemoteHost(remoteHost);
		tunnel.setLocalHost(localHost);
		tunnel.setRemotePort(remotePort);
		tunnel.setLocalPort(localPort);
		tunnel.setTarget(target);
		return tunnel;
	}

}
