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
