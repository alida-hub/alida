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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.*;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;
import de.unihalle.informatik.Alida.operator.ALDParameterDescriptor;

/**
 * Alida-specific Swing component linked to a {@link JCheckBox}.
 * 
 * @author moeller
 */
public class ALDSwingComponentCheckBox extends ALDSwingComponent 
	implements ItemListener {
	
	/**
	 * Descriptor of associated (operator) parameter.
	 */
	protected ALDParameterDescriptor paramDescriptor;
	
	/**
	 * Associated Swing component.
	 */
	protected JCheckBox compCheckBox = null;

	/**
	 * Default constructor.
	 * @param descr		Descriptor of associated (operator) parameter.
	 */
	public ALDSwingComponentCheckBox(ALDParameterDescriptor descr) {
		this.paramDescriptor = descr;
		this.compCheckBox = new JCheckBox();
		this.compCheckBox.addItemListener(this);
	}
	
	@Override
	public JCheckBox getJComponent() {
		return this.compCheckBox;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// only fire event if allowed by I/O manager
		if (ALDDataIOManagerSwing.getInstance().isTriggerValueChangeEvents())
			this.fireALDSwingValueChangeEvent(
				new ALDSwingValueChangeEvent(this, this.paramDescriptor));
	}

	@Override
  public void disableComponent() {
		this.compCheckBox.setEnabled(false);
  }

	@Override
  public void enableComponent() {
		this.compCheckBox.setEnabled(true);
  }

	@Override
  public void dispose() {
		// nothing to do here
  }
}
