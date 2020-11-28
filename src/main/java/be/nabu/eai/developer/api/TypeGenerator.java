package be.nabu.eai.developer.api;

import be.nabu.libs.resources.api.ReadableResource;

public interface TypeGenerator {
	// you can ask the user for example to drop a file, copy paste some json...
	public void requestUser(TypeGeneratorTarget target);
	// or someone else can get data somewhere (like a file) and it needs to be processed
	// the boolean return indicates whether or not this type generator can handle that content
	public boolean processResource(ReadableResource resource, TypeGeneratorTarget target);
}
