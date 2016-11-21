package be.nabu.eai.developer.util;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ContainerArtifactGUIManager.ContainerArtifactGUIInstance;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.api.VariableRefactorArtifactManager;
import be.nabu.jfx.control.tree.MovableTreeItem;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.TreeUtils.TreeItemCreator;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.ModifiableTypeInstance;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.StringMapCollectionHandlerProvider;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.java.BeanType;
import be.nabu.libs.types.properties.CollectionHandlerProviderProperty;
import be.nabu.libs.types.properties.HiddenProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class ElementTreeItem implements RemovableTreeItem<Element<?>>, MovableTreeItem<Element<?>> {
	
	public static final String UNNAMED = "unnamed";
	public static final String DATA_TYPE_DEFINED = "type";
	public static final String DATA_TYPE_ELEMENT = "element";
	public static final String DATA_TYPE_SERIALIZED_ELEMENT = "serializedElement";
	public static final String DATA_TYPE_SERIALIZED_ELEMENT_LIST = "serializedElementList";

	private ElementTreeItem parent;
	private BooleanProperty editableProperty = new SimpleBooleanProperty(false);
	private ObjectProperty<Element<?>> itemProperty = new SimpleObjectProperty<Element<?>>();
	private ObjectProperty<Node> graphicProperty = new SimpleObjectProperty<Node>();
	private BooleanProperty leafProperty = new SimpleBooleanProperty(false);
	private ObservableList<TreeItem<Element<?>>> children;
	
	private static Logger logger = LoggerFactory.getLogger(ElementTreeItem.class);
	
	/**
	 * You can set the editable to false on this one but force the children to still be editable
	 */
	private boolean forceChildrenEditable = false;
	private boolean allowNonLocalModification = false;
	
	public ElementTreeItem(Element<?> element, ElementTreeItem parent, boolean isEditable, boolean allowNonLocalModification) {
		this.allowNonLocalModification = allowNonLocalModification;
		this.itemProperty.set(element);
		this.parent = parent;
		editableProperty.set(isEditable);
		refresh(false);
	}
	
	public boolean isForceChildrenEditable() {
		return forceChildrenEditable;
	}
	public void setForceChildrenEditable(boolean forceChildrenEditable) {
		this.forceChildrenEditable = forceChildrenEditable;
	}

	@Override
	public BooleanProperty editableProperty() {
		return editableProperty;
	}

	@Override
	public ObservableList<TreeItem<Element<?>>> getChildren() {
		if (children == null) {
			children = FXCollections.observableArrayList();
			refresh(true);
		}
		return children;
	}
	
	@Override
	public void refresh() {
		refresh(true);
	}

	public static String getIcon(Type type, Value<?>...values) {
		// shortcut for maps
		CollectionHandlerProvider<?, ?> value = ValueUtils.getValue(new CollectionHandlerProviderProperty(), values);
		if (value != null && value.getClass().equals(StringMapCollectionHandlerProvider.class)) {
			return "types/map.gif";
		}
		
		String image;
		if (type instanceof SimpleType) {
			SimpleType<?> simpleType = (SimpleType<?>) type;
			String simpleName = simpleType.getInstanceClass().equals(byte[].class) 
				? "bytes" 
				: simpleType.getInstanceClass().getSimpleName().toLowerCase();
			image = "types/" + simpleName + ".gif";
		}
		else {
			if (type instanceof BeanType && ((BeanType<?>) type).getBeanClass().equals(Object.class)) {
				image = "types/object.gif";
			}
			else if (type.getSuperType() != null) {
				image = "types/structureextension.gif";
			}
			else {
				image = type instanceof DefinedType ? "types/definedstructure.gif" : "types/structure.gif";
			}
		}
		Integer maxOccurs = ValueUtils.getValue(MaxOccursProperty.getInstance(), values);
		if (maxOccurs != null && maxOccurs != 1) {
			image = image.replace(".gif", "list.gif");
		}
		return image;
	}
	
	private void refresh(boolean includeChildren) {
		leafProperty.set(!(itemProperty.get().getType() instanceof ComplexType) || (itemProperty.get().getType() instanceof BeanType && ((BeanType<?>) itemProperty.get().getType()).getBeanClass().equals(Object.class)));		
		HBox graphicBox = new HBox();
		graphicBox.getChildren().add(MainController.loadGraphic(getIcon(itemProperty.get().getType(), itemProperty.get().getProperties())));
		Integer minOccurs = ValueUtils.contains(MinOccursProperty.getInstance(), itemProperty.get().getProperties()) 
			? ValueUtils.getValue(MinOccursProperty.getInstance(), itemProperty.get().getProperties()) 
			: null;
		if (minOccurs == null || minOccurs > 0) {
			graphicBox.getChildren().add(MainController.loadGraphic("types/mandatory.png"));
		}
		else {
			graphicBox.getChildren().add(MainController.loadGraphic("types/optional.png"));
		}
		graphicProperty.set(graphicBox);
		if (!leafProperty.get() && includeChildren) {
			TreeUtils.refreshChildren(new TreeItemCreator<Element<?>>() {
				@Override
				public TreeItem<Element<?>> create(TreeItem<Element<?>> parent, Element<?> child) {
					boolean isRemotelyDefined = parent.getParent() != null && itemProperty.get().getType() instanceof DefinedType;
					boolean isLocal = TypeUtils.getLocalChild((ComplexType) itemProperty.get().getType(), child.getName()) != null;
					return new ElementTreeItem(child, (ElementTreeItem) parent, (allowNonLocalModification || (isLocal && !isRemotelyDefined)) && (forceChildrenEditable || editableProperty.get()), allowNonLocalModification);	
				}
			}, this, filterTemporary(TypeUtils.getAllChildren((ComplexType) itemProperty.get().getType())));
		}
	}
	
	private Collection<Element<?>> filterTemporary(Collection<Element<?>> children) {
		Iterator<Element<?>> iterator = children.iterator();
		while (iterator.hasNext()) {
			Element<?> next = iterator.next();
			Boolean value = ValueUtils.getValue(HiddenProperty.getInstance(), next.getProperties());
			if (value != null && value) {
				iterator.remove();
			}
		}
		return children;
	}

	@Override
	public String getName() {
		return (itemProperty().get() instanceof Attribute ? "@" : "") + itemProperty.get().getName();
	}

	@Override
	public TreeItem<Element<?>> getParent() {
		return parent;
	}

	@Override
	public ObjectProperty<Node> graphicProperty() {
		return graphicProperty;
	}

	@Override
	public ObjectProperty<Element<?>> itemProperty() {
		return itemProperty;
	}

	@Override
	public BooleanProperty leafProperty() {
		return leafProperty;
	}

	@Override
	public boolean remove() {
		if (itemProperty().get().getParent() != null && itemProperty().get().getParent() instanceof ModifiableComplexType) {
			ModifiableComplexType type = (ModifiableComplexType) itemProperty().get().getParent();
			type.remove(itemProperty.get());
			if (type.getSuperType() != null && getParent().itemProperty().get() instanceof ModifiableTypeInstance) {
				boolean allInherited = !type.iterator().hasNext();
				// if everything is inherited, replace with actual type
				if (allInherited) {
					((ModifiableTypeInstance) getParent().itemProperty().get()).setType(type.getSuperType());
				}
			}
			MainController.getInstance().setChanged();
			return true;
		}
		return false;
	}

	@Override
	public TreeItem<Element<?>> move(be.nabu.jfx.control.tree.MovableTreeItem.Direction direction) {
		if (editableProperty().get() && itemProperty().get().getParent() instanceof ModifiableComplexType) {
			switch(direction) {
				case UP:
				case DOWN:
					// get the local elements
					ModifiableComplexType target = (ModifiableComplexType) itemProperty().get().getParent();
					Iterator<Element<?>> iterator = target.iterator();
					List<Element<?>> currentChildren = new ArrayList<Element<?>>();
					while (iterator.hasNext()) {
						Element<?> next = iterator.next();
						if (TypeUtils.getLocalChild(target, next.getName()) != null) {
							currentChildren.add(next);
							iterator.remove();
						}
					}
					int index = currentChildren.indexOf(itemProperty().get());
					if (direction == Direction.UP && index > 0) {
						currentChildren.remove(index);
						currentChildren.add(index - 1, itemProperty().get());
					}
					else if (direction == Direction.DOWN && index < currentChildren.size() - 1) {
						currentChildren.remove(index);
						currentChildren.add(index + 1, itemProperty().get());
					}
					for (Element<?> element : currentChildren) {
						target.add(element);
					}
					getParent().refresh();
					MainController.getInstance().setChanged();
				break;
				case RIGHT:
					Element<?> oldElement = itemProperty().get();
					ModifiableComplexType parent = (ModifiableComplexType) oldElement.getParent();
					Iterator<Element<?>> parentIterator = parent.iterator();
					ComplexType last = null;
					while(parentIterator.hasNext()) {
						Element<?> next = parentIterator.next();
						if (next.equals(itemProperty().get())) {
							break;
						}
						if (next.getType() instanceof ComplexType) {
							last = (ComplexType) next.getType();
						}
					}
					if (last instanceof ModifiableComplexType) {
						if (last.get(itemProperty().get().getName()) == null) {
							Element<?> clonedElement = cloneForParent(oldElement, last);
							parentIterator.remove();
							((ModifiableComplexType) last).add(clonedElement);
							for (TreeItem<Element<?>> child : getParent().getChildren()) {
								child.refresh();
							}
							getParent().refresh();
							MainController.getInstance().setChanged();
							return findItem(getParent(), clonedElement);
						}
					}
				break;
				case LEFT:
					if (getParent() != null && getParent().getParent() != null) {
						ModifiableComplexType parentType = (ModifiableComplexType) getParent().itemProperty().get().getType();
						ModifiableComplexType grandParentType = (ModifiableComplexType) getParent().getParent().itemProperty().get().getType();
						Element<?> elementToMove = itemProperty.get();
						if (grandParentType.get(elementToMove.getName()) == null) {
							Element<?> clonedElement = cloneForParent(elementToMove, grandParentType);
							parentType.remove(elementToMove);
							grandParentType.add(clonedElement);
							getParent().getParent().refresh();
							getParent().refresh();
							MainController.getInstance().setChanged();
							return findItem(getParent().getParent(), clonedElement);
						}
					}
				break;
			}
		}
		return null;
	}
	
	public static TreeItem<Element<?>> findItem(TreeItem<Element<?>> item, Element<?> elementToSelect) {
		if (item.itemProperty().get().equals(elementToSelect)) {
			return item;
		}
		for (TreeItem<Element<?>> child : item.getChildren()) {
			TreeItem<Element<?>> select = findItem(child, elementToSelect);
			if (select != null) {
				return select;
			}
		}
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Element<?> cloneForParent(Element<?> element, ComplexType newParent) {
		return element.getType() instanceof ComplexType
			? new ComplexElementImpl(element.getName(), (ComplexType) element.getType(), newParent, element.getProperties())
			: new SimpleElementImpl(element.getName(), (SimpleType) element.getType(), newParent, element.getProperties());
	}
	
	public static boolean rename(MainController controller, TreeItem<Element<?>> cell, String name) {
		if (!cell.itemProperty().get().getSupportedProperties().contains(new NameProperty())) {
			controller.notify(new ValidationMessage(Severity.ERROR, "Can not update this name"));
		}
		else if (!isValidName(name)) {
			controller.notify(new ValidationMessage(Severity.ERROR, "The name '" + name + "' is not a valid field name"));
		}
		else {
			Element<?> existingChild = cell.getParent() == null ? null : ((ComplexType)(cell.getParent().itemProperty().get().getType())).get(name);
			if (existingChild == null) {
				String oldPath = TreeUtils.getPath(cell);
				int index = oldPath.lastIndexOf('/');
				String newPath;
				if (index < 0) {
					newPath = name;
				}
				else {
					newPath = oldPath.substring(0, index) + "/" + name;
				}
				// set name
				cell.itemProperty().get().setProperty(new ValueImpl<String>(NameProperty.getInstance(), name));
				controller.setChanged();
				
				// make sure we update the paths in dependencies
				renameVariable(controller, oldPath, newPath);
				
				return true;
			}
			else {
				controller.notify(new ValidationMessage(Severity.ERROR, "There is already an element with the name " + name));
			}
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	public static void renameVariable(MainController controller, String oldPath, String newPath) {
		ArtifactGUIInstance currentInstance = controller.getCurrentInstance();
		if (currentInstance != null) {
			try {
				if (currentInstance instanceof ContainerArtifactGUIInstance) {
					Artifact currentChildArtifact = ((ContainerArtifactGUIInstance) currentInstance).getCurrentActiveArtifact();
					logger.info("Updating contained artifact: " + currentChildArtifact.getId());
					// we send in the child artifact, but we use the global id of the container artifact to refactor any dependencies
					updateVariables(controller.getRepository(), currentChildArtifact, currentInstance.getId(), oldPath, newPath);
				}
				else {
					updateVariables(controller.getRepository(), currentInstance.getArtifact(), currentInstance.getId(), oldPath, newPath);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void updateVariables(Repository repository, Artifact impactedArtifact, String artifactId, String oldPath, String newPath) throws ParseException {
		ArtifactManager artifactManager = EAIRepositoryUtils.getArtifactManager(impactedArtifact.getClass());
		if (artifactManager instanceof VariableRefactorArtifactManager) {
			try {
				logger.info("Replacing old path '" + oldPath + "' with new path '" + newPath + "' in artifact '" + impactedArtifact.getId() + "'");
				((VariableRefactorArtifactManager) artifactManager).updateVariableName(impactedArtifact, impactedArtifact, oldPath, newPath);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		for (String dependency : repository.getDependencies(artifactId)) {
			Entry entry = repository.getEntry(dependency);
			if (entry.isNode() && entry instanceof ResourceEntry) {
				try {
					ArtifactGUIInstance artifactInstance = MainController.getInstance().getArtifactInstance(dependency);
					Artifact resolve = artifactInstance != null ? artifactInstance.getArtifact() : entry.getNode().getArtifact();
					artifactManager = EAIRepositoryUtils.getArtifactManager(resolve.getClass());
					if (artifactManager instanceof VariableRefactorArtifactManager) {
						logger.info("Replacing old path '" + oldPath + "' with new path '" + newPath + "' in dependency '" + dependency + "'");
						if (((VariableRefactorArtifactManager) artifactManager).updateVariableName(resolve, impactedArtifact, oldPath, newPath)) {
							if (MainController.getInstance().getTab(dependency) == null) {
								RepositoryBrowser.open(MainController.getInstance(), entry);
							}
							MainController.getInstance().setChanged(dependency);
						}
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	/**
	 * XML Spec:
	 * - NameChar ::= Letter | Digit | '.' | '-' | '_' | ':' | CombiningChar | Extender
	 * - Name ::= (Letter | '_' | ':') (NameChar)*
	 * 
	 * So a name can start with a letter, an underscore or a ':'
	 * 
	 * There are some (non-enforced) best practices:
	 * - Avoid "-". If you name something "first-name", some software may think you want to subtract "name" from "first".
	 * - Avoid ".". If you name something "first.name", some software may think that "name" is a property of the object "first".
	 * - Avoid ":". Colons are reserved for namespaces (more later).
	 * 
	 * All my parsers offer the capability of changing '-' to camelcase so that is (currently) covered. Note that such rewriting is not unheard of in other stacks as well so seems a clean solution.
	 * The '.' is simply a bad idea all around but if required parsers could be extended to also camelcase this
	 * The ':' requirement is simply ignored, worst idea ever.
	 * 
	 * For a brief moment I added the ability to start a name with an underscore but currently it is better to use aliases
	 */
	private static boolean isValidName(String name) {
		// the full name must be a word and the first character must be a letter
		return (name.matches("^[\\w]+$") && name.substring(0, 1).matches("[a-zA-Z]"))
			// or an attribute
			|| (name.matches("^@[\\w]+$") && name.substring(1, 2).matches("[a-zA-Z]"));
	}
	
	public static int getLastCounter(ComplexType type) {
		return getLastCounter(type, UNNAMED);
	}
	public static int getLastCounter(ComplexType type, String name) {
		int last = -1;
		for (Element<?> child : TypeUtils.getAllChildren(type)) {
			if (child.getName().matches("^" + name + "[0-9]+$")) {
				int childNumber = new Integer(child.getName().replace(name, ""));
				if (childNumber > last) {
					last = childNumber;
				}
			}
		}
		return last + 1;
	}
}
