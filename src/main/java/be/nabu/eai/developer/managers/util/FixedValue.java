package be.nabu.eai.developer.managers.util;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.controllers.NameOnlyCreateController;
import be.nabu.jfx.control.tree.Tree;
import be.nabu.jfx.control.tree.TreeCell;
import be.nabu.jfx.control.tree.drag.TreeDragDrop;
import be.nabu.libs.services.vm.Invoke;
import be.nabu.libs.services.vm.Link;
import be.nabu.libs.services.vm.Map;
import be.nabu.libs.services.vm.Step;
import be.nabu.libs.types.ParsedPath;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.MarshalException;
import be.nabu.libs.types.api.Unmarshallable;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class FixedValue {
	
	private Link link;
	private TreeCell<Element<?>> cell;
	private ImageView image;
	
	public static void allowFixedValue(final MainController controller, final java.util.Map<Link, FixedValue> fixedValues, final Tree<Step> serviceTree, final Tree<Element<?>> tree) {
		tree.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				final TreeCell<Element<?>> selected = tree.getSelectionModel().getSelectedItem();
				// it must be unmarshallable and it _can_ be a list, if it's a list, you will get the opportunity to set the indexes
				if (selected != null && selected.getItem().itemProperty().get().getType() instanceof Unmarshallable) {
					if (event.getClickCount() == 2) {
						try {
							FXMLLoader loader = controller.load("new.nameOnly.fxml", "Fixed Value", true);
							final NameOnlyCreateController createController = loader.getController();
							// check if there is an existing value, if so, we use that
							for (FixedValue fixed : fixedValues.values()) {
								if (fixed.getCell().equals(tree.getSelectionModel().getSelectedItem())) {
									createController.getTxtName().setText(fixed.getLink().getFrom());
									break;
								}
							}
							createController.getBtnCreate().addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
								@Override
								public void handle(MouseEvent arg0) {
									String value = createController.getTxtName().getText();
									createController.close();
									FixedValue existing = null;
									for (FixedValue fixed : fixedValues.values()) {
										if (fixed.getCell().equals(tree.getSelectionModel().getSelectedItem())) {
											existing = fixed;
											break;
										}
									}
									if (value != null && value.isEmpty()) {
										value = null;
									}
									// if there is a fixed value, we need to remove it
									if (value == null) {
										if (existing != null) {
											fixedValues.remove(existing.getLink());
											existing.getLink().getParent().getChildren().remove(existing.getLink());
											((Pane) existing.getImage().getParent()).getChildren().remove(existing.getImage());
										}
									}
									else {
										try {
											// need to check if it is a valid unmarshallable value
											Object unmarshalled = ((Unmarshallable<?>) selected.getItem().itemProperty().get().getType()).unmarshal(value, selected.getItem().itemProperty().get().getProperties());
											if (existing != null) {
												existing.getLink().setFrom(value);
											}
											else {
												Link link = new Link();
												link.setFixedValue(true);
												link.setFrom(value);
												
												ParsedPath path = new ParsedPath(TreeDragDrop.getPath(selected.getItem()));
												if (tree.get("invoke") != null) {
													Invoke invoke = (Invoke) tree.get("invoke");
													// the first entry must be input
													if (!path.getName().equals("input")) {
														throw new IllegalArgumentException("Can't set it here");
													}
													DropLinkListener.setDefaultIndexes(path.getChildPath(), tree.rootProperty().get());
													link.setTo(path.getChildPath().toString());
													invoke.getChildren().add(link);
													link.setParent(invoke);
												}
												else {
													if (!path.getName().equals("pipeline")) {
														throw new IllegalArgumentException("Can't set it here");
													}
													DropLinkListener.setDefaultIndexes(path.getChildPath(), tree.rootProperty().get());
													link.setTo(path.getChildPath().toString());
													link.setParent((Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get());
													((Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get()).getChildren().add(link);
												}
												fixedValues.put(link, new FixedValue(selected, link));
											}
										}
										catch (MarshalException e) {
											controller.notify(new ValidationMessage(Severity.ERROR, "The value '" + value + "' is incorrect for this field type"));
										}
																				
									}
								}
							});
						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		});
	}
	
	public FixedValue(TreeCell<Element<?>> cell, Link link) {
		this.cell = cell;
		this.link = link;
		draw();
	}
	
	private void draw() {
		image = MainController.loadGraphic("fixed-value.png");
		image.setManaged(false);
		((Pane) cell.getTree().getParent()).getChildren().add(image);
		image.layoutXProperty().bind(cell.leftAnchorXProperty().subtract(10));
		// image is 16 pixels, we want to center it
		image.layoutYProperty().bind(cell.leftAnchorYProperty().subtract(8));
		image.visibleProperty().bind(cell.getNode().visibleProperty());
		image.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				System.out.println("Bound y? " + image.layoutYProperty().isBound());
			}
		});
	}

	public ImageView getImage() {
		return image;
	}

	public Link getLink() {
		return link;
	}

	public TreeCell<Element<?>> getCell() {
		return cell;
	}
}