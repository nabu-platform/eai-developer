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

import java.util.Collection;
import java.util.Iterator;

import be.nabu.eai.developer.api.FindFilter;
import be.nabu.jfx.control.tree.Marshallable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

// leaving it in for backwards compatibility, old web applications will still be using it
// remove in future release
@Deprecated
public class FindInFiles<T> {
	
	private Marshallable<T> marshallable;
	private FindFilter<T> filter;
	private SimpleObjectProperty<T> selected = new SimpleObjectProperty<T>();
	private ListView<T> list;
	
	public FindInFiles(Marshallable<T> marshallable) {
		this(marshallable, new FindNameFilter<T>(marshallable, true));
	}
	
	public FindInFiles(Marshallable<T> marshallable, FindFilter<T> filter) {
		this.marshallable = marshallable;
		this.filter = filter;
	}
	
	public void show(Collection<T> items) {
		VBox box = new VBox();
		TextField field = new TextField();
		list = new ListView<T>();
		list.setCellFactory(new Callback<ListView<T>, ListCell<T>>(){
            @Override
            public ListCell<T> call(ListView<T> p) {
                return new ListCell<T>(){
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        setText(item == null ? null : marshallable.marshal(item));
                    }
                };
            }
        });
		list.getItems().addAll(items);
		box.getChildren().addAll(field, list);
		box.setMinWidth(750d);
		box.setPrefWidth(750d);
		final Stage stage = EAIDeveloperUtils.buildPopup("Find In Files", box);
		field.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					T selectedItem = list.getSelectionModel().getSelectedItem();
					// if we have selected something, open it
					if (selectedItem != null) {
						selected.set(selectedItem);
						stage.close();
					}
					// otherwise, trigger the search
					else {
						String newValue = field.getText();
						// always re-add all the items
						list.getItems().clear();
						list.getItems().addAll(items);
						if (newValue != null && !newValue.trim().isEmpty()) {
							Iterator<T> iterator = list.getItems().iterator();
							while(iterator.hasNext()) {
								if (!filter.accept(iterator.next(), newValue)) {
									iterator.remove();
								}
							}		
						}
					}
					event.consume();
				}
				else if (event.getCode() == KeyCode.DOWN) {
					list.getSelectionModel().selectNext();
					event.consume();
				}
				else if (event.getCode() == KeyCode.UP) {
					list.getSelectionModel().selectPrevious();
					event.consume();
				}
			}
		});
		list.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				T selectedItem = list.getSelectionModel().getSelectedItem();
				if (event.getClickCount() > 1) {
					if (selectedItem != null) {
						selected.set(selectedItem);
						stage.close();
					}
					event.consume();
				}
			}
		});
		list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<T>() {
			@Override
			public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
				selected.set(newValue);
			}
		});
	}
	
	public ReadOnlyObjectProperty<T> selectedItemProperty() {
		return selected;
	}
	
}
