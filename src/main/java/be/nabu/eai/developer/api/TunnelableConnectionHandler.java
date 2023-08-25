package be.nabu.eai.developer.api;

public interface TunnelableConnectionHandler {
	public ConnectionTunnel newTunnel(ConnectionTarget target, String localHost, int localPort, String remoteHost, int remotePort);
}
