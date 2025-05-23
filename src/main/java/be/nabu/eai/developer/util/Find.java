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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.FindFilter;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.StringMarshallable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

public class Find<T> {
	
	private Marshallable<T> marshallable;
	private FindFilter<T> filter;
	private SimpleObjectProperty<T> selected = new SimpleObjectProperty<T>();
	private SimpleObjectProperty<T> finalSelected = new SimpleObjectProperty<T>();
	private ListView<T> list = new ListView<T>();
	private Stage stage;
	private TextField field = new TextField();
	// if we have a heavy search, you have to trigger it explicitly
	private boolean heavySearch;
	private String lastSearch;
	private Node additional;
	private Collection<T> items;
	
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
	
	public void focus() {
		if (stage != null && stage.isShowing()) {
			stage.requestFocus();
			field.selectAll();
			field.requestFocus();
		}
	}
	
	public void show(Collection<T> items) {
		this.show(items, "Find");
	}
	
	public void show(Collection<T> items, String title) {
		show(items, title, MainController.getInstance().getStage());
	}
	
	public void show(Collection<T> items, String title, Stage owner) {
		this.items = items;
		VBox box = new VBox();
		if (list.getCellFactory() == null) {
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
		}
		list.getItems().addAll(items);
		VBox.setVgrow(list, Priority.SOMETIMES);
		
		HBox input = new HBox();
		input.setAlignment(Pos.CENTER_LEFT);
		input.setPadding(new Insets(10));
		input.getStyleClass().add("find-input");
		Label inputLabel = new Label("Find:");
		inputLabel.setPadding(new Insets(4, 10, 0, 5));
		inputLabel.getStyleClass().add("find-input-label");
		field.getStyleClass().add("find-input-text");
		input.getChildren().addAll(inputLabel, field);
		box.getChildren().add(input);
		HBox.setHgrow(field, Priority.ALWAYS);
		
		if (additional != null) {
			box.getChildren().add(additional);
		}
		
		list.getStyleClass().add("find-list");
		
		box.getChildren().addAll(list);
		box.setMinWidth(750d);
		box.setPrefWidth(750d);
		
		HBox actions = new HBox();
		actions.setPadding(new Insets(10));
		actions.setAlignment(Pos.CENTER_RIGHT);
		actions.getStyleClass().add("find-actions");
		if (heavySearch) {
			Button searchButton = new Button("Find");
			searchButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					filter(list, items, field.getText(), false);
				}
			});
			actions.getChildren().addAll(searchButton);
		}
		Button closeButton = new Button("Close");
		actions.getChildren().addAll(closeButton);
		box.getChildren().addAll(actions);

		stage = EAIDeveloperUtils.buildPopup(title, box, owner, null, true);
		EventHandler<KeyEvent> keyEventHandler = new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.ENTER) {
					// second enter, open it
					if (heavySearch && (lastSearch == null || !lastSearch.equals(field.getText()))) {
						filter(list, items, field.getText(), false);
					}
					else {
						T selectedItem = list.getSelectionModel().getSelectedItem();
						if (selectedItem != null) {
							selected.set(selectedItem);
							finalSelected.set(selectedItem);
							stage.close();
						}
					}
					event.consume();
				}
				else if (event.getCode() == KeyCode.DOWN) {
					if (list.getSelectionModel().getSelectedItem() == null) {
						list.getSelectionModel().select(list.getItems().get(0));
					}
					else {
						int indexOf = list.getItems().indexOf(list.getSelectionModel().getSelectedItem());
						if (indexOf < list.getItems().size() - 1) {
							list.getSelectionModel().selectNext();
						}
					}
					event.consume();
				}
				else if (event.getCode() == KeyCode.UP) {
					if (list.getSelectionModel().getSelectedItem() == null) {
						list.getSelectionModel().select(list.getItems().get(0));
					}
					else {
						int indexOf = list.getItems().indexOf(list.getSelectionModel().getSelectedItem());
						if (indexOf >= 1) {
							list.getSelectionModel().selectPrevious();
						}
					}
					event.consume();
				}
				else if (event.getCode() == KeyCode.ESCAPE) {
					stage.close();
				}
				else if (event.getCode() == KeyCode.F && event.isControlDown()) {
					field.selectAll();
					field.requestFocus();
				}
			}
		};
		field.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
		
		// if not heavy, we trigger search on change
		if (!heavySearch) {
			field.textProperty().addListener(new ChangeListener<String>() {
				@Override
				public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
					filter(list, items, newValue, false);
				}
			});
		}
		
		list.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				T selectedItem = list.getSelectionModel().getSelectedItem();
				if (event.getClickCount() > 1) {
					if (selectedItem != null) {
						selected.set(selectedItem);
						finalSelected.set(selectedItem);
						stage.close();
					}
					event.consume();
				}
			}
		});
		list.addEventHandler(KeyEvent.KEY_PRESSED, keyEventHandler);
		list.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<T>() {
			@Override
			public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
				selected.set(newValue);
			}
		});
		closeButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
	}
	
	public void refilter() {
		filter(list, items, field.getText(), true);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void filter(ListView<T> list, Collection<T> items, String filterValue, boolean force) {
		if (lastSearch == null || !lastSearch.equals(filterValue) || force) {
			lastSearch = filterValue;
			// do all the logic outside of listview, otherwise it triggers... a lot
			List<T> toFilter = new ArrayList<T>(items);
			
			// filter current list
			Iterator<T> iterator = toFilter.iterator();
			while(iterator.hasNext()) {
				if (!filter.accept(iterator.next(), filterValue)) {
					iterator.remove();
				}
			}
			if (!toFilter.isEmpty() && toFilter.get(0) instanceof Comparable) {
				Collections.sort((List<? extends Comparable>) toFilter);
			}
			list.getItems().clear();
			list.getItems().addAll(toFilter);
		}
	}
	
	public ReadOnlyObjectProperty<T> selectedItemProperty() {
		return selected;
	}
	
	public ReadOnlyObjectProperty<T> finalSelectedItemProperty() {
		return finalSelected;
	}

	public ListView<T> getList() {
		return list;
	}
	
	public TextField getField() {
		return field;
	}

	public Stage getStage() {
		return stage;
	}

	public boolean isHeavySearch() {
		return heavySearch;
	}

	public void setHeavySearch(boolean heavySearch) {
		this.heavySearch = heavySearch;
	}
	
	public void close() {
		if (this.stage != null) {
			this.stage.close();
		}
	}

	public Node getAdditional() {
		return additional;
	}

	public void setAdditional(Node additional) {
		this.additional = additional;
	}
}
