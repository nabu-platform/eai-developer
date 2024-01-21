package be.nabu.eai.developer.impl;

import be.nabu.eai.developer.api.ConnectionTarget;

public class ConnectionTargetImpl implements ConnectionTarget {

	private String host, username, password, key;
	private Integer port;
	
	public ConnectionTargetImpl() {
		// auto
	}
	public ConnectionTargetImpl(String host, Integer port, String username, String password, String key) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.key = key;
	}
	
	@Override
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	@Override
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@Override
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@Override
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	@Override
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	
}