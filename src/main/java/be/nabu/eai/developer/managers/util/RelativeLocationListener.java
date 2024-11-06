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
