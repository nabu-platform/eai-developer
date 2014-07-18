package be.nabu.eai.developer.managers.util;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.transform.Transform;

public class RelativeLocationListener implements ChangeListener<Transform> {

	private DoubleProperty x = new SimpleDoubleProperty();
	private DoubleProperty y = new SimpleDoubleProperty();
	
	public RelativeLocationListener(ReadOnlyObjectProperty<Transform> transform) {
		// initial values
		x.set(transform.get().getTx());
		y.set(transform.get().getTy());
		// listen for updates
		transform.addListener(this);
	}

	@Override
	public void changed(ObservableValue<? extends Transform> arg0, Transform arg1, Transform newValue) {
		x.set(newValue.getTx());
		y.set(newValue.getTy());
	}

	public ReadOnlyDoubleProperty xProperty() {
		return x;
	}
	public ReadOnlyDoubleProperty yProperty() {
		return y;
	}
}
