package be.nabu.eai.developer.lister;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import be.nabu.eai.developer.api.InterfaceLister;
import be.nabu.eai.developer.util.InterfaceDescriptionImpl;

public class ObjectEnrichmentLister implements InterfaceLister {

	private static Collection<InterfaceDescription> descriptions = null;
	
	@Override
	public Collection<InterfaceDescription> getInterfaces() {
		if (descriptions == null) {
			synchronized(ObjectEnrichmentLister.class) {
				if (descriptions == null) {
					List<InterfaceDescription> descriptions = new ArrayList<InterfaceDescription>();
					descriptions.add(new InterfaceDescriptionImpl("Object Enrichment", "Enrich", "be.nabu.eai.repository.api.ObjectEnricher.apply"));
					descriptions.add(new InterfaceDescriptionImpl("Object Enrichment", "Persist", "be.nabu.eai.repository.api.ObjectEnricher.persist"));
					ObjectEnrichmentLister.descriptions = descriptions;
				}
			}
		}
		return descriptions;
	}

}
