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

package be.nabu.eai.developer.managers.base;

import java.lang.annotation.Annotation;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import be.nabu.eai.api.EnvironmentSpecific;
import be.nabu.eai.developer.ComplexContentEditor;
import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.api.ArtifactMerger;
import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.api.annotation.Field;
import be.nabu.libs.types.base.TypeBaseUtils;
import be.nabu.libs.types.java.BeanInstance;
import be.nabu.libs.types.java.BeanType;
import be.nabu.libs.types.structure.Structure;
import be.nabu.libs.types.structure.StructureInstance;

public class JAXBComplexArtifactMerger<C, T extends JAXBArtifact<C>> implements ArtifactMerger<T> {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public boolean merge(T source, T target, AnchorPane pane, Repository targetRepository) {
		C config = source.getConfig();
		
		BeanInstance sourceInstance = new BeanInstance(config);
		BeanInstance targetInstance = target == null ? null : new BeanInstance(target.getConfig());
		
		BeanType type = sourceInstance.getType();
		
		// we build a new structure with only the fields that should differ
		Structure structure = new Structure();
		structure.setName(type.getName());
		
		StructureInstance mergeInstance = structure.newInstance();
		boolean hasFields = false;
		// check all the types for environment specific
		for (Element<?> element : TypeUtils.getAllChildren(type)) {
			for (Annotation annotation : ((BeanType) element.getParent()).getAnnotations(element.getName())) {
				// if it is environment specific, add it to the structure
				if (annotation instanceof EnvironmentSpecific || (annotation instanceof Field && ((Field) annotation).environmentSpecific())) {
					hasFields = true;
					structure.add(TypeBaseUtils.clone(element, structure));
					// already initially set the target value in the source!
					if (target != null) {
						sourceInstance.set(element.getName(), targetInstance.get(element.getName()));
						mergeInstance.set(element.getName(), targetInstance.get(element.getName()));
					}
					else {
						mergeInstance.set(element.getName(), sourceInstance.get(element.getName()));
					}
				}
			}
		}
		
		if (hasFields) {
			ComplexContentEditor editor = new ComplexContentEditor(mergeInstance, true, targetRepository) {
				@Override
				public void update() {
					super.update();
					for (Element<?> element : TypeUtils.getAllChildren(structure)) {
						sourceInstance.set(element.getName(), mergeInstance.get(element.getName()));
					}
					MainController.getInstance().setChanged();
				}
			};
			ScrollPane scroll = new ScrollPane();
			scroll.setContent(editor.getTree());
			editor.getTree().getRootCell().expandAll(1);
			pane.getChildren().add(scroll);
			AnchorPane.setLeftAnchor(scroll, 0d);
			AnchorPane.setRightAnchor(scroll, 0d);
			AnchorPane.setTopAnchor(scroll, 0d);
			AnchorPane.setBottomAnchor(scroll, 0d);
			editor.getTree().prefWidthProperty().bind(scroll.widthProperty());
		}
		return hasFields;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Class getArtifactClass() {
		return JAXBArtifact.class;
	}

}
