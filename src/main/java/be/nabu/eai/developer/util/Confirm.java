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

import be.nabu.eai.developer.MainController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class Confirm {
	
	public enum ConfirmType {
		ERROR("dialog/dialog-error.png"),
		INFORMATION("dialog/dialog-information.png"),
		WARNING("dialog/dialog-warning.png"),
		QUESTION("dialog/dialog-question.png")
		;

		private String image;

		private ConfirmType(String image) {
			this.image = image;
		}
		
		public String getImage() {
			return image;
		}
	}
	
	public static Stage confirm(ConfirmType confirmType, String title, String question, EventHandler<ActionEvent> eventHandler) {
		return confirm(confirmType, title, question, eventHandler, null);
	}
	
	public static Stage confirm(ConfirmType confirmType, String title, String question, EventHandler<ActionEvent> eventHandler, EventHandler<ActionEvent> cancelHandler) {
		VBox vbox = new VBox();
		vbox.setAlignment(Pos.CENTER);
		vbox.setMinWidth(450);
		vbox.setSpacing(20);
		HBox content = new HBox();
		content.setAlignment(Pos.CENTER);
		content.setSpacing(20);
		TextArea label = new TextArea(question);
		HBox.setHgrow(label, Priority.ALWAYS);
		label.setWrapText(true);
		label.setEditable(false);
		label.setPrefRowCount(question.length() - question.replace("\n", "").length() + 1);
//		Label label = new Label(question);
//		label.setWrapText(true);
		content.getChildren().addAll(MainController.loadGraphic(confirmType.getImage()), label);
		content.prefWidthProperty().bind(vbox.widthProperty());
		
		final Stage stage = new Stage();
		Stage activeStage = MainController.getInstance().getActiveStage();
		stage.initOwner(activeStage == null ? MainController.getInstance().getStage() : activeStage);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.setScene(new Scene(vbox));
		stage.setTitle(title);

		stage.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				if (arg0.getCode() == KeyCode.ESCAPE) {
					stage.hide();
				}
			}
		});
		HBox buttons = new HBox();
		Button confirm = new Button("Ok");
		confirm.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if (eventHandler != null) {
					eventHandler.handle(arg0);
				}
				stage.hide();
			}
		});
		buttons.getChildren().add(confirm);
		boolean ignoreCancel = eventHandler != null && cancelHandler != null && eventHandler.equals(cancelHandler);
		// only add a cancel if the confirmation has an action attached to it, we did a sneaky workaround for legacy reasons in case you want a single button but with a custom action...
		if ((eventHandler != null || cancelHandler != null) && !ignoreCancel) {
			Button cancel = new Button("Cancel");
			cancel.addEventHandler(ActionEvent.ANY, new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					if (cancelHandler != null) {
						cancelHandler.handle(arg0);
					}
					stage.hide();
				}
			});
			buttons.getChildren().add(cancel);
		}
		buttons.setAlignment(Pos.CENTER);
		buttons.prefWidthProperty().bind(vbox.widthProperty());
		
		vbox.getChildren().addAll(content, buttons);
		VBox.setVgrow(content, Priority.ALWAYS);
		vbox.setPadding(new Insets(20));
		stage.show();
		return stage;
	}
}
