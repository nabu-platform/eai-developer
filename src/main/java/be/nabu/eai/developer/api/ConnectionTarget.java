package be.nabu.eai.developer.api;

public interface ConnectionTarget {
	public String getHost();
	public Integer getPort();
	public String getUsername();
	public String getPassword();
	public String getKey();
}
