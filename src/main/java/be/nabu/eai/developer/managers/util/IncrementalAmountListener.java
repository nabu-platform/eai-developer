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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class IncrementalAmountListener<T extends Number> {

	private ObjectProperty<T> value = new SimpleObjectProperty<T>();
	private int increment;
	
	public IncrementalAmountListener(ObservableValue<T> observable, int increment) {
		this(observable, increment, null);
	}
	
	public IncrementalAmountListener(ObservableValue<T> observable, int increment, T initial) {
		this.increment = increment;
		if (initial != null) {
			value.set(initial);
		}
		observable.addListener(new ChangeListener<T>() {
			@Override
			public void changed(ObservableValue<? extends T> arg0, T arg1, T arg2) {
				if (arg2.intValue() > 0 && (IncrementalAmountListener.this.increment == 0 || arg2.intValue() % IncrementalAmountListener.this.increment == 0)) {
					value.set(arg2);
				}
			}
		});
	}
	
	public ReadOnlyObjectProperty<T> valueProperty() {
		return value;
	}

	public int getIncrement() {
		return increment;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}
	
}
