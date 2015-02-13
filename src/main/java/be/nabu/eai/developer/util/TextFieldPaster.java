package be.nabu.eai.developer.util;

import be.nabu.eai.repository.api.Entry;
import be.nabu.jfx.control.tree.Tree;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class TextFieldPaster {
	public static void makePastableFromBrowser(final TextField field, final Tree<Entry> tree) {
		field.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if (event.getCode() == KeyCode.V && event.isShiftDown() && event.isControlDown()) {
					if (tree.getCopied() != null && !tree.getCopied().isEmpty()) {
						Platform.runLater(new Runnable() {
							public void run() {
								field.textProperty().set(tree.getCopied().get(0).getItem().itemProperty().get().getId());
							}
						});
						event.consume();
					}
				}
			}
		});
	}
}
