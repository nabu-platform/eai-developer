package be.nabu.eai.developer.impl;

import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import be.nabu.eai.api.LargeText;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.EntryContextMenuProvider;
import be.nabu.eai.developer.api.NodeContainer;
import be.nabu.eai.developer.api.SaveableContent;
import be.nabu.eai.developer.components.RepositoryBrowser;
import be.nabu.eai.developer.components.RepositoryBrowser.RepositoryTreeItem;
import be.nabu.eai.developer.managers.util.SimpleProperty;
import be.nabu.eai.developer.managers.util.SimplePropertyUpdater;
import be.nabu.eai.developer.util.Confirm;
import be.nabu.eai.developer.util.Confirm.ConfirmType;
import be.nabu.eai.developer.util.EAIDeveloperUtils;
import be.nabu.eai.developer.util.EAIDeveloperUtils.PropertyUpdaterListener;
import be.nabu.eai.repository.EAINode;
import be.nabu.eai.repository.EAIRepositoryUtils;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.eai.repository.api.Entry;
import be.nabu.eai.repository.api.ResourceEntry;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.jfx.control.tree.RemovableTreeItem;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.TreeCellValueLabel;
import be.nabu.jfx.control.tree.TreeItem;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.types.api.annotation.ComplexTypeDescriptor;

public class EditContextMenu implements EntryContextMenuProvider {

	private void listDeprecated(Entry entry, List<Entry> entries) {
		if (entry.isNode() && entry.getNode() instanceof EAINode && ((EAINode) entry.getNode()).getDeprecated() != null) {
			entries.add(entry);
		}
		for (Entry child : entry) {
			listDeprecated(child, entries);
		}
	}
	
	private void listUnused(Entry entry, List<Entry> entries) {
		if (entry.isNode() && entry.getNode() instanceof EAINode && ((EAINode) entry.getNode()).getDeprecated() == null) {
			List<String> dependencies = entry.getRepository().getDependencies(entry.getId());
			if (dependencies == null || dependencies.isEmpty()) {
				entries.add(entry);
			}
		}
		for (Entry child : entry) {
			listUnused(child, entries);
		}
	}
	
	private void undeprecateAll(Entry entry) {
		if (entry.isNode() && entry.getNode() instanceof EAINode && entry instanceof RepositoryEntry) {
			((EAINode) entry.getNode()).setDeprecated(null);
			((RepositoryEntry) entry).saveNode();
			TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
			if (resolve instanceof RepositoryTreeItem) {
				((RepositoryTreeItem) resolve).deprecatedProperty().set(null);
			}
		}
		for (Entry child : entry) {
			undeprecateAll(child);
		}
	}
	
	private void deprecateAll(Entry entry) {
		// it may already be deprecated, we don't want to update the date
		if (entry.isNode() && entry.getNode() instanceof EAINode && entry instanceof RepositoryEntry && ((EAINode) entry.getNode()).getDeprecated() == null) {
			((EAINode) entry.getNode()).setDeprecated(new Date());
			((RepositoryEntry) entry).saveNode();
			TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
			if (resolve instanceof RepositoryTreeItem) {
				ObjectProperty<Date> deprecatedProperty = ((RepositoryTreeItem) resolve).deprecatedProperty();
				if (deprecatedProperty.get() == null) {
					deprecatedProperty.set(((EAINode) entry.getNode()).getDeprecated());
				}
			}
		}
		for (Entry child : entry) {
			deprecateAll(child);
		}
	}
	
	private void markDeprecatedDependencies(Entry entry, List<Entry> deprecations, List<String> checked) {
		// we start from a deprecated node and go from there
		if (entry.isNode() && entry.getNode() instanceof EAINode && ((EAINode) entry.getNode()).getDeprecated() != null) {
			// we take all references for a deprecated node
			List<String> references = EAIResourceRepository.getInstance().getReferences(entry.getId());
			// for each reference, we check all dependencies to see if everything is deprecated
			for (String reference : references) {
				if (reference == null) {
					continue;
				}
				if (checked.contains(reference)) {
					continue;
				}
				else {
					checked.add(reference);
				}
				Entry referencedEntry = entry.getRepository().getEntry(reference);
				// only proceed if the reference is not yet deprecated _and_ we can actually update its deprecation status
				if (referencedEntry instanceof RepositoryEntry && referencedEntry.getNode().getDeprecated() == null && !deprecations.contains(referencedEntry)) {
					List<String> dependencies = EAIResourceRepository.getInstance().getDependencies(reference);
					boolean allDependenciesDeprecated = true;
					for (String dependency : dependencies) {
						// if at least one dependency is not deprecated, we don't deprecate this node
						if (entry.getRepository().getNode(dependency).getDeprecated() == null) {
							allDependenciesDeprecated = false;
							break;
						}
					}
					if (allDependenciesDeprecated) {
						deprecations.add(referencedEntry);
						deprecate(Arrays.asList(referencedEntry));
					}
				}
			}
			
		}
		for (Entry child : entry) {
			markDeprecatedDependencies(child, deprecations, checked);
		}
	}
	
