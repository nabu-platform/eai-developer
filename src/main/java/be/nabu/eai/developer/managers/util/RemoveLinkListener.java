package be.nabu.eai.developer.managers.util;

import be.nabu.eai.developer.managers.util.Mapping.RemoveMapping;
import be.nabu.libs.services.vm.step.Link;

public class RemoveLinkListener implements RemoveMapping {

	private Link link;
	
	public RemoveLinkListener(Link link) {
		this.link = link;
	}
	
	@Override
	public boolean remove(Mapping mapping) {
		if (link.getParent() != null) {
			link.getParent().getChildren().remove(link);
			return true;
		}
		return false;
	}
}
