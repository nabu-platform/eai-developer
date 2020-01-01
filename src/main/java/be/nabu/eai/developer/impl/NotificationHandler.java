package be.nabu.eai.developer.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import be.nabu.eai.developer.MainController;
import be.nabu.libs.validator.api.ValidationMessage.Severity;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class NotificationHandler {
	private Map<HBox, Long> toShow = new LinkedHashMap<HBox, Long>();
	private Stage current;
	private Date currentUntil;
	private Thread thread;
	
	public NotificationHandler(Pane target) {
		thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					synchronized(NotificationHandler.this) {
						// allow for some wiggle room in the timing
						if (currentUntil != null && currentUntil.getTime() <= new Date().getTime() + 100) {
							if (current != null) {
								final Stage currentToMove = current;
								Platform.runLater(new Runnable() {
									@Override
									public void run() {
										currentToMove.hide();
										target.getChildren().add(0, currentToMove.getScene().getRoot());
										if (target.getChildren().size() > 50) {
											target.getChildren().remove(target.getChildren().size() - 1);
										}
									}
								});
								current = null;
							}
							currentUntil = null;
						}
						Iterator<Entry<HBox, Long>> iterator = toShow.entrySet().iterator();
						if (iterator.hasNext()) {
							Entry<HBox, Long> next = iterator.next();
							iterator.remove();
							show(next.getKey(), next.getValue());
						}
					}
					try {
						long millis;
						if (currentUntil != null) {
							millis = currentUntil.getTime() - new Date().getTime();		
						}
						else {
							millis = 1000l * 60 * 60;
						}
						Thread.sleep(millis);
					}
					catch (InterruptedException e) {
						// continue
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
	
	public void notify(String message, Long duration, Severity severity, Button...buttons) {
		HBox box = new HBox();
		box.setPadding(new Insets(10));
		box.setStyle("-fx-background-color: #fafafa; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-border-color:#cccccc;");
		switch(severity) {
			case CRITICAL:
			case ERROR:
				box.getChildren().add(MainController.loadGraphic("dialog/dialog-error.png"));
			break;
			case WARNING:
				box.getChildren().add(MainController.loadGraphic("dialog/dialog-warning.png"));
			break;
			default:
				box.getChildren().add(MainController.loadGraphic("dialog/dialog-information.png"));
			break;
		}
		Label label = new Label(message);
		label.setWrapText(true);
		label.setMaxWidth(400d);
		HBox.setHgrow(label, Priority.ALWAYS);
		box.setAlignment(Pos.CENTER);
		HBox.setMargin(label, new Insets(0, 10, 0, 10));
		box.getChildren().add(label);
		if (buttons != null && buttons.length > 0) {
			HBox buttonWrapper = new HBox();
			buttonWrapper.setAlignment(Pos.CENTER);
			buttonWrapper.getChildren().addAll(buttons);
			box.getChildren().add(buttonWrapper);
		}
		synchronized (this) {
			if (current != null) {
				toShow.put(box, duration);
			}
			else {
				show(box, duration);
			}
		}
	}

	private void show(HBox box, Long duration) {
		current = new Stage();
		Scene scene = new Scene(box);
		current.setScene(scene);
		current.initStyle(StageStyle.UNDECORATED);
		Stage stage = MainController.getInstance().getStage();
		// javafx will position it correctly (hopefully)
		current.setX(stage.getWidth() - 10);
		current.setY(stage.getHeight() - 10);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				current.show();
				current.requestFocus();
				current.setAlwaysOnTop(true);
			}
		});
		currentUntil = new Date(new Date().getTime() + duration);
		thread.interrupt();
	}
	
}
