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
 * $Rev$
 * $Date$
 * $Author$
 * 
 */

package de.unihalle.informatik.Alida.grappa;

import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;

import javax.swing.*;

/**
 * Graphical component to represent Grappa links in Alida config windows.
 * <p>
 * Note, this is not a real provider. It is not registered on startup,
 * i.e. objects of this class have to instantiated explicitly outside the
 * framework of automatic provider handling.
 * 
 * @author moeller
 *
 */
public class ALDGrappaLinkDataIOSwing extends ALDSwingComponent {
	
	GrappaLinkTextField textField = null;

	public ALDGrappaLinkDataIOSwing(String value) {
		this.textField = new GrappaLinkTextField(this);
		this.textField.setEditable(false);
		if ( value != null ) {
			this.textField.setText( value);
		}
	}

	public void setValue(String val) {
		if (val == null) {
			this.textField.setText(new String());
		}
		else {
			this.textField.setText(val);
		}
	}

	public class GrappaLinkTextField extends JTextField {
		
		protected ALDGrappaLinkDataIOSwing dataIOObj;
		
		public GrappaLinkTextField(ALDGrappaLinkDataIOSwing obj) {
			this.dataIOObj = obj;
		}
		
	}
	
	@Override
  public JComponent getJComponent() {
		return this.textField;
  }

	@Override
  public void disableComponent() {
		return;
  }

	@Override
  public void enableComponent() {
		return;
  }
	
	@Override
  public void dispose() {
		return;
	}
}
