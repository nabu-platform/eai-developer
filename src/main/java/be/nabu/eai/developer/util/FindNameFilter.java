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
		if (newValue == null || newValue.trim().isEmpty()) {
			return true;
		}
		boolean useRegex = this.useRegex;
		if (useRegex && (newValue.contains("*") || newValue.contains("^") || newValue.contains("$"))) {
			useRegex = true;
			newValue = newValue.toLowerCase().replace("*", ".*");
			// what often happens is you start typing multiple space separated words (for non-regex search), then add a $ at the end
			// this however breaks because the spaces are no longer valid in regex shizzle
			newValue = newValue.replaceAll("[\\s]+", ".*");
			if (!newValue.startsWith("^")) {
				newValue = ".*" + newValue;
			}
			if (!newValue.endsWith("$")) {
				newValue += ".*";
			}
		}
		else {
			useRegex = false;
		}
		if (useRegex) {
			return marshallable.marshal(item).matches("(?i)" + newValue);
		}
		// if you type regular text and use spaces, we match all parts
		else {
			String marshal = marshallable.marshal(item).toLowerCase();
			boolean matches = true;
			for (String part : newValue.toLowerCase().split("[\\s]+")) {
				if (!marshal.contains(part)) {
					matches = false;
					break;
				}
			}
			return matches;
		}
	}

}
