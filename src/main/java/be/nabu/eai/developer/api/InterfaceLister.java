package be.nabu.eai.developer.api;

import java.util.Collection;

public interface InterfaceLister {
	
	public Collection<InterfaceDescription> getInterfaces();
	
	public static interface InterfaceDescription {
		public String getName();
		public String getCategory();
		public String getInterface();
	}
}
