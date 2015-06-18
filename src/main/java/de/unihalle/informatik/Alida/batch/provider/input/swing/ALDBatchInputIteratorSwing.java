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

/* 
 * Most recent change(s):
 * 
 * $Rev: 5825 $
 * $Date: 2012-07-24 09:35:34 +0200 (Di, 24 Jul 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.batch.provider.input.swing;

import java.lang.reflect.Field;
import java.util.Iterator;

import de.unihalle.informatik.Alida.batch.provider.input.ALDBatchInputIterator;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOProviderException;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Interface for Alida's automatic batch data I/O via GUIs based on Swing.
 * 
 * @author moeller
 */
public interface ALDBatchInputIteratorSwing	extends ALDBatchInputIterator {

	/**
	 * Interface method to create a GUI element for batch data input.
	 * @param cl 			Class of object to be returned.
	 * @param obj	 		The value of this object to be set as inital value.
	 * @param descr		Optional descriptor for additional information.
	 * @return	Alida GUI component to input batch values.
	 */
	public abstract ALDSwingComponent createGUIElement(
			Field field, Class<?> cl, Object obj, ALDParameterDescriptor descr) 
		throws ALDBatchIOProviderException;

	/**
	 * Interface method for setting new parameter values in GUI.
	 * 
	 * @param field				Field of parameter object to be handled.
	 * @param cl 					Class of parameter object to be handled.
	 * @param guiElement	Corresponding GUI element.
	 * @param value				New value of the parameter.
	 */
	public abstract void setValue(
			Field field, Class<?> cl, ALDSwingComponent guiElement, Object value)
		throws ALDBatchIOProviderException;

	/**
	 * Interface method for getting parameter values via a GUI element.
	 * 
	 * @param field				Field associated with object to be returned.
	 * @param cl 					Class of object to be returned.
	 * @param guiElement 	Component from where to read data.
	 * @return	Iterator for values read from source, might be <code>null</code>.
	 */
	public abstract Iterator<Object> readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement)
		throws ALDBatchIOProviderException;
}
