package be.nabu.eai.developer.util;

import be.nabu.eai.developer.api.FindFilter;
import be.nabu.jfx.control.tree.Marshallable;

public class FindNameFilter<T> implements FindFilter<T> {
	
	private Marshallable<T> marshallable;
	private boolean useRegex;

	public FindNameFilter(Marshallable<T> marshallable, boolean useRegex) {
		this.marshallable = marshallable;
		this.useRegex = useRegex;
	}

	@Override
	public boolean accept(T item, String newValue) {
		newValue = newValue.toLowerCase().replace("*", ".*");
		if (!newValue.startsWith("^")) {
			newValue = ".*" + newValue + ".*";
		}
		return useRegex 
			? marshallable.marshal(item).matches("(?i)" + newValue)
			: marshallable.marshal(item).contains(newValue);
	}

}
