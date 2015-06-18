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

import de.unihalle.informatik.Alida.dataio.provider.swing.events.*;

/**
 * Class defining Alida-specific Swing GUI components.
 * <p>
 * Objects of this type basically wrap objects of type {@link JComponent} which
 * are used for automatically generating complex graphical user interfaces for 
 * Alida operators. In addition to {@link JComponent}, however, the class 
 * implements an event reporter mechanism by which information about value 
 * changes within a provider is made available to the public.
 * 
 * @author moeller
 */
public abstract class ALDSwingComponent 
	extends ALDSwingValueChangeReporter {
	
	/**
	 * Method to request the provider's GUI element.
	 * @return	Component to be integrated in a graphical user interface.
	 */
	public abstract JComponent getJComponent();
	
	/**
	 * Method to disable the component, i.e. all graphical elements, to 
	 * prohibit parameter changes.
	 */
	public abstract void disableComponent();

	/**
	 * Method to enable the component with all graphical elements again.
	 */
	public abstract void enableComponent();
	
	/**
	 * Releases all resources, i.e. closes all sub-windows.
	 */
	public abstract void dispose();
}
