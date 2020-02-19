/*
 * This file is part of Alida, a Java library for 
 * Advanced Library for Integrated Development of Data Analysis Applications.
 *
 * Copyright (C) 2010 - @YEAR@
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Fore more information on Alida, visit
 *
 *    http://www.informatik.uni-halle.de/alida/
 *
 */

package de.unihalle.informatik.Alida.gui;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.swing.tree.DefaultMutableTreeNode;

import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;
/**
 * Class for managing nodes of the tree view. 	
 * <p>
 * It additionally contains the fully specified class name of the operator
 * and a boolean indicating whether this node is a operator or just a package
 * (containing operators).
 * <p>
 * The member <code>name</code> of the super class holds the operator name without packages.
 * @author posch
 */
public class ALDOperatorChooserTreeNode extends DefaultMutableTreeNode {
	/**
	 * The location object for this node. Is null for inner nodes, i.e.
	 * nodes which are not operators
	 */
	private ALDOperatorLocation location;
	
	/**
	 * Short description of operator.
	 */
	private final String shortDescription;

	/** constructor
	 * @param name    operator name without packages
	 * @param _location  location object
	 * @param isOperator is this node a operator?
	 */
	ALDOperatorChooserTreeNode(String name, ALDOperatorLocation _location) {
		super(name);
		this.location = _location;

		// check if operator has a short description and store for later use
		// as tooltip in the operator chooser
		String shortInfo = null;
		if ( this.location != null && this.location.getName() != null ) {
			try {
				Class<?> c = Class.forName( this.location.getName());
				Annotation anno = c.getAnnotation(ALDAOperator.class);
		
				if (anno != null) {
					Class<? extends Annotation> type = anno.annotationType();
					// get the value of the short description
					Method m = type.getDeclaredMethod("shortDescription");
					Object value = m.invoke(anno, (Object[])null);
					shortInfo = value.toString();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		this.shortDescription = shortInfo;

		if ( ALDOperatorChooserTree.debug ) {
			System.out.println(
					"ALDOperatorChooserTreeNode::ALDOperatorChooserTreeNode name " 
							+ name + " location " + this.location);
			if ( this.location != null ) 
				System.out.println("        location.name " +this.location.getName());
		}
	}

	/**
	 * Get location object.
	 * 
	 * @return location object.
	 */
	public ALDOperatorLocation getLocation() {
		return this.location;
	}
	
	/**
	 * Get the short description of the operator as extracted from annotation.
	 * @return Short description string.
	 */
	public String getShortDescription() {
		return this.shortDescription;
	}

	/** Is this node a operator?
	 * 
	 * @return	True, if node is an operator class.
	 */
	public boolean isOperator() {
		return this.location != null;
	}
}



