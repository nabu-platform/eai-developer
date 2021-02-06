package be.nabu.eai.developer.api;

import java.util.List;
import java.util.Map;

import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.structure.Structure;

public interface TypeGeneratorTarget {
	public void generate(Map<Structure, List<ComplexContent>> content);
}
