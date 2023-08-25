package be.nabu.eai.developer.api;

public interface ConnectionTunnel {
	public ConnectionTarget getTarget();
	public String getRemoteHost();
	public String getLocalHost();
	public int getRemotePort();
	public int getLocalPort();
	// can be used to set up initial connection or reconnect
	public void connect();
	public void disconnect();
	public boolean isConnected();
}
