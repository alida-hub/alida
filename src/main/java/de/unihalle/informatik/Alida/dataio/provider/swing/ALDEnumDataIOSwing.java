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

package de.unihalle.informatik.Alida.dataio.provider.swing;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwingInitialGUIValueDefaultHandler;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentComboBox;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentComboBoxItem;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Class for generic loading/saving of enumerations via GUI in Alida.
 * 
 * @author posch
 * @author moeller
 */
@ALDDataIOProvider
public class ALDEnumDataIOSwing 
	extends ALDDataIOSwingInitialGUIValueDefaultHandler {
	
	/**
	 * Default item if no value is selected.
	 */
	private static ALDSwingComponentComboBoxItem boxItemNone = 
			new ALDSwingComponentComboBoxItem(null, "none", "none");

	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add( Enum.class);
		return classes;
	}
	
	/** 
	 * Generate GUI element for generic reading of enumeration types.
	 */
	@Override
	public ALDSwingComponent createGUIElement(Field field, Class<?> cl, 
			Object obj, ALDParameterDescriptor descr) {

		Vector<ALDSwingComponentComboBoxItem> fields = 
				new Vector<ALDSwingComponentComboBoxItem>();

		Object[]    consts = cl.getEnumConstants();
		// add the default item for no selection
		fields.add(boxItemNone);
		for ( Object c : consts ) {
			fields.add(
					new ALDSwingComponentComboBoxItem(c, c.toString(), null));
		}

		ALDSwingComponentComboBox combo = 
			new ALDSwingComponentComboBox(descr, fields);

		// set default; if none provided, select first entry "none"
		if ( obj != null )
			combo.setSelectedItem(obj);
		else
			combo.getJComponent().setSelectedIndex(0);

		return combo;
	}

	@Override
  public void setValue(Field field, Class<?> cl, 
  		ALDSwingComponent guiElement, Object value) 
  	throws ALDDataIOProviderException {
		if (!(guiElement instanceof ALDSwingComponentComboBox))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"ALDEnumDataIOSwing: readData received invalid GUI element!");
		((ALDSwingComponentComboBox)guiElement).setSelectedItem(value);
  }

	@Override
	public Object readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement)
					throws ALDDataIOProviderException {
		if (!(guiElement instanceof ALDSwingComponentComboBox))
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
					"ALDEnumDataIOSwing: readData received invalid GUI element!");
		return ((ALDSwingComponentComboBoxItem)
				((ALDSwingComponentComboBox)guiElement).getJComponent().
				getSelectedItem()).getObject();
	}

	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) {
		JTextField textfield = new JTextField(25);
		textfield.setText( obj.toString());
		textfield.setEditable( false);
		return textfield;
	}
}
