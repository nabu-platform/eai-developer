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

package be.nabu.eai.developer.util;

import be.nabu.libs.artifacts.ArtifactResolverFactory;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.converter.api.Converter;

public class ArtifactAwareConverter2 implements Converter {

	@Override
	public boolean canConvert(Class<?> instanceClass, Class<?> targetClass) {
		if (Artifact.class.isAssignableFrom(instanceClass) && String.class.equals(targetClass)) {
			return true;
		}
		else if (Artifact.class.isAssignableFrom(targetClass) && String.class.equals(instanceClass)) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T convert(Object instance, Class<T> targetClass) {
		if (instance instanceof String && Artifact.class.isAssignableFrom(targetClass)) {
			return (T) ArtifactResolverFactory.getInstance().getResolver().resolve((String) instance);
		}
		else if (instance instanceof Artifact && String.class.equals(targetClass)) {
			return (T) ((Artifact) instance).getId();
		}
		return null;
	}
	
}
