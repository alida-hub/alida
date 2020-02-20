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

package de.unihalle.informatik.Alida.dataio.provider.swing.events;

import java.util.EventListener;

import de.unihalle.informatik.Alida.dataio.provider.swing.components.ALDSwingComponent;

/**
 * Listener interface for change value events reported by provider GUI elements.
 * <p>
 * Providers for graphical user interfaces implemented in Swing are linked to 
 * an object of type {@link javax.swing.JComponent} which Alida uses to automatically 
 * generate graphical user interfaces for operators. The components are 
 * wrapped into objects of type {@link ALDSwingComponent} which reports events
 * when values within the grapical component linked to a certain provider 
 * are changed. This listener interface needs to be implemented to register
 * for these events.
 * 
 * @author moeller
 */
public interface ALDSwingValueChangeListener extends EventListener {
	
	/**
	 * Method which is called on event occurence.
	 * 
	 * @param event		Event to be handled.
	 */
	public void handleValueChangeEvent(ALDSwingValueChangeEvent event);
	
}
