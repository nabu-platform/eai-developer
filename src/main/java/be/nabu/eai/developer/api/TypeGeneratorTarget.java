package be.nabu.eai.developer.api;

import java.util.List;

import be.nabu.libs.types.structure.Structure;

public interface TypeGeneratorTarget {
	public void generate(List<Structure> types);
}
