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

package de.unihalle.informatik.Alida.operator.events;

import java.util.EventListener;


/**
 * Interface for classes that generate events of type `ALDControlEvent`.
 * 
 * @author moeller
 */
public interface ALDControlEventReporter extends EventListener {
	
	/**
	 * Adds a listener to this reporter.
	 * 
	 * @param listener		Listener to be added.
	 */
	public void addALDControlEventListener(ALDControlEventListener listener);
	
	/**
	 * Removes a listener from this reporter.
	 * 
	 * @param listener		Listener to be removed.
	 */
	public void removeALDControlEventListener(ALDControlEventListener listener);
	
	/**
	 * Sends the given event to all registered listeners.
	 * 
	 * @param event		Event to be send to all listeners.
	 */
	public void fireALDControlEvent(ALDControlEvent event);
}
