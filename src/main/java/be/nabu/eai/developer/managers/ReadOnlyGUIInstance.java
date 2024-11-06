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

package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.libs.artifacts.ArtifactResolverFactory;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.validator.api.Validation;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class ReadOnlyGUIInstance implements ArtifactGUIInstance {

	private String id;
	private boolean changed;

	public ReadOnlyGUIInstance(String id) {
		this.id = id;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public List<Validation<?>> save() throws IOException {
		return Arrays.asList(new ValidationMessage [] { new ValidationMessage(Severity.ERROR, "Can not save this read-only artifact") });
	}

	@Override
	public boolean hasChanged() {
		return changed;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public void setChanged(boolean changed) {
		this.changed = changed;
	}

	@Override
	public Artifact getArtifact() {
		return ArtifactResolverFactory.getInstance().getResolver().resolve(getId());
	}
}
