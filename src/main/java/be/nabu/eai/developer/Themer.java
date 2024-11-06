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

package be.nabu.eai.developer;

import java.net.URL;

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
