package be.nabu.eai.developer.lister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.api.InterfaceLister;
import be.nabu.eai.developer.util.InterfaceDescriptionImpl;

public class HTTPAuthenticationInterfaceLister implements InterfaceLister {

	private static Collection<InterfaceDescription> descriptions = null;
	
	@Override
	public Collection<InterfaceDescription> getInterfaces() {
		if (descriptions == null) {
			synchronized(HTTPAuthenticationInterfaceLister.class) {
				if (descriptions == null) {
					List<InterfaceDescription> descriptions = new ArrayList<InterfaceDescription>();
					descriptions.add(new InterfaceDescriptionImpl("HTTP", "HTTP Authenticator", "be.nabu.libs.http.api.HTTPRequestAuthenticator.authenticate"));
					HTTPAuthenticationInterfaceLister.descriptions = descriptions;
				}
			}
		}
		return descriptions;
	}

}
