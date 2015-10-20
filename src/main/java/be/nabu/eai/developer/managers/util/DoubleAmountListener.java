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
