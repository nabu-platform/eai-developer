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

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactGUIInstance;
import be.nabu.eai.developer.api.ContainerArtifactGUIManager.ContainerArtifactGUIInstance;
import be.nabu.eai.developer.events.VariableRenameEvent;
import be.nabu.eai.developer.impl.CustomTooltip;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.ArtifactManager;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.api.VariableRefactorArtifactManager;
import be.nabu.jfx.control.tree.MovableTreeItem;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.jfx.control.tree.TreeUtils;
import be.nabu.jfx.control.tree.TreeUtils.TreeItemCreator;
import be.nabu.libs.artifacts.api.Artifact;
import be.nabu.libs.property.ValueUtils;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.Attribute;
import be.nabu.libs.types.api.CollectionHandlerProvider;
import be.nabu.libs.types.api.ComplexType;
import be.nabu.libs.types.api.DefinedType;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.ModifiableComplexType;
import be.nabu.libs.types.api.ModifiableElement;
import be.nabu.libs.types.api.ModifiableTypeInstance;
import be.nabu.libs.types.api.SimpleType;
import be.nabu.libs.types.api.Type;
import be.nabu.libs.types.base.ComplexElementImpl;
import be.nabu.libs.types.base.SimpleElementImpl;
import be.nabu.libs.types.base.StringMapCollectionHandlerProvider;
import be.nabu.libs.types.base.TypeBaseUtils;
import be.nabu.libs.types.base.ValueImpl;
import be.nabu.libs.types.java.BeanType;
import be.nabu.libs.types.properties.CollectionHandlerProviderProperty;
import be.nabu.libs.types.properties.DynamicForeignKeyProperty;
import be.nabu.libs.types.properties.ForeignKeyProperty;
import be.nabu.libs.types.properties.HiddenProperty;
import be.nabu.libs.types.properties.MaxOccursProperty;
import be.nabu.libs.types.properties.MinOccursProperty;
import be.nabu.libs.types.properties.NameProperty;
import be.nabu.libs.types.properties.PrimaryKeyProperty;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class ElementTreeItem implements RemovableTreeItem<Element<?>>, MovableTreeItem<Element<?>> {
	
	public static interface ChildSelector {
		public List<Element<?>> getChildren(ComplexType type);
	}
	
	public static final String UNNAMED = "unnamed";
	public static final String DATA_TYPE_DEFINED = "type";
	public static final String DATA_TYPE_ELEMENT = "element";
	public static final String DATA_TYPE_SERIALIZED_ELEMENT = "serializedElement";
	public static final String DATA_TYPE_SERIALIZED_ELEMENT_LIST = "serializedElementList";
	
	// @2023-04-14: do we want to revert to the original type if you have removed the last of the extension information?
	// in the past if you removed the last custom field in an extension, we would revert to the parent type
	// this is done to make "local extensions" in the pipeline manageable: if you accidently add a field to a defined type, you make a local extension which you want to "undo"
	// however, when just doing regular structure editing (so not via the pipeline), this is _really_ annoying and sometimes you corrupt parent types accidently
	// because pipeline-level local extensions are actually unused and should be disabled, we set this to false
	private boolean revertToOriginalType = false;
	
	// you can choose to only show local children
	private ChildSelector childSelector;
	
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
	private boolean shallowAllowNonLocalModification = false;
	
	public ElementTreeItem(Element<?> element, ElementTreeItem parent, boolean isEditable, boolean allowNonLocalModification) {
		this.allowNonLocalModification = allowNonLocalModification;
		this.itemProperty.set(element);
		this.parent = parent;
		editableProperty.set(isEditable);
		internalRefreh(false);
	}

	public void setEditable(boolean editable) {
		editableProperty.set(editable);
		if (children != null) {
			children = null;
			refresh();
		}
	}
	
	public boolean isForceChildrenEditable() {
		return forceChildrenEditable;
	}
	public void setForceChildrenEditable(boolean forceChildrenEditable) {
		this.forceChildrenEditable = forceChildrenEditable;
		if (children != null) {
			children = null;
			refresh();
		}
	}

	@Override
	public BooleanProperty editableProperty() {
		return editableProperty;
	}

	@Override
	public ObservableList<TreeItem<Element<?>>> getChildren() {
		if (children == null) {
			children = FXCollections.observableArrayList();
			internalRefreh(true);
		}
		return children;
	}
	
	@Override
	public void refresh() {
		internalRefreh(true);
	}

	public static String getIcon(Type type, Value<?>...values) {
		// shortcut for maps
		CollectionHandlerProvider<?, ?> value = ValueUtils.getValue(new CollectionHandlerProviderProperty(), values);
		if (value != null && value.getClass().equals(StringMapCollectionHandlerProvider.class)) {
			return "types/map.gif";
		}
		
		Integer maxOccurs = ValueUtils.getValue(MaxOccursProperty.getInstance(), values);
		String image;
		if (type instanceof SimpleType) {
			SimpleType<?> simpleType = (SimpleType<?>) type;
			String simpleName = simpleType.getInstanceClass().equals(byte[].class) 
				? "bytes" 
				: simpleType.getInstanceClass().getSimpleName().toLowerCase();
			// these have key variants
			if ((maxOccurs == null || maxOccurs == 1) && (simpleName.equals("uuid") || simpleName.equals("string") || simpleName.equals("long"))) {
				Boolean isPrimary = ValueUtils.getValue(PrimaryKeyProperty.getInstance(), values);
				if (isPrimary != null && isPrimary) {
					simpleName += "-p";
				}
				else {
					String foreignKey = ValueUtils.getValue(ForeignKeyProperty.getInstance(), values);
					if (foreignKey != null) {
						simpleName += "-f";
					}
					Boolean isDynamicForeign = ValueUtils.getValue(DynamicForeignKeyProperty.getInstance(), values);
					if (isDynamicForeign != null && isDynamicForeign) {
						simpleName += "-f";	
					}
				}
			}
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
		if (maxOccurs != null && maxOccurs != 1) {
			image = image.replace(".gif", "list.gif");
		}
		return image;
	}
	
	private void internalRefreh(boolean includeChildren) {
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
		if (itemProperty.get().getType() instanceof DefinedType) {
			String id = ((DefinedType) itemProperty.get().getType()).getId();
			// this might indicate byte arrays, e.g. [B
			if (id != null && !id.startsWith("[")) {
				be.nabu.eai.repository.api.Node node = EAIResourceRepository.getInstance().getNode(id);
				if (node != null && node.getDeprecated() != null && node.getDeprecated().before(new Date())) {
					Node loadGraphic = MainController.loadFixedSizeGraphic("severity-warning.png");
					new CustomTooltip("This type has been deprecated since " + node.getDeprecated()).install(loadGraphic);
					graphicBox.getChildren().add(loadGraphic);
				}
			}
		}
		graphicProperty.set(graphicBox);
		if (!leafProperty.get() && includeChildren) {
			TreeUtils.refreshChildren(new TreeItemCreator<Element<?>>() {
				@Override
				public TreeItem<Element<?>> create(TreeItem<Element<?>> parent, Element<?> child) {
					boolean isRemotelyDefined = parent.getParent() != null && itemProperty.get().getType() instanceof DefinedType;
					boolean isLocal = TypeUtils.isLocalChild((ComplexType) itemProperty.get().getType(), child.getName());
					return new ElementTreeItem(child, (ElementTreeItem) parent, (allowNonLocalModification || shallowAllowNonLocalModification || (isLocal && !isRemotelyDefined)) && (forceChildrenEditable || editableProperty.get()), allowNonLocalModification);	
				}
			}, this, filterTemporary(childSelector != null ? childSelector.getChildren((ComplexType) itemProperty.get().getType()) : TypeUtils.getAllChildren((ComplexType) itemProperty.get().getType())));
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

	public boolean isShallowAllowNonLocalModification() {
		return shallowAllowNonLocalModification;
	}

	public void setShallowAllowNonLocalModification(boolean shallowAllowNonLocalModification) {
		this.shallowAllowNonLocalModification = shallowAllowNonLocalModification;
		if (children != null) {
			children = null;
			refresh();
		}
	}

	public boolean isAllowNonLocalModification() {
		return allowNonLocalModification;
	}

	public void setAllowNonLocalModification(boolean allowNonLocalModification) {
		this.allowNonLocalModification = allowNonLocalModification;
		if (children != null) {
			children = null;
			refresh();
		}
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
			// only remove if it is allowed!!!
			if (editableProperty().get() && parent != null && parent.editableProperty().get()) {
				ModifiableComplexType type = (ModifiableComplexType) itemProperty().get().getParent();
				type.remove(itemProperty.get());
				if (type.getSuperType() != null && getParent().itemProperty().get() instanceof ModifiableTypeInstance && revertToOriginalType) {
					boolean allInherited = !type.iterator().hasNext();
					// if everything is inherited, replace with actual type
					if (allInherited) {
						((ModifiableTypeInstance) getParent().itemProperty().get()).setType(type.getSuperType());
					}
				}
				MainController.getInstance().setChanged();
				return true;
			}
		}
		return false;
	}
	
	@Override
	public TreeItem<Element<?>> move(be.nabu.jfx.control.tree.MovableTreeItem.Direction direction) {
		if (editableProperty().get() && itemProperty().get().getParent() instanceof ModifiableComplexType && canRename(this)) {
			switch(direction) {
				case UP:
				case DOWN:
					// get the local elements
					ModifiableComplexType target = (ModifiableComplexType) itemProperty().get().getParent();
					Iterator<Element<?>> iterator = target.iterator();
					List<Element<?>> currentChildren = new ArrayList<Element<?>>();
					while (iterator.hasNext()) {
						Element<?> next = iterator.next();
						if (TypeUtils.isLocalChild(target, next.getName())) {
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
	
	public static boolean canRename(TreeItem<Element<?>> cell) {
		return cell.itemProperty().get().getSupportedProperties().contains(NameProperty.getInstance());
	}
	
	public static boolean rename(MainController controller, TreeItem<Element<?>> cell, String name) {
		if (!cell.itemProperty().get().getSupportedProperties().contains(NameProperty.getInstance())) {
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
				boolean updateVariableName = ((VariableRefactorArtifactManager) artifactManager).updateVariableName(impactedArtifact, impactedArtifact, oldPath, newPath);
				if (updateVariableName) {
					if (MainController.getInstance().getContainer(impactedArtifact.getId()) == null) {
//						RepositoryBrowser.open(MainController.getInstance(), repository.getEntry(impactedArtifact.getId()));
						MainController.getInstance().open(impactedArtifact.getId());
					}
					MainController.getInstance().setChanged(impactedArtifact.getId());
					MainController.getInstance().getDispatcher().fire(new VariableRenameEvent(impactedArtifact.getId(), oldPath, newPath), repository);
				}
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
							if (MainController.getInstance().getContainer(dependency) == null) {
//								RepositoryBrowser.open(MainController.getInstance(), entry);
								MainController.getInstance().open(entry.getId());
								artifactInstance = MainController.getInstance().getArtifactInstance(dependency);
								resolve = artifactInstance != null ? artifactInstance.getArtifact() : entry.getNode().getArtifact();
								// we update again because the act of opening it triggers a refresh
								((VariableRefactorArtifactManager) artifactManager).updateVariableName(resolve, impactedArtifact, oldPath, newPath);
							}
							MainController.getInstance().setChanged(dependency);
							MainController.getInstance().getDispatcher().fire(new VariableRenameEvent(dependency, oldPath, newPath), repository);
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
	 * 
	 * Check definition of combiningchar and extener here: https://www.w3.org/TR/REC-xml/ 
	 */
	public static boolean isValidName(String name) {
		// the full name must be a word and the first character must be a letter or an underscore
		return (name.matches("^[\\w.]+$") && name.substring(0, 1).matches("[a-zA-Z_]"))
			// or an attribute
			|| (name.matches("^@[\\w.]+$") && name.substring(1, 2).matches("[a-zA-Z_]"));
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
	
	public static void setListeners(Tree<Element<?>> tree, ReadOnlyBooleanProperty lock) {
		setListeners(tree, lock, false);
	}
	public static void setListeners(Tree<Element<?>> tree, ReadOnlyBooleanProperty lock, boolean forceEdit) {
		tree.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@SuppressWarnings("rawtypes")
			@Override
			public void handle(KeyEvent event) {
				TreeCell<Element<?>> selectedItem = tree.getSelectionModel().getSelectedItem();
				if (selectedItem != null && (forceEdit || selectedItem.getItem().editableProperty().get()) && lock.get()) {
					Element<?> element = selectedItem.getItem().itemProperty().get();
					if (event.isMetaDown()) {
						// can only switch simple types
						if (element.getType() instanceof SimpleType) {
							if (event.getCode() == KeyCode.F1) {
								((ModifiableElement) element).setType(SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(String.class));
							}
							else if (event.getCode() == KeyCode.F2) {
								((ModifiableElement) element).setType(SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(UUID.class));
							}
							else if (event.getCode() == KeyCode.F3) {
								((ModifiableElement) element).setType(SimpleTypeWrapperFactory.getInstance().getWrapper().wrap(Long.class));
							}
							MainController.getInstance().setChanged();
							selectedItem.refresh();
							event.consume();
						}
					}
					else if (event.getCode() == KeyCode.F3) {
						if (element.getSupportedProperties().contains(MinOccursProperty.getInstance())) {
							Value<Integer> property = element.getProperty(MinOccursProperty.getInstance());
							if (property == null || property.getValue() != 0) {
								element.setProperty(new ValueImpl<Integer>(MinOccursProperty.getInstance(), 0));
							}
							else {
								element.setProperty(new ValueImpl<Integer>(MinOccursProperty.getInstance(), 1));
							}
							MainController.getInstance().setChanged();
							selectedItem.refresh();
							event.consume();
						}
					}
					else if (event.getCode() == KeyCode.F4) {
						if (element.getSupportedProperties().contains(MaxOccursProperty.getInstance())) {
							// don't set max occurs for maps!
							Value<CollectionHandlerProvider> collectionHandlerProperty = element.getProperty(CollectionHandlerProviderProperty.getInstance());
							if (collectionHandlerProperty == null || !(collectionHandlerProperty.getValue() instanceof StringMapCollectionHandlerProvider)) {
								Value<Integer> property = element.getProperty(MaxOccursProperty.getInstance());
								if (property == null || property.getValue() != 0) {
									element.setProperty(new ValueImpl<Integer>(MaxOccursProperty.getInstance(), 0));
								}
								else {
									element.setProperty(new ValueImpl<Integer>(MaxOccursProperty.getInstance(), 1));
								}
								MainController.getInstance().setChanged();
								selectedItem.refresh();
							}
							event.consume();
						}
					}
					else if (event.getCode() == KeyCode.ENTER && event.isControlDown()) {
						TreeCell<Element<?>> parent = selectedItem.getParent();
						if (parent != null) {
							Element<?> parentElement = parent.getItem().itemProperty().get();
							Type type = parentElement.getType();
							if (type instanceof ModifiableComplexType) {
								Element<?> clone = TypeBaseUtils.clone(selectedItem.getItem().itemProperty().get(), (ComplexType) type);
								clone.setProperty(new ValueImpl<String>(NameProperty.getInstance(), clone.getName() + getLastCounter((ComplexType) type, clone.getName())));
								((ModifiableComplexType) type).add(clone);
								MainController.getInstance().setChanged();
								parent.refresh();
							}
						}
						event.consume();
					}
				}
			}
		});
	}

	@Override
	public ReadOnlyBooleanProperty renameableProperty() {
		// in some cases we allow editable (because of children) but we still don't want to support renaming
		return new SimpleBooleanProperty(editableProperty.get() && itemProperty.get().getSupportedProperties().contains(NameProperty.getInstance())); 
	}

	public ChildSelector getChildSelector() {
		return childSelector;
	}

	public void setChildSelector(ChildSelector childSelector) {
		this.childSelector = childSelector;
	}

}
