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

package be.nabu.eai.developer.api;

import be.nabu.libs.resources.api.ReadableResource;

public interface TypeGenerator {
	// you can ask the user for example to drop a file, copy paste some json...
	public void requestUser(TypeGeneratorTarget target);
	// or someone else can get data somewhere (like a file) and it needs to be processed
	// the boolean return indicates whether or not this type generator can handle that content
	public boolean processResource(ReadableResource resource, TypeGeneratorTarget target);
}
