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

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import be.nabu.eai.developer.api.TypeGenerator;

public class TypeGeneratorFactory {
	
	private List<TypeGenerator> generators;
	
	private static TypeGeneratorFactory instance;
	
	public static TypeGeneratorFactory getInstance() {
		if (instance == null) {
			instance = new TypeGeneratorFactory();
		}
		return instance;
	}
	
	public List<TypeGenerator> getTypeGenerators() {
		if (generators == null) {
			synchronized(this) {
				if (generators == null) {
					List<TypeGenerator> generators = new ArrayList<TypeGenerator>();
					for (TypeGenerator generator : ServiceLoader.load(TypeGenerator.class)) {
						generators.add(generator);
					}
					this.generators = generators;
				}
			}
		}
		return generators;
	}
}
