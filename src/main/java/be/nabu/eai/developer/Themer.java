package be.nabu.eai.developer;

import java.net.URL;

import javafx.scene.text.Font;

public class Themer {
	public void load() {
//		Font f = Font.loadFont(Themer.class.getResource("/theme/main/Lato-Regular.ttf").toExternalForm(), 12);
		
		URL resource = Themer.class.getClassLoader().getResource("theme/main/theme.css");
		System.out.println("Loading theme: " + resource);
		if (resource != null) {
			MainController.registerStyleSheet(resource.toExternalForm());
		}
		// we want the combo boxes!
		MainController.getInstance().setLeftAlignComboBox(true);
	}
}
