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
