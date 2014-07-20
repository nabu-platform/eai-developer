package be.nabu.eai.developer.managers.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
		this.values.addAll((Collection<? extends ObservableValue<? extends T>>) Arrays.asList(values));
		for (ObservableValue<? extends T> property : values) {
			property.addListener(this);
		}
		setMinMax();
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
		min.set(currentMin);
		max.set(currentMax);
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
