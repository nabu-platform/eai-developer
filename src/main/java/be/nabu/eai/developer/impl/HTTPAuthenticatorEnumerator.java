package be.nabu.eai.developer.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import be.nabu.eai.api.Enumerator;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.libs.services.api.DefinedService;
import be.nabu.libs.services.api.DefinedServiceInterface;
import be.nabu.libs.services.api.ServiceInterface;

public class HTTPAuthenticatorEnumerator implements Enumerator {

	@Override
	public List<?> enumerate() {
		List<String> names = new ArrayList<String>();
		EAIResourceRepository repository = EAIResourceRepository.getInstance();
		String interfaceId = "be.nabu.libs.http.api.HTTPRequestAuthenticator.authenticate";
		for (DefinedService service : repository.getArtifacts(DefinedService.class)) {
			// interfaces themselves are also services, don't count them though
			if (service instanceof DefinedServiceInterface) {
				continue;
			}
			ServiceInterface serviceInterface = service.getServiceInterface();
			while (serviceInterface != null) {
				if (serviceInterface instanceof DefinedServiceInterface) {
					if (interfaceId.equals(((DefinedServiceInterface) serviceInterface).getId())) {
						// once we found a service that actually implements the specification, we need to check the type which is registered in the node properties
						Entry entry = repository.getEntry(service.getId());
						if (entry != null) {
							Map<String, String> properties = entry.getNode().getProperties();
							if (properties != null && properties.containsKey("authenticationType")) {
								names.add(properties.get("authenticationType"));
							}
						}
						break;
					}
				}
				serviceInterface = serviceInterface.getParent();
			}
		}
		return names;
	}

}
