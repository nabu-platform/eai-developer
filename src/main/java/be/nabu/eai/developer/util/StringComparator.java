package be.nabu.eai.developer.util;

import java.util.Comparator;

public class StringComparator implements Comparator<String> {

	@Override
	public int compare(String o1, String o2) {
		if (o1 == null) {
			if (o2 == null) {
				return 0;
			}
			else {
				return -1;
			}
		}
		else if (o2 == null) {
			return 1;
		}
		return o1.compareTo(o2);
	}

}
