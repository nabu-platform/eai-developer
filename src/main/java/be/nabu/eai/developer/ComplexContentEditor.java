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

package be.nabu.eai.developer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import be.nabu.eai.api.NamingConvention;
import be.nabu.eai.developer.MainController.SinglePropertyDrawer;
import be.nabu.eai.developer.managers.base.BaseConfigurationGUIManager;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.ElementTreeItem;
import be.nabu.eai.repository.api.Repository;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeCellValue;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.TreeUtils.TreeItemCreator;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.properties.CommentProperty;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.validator.api.ValidationMessage;

public class ComplexContentEditor {
	
	private List<Property<?>> properties;
	private boolean updateChanged;
	private Repository repository;
	private ComplexContent content;
	private Tree<ValueWrapper> tree;
	private String sourceId;
	private Map<String, Object> state = new HashMap<String, Object>();
	private List<AddHandler> addHandlers = new ArrayList<AddHandler>();
	private boolean prettifyLabels = true;
	private boolean prefillBooleans = false;

	public ComplexContentEditor(ComplexContent content, boolean updateChanged, Repository repository) {
		this.content = content;
		this.updateChanged = updateChanged;
		this.repository = repository;
		properties = BaseConfigurationGUIManager.createProperty(new ComplexElementImpl(content.getType(), null));
	}
	
	public Tree<ValueWrapper> getTree() {
		if (tree == null) {
			tree = new Tree<ValueWrapper>(new ValueWrapperCellFactory());
			tree.rootProperty().set(new ValueWrapperItem(null, new ValueWrapper(null, new ComplexElementImpl(content.getType(), null), content, null)));
		}
		return tree;
	}
	
	public Map<String, Object> getState() {
		return state;
	}
	
	public ComplexContent getContent() {
		return content;
	}
	
	public void addHandler(AddHandler...addHandlers) {
		this.addHandlers.addAll(Arrays.asList(addHandlers));
	}
	
	public static interface AddHandler {
		public String getName();
		public boolean affects(TreeItem<ValueWrapper> item);
		public ComplexContent newInstance(TreeItem<ValueWrapper> item);
	}
	
	private void prettifyLabel(Label labelToStyle, boolean hasValue) {
		if (prettifyLabels) {
			String originalText = ((Label) labelToStyle).getText();
			String newText = NamingConvention.UPPER_TEXT.apply(originalText) + (hasValue ? ":" : "");
			((Label) labelToStyle).setText(newText);
			labelToStyle.setStyle("-fx-text-fill: #666666");
		}
	}
	
	public void update() {
		// do nothing
	}
	
