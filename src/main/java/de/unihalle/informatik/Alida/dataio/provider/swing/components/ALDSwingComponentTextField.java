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

package de.unihalle.informatik.Alida.dataio.provider.swing.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.*;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing.ProviderInteractionLevel;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Alida-specific Swing component linked to a {@link JTextField}.
 * <p>
 * This component reports events on changes of the text entry. In addition it
 * features a value checking. It verifies if the current text entry can be
 * converted to an object of the class initially specified on contruction of 
 * the object. If not, a warning message is displayed.
 * <p>
 * Important note for programmers using this component: <br>
 * this checking mechanism does only properly work if the text in the 
 * component is never changed directly. Changes should exclusively be done by 
 * using the setText(String t) method of this class.
 * 
 * @author moeller
 */
public class ALDSwingComponentTextField extends ALDSwingComponent
	implements ActionListener, FocusListener {
	
	/**
	 * Associated Swing component.
	 */
	protected JTextField compTextField = null;
	
	/**
	 * Current value of the text field.
	 */
	protected String value = null;
	
	/**
	 * Class of objects to be read through this text field.
	 */
	protected Class<?>	objCl;

	/**
	 * Descriptor of associated (operator) parameter.
	 */
	protected ALDParameterDescriptor paramDescriptor;
	
	/**
	 * Default constructor.
	 * <p>
	 * The class parameter allows to specify a class which is used to check the
	 * validity of text field entries. If the class parameter is null, validation
	 * is disabled.
	 * 
	 * @param cl	Class of objects to be read via text field.
	 * @param d		Descriptor of corresponding parameter.
	 * @param columns		Width of text field.
	 */
	public ALDSwingComponentTextField(Class<?> cl, ALDParameterDescriptor d,
			int columns) {
		this.compTextField = new JTextField(columns);
		this.compTextField.addActionListener(this);
		this.compTextField.addFocusListener(this);
		this.paramDescriptor = d;
		this.objCl = cl;
	}
	
	@Override
	public JTextField getJComponent() {
		return this.compTextField;
	}

	/**
	 * Set text of associated text field.
	 * @param t		New text to be displayed.
	 */
	public void setText(String t) {
		this.checkValue(t);
	}
	
	/**
	 * Returns text of text field.
	 * @return	Current text in field.
	 */
	public String getText() {
		return this.compTextField.getText();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		this.checkValue(null);
	}

	@Override
	public void focusGained(FocusEvent e) {
		// this event is not interesting for us, just ignore it...
	}

	@Override
	public void focusLost(FocusEvent e) {
		this.checkValue(null);
	}
	
	/**
	 * Method that handles events triggered by the text field.
	 * <p>
	 * The method at first checks if the value of the text field has changed.
	 * If so, it subsequently validates if the new value is valid with regard 
	 * to the class linked to this GUI element. If both checks are passed, the
	 * text field is updated if text is provided from somewhere else, and
	 * a {@link ALDSwingValueChangeEvent} is triggered. If the new value is 
	 * invalid, a warning is displayed to the user.
	 * 
	 * @param textToCheck	New text to be set in text field.
	 * @return	True if text value is valid.
	 */
	protected boolean checkValue(String textToCheck) {
		// only do the checks if providers are allowed to show warnings
		ProviderInteractionLevel plevel = 
				ALDDataIOManagerSwing.getInstance().getProviderInteractionLevel();
		if (   !plevel.equals(ProviderInteractionLevel.ALL_ALLOWED)
				&& !plevel.equals(ProviderInteractionLevel.WARNINGS_ONLY)) 
			return true;
		
		if (this.objCl == null) {
			// no class specified, nothing happens
			return true;
		}
		
		String newText = textToCheck;
		if (textToCheck == null)
			newText = this.compTextField.getText();
		
		if (this.value != null && this.value.equals(newText)) {
			// text did not change or was not set before, nothing happens...
			return true;
		}
		// if new text is empty this is only allowed if class is String
		if (newText.isEmpty() && this.objCl.equals(String.class)) {
			if (   this.value != null && !this.value.isEmpty()
					|| this.value == null) {
				this.value = newText;
				if (textToCheck != null)
					this.compTextField.setText(newText);
				this.fireALDSwingValueChangeEvent(
					new ALDSwingValueChangeEvent(this,this.paramDescriptor));
			}
			return true;
		}
		try {
			// native types, do pseudo-casts to check for
			if (this.objCl.equals(boolean.class))
				Boolean.valueOf(newText);
			else if (this.objCl.equals(byte.class))
				Byte.valueOf(newText);
			else if (this.objCl.equals(double.class))
				Double.valueOf(newText);
			else if (this.objCl.equals(float.class))
				Float.valueOf(newText);
			else if (this.objCl.equals(int.class))
				Integer.valueOf(newText);
			else if (this.objCl.equals(long.class))
				Long.valueOf(newText);
			else if (this.objCl.equals(short.class))
				Short.valueOf(newText);
			// wrapper
			else if (this.objCl.equals(Boolean.class)) 
				Boolean.valueOf(newText);
			else if (this.objCl.equals(Byte.class))
				Byte.valueOf(newText);
			else if (this.objCl.equals(Double.class))
				Double.valueOf(newText);
			else if (this.objCl.equals(Float.class))
				Float.valueOf(newText);
			else if (this.objCl.equals(Integer.class))
				Integer.valueOf(newText);
			else if (this.objCl.equals(Long.class))
				Long.valueOf(newText);
			else if (this.objCl.equals(Short.class))
				Short.valueOf(newText);
			// else if (this.objCl.equals(MTBDoubleData.class)) {
			// 	Double.valueOf(newText);
			// }
			// additional classes
			else if (this.objCl.equals(String.class)) {
				// nothing happens, new text is of desired type
			}
			else
				System.err.println("[ALDSwingComponentTextField] Unknown class...");
		}
		catch (Exception e) {
			// text entry is invalid...
			Object[] options = { "OK" };
			JOptionPane.showOptionDialog(null, 
					"Attention! You just entered an invalid value...\n" +
							"String \"" + newText + "\" cannot be cast to " + 
							this.objCl.getSimpleName() + "!",
							"Warning", JOptionPane.DEFAULT_OPTION,
							JOptionPane.WARNING_MESSAGE,
							null, options, options[0]);
			// reset text without validation
			this.compTextField.setText(this.value);
			return false;
		}
		this.value = newText;
		if (textToCheck != null)
			this.compTextField.setText(newText);
		// only fire event if allowed by I/O manager
		if (ALDDataIOManagerSwing.getInstance().isTriggerValueChangeEvents())
			this.fireALDSwingValueChangeEvent(
				new ALDSwingValueChangeEvent(this, this.paramDescriptor));
		return true;
	}

	@Override
  public void disableComponent() {
		this.compTextField.setEditable(false);
  }

	@Override
  public void enableComponent() {
		this.compTextField.setEditable(true);
  }
	
	@Override
  public void dispose() {
		// nothing to do here
  }
}
