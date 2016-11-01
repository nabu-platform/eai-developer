package be.nabu.eai.developer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.util.Callback;
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
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.CollectionHandlerFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.validator.api.ValidationMessage;

public class ComplexContentEditor {
	
	private List<Property<?>> properties;
	private boolean updateChanged;
	private Repository repository;
	private ComplexContent content;
	private Tree<ValueWrapper> tree;

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
	
//	public Pane build(ComplexContent content) {
//		
//	}
//	
//	private Pane build(final ComplexContent content, final String path) {
//		VBox vbox = new VBox();
//		for (Element<?> element : TypeUtils.getAllChildren(content.getType())) {
//			HBox elementBox = new HBox();
//			final String childPath = path == null ? element.getName() : path + "/" + element.getName();
//			Object value = content.get(element.getName());
//			if (element.getType().isList(element.getProperties())) {
//				VBox listBox = new VBox();
//				HBox buttonsBox = new HBox();
//				Button button = new Button("Add " + element.getName());
//				button.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
//					@Override
//					public void handle(ActionEvent arg0) {
//						// get current size of collection
//						Object currentValue = content.get(element.getName());
//						int index;
//						if (currentValue == null) {
//							index = 0;
//						}
//						else {
//							CollectionHandlerProvider collectionHandler = CollectionHandlerFactory.getInstance().getHandler().getHandler(currentValue.getClass());
//							index = collectionHandler.getAsCollection(currentValue).size();
//						}
//						if (element.getType() instanceof ComplexType) {
//							ComplexContent newInstance = ((ComplexType) element.getType()).newInstance();
//							content.set(childPath + "[" + index + "]", newInstance);
//							listBox.getChildren().add(build(newInstance, childPath + "[" + index + "]"));
//						}
//					}
//				});
//				if (value == null) {
//					
//				}
//				else {
//					
//				}
//				CollectionHandlerProvider collectionHandler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value);
//			}
//		}
//		return vbox;
//	}
	
	public static void save(ComplexContent content, ValueWrapper wrapper) {
//		// unset everything
//		for (Element<?> child : TypeUtils.getAllChildren(content.getType())) {
//			content.set(child.getName(), null);
//		}
//		for (ValueWrapper child : wrapper.getChildren()) {
//			if (child.getValue() != null) {
//				if (child.getElement().getType() instanceof ComplexType) {
//					ComplexContent complexInstance = (ComplexContent) child.getValue();
//					save(complexInstance, child);
//					System.out.println("Setting complex " + child.getName() + " = " + complexInstance);
//					content.set(child.getName(), complexInstance);
//				}
//				else {
//					System.out.println("Setting simple " + child.getName() + " = " + child.getValue());
//					content.set(child.getName(), child.getValue());
//				}
//			}
//		}
	}
	
	public void save() {
		save(content, getTree().rootProperty().get().itemProperty().get());
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
					if (item.itemProperty().get().getParent() == null || item.itemProperty().get().getElement().getType() instanceof ComplexType) {
						box.getChildren().add(new Label(item.itemProperty().get().getElement().getName()));
					}
					else {
						SinglePropertyDrawer drawer = new SinglePropertyDrawer() {
							@Override
							public void draw(Node label, Node value, Node additional) {
								box.getChildren().clear();
								box.getChildren().addAll(label, value);
								if (additional != null) {
									box.getChildren().add(additional);
								}
							}
						};
						Property<?> property = item.itemProperty().get().getProperty();
						SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new HashSet(Arrays.asList(property)), new ValueImpl(property, item.itemProperty().get().getValue())) {
							@Override
							public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
								item.itemProperty().get().setValue(value);
								return super.updateProperty(property, value);
							}
						};
						MainController.getInstance().drawSingleProperty(updater, property, null, drawer, repository, updateChanged);
					}
					// add buttons to manipulate if you are not on the root
					if (item.itemProperty().get().getParent() != null) {
						boolean isList = item.itemProperty().get().getElement().getType().isList(item.itemProperty().get().getElement().getProperties());
						if (item.itemProperty().get().getElement().getType() instanceof ComplexType && item.itemProperty().get().getValue() == null) {
							Button addButton = new Button("Add");
							addButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									List<ValueWrapper> children = item.itemProperty().get().getParent().getChildren();
									int indexOf = children.indexOf(item.itemProperty().get());
									ValueWrapper element = new ValueWrapper(item.itemProperty().get().getParent(), item.itemProperty().get().getElement(), ((ComplexType) item.itemProperty().get().getElement().getType()).newInstance(), isList ? 0 : null);
									children.set(indexOf, element);
									element.save();
									if (cell.get().getParent() != null) {
										cell.get().getParent().refresh();
									}
									MainController.getInstance().setChanged();
								}
							});
							box.getChildren().addAll(addButton);
						}
						else if (!isList && item.itemProperty().get().getElement().getType() instanceof ComplexType && item.itemProperty().get().getValue() != null) {
							Button removeButton = new Button("Remove");
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
									MainController.getInstance().setChanged();
								}
							});
							box.getChildren().addAll(removeButton);
						}
						else if (isList) {
							Button addButton = new Button("Add");
							addButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									Object value = item.itemProperty().get().getElement().getType() instanceof ComplexType ? ((ComplexType) item.itemProperty().get().getElement().getType()).newInstance() : null;
									int childIndex = item.itemProperty().get().getParent().getChildren().indexOf(item.itemProperty().get());
									item.itemProperty().get().getParent().getChildren().add(childIndex + 1, new ValueWrapper(item.itemProperty().get().getParent(), item.itemProperty().get().getElement(), value, item.itemProperty().get().getIndex() + 1));
									// increase the index of all the children
									for (int i = childIndex + 2; i < item.itemProperty().get().getParent().getChildren().size(); i++) {
										ValueWrapper next = item.itemProperty().get().getParent().getChildren().get(i);
										System.out.println("Should increase " + next.getElement().getName() + "? " + next.getIndex());
										if (next.getElement().equals(item.itemProperty().get().getElement())) {
											next.setIndex(next.getIndex() + 1);
											System.out.println("\t" + next.getIndex());
										}
									}
									if (cell.get().getParent() != null) {
										cell.get().getParent().refresh();
									}
									MainController.getInstance().setChanged();
								}
							});
							Button removeButton = new Button("Remove");
							removeButton.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									// unset the list
									if (item.itemProperty().get().getParent() != null) {
										((ComplexContent) item.itemProperty().get().getParent().getValue()).set(item.itemProperty().get().getElement().getName(), null);
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
									MainController.getInstance().setChanged();
								}
							});
							box.getChildren().addAll(addButton, removeButton);
						}
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
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
			save();
		}
		public void save() {
			if (parent != null) {
				((ComplexContent) parent.getValue()).set(getName(), value);
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
						property = copy;
					}
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
					for (Element<?> child : TypeUtils.getAllChildren((ComplexType) getElement().getType())) {
						Object value = getValue() == null ? null : ((ComplexContent) getValue()).get(child.getName());
						if (child.getType().isList(child.getProperties())) {
							if (value == null) {
								children.add(new ValueWrapper(this, child, null, 0));
							}
							else {
								CollectionHandlerProvider handler = CollectionHandlerFactory.getInstance().getHandler().getHandler(value.getClass());
								int index = 0;
								for (Object childValue : handler.getAsCollection(value)) {
									children.add(new ValueWrapper(this, child, childValue, index++));
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
}
