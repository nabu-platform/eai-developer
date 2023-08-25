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
