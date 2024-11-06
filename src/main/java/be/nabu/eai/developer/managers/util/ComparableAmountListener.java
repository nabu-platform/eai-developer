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

package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class ComparableAmountListener<T extends Comparable<T>> implements ChangeListener<T> {

	private ObjectProperty<T> min = new SimpleObjectProperty<T>();
	private ObjectProperty<T> max = new SimpleObjectProperty<T>();
	
	private List<ObservableValue<? extends T>> values = new ArrayList<ObservableValue<? extends T>>();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ComparableAmountListener(ObservableValue...values) {
		List list = Arrays.asList(values);
		this.values.addAll(list);
		for (ObservableValue<? extends T> property : values) {
			property.addListener(this);
		}
		setMinMax();
	}
	
	protected void setMinValue(T currentMin) {
		min.set(currentMin);
	}
	
	protected void setMaxValue(T currentMax) {
		max.set(currentMax);
	}
	
	private void setMinMax() {
		T currentMin = null;
		T currentMax = null;
		for (ObservableValue<? extends T> property : values) {
			T current = property.getValue();
			if (current != null) {
				if (currentMin == null || current.compareTo(currentMin) < 0) {
					currentMin = current;
				}
				if (currentMax == null || current.compareTo(currentMax) > 0) {
					currentMax = current;
				}
			}
		}
		setMinValue(currentMin);
		setMaxValue(currentMax);
	}
	@Override
	public void changed(ObservableValue<? extends T> arg0, T arg1, T arg2) {
		setMinMax();
	}

	public ReadOnlyObjectProperty<T> minProperty() {
		return min;
	}
	public ReadOnlyObjectProperty<T> maxProperty() {
		return max;
	}
}
