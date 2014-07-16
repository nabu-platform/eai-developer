package be.nabu.eai.developer.managers;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import be.nabu.libs.types.structure.SuperTypeProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.MainController.PropertyUpdater;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ArtifactGUIManager;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.eai.developer.managers.util.ElementTreeItem;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.handlers.StructureManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.Marshallable;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.Updateable;
import be.nabu.jfx.control.tree.drag.MouseLocation;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.jfx.control.tree.drag.TreeDragListener;
import be.nabu.jfx.control.tree.drag.TreeDropListener;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.ModifiableType;
import be.nabu.libs.types.api.ModifiableTypeInstance;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.RootElement;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.java.BeanType;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.types.structure.DefinedStructure;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class StructureGUIManager implements ArtifactGUIManager<DefinedStructure> {
	
	@Override
	public ArtifactManager<DefinedStructure> getArtifactManager() {
		return new StructureManager();
	}

	@Override
	public String getArtifactName() {
		return "Structure";
	}

	@Override
	public ImageView getGraphic() {
		return MainController.loadGraphic("types/structure.gif");
	}

	@Override
	public ArtifactGUIInstance create(final MainController controller, final TreeItem<RepositoryEntry> target) throws IOException {
		FXMLLoader loader = controller.load("new.nameOnly.fxml", "Create Structure");
		final NameOnlyCreateController createController = loader.getController();
		final StructureGUIInstance instance = new StructureGUIInstance();
		createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				try {
					String name = createController.getTxtName().getText();
					RepositoryEntry entry = target.itemProperty().get().createNode(name, getArtifactManager());
					DefinedStructure structure = new DefinedStructure();
					structure.setName("root");
					getArtifactManager().save(entry, structure);
					controller.getRepositoryBrowser().refresh();
					createController.close();
					Tab tab = controller.newTab(entry.getId());
					AnchorPane pane = new AnchorPane();
					tab.setContent(pane);
					display(controller, pane, entry);
					instance.setEntry(entry);
					instance.setStructure(structure);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				catch (ParseException e) {
					throw new RuntimeException(e);
				}
			}
		});
		return instance;
	}

	@Override
	public ArtifactGUIInstance view(MainController controller, TreeItem<RepositoryEntry> target) throws IOException, ParseException {
		Tab tab = controller.newTab(target.itemProperty().get().getId());
		AnchorPane pane = new AnchorPane();
		tab.setContent(pane);
		return new StructureGUIInstance(target.itemProperty().get(), display(controller, pane, target.itemProperty().get()));
	}
	
	private boolean rename(MainController controller, TreeItem<Element<?>> cell, String name) {
		if (!isValidName(name)) {
			controller.notify(new ValidationMessage(Severity.ERROR, "The name '" + name + "' is not a valid field name"));
		}
		else {
			Element<?> existingChild = cell.getParent() == null ? null : ((ComplexType)(cell.getParent().itemProperty().get().getType())).get(name);
			if (existingChild == null) {
				cell.itemProperty().get().setProperty(new ValueImpl<String>(new NameProperty(), name));
				return true;
			}
			else {
				controller.notify(new ValidationMessage(Severity.ERROR, "There is already an element with the name " + name));
			}
		}
		return false;
	}
	
	private boolean isValidName(String name) {
		return name.matches("^[\\w]+$");
	}
	private DefinedStructure display(final MainController controller, Pane pane, RepositoryEntry entry) throws IOException, ParseException {
		// tree
		DefinedStructure structure = (DefinedStructure) entry.getNode().getArtifact();
		
		final Tree<Element<?>> tree = new Tree<Element<?>>(new Marshallable<Element<?>>() {
			@Override
			public String marshal(Element<?> arg0) {
				return arg0.getName();
			}
		}, new Updateable<Element<?>>() {
			@Override
			public Element<?> update(TreeCell<Element<?>> cell, String name) {
				rename(controller, cell.getItem(), name);
				return cell.getItem().itemProperty().get();
			}
		});
		tree.rootProperty().set(new ElementTreeItem(new RootElement(structure), null, true));

		// buttons
		HBox buttons = new HBox();
		
		Button newStructure = new Button();
		newStructure.setGraphic(MainController.loadGraphic(getIcon(getType(Structure.class))));
		newStructure.addEventHandler(ActionEvent.ACTION, new StructureAddHandler(tree, Structure.class));
		buttons.getChildren().add(newStructure);
		
		Button newString = new Button();
		newString.setGraphic(MainController.loadGraphic(getIcon(getType(String.class))));
		newString.addEventHandler(ActionEvent.ACTION, new StructureAddHandler(tree, String.class));
		buttons.getChildren().add(newString);
		
		Button newDate = new Button();
		newDate.setGraphic(MainController.loadGraphic(getIcon(getType(Date.class))));
		newDate.addEventHandler(ActionEvent.ACTION, new StructureAddHandler(tree, Date.class));
		buttons.getChildren().add(newDate);
		
		VBox vbox = new VBox();
		vbox.getChildren().addAll(buttons, tree);
		pane.getChildren().add(vbox);
		
		tree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeCell<Element<?>>>() {
			@Override
			public void changed(ObservableValue<? extends TreeCell<Element<?>>> arg0, TreeCell<Element<?>> arg1, final TreeCell<Element<?>> newElement) {
				controller.showProperties(new PropertyUpdater() {
					@Override
					public Set<Property<?>> getSupportedProperties() {
						return newElement.getItem().itemProperty().get().getSupportedProperties();
					}
					@Override
					public Value<?>[] getValues() {
						return newElement.getItem().itemProperty().get().getProperties();
					}
					@Override
					public boolean canUpdate(Property<?> property) {
						return true;
					}
					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public List<ValidationMessage> updateProperty(Property<?> property, Object value) {
						if (property.equals(new NameProperty())) {
							rename(controller, newElement.getItem(), (String) value);
						}
						// need to set this on the type instead of on the element
						else if (property.equals(new SuperTypeProperty())) {
							if (!(newElement.getItem().itemProperty().get().getType() instanceof ModifiableType)) {
								return Arrays.asList(new ValidationMessage(Severity.ERROR, "The type can not be modified"));
							}
							else {
								((ModifiableType) newElement.getItem().itemProperty().get().getType()).setProperty(new ValueImpl(property, value));
							}
						}
						else {
							newElement.getItem().itemProperty().get().setProperty(new ValueImpl(property, value));
						}
						if (newElement.getItem().getParent() != null) {
							((ElementTreeItem) newElement.getItem().getParent()).refresh();
						}
						return new ArrayList<ValidationMessage>();
					}
				});
			}
		});
		
		TreeDragDrop.makeDraggable(tree, new TreeDragListener<Element<?>>() {
			@Override
			public boolean canDrag(TreeCell<Element<?>> cell) {
				// it can not be part of a defined type nor can it be the root
				// also: the source type has to be modifiable because you will be dragging it from there
				return cell.getItem().getParent() != null
					&& (cell.getItem().getParent().itemProperty().get().getType() instanceof ModifiableComplexType)
					&& cell.getItem().getParent().editableProperty().get()
					&& cell.getItem().editableProperty().get();
			}
			@Override
			public void drag(TreeCell<Element<?>> cell, MouseLocation arg1) {
				// do nothing
			}
			@Override
			public String getDataType(TreeCell<Element<?>> cell) {
//				return cell.getItem().itemProperty().get().getType().getNamespace() + ":" + cell.getItem().itemProperty().get().getType().getName();
				return "type";
			}
			@Override
			public TransferMode getTransferMode() {
				return TransferMode.MOVE;
			}
			@Override
			public void stopDrag(TreeCell<Element<?>> arg0, boolean successful) {
				// do nothing
			}
		});
		// can only make it drag/droppable after it's added because it needs the scene
		TreeDragDrop.makeDroppable(tree, new TreeDropListener<Element<?>>() {
			@Override
			public boolean canDrop(String dataType, TreeCell<Element<?>> target, TreeCell<?> dragged) {
				if (!dataType.equals("type")) {
					return false;
				}
				else if (!target.getItem().editableProperty().get()) {
					return false;
				}
				// if it's the root and the root is modifiable, we can drop it there
				else if (target.getItem().getParent() == null || (!target.getItem().leafProperty().get() && !target.getItem().equals(dragged.getItem().getParent()))) {
					return target.getItem().itemProperty().get().getType() instanceof ModifiableComplexType;
				}
				return false;
			}
			@SuppressWarnings("unchecked")
			@Override
			public void drop(String dataType, TreeCell<Element<?>> target, TreeCell<?> dragged) {
				// if the cell to drop is from this tree, we need to actually move it
				if (dragged.getTree().equals(tree)) {
					ModifiableComplexType newParent;
					// we need to wrap an extension around it
					if (target.getItem().itemProperty().get().getType() instanceof DefinedType) {
						Structure structure = new Structure();
						structure.setSuperType(target.getItem().itemProperty().get().getType());
						((ModifiableTypeInstance) target.getItem().itemProperty().get()).setType(structure);
						newParent = structure;
					}
					else {
						newParent = (ModifiableComplexType) target.getItem().itemProperty().get().getType();
					}
					TreeCell<Element<?>> draggedElement = (TreeCell<Element<?>>) dragged;
					ModifiableComplexType originalParent = (ModifiableComplexType) draggedElement.getItem().getParent().itemProperty().get().getType();
					// if there are no validation errors when adding, remove the old one
					List<ValidationMessage> messages = newParent.add(draggedElement.getItem().itemProperty().get());
					if (messages.isEmpty()) {
						originalParent.remove(draggedElement.getItem().itemProperty().get());
					}
					else {
						controller.notify(messages.toArray(new ValidationMessage[0]));
					}
					// refresh both
					((ElementTreeItem) target.getItem()).refresh();
					((ElementTreeItem) dragged.getParent().getItem()).refresh();
				}
				// if it is from the repository tree, we need to add it
				else if (MainController.isRepositoryTree(dragged.getTree())) {
					
				}
			}
			
		});
		
		return structure;
	}
	
	private class StructureAddHandler implements EventHandler<Event> {

		private Tree<Element<?>> tree;
		private Class<?> type;
		
		public StructureAddHandler(Tree<Element<?>> tree, Class<?> type) {
			this.tree = tree;
			this.type = type;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public void handle(Event arg0) {
			TreeCell<Element<?>> selectedItem = tree.getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem.getItem().editableProperty().get()) {
				// add an element in it
				if (selectedItem.getItem().itemProperty().get().getType() instanceof ComplexType) {
					if (selectedItem.getItem().itemProperty().get().getType() instanceof ModifiableComplexType) {
						ModifiableComplexType target = (ModifiableComplexType) selectedItem.getItem().itemProperty().get().getType();
						// try a simple approach
						Type resolvedType = getType(type);
						if (resolvedType instanceof SimpleType) {
							target.add(new SimpleElementImpl("unnamed" + getLastCounter(target), (SimpleType<?>) resolvedType, target));
						}
						else {
							target.add(new ComplexElementImpl("unnamed" + getLastCounter(target), (ComplexType) resolvedType, target));
						}
					}
				}
				((ElementTreeItem) selectedItem.getItem()).refresh();
				// add an element next to it
			}
		}
		
		public int getLastCounter(ComplexType type) {
			int last = -1;
			for (Element<?> child : TypeUtils.getAllChildren(type)) {
				if (child.getName().matches("^unnamed[0-9]+$")) {
					int childNumber = new Integer(child.getName().replace("unnamed", ""));
					if (childNumber > last) {
						last = childNumber;
					}
				}
			}
			return last + 1;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Type getType(Class<?> clazz) {
		Type type = SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(clazz);
		if (type == null) {
			try {
				type = ComplexType.class.isAssignableFrom(clazz)
					? (ComplexType) clazz.newInstance()
					: new BeanType(clazz);
			}
			catch (InstantiationException e) {
				throw new RuntimeException(e);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return type;
	}
	
	public static String getIcon(Type type, Value<?>...values) {
		String image;
		if (type instanceof SimpleType) {
			SimpleType<?> simpleType = (SimpleType<?>) type;
			if (String.class.isAssignableFrom(simpleType.getInstanceClass())) {
				image = "types/string.gif";
			}
			else if (Date.class.isAssignableFrom(simpleType.getInstanceClass())) {
				image = "types/date.gif";
			}
			else if (Integer.class.isAssignableFrom(simpleType.getInstanceClass())) {
				image = "types/integer.gif";
			}
			else if (Long.class.isAssignableFrom(simpleType.getInstanceClass())) {
				image = "types/long.gif";
			}
			else if (Float.class.isAssignableFrom(simpleType.getInstanceClass())) {
				image = "types/float.gif";
			}
			else if (Double.class.isAssignableFrom(simpleType.getInstanceClass())) {
				image = "types/float.gif";
			}
			else {
				image = "types/object.gif";
			}
		}
		else {
			if (type.getSuperType() != null) {
				image = "types/structureextension.gif";
			}
			else {
				image = type instanceof DefinedType ? "types/definedstructure.gif" : "types/structure.gif";
			}
		}
		Integer maxOccurs = ValueUtils.getValue(new MaxOccursProperty(), values);
		if (maxOccurs != null && maxOccurs != 1) {
			image = image.replace(".gif", "list.gif");
		}
		return image;
	}
}
