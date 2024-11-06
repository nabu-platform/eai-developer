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

package be.nabu.eai.developer.base;

import be.nabu.eai.developer.api.TunnelableConnectionHandler;
import be.nabu.eai.developer.api.ConnectionTarget;
import be.nabu.eai.developer.api.ConnectionTunnel;

abstract public class ConnectionTunnelBase implements ConnectionTunnel {
	
	private ConnectionTarget target;
	private String localHost, remoteHost;
	private int localPort, remotePort;
	
	@Override
	public String getLocalHost() {
		return localHost;
	}
	public void setLocalHost(String localHost) {
		this.localHost = localHost;
	}
	@Override
	public String getRemoteHost() {
		return remoteHost;
	}
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}
	@Override
	public int getLocalPort() {
		return localPort;
	}
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}
	@Override
	public int getRemotePort() {
		return remotePort;
	}
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}
	@Override
	public ConnectionTarget getTarget() {
		return target;
	}
	public void setTarget(ConnectionTarget target) {
		this.target = target;
	}
	
}
