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
import be.nabu.libs.services.vm.step.Invoke;
import be.nabu.libs.services.vm.step.Link;
import be.nabu.libs.services.vm.step.Map;
import be.nabu.libs.services.vm.api.Step;
import be.nabu.libs.types.BaseTypeInstance;
import be.nabu.libs.types.ParsedPath;
import be.nabu.libs.types.SimpleTypeWrapperFactory;
import be.nabu.libs.types.TypeConverterFactory;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.MarshalException;
import be.nabu.libs.types.api.SimpleTypeWrapper;
import be.nabu.libs.types.api.TypeConverter;
import be.nabu.libs.types.api.Unmarshallable;
import be.nabu.libs.validator.api.ValidationMessage;
import be.nabu.libs.validator.api.ValidationMessage.Severity;

public class FixedValue {
	
	public static final String SHOW_HIDDEN_FIXED = "be.nabu.eai.developer.showHiddenFixedValues";
	
	private Link link;
	private TreeCell<Element<?>> cell;
	private ImageView image;
	
	public static void allowFixedValue(final MainController controller, final java.util.Map<Link, FixedValue> fixedValues, final Tree<Step> serviceTree, final Tree<Element<?>> tree) {
		final SimpleTypeWrapper simpleTypeWrapper = SimpleTypeWrapperFactory.getInstance().getWrapper();
		final TypeConverter typeConverter = TypeConverterFactory.getInstance().getConverter();
		tree.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				final TreeCell<Element<?>> selected = tree.getSelectionModel().getSelectedItem();
				// it must be unmarshallable and it _can_ be a list, if it's a list, you will get the opportunity to set the indexes
				if (selected != null && (selected.getItem().itemProperty().get().getType() instanceof Unmarshallable || typeConverter.canConvert(new BaseTypeInstance(simpleTypeWrapper.wrap(String.class)), selected.getItem().itemProperty().get()))) {
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
											Object unmarshalled;
											if (selected.getItem().itemProperty().get().getType() instanceof Unmarshallable) {
												unmarshalled = ((Unmarshallable<?>) selected.getItem().itemProperty().get().getType()).unmarshal(value, selected.getItem().itemProperty().get().getProperties());
											}
											else {
												unmarshalled = typeConverter.convert(value, new BaseTypeInstance(simpleTypeWrapper.wrap(String.class)), selected.getItem().itemProperty().get());
											}
											if (unmarshalled == null) {
												throw new MarshalException("Can not unmarshal this value");	
											}
											MainController.getInstance().setChanged();
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
													DropLinkListener.setDefaultIndexes(path.getChildPath(), tree.rootProperty().get(), true);
													link.setTo(path.getChildPath().toString());
													invoke.getChildren().add(link);
													link.setParent(invoke);
												}
												else {
													if (!path.getName().equals("pipeline")) {
														throw new IllegalArgumentException("Can't set it here");
													}
													DropLinkListener.setDefaultIndexes(path.getChildPath(), tree.rootProperty().get(), true);
													link.setTo(path.getChildPath().toString());
													link.setParent((Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get());
													((Map) serviceTree.getSelectionModel().getSelectedItem().getItem().itemProperty().get()).getChildren().add(link);
												}
												fixedValues.put(link, new FixedValue(controller, selected, link));
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
	
	public FixedValue(MainController controller, TreeCell<Element<?>> cell, Link link) {
		this.cell = cell;
		this.link = link;
		draw(controller, link);
	}
	
	private void draw(final MainController controller, final Link link) {
		image = MainController.loadGraphic("fixed-value.png");
		image.setManaged(false);
		((Pane) cell.getTree().getParent()).getChildren().add(image);
		image.layoutXProperty().bind(cell.leftAnchorXProperty().subtract(10));
		// image is 16 pixels, we want to center it
		image.layoutYProperty().bind(cell.leftAnchorYProperty().subtract(8));
		// make invisible if it is not in scope
		if (Boolean.FALSE.toString().equals(System.getProperty(SHOW_HIDDEN_FIXED, "true"))) {
			image.visibleProperty().bind(cell.getNode().visibleProperty());
		}
		image.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				cell.show();
				controller.showProperties(new LinkPropertyUpdater(link));
			}
		});
	}
	
	public void remove() {
		((Pane) image.getParent()).getChildren().remove(image);
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