	private void deprecate(List<Entry> deprecations) {
		for (Entry deprecation : deprecations) {
			if (deprecation instanceof RepositoryEntry) {
				((EAINode) deprecation.getNode()).setDeprecated(new Date());
				((RepositoryEntry) deprecation).saveNode();
				TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(deprecation.getId().replace(".", "/"));
				if (resolve instanceof RepositoryTreeItem) {
					((RepositoryTreeItem) resolve).deprecatedProperty().set(new Date());
				}
			}
		}
	}
	
	private void undeprecate(List<Entry> deprecations) {
		for (Entry deprecation : deprecations) {
			if (deprecation instanceof RepositoryEntry) {
				((EAINode) deprecation.getNode()).setDeprecated(null);
				((RepositoryEntry) deprecation).saveNode();
				TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(deprecation.getId().replace(".", "/"));
				if (resolve instanceof RepositoryTreeItem) {
					((RepositoryTreeItem) resolve).deprecatedProperty().set(null);
				}
			}
		}
	}
	
	@ComplexTypeDescriptor(propOrder = {"summary", "description", "reference", "tags", "mergeScript", "priority", "properties"})
	public static class NodeSummary {
		private String summary, description, mergeScript, reference;
		private Integer priority;
		private List<String> tags;
		private Map<String, String> properties;
		public String getSummary() {
			return summary;
		}
		public void setSummary(String summary) {
			this.summary = summary;
		}
		@LargeText
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public List<String> getTags() {
			return tags;
		}
		public void setTags(List<String> tags) {
			this.tags = tags;
		}
		@LargeText
		public String getMergeScript() {
			return mergeScript;
		}
		public void setMergeScript(String mergeScript) {
			this.mergeScript = mergeScript;
		}
		public Map<String, String> getProperties() {
			return properties;
		}
		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}
		public String getReference() {
			return reference;
		}
		public void setReference(String reference) {
			this.reference = reference;
		}
		public Integer getPriority() {
			return priority;
		}
		public void setPriority(Integer priority) {
			this.priority = priority;
		}
	}
	
	@Override
	public MenuItem getContext(Entry entry) {
		Menu menu = new Menu("Edit");
		
		if (entry.isNode()) {
			MenuItem open = new MenuItem("Open");
			open.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					MainController.getInstance().open(entry.getId());
//					RepositoryBrowser.open(MainController.getInstance(), entry);
				}
			});
			open.setGraphic(MainController.loadGraphic("edit-open.png"));
			menu.getItems().add(open);
			MenuItem copy = new MenuItem("Copy");
			copy.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						MainController.copy(entry.getNode().getArtifact());
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			copy.setGraphic(MainController.loadGraphic("edit-copy.png"));
			menu.getItems().add(copy);
			
			MenuItem cut = new MenuItem("Cut");
			cut.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						MainController.cut(entry.getId());
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			cut.setGraphic(MainController.loadGraphic("edit-copy.png"));
			menu.getItems().add(cut);
			
			// only matters if you can persist it
			if (entry instanceof RepositoryEntry && entry.getNode() instanceof EAINode) {
				final MenuItem lock = new MenuItem(entry.getNode().isLocked() ? "Unlock" : "Lock");
				menu.getItems().add(lock);
				lock.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						((EAINode) entry.getNode()).setLocked(!entry.getNode().isLocked());
						((RepositoryEntry) entry).saveNode();
						// update label
						lock.setText(entry.getNode().isLocked() ? "Unlock" : "Lock");
						TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
						if (resolve instanceof RepositoryTreeItem) {
							((RepositoryTreeItem) resolve).lockedProperty().set(entry.getNode().isLocked());
						}
						// if you unlocked it, clear the lock
						if (!entry.getNode().isLocked()) {
							MainController.getInstance().lock(entry.getId()).set(null);
						}
					}
				});
				
				if (entry.getNode().getDeprecated() != null) {
					MenuItem undeprecate = new MenuItem("Undo Deprecate");
					undeprecate.setGraphic(MainController.loadFixedSizeGraphic("deprecated.png", 16));
					undeprecate.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							((EAINode) entry.getNode()).setDeprecated(null);
							((RepositoryEntry) entry).saveNode();
//							MainController.getInstance().getRepositoryBrowser().refresh();
							TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
							if (resolve instanceof RepositoryTreeItem) {
								((RepositoryTreeItem) resolve).deprecatedProperty().set(null);
							}
						}
					});
					menu.getItems().add(undeprecate);
				}
				else {
					MenuItem deprecate = new MenuItem("Deprecate");
					deprecate.setGraphic(MainController.loadFixedSizeGraphic("deprecated.png", 16));
					deprecate.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							((EAINode) entry.getNode()).setDeprecated(new Date());
							((RepositoryEntry) entry).saveNode();
//							MainController.getInstance().getRepositoryBrowser().refresh();
							TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
							if (resolve instanceof RepositoryTreeItem) {
								((RepositoryTreeItem) resolve).deprecatedProperty().set(new Date());
							}
						}
					});
					menu.getItems().add(deprecate);
				}
				
				MenuItem properties = new MenuItem("Properties");
				menu.getItems().add(0, properties);
				
				properties.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent arg0) {
						NodeContainer<?> container = MainController.getInstance().getContainer(entry.getId() + ":properties");
						if (container != null) {
							container.activate();
						}
						else {
							NodeSummary nodeSummary = new NodeSummary();
							nodeSummary.setDescription(entry.getNode().getDescription());
							nodeSummary.setSummary(entry.getNode().getSummary());
							nodeSummary.setTags(entry.getNode().getTags());
							nodeSummary.setMergeScript(entry.getNode().getMergeScript());
							nodeSummary.setProperties(entry.getNode().getProperties() == null ? new HashMap<String, String>() : entry.getNode().getProperties());
							nodeSummary.setReference(entry.getNode().getReference());
							nodeSummary.setPriority(entry.getNode().getPriority());
							SimplePropertyUpdater createUpdater = EAIDeveloperUtils.createUpdater(nodeSummary, new PropertyUpdaterListener() {
								@Override
								public boolean updateProperty(Property<?> property, Object value) {
									NodeContainer<?> container = MainController.getInstance().getContainer(entry.getId() + ":properties");
									container.setChanged(true);
									return true;
								}
							});
							VBox summaryContainer = new VBox();
							VBox summary = new VBox();
							summary.setPadding(new Insets(10));
							MainController.getInstance().showProperties(createUpdater, summary, true);
							
							HBox buttons = new HBox();
							buttons.setPadding(new Insets(10));
							Button addProperty = new Button("Add Property");
							buttons.getChildren().add(addProperty);
							summaryContainer.getChildren().addAll(summary, buttons);
							
							addProperty.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
								@Override
								public void handle(ActionEvent arg0) {
									List<Property<?>> properties = new ArrayList<Property<?>>();
									properties.add(new SimpleProperty<String>("Name", String.class, true));
									SimplePropertyUpdater propertyUpdater = new SimplePropertyUpdater(true, new LinkedHashSet<Property<?>>(properties));
									EAIDeveloperUtils.buildPopup(MainController.getInstance(), propertyUpdater, "Add property", new EventHandler<ActionEvent>() {
										@Override
										public void handle(ActionEvent arg0) {
											String value = propertyUpdater.getValue("Name");
											if (value != null) {
												nodeSummary.getProperties().put(value, null);
												// redraw
												MainController.getInstance().showProperties(createUpdater, summary, true);
											}
										}
									});
								}
							});
							
							Tab newTab = MainController.getInstance().newTab("Properties for: " + entry.getId());
							newTab.setId(entry.getId() + ":properties");
							newTab.setContent(summaryContainer);
							newTab.setUserData(new SaveableContent() {
								@Override
								public void save() {
									EAINode node = (EAINode) entry.getNode();
									node.setSummary(nodeSummary.getSummary());
									node.setDescription(nodeSummary.getDescription());
									node.setTags(nodeSummary.getTags());
									node.setMergeScript(nodeSummary.getMergeScript());
									node.setReference(nodeSummary.getReference());
									node.setPriority(nodeSummary.getPriority());
									// update the todos!
									node.setTodos(EAIRepositoryUtils.getTodos(entry.getId()));
									EAIResourceRepository.getInstance().updateTodos(entry.getId(), node.getTodos());
									// remove empty properties
									Iterator<java.util.Map.Entry<String, String>> iterator = nodeSummary.getProperties().entrySet().iterator();
									while (iterator.hasNext()) {
										if (iterator.next().getValue() == null) {
											iterator.remove();
										}
									}
									node.setProperties(nodeSummary.getProperties());
									((RepositoryEntry) entry).saveNode();
									NodeContainer<?> container = MainController.getInstance().getContainer(entry.getId() + ":properties");
									container.setChanged(false);
									// make sure everyone knows!
									EAIDeveloperUtils.updated(entry.getId());
								}
							});
						}
					}
				});
			}
		}
		else if (entry instanceof ResourceEntry) {
			MenuItem paste = new MenuItem("Paste");
			paste.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					try {
						MainController.getInstance().getTree().getClipboardHandler().setClipboard(Clipboard.getSystemClipboard());
					}
					catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			paste.setGraphic(MainController.loadGraphic("edit-paste.png"));
			menu.getItems().add(paste);
			
			MenuItem recursiveDeprecate = new MenuItem("Calculate Deprecated Dependencies");
			recursiveDeprecate.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					// loop until everything is resolved or until we hit an unseemly limit which indicates a neverending loop (shouldn't occur?)
					int counter = 0;
					List<Entry> deprecations = new ArrayList<Entry>();
					while (counter < 100) {
						int initial = deprecations.size();
						markDeprecatedDependencies(entry, deprecations, new ArrayList<String>());
						int afterwards = deprecations.size();
						if (initial == afterwards) {
							break;
						}
						counter++;
					}
					if (deprecations.isEmpty()) {
						Confirm.confirm(ConfirmType.INFORMATION, "No deprecations", "Did not find any new items that are deprecated", null);
					}
					else {
						Confirm.confirm(ConfirmType.WARNING, deprecations.size() + " new deprecations", "Do you want to apply deprecation to the following artifacts?\n" + stringify(deprecations), new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								// do nothing, it is already persisted							
							}
						}, new EventHandler<ActionEvent>() {
							@Override
							public void handle(ActionEvent event) {
								undeprecate(deprecations);								
							}
						});
					}
				}

			});
			menu.getItems().add(recursiveDeprecate);
			
			MenuItem undeprecateAll = new MenuItem("Undeprecate everything");
			undeprecateAll.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					undeprecateAll(entry);
				}
			});
			menu.getItems().add(undeprecateAll);
			
			MenuItem deprecateAll = new MenuItem("Deprecate everything");
			deprecateAll.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					deprecateAll(entry);
				}
			});
			menu.getItems().add(deprecateAll);
			
			MenuItem deleteDeprecated = new MenuItem("Delete deprecated");
			deleteDeprecated.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					List<Entry> deprecated = new ArrayList<Entry>();
					listDeprecated(entry, deprecated);
					Confirm.confirm(ConfirmType.WARNING, deprecated.size() + " items to remove", "Are you sure you want to remove all these deprecated artifacts?\n" + stringify(deprecated), new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							for (Entry single : deprecated) {
								if (single instanceof ResourceEntry) {
									MainController.getInstance().getRepositoryBrowser().delete((ResourceEntry) single);
								}
							}
						}
					});
				}
			});
			menu.getItems().add(deleteDeprecated);
			
			MenuItem deprecateUnused = new MenuItem("Deprecate unused");
			deprecateUnused.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					List<Entry> deprecated = new ArrayList<Entry>();
					listUnused(entry, deprecated);
					Confirm.confirm(ConfirmType.WARNING, deprecated.size() + " items to deprecate", "Are you sure you want to deprecate all these unused artifacts?\n" + stringify(deprecated), new EventHandler<ActionEvent>() {
						@Override
						public void handle(ActionEvent event) {
							deprecate(deprecated);
						}
					});
				}
			});
			menu.getItems().add(deprecateUnused);
		}
		
		if (entry.getParent() != null && entry instanceof ResourceEntry) {
			MenuItem delete = new MenuItem("Delete");
			delete.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
					if (resolve instanceof RemovableTreeItem) {
						((RemovableTreeItem<Entry>) resolve).remove();
					}
				}
			});
			delete.setGraphic(MainController.loadGraphic("edit-delete.png"));
			menu.getItems().add(delete);
			
			MenuItem rename = new MenuItem("Rename");
			rename.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					TreeItem<Entry> resolve = MainController.getInstance().getTree().resolve(entry.getId().replace(".", "/"));
					if (resolve != null) {
						TreeCell<Entry> treeCell = MainController.getInstance().getTree().getTreeCell(resolve);
						if (treeCell != null && treeCell.getCellValue() instanceof TreeCellValueLabel) {
							((TreeCellValueLabel<?>) treeCell.getCellValue()).edit();
						}
					}
				}
			});
			rename.setGraphic(MainController.loadGraphic("edit-edit.png"));
			menu.getItems().add(rename);
		}
		return menu.getItems().isEmpty() ? null : menu;
	}

	private static String stringify(List<Entry> deprecations) {
		StringBuilder builder = new StringBuilder();
		for (Entry deprecation : deprecations) {
			if (!builder.toString().isEmpty()) {
				builder.append("\n");
			}
			builder.append(deprecation.getId());
		}
		return builder.toString();
	}
}
