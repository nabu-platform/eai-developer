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
import javafx.scene.Node;
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

public class ComplexContentEditor {
	
	private List<Property<?>> properties;
	private boolean updateChanged;
	private Repository repository;
	private ComplexContent content;

	public ComplexContentEditor(ComplexContent content, boolean updateChanged, Repository repository) {
		this.content = content;
		this.updateChanged = updateChanged;
		this.repository = repository;
		properties = BaseConfigurationGUIManager.createProperty(new ComplexElementImpl(content.getType(), null));
	}
	
	public Tree<ValueWrapper> build() {
		Tree<ValueWrapper> tree = new Tree<ValueWrapper>(new ValueWrapperCellFactory());
		tree.rootProperty().set(new ValueWrapperItem(null, new ValueWrapper(null, new ComplexElementImpl(content.getType(), null), content, null)));
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

	public class ValueWrapperCellFactory implements Callback<TreeItem<ValueWrapper>, TreeCellValue<ValueWrapper>> {
		@Override
		public TreeCellValue<ValueWrapper> call(TreeItem<ValueWrapper> item) {
			return new TreeCellValue<ValueWrapper>() {
				private ObjectProperty<TreeCell<ValueWrapper>> cell = new SimpleObjectProperty<TreeCell<ValueWrapper>>();
				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public Region getNode() {
					final HBox box = new HBox();
					if (item.itemProperty().get().getParent() == null) {
						box.getChildren().add(new Label(item.itemProperty().get().getName()));
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
						SimplePropertyUpdater updater = new SimplePropertyUpdater(true, new HashSet(Arrays.asList(property)), new ValueImpl(property, item.itemProperty().get().getValue()));
						MainController.getInstance().drawSingleProperty(updater, property, null, drawer, repository, updateChanged);
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
				TreeUtils.refreshChildren(new TreeItemCreator<ValueWrapper>() {
					@Override
					public TreeItem<ValueWrapper> create(TreeItem<ValueWrapper> parent, ValueWrapper child) {
						return new ValueWrapperItem((ValueWrapperItem) parent, child);
					}
				}, this, item.get().getChildren());
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
		}
		public Object getValue() {
			return value;
		}
		public void setValue(Object value) {
			this.value = value;
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
			if (children == null) {
				children = new ArrayList<ValueWrapper>();
				if (getElement().getType() instanceof ComplexType) {
					for (Element<?> child : TypeUtils.getAllChildren((ComplexType) getElement().getType())) {
						Object value = getValue() == null ? null : ((ComplexContent) getValue()).get(child.getName());
						if (child.getType().isList(child.getProperties())) {
							if (value == null) {
								children.add(new ValueWrapper(this, child, value, 0));
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
