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
