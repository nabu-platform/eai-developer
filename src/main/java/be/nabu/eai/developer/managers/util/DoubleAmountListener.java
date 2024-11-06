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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

public class DoubleAmountListener extends ComparableAmountListener<Double> {

	private DoubleProperty min;
	private DoubleProperty max;
	
	public DoubleAmountListener(ObservableDoubleValue...values) {
		super(values);
	}
	
	public ReadOnlyDoubleProperty minDoubleProperty() {
		return min;
	}
	public ReadOnlyDoubleProperty maxDoubleProperty() {
		return max;
	}

	@Override
	protected void setMinValue(Double currentMin) {
		if (min == null) {
			min = new SimpleDoubleProperty();
		}
		min.set(currentMin);
	}

	@Override
	protected void setMaxValue(Double currentMax) {
		if (max == null) {
			max = new SimpleDoubleProperty();
		}
		max.set(currentMax);
	}
	
}
