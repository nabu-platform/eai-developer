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
		if (useRegex && (newValue.contains("*") || newValue.contains("^") || newValue.contains("$") || newValue.contains("."))) {
			useRegex = true;
			// if you type "node.list" you want to hit "node.services.list" but not "nodeAddress.services.list"
			newValue = newValue.replaceAll("([\\w]+)\\.([\\w]+)", "$1\\\\.([\\\\w]+\\\\.|)$2");
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
