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
 * $Rev: 5413 $
 * $Date: 2012-04-12 11:01:03 +0200 (Do, 12 Apr 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.dataio.provider.swing.components;

import javax.swing.*;


/**
 * Alida-specific Swing component linked to a {@link JLabel}.
 * 
 * @author moeller
 */
public class ALDSwingComponentLabel extends ALDSwingComponent {
	
	/**
	 * Associated Swing component.
	 */
	protected JLabel compLabel = null;

	/**
	 * Default constructor.
	 * @param labelText		Text for the label.
	 */
	public ALDSwingComponentLabel(String labelText) {
		this.compLabel = new JLabel(labelText);
	}
	
	@Override
	public JLabel getJComponent() {
		return this.compLabel;
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
		// nothing to do here
  }
}
