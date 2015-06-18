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
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentCheckBox;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentLabel;
import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponentTextField;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDParser;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

import javax.swing.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Class for loading/saving primitive datatypes in Alida.
 * 
 * @author moeller
 *
 */
@ALDDataIOProvider
public class ALDNativeDataIOSwing implements ALDDataIOSwing {

	/**
	 * Interface method to announce class for which IO is provided for.
	 * 
	 * @return  Collection of classes provided.
	 */
	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add( String.class);
		classes.add( boolean.class);
		classes.add( byte.class);
		classes.add( double.class);
		classes.add( float.class);
		classes.add( int.class);
		classes.add( long.class);
		classes.add( short.class);
		classes.add( Boolean.class);
		classes.add( Byte.class);
		classes.add( Double.class);
		classes.add( Float.class);
		classes.add( Integer.class);
		classes.add( Long.class);
		classes.add( Short.class);
		return classes;
	}
	
	@Override
  public Object getInitialGUIValue(Field field, 
			Class<?> cl, Object obj, ALDParameterDescriptor descr) 
					throws ALDDataIOProviderException {
		// info strings are just shown in a label
		if (cl.equals(String.class) && descr != null && descr.isInfo()) {
			if (obj == null)
				return new ALDSwingComponentLabel("");
			return this.reformatInfoString(obj.toString());
		}
		// all data types except booleans
		if ( cl != boolean.class && cl != Boolean.class ) {
			if ( obj != null ) 
				return obj;
			return this.getDummyInitializer(cl);
		}
		// handling of booleans, if no value is provided, checkbox will
		// initially be unselected
		if ( obj != null )
			return obj;
		return new Boolean(false);
	}

	@Override
	public ALDSwingComponent createGUIElement(Field field, Class<?> cl, 
			Object obj, ALDParameterDescriptor descr)
		throws ALDDataIOProviderException {
		// info strings are just shown in a label
		if (cl.equals(String.class) && descr != null && descr.isInfo()) {
			if (obj == null)
				return new ALDSwingComponentLabel("");
			return new ALDSwingComponentLabel(
					this.reformatInfoString(obj.toString()));
		}
		// all data types except booleans
		if ( cl != boolean.class && cl != Boolean.class ) {
			ALDSwingComponentTextField textfield= 
					new ALDSwingComponentTextField(cl, descr, 25);
			if ( obj != null ) {
				if ( supported1DArray( cl) ) {
					textfield.setText( ALDParser.arrayToString( obj));
				} else {
					textfield.setText( obj.toString());
				}
			}
			else {
				// fill textfield with default value
				Object dummyObject = this.getDummyInitializer(cl);
				textfield.setText(dummyObject.toString());
			}
			return textfield;
		}
		// handling of booleans
		ALDSwingComponentCheckBox checkbox = 
				new ALDSwingComponentCheckBox(descr);
		if ( obj != null )
			checkbox.getJComponent().setSelected(
					((Boolean)obj).booleanValue());
		return checkbox;
	}

	/**
	 * Method to instantiate dummy object of requested class for default.
	 * @param cl Class of parameter object.
	 * @return	Returns object of requested class set to default value.
	 * @throws ALDDataIOProviderException Thrown if class is not supported.
	 */
	private Object getDummyInitializer(Class<?> cl) 
		throws ALDDataIOProviderException {
		
		if (cl.equals(String.class))
			return new String();
		if (cl.equals(byte.class) || cl.equals(Byte.class)) {
			return new Byte((byte)0);
		}
		if (cl.equals(double.class) || cl.equals(Double.class)) {
			return new Double(0);
		}
		if (cl.equals(float.class) || cl.equals(Float.class)) {
			return new Float(0);
		}
		if (cl.equals(int.class) || cl.equals(Integer.class)) {
			return new Integer(0);
		}
		if (cl.equals(long.class) || cl.equals(Long.class)) {
			return new Long(0);
		}
		if (cl.equals(short.class) || cl.equals(Short.class)) {
			return new Short((short)0);
		}
		throw new ALDDataIOProviderException(
				ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
				"[ALDNativeDataIOSwing::getDummyInitializer] got request for " + 
				"unsupported class <" + cl.getName() + ">...");
	}
	
	@Override
	public void setValue(Field field, Class<?> cl, 
			ALDSwingComponent guiElement,	Object value) 
		throws ALDDataIOProviderException {
		
		// info strings
		if (guiElement instanceof ALDSwingComponentLabel) {
			String newString = this.reformatInfoString(value.toString());
			((ALDSwingComponentLabel)guiElement).getJComponent().setText(
					newString);
			return;
		}
			
		// all other classes except boolean
		if ( cl != boolean.class && cl != Boolean.class ) {
			if (!(guiElement instanceof ALDSwingComponentTextField))
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
						"NativeDataIO: readData received invalid GUI element!");

			// 2D-arrays
			if (cl.getName().startsWith("[[")) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR, 
						"ALDNativeDataIO: 2D array reading not available!");
			}
			if (value == null) {
				((ALDSwingComponentTextField)guiElement).setText(
						this.getDummyInitializer(cl).toString());
      }
      else {
      	((ALDSwingComponentTextField)guiElement).setText(
      			new String(value.toString()));
      }
		}
		else {
			// handle booleans
			if (value == null) {
				// set to default
				((ALDSwingComponentCheckBox)guiElement).getJComponent().
				setSelected(false);
			}
			else
				((ALDSwingComponentCheckBox)guiElement).getJComponent().
				setSelected(((Boolean)value).booleanValue());
		}
	}

	@Override
	public Object readData(
			Field field, Class<?> cl, ALDSwingComponent guiElement) 
		throws ALDDataIOProviderException {

		// ignore info strings
		if (guiElement instanceof ALDSwingComponentLabel) {
			return 
					((ALDSwingComponentLabel)guiElement).getJComponent().getText();
		}

		if ( cl != boolean.class && cl != Boolean.class ) {

			if (!(guiElement instanceof ALDSwingComponentTextField))
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
						"NativeDataIO: readData received invalid GUI element!");

			String text = 
					((ALDSwingComponentTextField)guiElement).getText();
			// if there are no contents in the text field, we have a problem...
			if (text.isEmpty() && !(cl.equals(String.class)))
				return null;

			if (cl.equals(String.class)) 
				return text;

			try {
				// native types
				if (cl.equals(boolean.class))
					return Boolean.valueOf(text);
				if (cl.equals(byte.class))
					return Byte.valueOf(text);
				if (cl.equals(double.class))
					return Double.valueOf(text);
				if (cl.equals(float.class))
					return Float.valueOf(text);
				if (cl.equals(int.class))
					return Integer.valueOf(text);
				if (cl.equals(long.class))
					return Long.valueOf(text);
				if (cl.equals(short.class))
					return Short.valueOf(text);
				// wrapper
				if (cl.equals(Boolean.class)) 
					return Boolean.valueOf(text);
				if (cl.equals(Byte.class))
					return Byte.valueOf(text);
				if (cl.equals(Double.class))
					return Double.valueOf(text);
				if (cl.equals(Float.class))
					return Float.valueOf(text);
				if (cl.equals(Integer.class))
					return Integer.valueOf(text);
				if (cl.equals(Long.class))
					return Long.valueOf(text);
				if (cl.equals(Short.class))
					return Short.valueOf(text);
				// 2D-arrays
				if (cl.getName().startsWith("[[")) {
					throw new ALDDataIOProviderException(
							ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR, 
							"ALDNativeDataIO: 2D array reading not available!");
				}
				//    			// 1D-arrays
				//    			else if ( supported1DArray( cl) ) {
				//    				return ALDParser.readArray1D(cl, text);
				//    			}

			} catch (Exception e) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR, 
						"ALDNativeDataIO: something went wrong in parsing..." +
						e.getMessage());
			}
		} else {
			if (!(guiElement instanceof ALDSwingComponentCheckBox))
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.INVALID_GUI_ELEMENT, 
						"NativeDataIO: readData received invalid GUI element!");
			return 
					new Boolean(((ALDSwingComponentCheckBox)guiElement).
							getJComponent().isSelected());
		}
		// we should never end up here
		throw new ALDDataIOProviderException(
				ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
				"ALDNativeDataIOSwing - something went wrong, " 
				+ "I don't know what...");
	}

	@Override
	public JComponent writeData(Object obj, ALDParameterDescriptor descr) 
			throws ALDDataIOProviderException {
		if (obj == null)
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR,
					"[ALDNativeDataIOSwing::writeData()] received null object!");
		
		// info strings
		if (obj.getClass() == String.class && descr.isInfo()) {
			return new JLabel(this.reformatInfoString(obj.toString()));
		}
		
		if (   obj.getClass() != boolean.class 
				&& obj.getClass() != Boolean.class ) {
			JTextField textfield = new JTextField(25);

			if ( supported1DArray( obj.getClass()) ) 
				textfield.setText( ALDParser.arrayToString( obj));
			else
				textfield.setText( obj.toString());

			textfield.setEditable( false);
			return textfield;
		}
		JCheckBox checkbox = new JCheckBox();
		checkbox.setSelected( ((Boolean)obj).booleanValue() );
		checkbox.setEnabled( false);
		return checkbox;

	}

	/**
	 * Checks if a given class is supported or not.
	 * 
	 * @param cl	Class to check.
	 * @return	True if class is supported.
	 */
	private boolean supported1DArray( Class<?> cl ) {
		if (   cl == Boolean[].class 
				|| cl == Byte[].class 
				|| cl == Double[].class 
				|| cl == Float[].class 
				|| cl == Integer[].class 
				|| cl == Short[].class 
				|| cl == String[].class 
				|| cl == boolean[].class 
				|| cl == byte[].class 
				|| cl == double[].class 
				|| cl == float[].class 
				|| cl == int[].class 
				|| cl == short[].class)
			return true;
		return false;
	}
	
	/**
	 * Reformats an info message to multiple lines if it contains newlines.
	 * @param infoMsg		Message to reformat.
	 * @return	Reformatted string.
	 */
	private String reformatInfoString(String infoMsg) {
		String newInfoMsg = infoMsg;
		if (infoMsg.contains("\n")) {
			newInfoMsg = "<html>" + infoMsg.replace("\n", "<br>") + "</html>";
		}
		return newInfoMsg;
	}
}
