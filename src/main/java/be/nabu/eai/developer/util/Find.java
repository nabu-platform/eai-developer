package be.nabu.eai.developer.util;

import java.util.Collection;
import java.util.Iterator;

import be.nabu.eai.developer.api.FindFilter;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.StringMarshallable;
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

public class Find<T> {
	
	private Marshallable<T> marshallable;
	private FindFilter<T> filter;
	private SimpleObjectProperty<T> selected = new SimpleObjectProperty<T>();
	private ListView<T> list;
	
	@SuppressWarnings("unchecked")
	public Find(Collection<String> items) {
		this((Marshallable<T>) new StringMarshallable());
	}
	
	public Find(Marshallable<T> marshallable) {
		this(marshallable, new FindNameFilter<T>(marshallable, true));
	}
	
	public Find(Marshallable<T> marshallable, FindFilter<T> filter) {
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
		final Stage stage = EAIDeveloperUtils.buildPopup("Find", box);
		field.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					T selectedItem = list.getSelectionModel().getSelectedItem();
					if (selectedItem != null) {
						selected.set(selectedItem);
						stage.close();
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
		field.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue == null || oldValue == null || !newValue.startsWith(oldValue)) {
					list.getItems().clear();
					list.getItems().addAll(items);
				}
				// filter current list
				if (newValue != null && !newValue.trim().isEmpty()) {
					Iterator<T> iterator = list.getItems().iterator();
					while(iterator.hasNext()) {
						if (!filter.accept(iterator.next(), newValue)) {
							iterator.remove();
						}
					}
				}
			}
		});
		list.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				T selectedItem = list.getSelectionModel().getSelectedItem();
				if (event.getClickCount() > 1) {
					if (selectedItem != null) {
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
