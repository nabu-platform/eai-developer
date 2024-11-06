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

package be.nabu.eai.developer.managers.base;

import java.io.IOException;

import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;

abstract public class BaseJAXBGUIManager<C, T extends JAXBArtifact<C>> extends BaseConfigurationGUIManager<T, C> {

	public BaseJAXBGUIManager(String name, Class<T> artifactClass, ArtifactManager<T> artifactManager, Class<C> configurationClass) {
		super(name, artifactClass, artifactManager, configurationClass);
	}

	@Override
	public C getConfiguration(T instance) {
		try {
			return instance.getConfiguration();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Repository getRepository(T instance) {
		return instance.getRepository();
	}

}
