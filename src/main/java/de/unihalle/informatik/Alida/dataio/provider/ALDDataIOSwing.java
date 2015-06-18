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

package de.unihalle.informatik.Alida.dataio.provider;

import java.lang.reflect.Field;

import javax.swing.*;

import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Interface for Alida's automatic data I/O via GUIs based on Swing.
 * 
 * @author posch
 */
public interface ALDDataIOSwing extends ALDDataIO {

	/**
	 * Interface method to request initial GUI values of provider.
	 * <p> 
	 * Note that while the object is allowed to be <code>null</code>
	 * the descriptor has to be different from <code>null</code> in any 
	 * case.
	 * 
	 * @param field	Field of the parameter the GUI element is linked to.
	 * @param cl 		Class of the parameter and the returned object.
	 * @param obj 	The current value of corresponding parameter.
	 * @param descr Optional descriptor to provide additional information.
	 * @return	Value that the provider will display in GUI.
	 * @throws ALDDataIOProviderException Thrown in case of failure.
	 */
	public abstract Object getInitialGUIValue(Field field, 
			Class<?> cl, Object obj, ALDParameterDescriptor descr) 
		throws ALDDataIOProviderException;

	/**
	 * Interface method to create a GUI element for data input.
	 * <p> 
	 * Optionally an object may be supplied which is assumed to be of 
	 * type <code>cl</code> and used as default. 
	 * 
	 * @param field	Field of the parameter the GUI element is linked to.
	 * @param cl 		Class of object to be returned.
	 * @param obj 	The value of this object to be set as inital value.
	 * @param descr Optional descriptor to provide additional information.
	 * @return	Alida GUI component to input values.
	 * @throws ALDDataIOProviderException Thrown in case of failure.
	 */
	public abstract ALDSwingComponent createGUIElement(Field field, 
			Class<?> cl, Object obj, ALDParameterDescriptor descr) 
		throws ALDDataIOProviderException;

	/**
	 * Interface method for setting new parameter values in GUI.
	 * 
	 * @param field				Field of parameter object to be handled.
	 * @param cl 					Class of parameter object to be handled.
	 * @param guiElement	Corresponding GUI element.
	 * @param value				New value of the parameter.
	 * @throws ALDDataIOProviderException Thrown in case of failure.
	 */
	public abstract void setValue(Field field, Class<?> cl, 
			ALDSwingComponent guiElement, Object value)
		throws ALDDataIOProviderException;

	/**
	 * Interface method for getting parameter values via a GUI element.
	 * 
	 * @param field				Field associated with object to be returned.
	 * @param cl 					Class of object to be returned.
	 * @param guiElement 	Component from where to read data.
	 * @return	Object read from source, might be <code>null</code>.
	 * @throws ALDDataIOProviderException Thrown in case of failure.
	 */
	public abstract Object readData(Field field, Class<?> cl, 
			ALDSwingComponent guiElement)
		throws ALDDataIOProviderException;
		
	/**
	 * Interface method for displaying an object in the GUI.
	 * 
	 * @param obj		Object to be displayed or saved.
	 * @param d 		Optional descriptor to hand over additional information.
	 * @return Component visualizing the object.
	 * @throws ALDDataIOProviderException Thrown in case of failure.
	 */
	public abstract JComponent writeData(Object obj, 
			ALDParameterDescriptor d)
		throws ALDDataIOProviderException;
}