	public class ValueWrapperCellFactory implements Callback<TreeItem<ValueWrapper>, TreeCellValue<ValueWrapper>> {
		@Override
		public TreeCellValue<ValueWrapper> call(TreeItem<ValueWrapper> item) {
			return new TreeCellValue<ValueWrapper>() {
				private ObjectProperty<TreeCell<ValueWrapper>> cell = new SimpleObjectProperty<TreeCell<ValueWrapper>>();
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Region getNode() {
					final HBox box = new HBox();
					box.setAlignment(Pos.CENTER_LEFT);
					
					HBox.setHgrow(box, Priority.ALWAYS);
					Label labelToGrow = null;
					if (item.itemProperty().get().getParent() == null || item.itemProperty().get().getElement().getType() instanceof ComplexType) {
						Label label = new Label(item.itemProperty().get().getElement().getName());
						labelToGrow = label;
						prettifyLabel(label, false);
						box.getChildren().add(label);
					}
					else {
						SinglePropertyDrawer drawer = new SinglePropertyDrawer() {
							@Override
							public void draw(Property<?> property, Node label, Node value, Node additional) {
								box.getChildren().clear();
								box.getChildren().addAll(label, value);
								if (additional != null) {
									box.getChildren().add(additional);
								}
								
								Label labelToStyle = null;
								if (label instanceof Label) {
									labelToStyle = (Label) label;
								}
								else {
									labelToStyle = (Label) label.lookup("#property-name");
								}
								
								HBox.setHgrow(value, Priority.ALWAYS);
								if (label instanceof Label) {
									((Label) label).setPadding(new Insets(4, 10, 0, 5));
									((Label) label).setMinWidth(150);
									if (((Label) label).getText().endsWith("*")) {
										((Label) label).setText(((Label) label).getText().replaceAll("[\\s]*\\*$", ""));
									}
//									((Label) label).setAlignment(Pos.CENTER_RIGHT);
								}
								
								if (labelToStyle != null) {
									prettifyLabel(labelToStyle, true);
								}
							}

						};
						Property<?> property = item.itemProperty().get().getProperty();
						SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new HashSet(Arrays.asList(property)), new ValueImpl(property, item.itemProperty().get().getValue())) {
							@Override
							public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
								item.itemProperty().get().setValue(value);
								List<ValidationMessage> updateProperty = super.updateProperty(property, value);
								update();
								return updateProperty;
							}
						};
						updater.setSourceId(sourceId);
						MainController.getInstance().drawSingleProperty(updater, property, null, drawer, repository, updateChanged);
					}
					// add buttons to manipulate if you are not on the root
					if (item.itemProperty().get().getParent() != null) {
						boolean isList = item.itemProperty().get().getElement().getType().isList(item.itemProperty().get().getElement().getProperties());
						if (item.itemProperty().get().getElement().getType() instanceof ComplexType && item.itemProperty().get().getValue() == null) {
							boolean found = false;
							for (final AddHandler addHandler : addHandlers) {
								if (addHandler.affects(item)) {
									found = true;
									Button addButton = new Button(addHandler.getName());
									addButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent arg0) {
											ComplexContent newInstance = addHandler.newInstance(item);
											setInstanceInEmpty(item, isList, newInstance);
										}
									});
									box.getChildren().addAll(addButton);
								}
							}
							if (!found) {
								Button addButton = new Button();
								addButton.setGraphic(MainController.loadFixedSizeGraphic("icons/add.png", 12));
								addButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										ComplexContent newInstance = ((ComplexType) item.itemProperty().get().getElement().getType()).newInstance();
										setInstanceInEmpty(item, isList, newInstance);
									}
								});
								box.getChildren().addAll(addButton);
							}
						}
						else if (!isList && item.itemProperty().get().getElement().getType() instanceof ComplexType && item.itemProperty().get().getValue() != null) {
							Button removeButton = new Button();
							removeButton.setGraphic(MainController.loadFixedSizeGraphic("icons/delete.png", 12));
							removeButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									List<ValueWrapper> children = item.itemProperty().get().getParent().getChildren();
									int indexOf = children.indexOf(item.itemProperty().get());
									ValueWrapper element = new ValueWrapper(item.itemProperty().get().getParent(), item.itemProperty().get().getElement(), null, null);
									children.set(indexOf, element);
									element.save();
									if (cell.get().getParent() != null) {
										cell.get().getParent().refresh();
									}
									update();
									if (updateChanged) {
										MainController.getInstance().setChanged();
									}
								}
							});
							box.getChildren().addAll(removeButton);
						}
						else if (isList) {
							boolean found = false;
							for (final AddHandler addHandler : addHandlers) {
								if (addHandler.affects(item)) {
									found = true;
									Button addButton = new Button(addHandler.getName());
									addButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent arg0) {
											Object value = addHandler.newInstance(item);
											addNewInList(item, value);
										}
									});
									box.getChildren().addAll(addButton);
								}
							}
							if (!found) {
								Button addButton = new Button();
								addButton.setGraphic(MainController.loadFixedSizeGraphic("icons/add.png", 12));
								addButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
									@Override
									public void handle(ActionEvent arg0) {
										Object value = item.itemProperty().get().getElement().getType() instanceof ComplexType ? ((ComplexType) item.itemProperty().get().getElement().getType()).newInstance() : null;
										addNewInList(item, value);
									}
								});
								box.getChildren().add(addButton);
							}
							
							// if we are not the topmost element, have a button to move down
							Button moveUpButton = new Button();
							moveUpButton.setGraphic(MainController.loadFixedSizeGraphic("move/up.png", 12));
							moveUpButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									List<ValueWrapper> list = getList(item);
									List<ValueWrapper> children = item.itemProperty().get().getParent().getChildren();
									int listIndex = list.indexOf(item.itemProperty().get());
									int childIndex = children.indexOf(item.itemProperty().get());
									if (listIndex > 0) {
										// unset the list
										if (item.itemProperty().get().getParent() != null) {
											item.itemProperty().get().getParent().getComplex().set(item.itemProperty().get().getElement().getName(), null);
										}
										ValueWrapper removed = children.remove(childIndex - 1);
										item.itemProperty().get().setIndex(listIndex - 1);
										removed.setIndex(listIndex);
										// save them all!
										for (ValueWrapper sibling : children) {
											if (sibling.getElement().equals(item.itemProperty().get().getElement())) {
												sibling.save();
											}
										}
										// if we simply add it first and refresh, nothing happens, refresh before adding as well...
										// tis not ideal...
										if (cell.get().getParent() != null) {
											cell.get().getParent().refresh();
										}
										children.add(childIndex, removed);
										removed.save();
										if (cell.get().getParent() != null) {
											cell.get().getParent().refresh();
										}
										ensureExpand(cell.get().getParent());
										update();
										if (updateChanged) {
											MainController.getInstance().setChanged();
										}
									}
								}
							});
							box.getChildren().add(moveUpButton);
							
							// if we are not the topmost element, have a button to move down
							Button moveDownButton = new Button();
							moveDownButton.setGraphic(MainController.loadFixedSizeGraphic("move/down.png", 12));
							moveDownButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									List<ValueWrapper> list = getList(item);
									List<ValueWrapper> children = item.itemProperty().get().getParent().getChildren();
									int listIndex = list.indexOf(item.itemProperty().get());
									int childIndex = children.indexOf(item.itemProperty().get());
									if (listIndex < list.size() - 1) {
										// unset the list
										if (item.itemProperty().get().getParent() != null) {
											item.itemProperty().get().getParent().getComplex().set(item.itemProperty().get().getElement().getName(), null);
										}
										ValueWrapper removed = children.remove(childIndex + 1);
										item.itemProperty().get().setIndex(listIndex + 1);
										removed.setIndex(listIndex);
										// save them all!
										for (ValueWrapper sibling : children) {
											if (sibling.getElement().equals(item.itemProperty().get().getElement())) {
												sibling.save();
											}
										}
										if (cell.get().getParent() != null) {
											cell.get().getParent().refresh();
										}
										children.add(childIndex, removed);
										removed.save();
										if (cell.get().getParent() != null) {
											cell.get().getParent().refresh();
										}
										ensureExpand(cell.get().getParent());
										update();
										if (updateChanged) {
											MainController.getInstance().setChanged();
										}
									}
								}
							});
							box.getChildren().add(moveDownButton);
							
							Button removeButton = new Button();
							removeButton.setGraphic(MainController.loadFixedSizeGraphic("icons/delete.png", 12));
							removeButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									// unset the list
									if (item.itemProperty().get().getParent() != null) {
										item.itemProperty().get().getParent().getComplex().set(item.itemProperty().get().getElement().getName(), null);
									}
									// manipulate the wrappers
									List<ValueWrapper> siblings = item.itemProperty().get().getParent().getChildren();
									int childIndex = siblings.indexOf(item.itemProperty().get());
									ValueWrapper next = siblings.size() > childIndex + 1 ? siblings.get(childIndex + 1) : null;
									ValueWrapper previous = childIndex > 0 ? siblings.get(childIndex - 1) : null;
									// if we have other siblings of the same type, just remove this one
									if ((next != null && next.getElement().equals(item.itemProperty().get().getElement())) || (previous != null && previous.getElement().equals(item.itemProperty().get().getElement()))) {
										siblings.remove(childIndex);
										// subtract subsequent indexes
										for (int i = childIndex; i < siblings.size(); i++) {
											if (siblings.get(i).getElement().equals(item.itemProperty().get().getElement())) {
												siblings.get(i).setIndex(siblings.get(i).getIndex() - 1);
											}
										}
									}
									// just unset the value
									else {
										// index has to be 0
										if (item.itemProperty().get().getIndex() == null || item.itemProperty().get().getIndex() != 0) {
											throw new RuntimeException("Wrong index: " + item.itemProperty().get().getIndex());
										}
										// set a new wrapper to trigger a reload (otherwise it won't refresh)
										ValueWrapper element = new ValueWrapper(item.itemProperty().get().getParent(), item.itemProperty().get().getElement(), null, 0);
										siblings.set(childIndex, element);
										element.save();
									}
									// save them all!
									for (ValueWrapper sibling : siblings) {
										if (sibling.getElement().equals(item.itemProperty().get().getElement())) {
											sibling.save();
										}
									}
									if (cell.get().getParent() != null) {
										cell.get().getParent().refresh();
									}
									ensureExpand(cell.get().getParent());
									update();
									if (updateChanged) {
										MainController.getInstance().setChanged();
									}
								}
							});
							box.getChildren().addAll(removeButton);
						}
						if (labelToGrow != null && box.getChildren().size() >= 2) {
							labelToGrow.setPadding(new Insets(4, 10, 0, 5));
						}
						box.prefWidthProperty().bind(tree.prefWidthProperty().subtract(25));
					}
					return box;
				}
				@Override
				public ObjectProperty<TreeCell<ValueWrapper>> cellProperty() {
					return cell;
				}
				@Override
				public void refresh() {
					// do nothing?
				}
			};
		}
		
	}

	private static List<ValueWrapper> getList(TreeItem<ValueWrapper> item) {
		List<ValueWrapper> siblings = new ArrayList<ValueWrapper>(item.itemProperty().get().getParent().getChildren());
		Iterator<ValueWrapper> iterator = siblings.iterator();
		while (iterator.hasNext()) {
			ValueWrapper next = iterator.next();
			if (!next.getElement().equals(item.itemProperty().get().getElement())) {
				iterator.remove();
			}
		}
		return siblings;
	}
	
	// make sure the parent stays expanded? when adding quickly it sometimes collapses
	private void ensureExpand(TreeCell<?> cell) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (cell != null) {
					cell.expandedProperty().set(true);
				}
				tree.getRootCell().expandedProperty().set(true);
			}
		});
	}
	
	private void setInstanceInEmpty(TreeItem<ValueWrapper> item, boolean isList, ComplexContent newInstance) {
		List<ValueWrapper> children = item.itemProperty().get().getParent().getChildren();
		int indexOf = children.indexOf(item.itemProperty().get());
		ValueWrapper element = new ValueWrapper(item.itemProperty().get().getParent(), item.itemProperty().get().getElement(), newInstance, isList ? 0 : null);
		children.set(indexOf, element);
		element.save();
		TreeCell<ValueWrapper> treeCell = getTree().getTreeCell(item);
		if (treeCell.getParent() != null) {
			treeCell.getParent().refresh();
			expandNew(item.getParent(), newInstance, treeCell.getTree());
			ensureExpand(treeCell.getParent());
		}
		update();
		if (updateChanged) {
			MainController.getInstance().setChanged();
		}
	}

	private void expandNew(TreeItem<ValueWrapper> parent, Object newInstance, Tree<ValueWrapper> tree) {
		// expand the new cell?
		for (TreeItem<ValueWrapper> child : parent.getChildren()) {
			if (newInstance.equals(child.itemProperty().get().getValue())) {
				TreeCell<ValueWrapper> treeCell2 = tree.getTreeCell(child);
				if (treeCell2 != null) {
					treeCell2.expandedProperty().set(true);
				}
			}
		}
	}
	
	private void addNewInList(TreeItem<ValueWrapper> item, Object value) {
		int childIndex = item.itemProperty().get().getParent().getChildren().indexOf(item.itemProperty().get());
		ValueWrapper element = new ValueWrapper(item.itemProperty().get().getParent(), item.itemProperty().get().getElement(), value, item.itemProperty().get().getIndex() + 1);
		item.itemProperty().get().getParent().getChildren().add(childIndex + 1, element);
		// increase the index of all the children
		for (int i = childIndex + 2; i < item.itemProperty().get().getParent().getChildren().size(); i++) {
			ValueWrapper next = item.itemProperty().get().getParent().getChildren().get(i);
			if (next.getElement().equals(item.itemProperty().get().getElement())) {
				next.setIndex(next.getIndex() + 1);
			}
		}
		element.save();
		TreeCell<ValueWrapper> treeCell = getTree().getTreeCell(item);
		if (treeCell.getParent() != null) {
			treeCell.getParent().refresh();
			if (value != null) {
				expandNew(item.getParent(), value, treeCell.getTree());
			}
			ensureExpand(treeCell.getParent());
		}
		update();
		if (updateChanged) {
			MainController.getInstance().setChanged();
		}
	}
	
	public class ValueWrapperItem implements TreeItem<ValueWrapper> {
		private SimpleBooleanProperty editable = new SimpleBooleanProperty(true);
		private SimpleBooleanProperty leaf;
		private SimpleObjectProperty<ValueWrapper> item;
		private SimpleObjectProperty<Node> graphic;
		private ValueWrapperItem parent;
		private ObservableList<TreeItem<ValueWrapper>> children;
		
		public ValueWrapperItem(ValueWrapperItem parent, ValueWrapper wrapper) {
			this.parent = parent;
			leaf = new SimpleBooleanProperty(!(wrapper.getElement().getType() instanceof ComplexType));
			item = new SimpleObjectProperty<ValueWrapper>(wrapper);
			HBox graphicBox = new HBox();
			graphicBox.setAlignment(Pos.CENTER);
			graphicBox.getChildren().add(MainController.loadGraphic(ElementTreeItem.getIcon(wrapper.getElement().getType(), wrapper.getElement().getProperties())));
			Integer minOccurs = ValueUtils.contains(MinOccursProperty.getInstance(), wrapper.getElement().getProperties()) 
				? ValueUtils.getValue(MinOccursProperty.getInstance(), wrapper.getElement().getProperties()) 
				: null;
			if (minOccurs == null || minOccurs > 0) {
				graphicBox.getChildren().add(MainController.loadGraphic("types/mandatory.png"));
			}
			else {
				graphicBox.getChildren().add(MainController.loadGraphic("types/optional.png"));
			}
			graphic = new SimpleObjectProperty<Node>(graphicBox);
		}
		
		@Override
		public void refresh() {
			if (!leaf.get()) {
				List<ValueWrapper> children = item.get().getChildren();
				if (children != null) {
					TreeUtils.refreshChildren(new TreeItemCreator<ValueWrapper>() {
						@Override
						public TreeItem<ValueWrapper> create(TreeItem<ValueWrapper> parent, ValueWrapper child) {
							return new ValueWrapperItem((ValueWrapperItem) parent, child);
						}
					}, this, children);
				}
				else {
					getChildren().clear();
				}
			}
		}
		@Override
		public BooleanProperty editableProperty() {
			return editable;
		}
		@Override
		public BooleanProperty leafProperty() {
			return leaf;
		}
		@Override
		public ObjectProperty<ValueWrapper> itemProperty() {
			return item;
		}
		@Override
		public ObjectProperty<Node> graphicProperty() {
			return graphic;
		}
		@Override
		public ObservableList<TreeItem<ValueWrapper>> getChildren() {
			if (children == null) {
				children = FXCollections.observableArrayList();
				refresh();
			}
			return children;
		}
		@Override
		public ValueWrapperItem getParent() {
			return parent;
		}
		@Override
		public String getName() {
			return item.get().getElement().getName();
		}
	}
	
	public class ValueWrapper {
		private Object value;
		private Element<?> element;
		private Property<?> property;
		private ValueWrapper parent;
		private List<ValueWrapper> children;
		private Integer index;
		
		public ValueWrapper(ValueWrapper parent, Element<?> element, Object value, Integer index) {
			this.parent = parent;
			this.element = element;
			this.value = value;
			this.index = index;
			// check if we have to prefill the booleans
			boolean prefillIt = prefillBooleans && element.getType() instanceof SimpleType && Boolean.class.isAssignableFrom(((SimpleType<?>) element.getType()).getInstanceClass());
			// if so, check that it is mandatory
			if (prefillIt) {
				Integer minOccurs = ValueUtils.getValue(MinOccursProperty.getInstance(), element.getProperties());
				prefillIt = minOccurs == null || minOccurs >= 1;
			}
			if (this.value == null && (state.containsKey(getPath(false)) || prefillIt)) {
				Object currentValue = state.get(getPath(false));
				// since we updated mandatory booleans to be checkboxes, they "appear" false but are actually null when first loaded
				// this can be frustrating...
				if (currentValue == null && prefillIt) {
					currentValue = false;
				}
				if (currentValue != null) {
					Object converted = getProperty().getValueClass().isAssignableFrom(currentValue.getClass()) 
						? currentValue
						: ConverterFactory.getInstance().getConverter().convert(currentValue, getProperty().getValueClass());
					if (converted != null) {
						this.value = converted;
						save();
					}
				}
			}
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
			save();
		}
		@SuppressWarnings("unchecked")
		private ComplexContent getComplex() {
			return value instanceof ComplexContent ? (ComplexContent) value : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(value);
		}
		public void save() {
			if (parent != null) {
				getParent().getComplex().set(getName(), value);
			}
			if (!(value instanceof ComplexContent)) {
				state.put(getPath(false), value);
			}
		}
		public ValueWrapper getParent() {
			return parent;
		}
		public void setParent(ValueWrapper parent) {
			this.parent = parent;
		}
		public Element<?> getElement() {
			return element;
		}
		public void setElement(Element<?> element) {
			this.element = element;
		}
		
		public String getPath(boolean includeRoot) {
			String path = getName();
			if (parent != null && (parent.getParent() != null || includeRoot)) {
				path = parent.getPath(includeRoot) + "/" + path;
			}
			return path;
		}
		
		public Integer getIndex() {
			return index;
		}
		public void setIndex(Integer index) {
			this.index = index;
			save();
		}
		@SuppressWarnings("rawtypes")
		public Property<?> getProperty() {
			if (property == null) {
				for (Property<?> potential : properties) {
					if (potential.getName().replaceAll("\\[[^\\]]+\\]", "").equals(getPath(false).replaceAll("\\[[^\\]]+\\]", ""))) {
						SimpleProperty copy = ((SimpleProperty) potential).clone();
						copy.setName(element.getName());
						copy.setTitle(ValueUtils.getValue(CommentProperty.getInstance(), element.getProperties()));
						property = copy;
					}
				}
				if (property == null && getParent() != null && getParent().getElement().getType() instanceof ComplexType) {
					for (Property<?> potential : BaseConfigurationGUIManager.createProperty(new ComplexElementImpl(getParent().getComplex().getType(), null))) {
						if (potential.getName().replaceAll("\\[[^\\]]+\\]", "").equals(element.getName())) {
							SimpleProperty copy = ((SimpleProperty) potential).clone();
							copy.setName(element.getName());
							copy.setTitle(ValueUtils.getValue(CommentProperty.getInstance(), element.getProperties()));
							property = copy;
						}
					}
				}
				if (property == null) {
					throw new IllegalArgumentException("Could not find property in parent for: " + getPath(false));
				}
			}
			return property;
		}
		public String getName() {
			String name = element.getName();
			if (index != null) {
				name += "[" + index + "]";
			}
			return name;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public List<ValueWrapper> getChildren() {
			if (children == null && value != null) {
				children = new ArrayList<ValueWrapper>();
				if (getElement().getType() instanceof ComplexType) {
					// use the runtime type, not the definition, could be an extension
					ComplexContent content = value instanceof ComplexContent ? (ComplexContent) value : ComplexContentWrapperFactory.getInstance().getWrapper().wrap(value);
					for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
						Object value = getValue() == null ? null : content.get(child.getName());
						if (child.getType().isList(child.getProperties())) {
							if (value == null) {
								children.add(new ValueWrapper(this, child, null, 0));
							}
							else {
								CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
								// this can happen for example when you update a single value to a list, in most cases an array list will suffice...
								if (handler == null) {
									ArrayList list = new ArrayList();
									list.add(value);
									value = list;
									handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
								}
								int index = 0;
								for (Object childValue : handler.getAsCollection(value)) {
									children.add(new ValueWrapper(this, child, childValue, index++));
								}
								// collection was empty
								if (index == 0) {
									children.add(new ValueWrapper(this, child, null, 0));
								}
							}
						}
						else {
							children.add(new ValueWrapper(this, child, value, null));
						}
					}
				}
			}
			return children;
		}	
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public boolean isPrefillBooleans() {
		return prefillBooleans;
	}

	public void setPrefillBooleans(boolean prefillBooleans) {
		this.prefillBooleans = prefillBooleans;
	}
	
}
