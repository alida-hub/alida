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

import javax.swing.event.EventListenerList;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;

/**
 * Reporter interface for change value events in Alida Swing GUI components.
 * <p>
 * This class implements an event reporter mechanism by which information about
 * value changes within a provider, i.e. its GUI element, is made available to 
 * the public in a generic fashion. This might for example be used to change 
 * the status of operators in processing graphs to indicate after changing 
 * parameter values that the current results might no longer be valid.
 * 
 * @author moeller
 */
public class ALDSwingValueChangeReporter {
	
	/**
	 * List of registered event listeners.
	 */
	protected volatile EventListenerList listenerList = 
			new EventListenerList();

	/**
	 * Adds a listener to this reporter.
	 * @param listener		Listener to be added.
	 */
	public void addValueChangeEventListener(
																			ALDSwingValueChangeListener listener) {
		this.listenerList.add(ALDSwingValueChangeListener.class, listener);
	}

	/**
	 * Removes a listener from this reporter.
	 * @param listener		Listener to be removed.
	 */
	public void removeValueChangeEventListener(
																			ALDSwingValueChangeListener listener) {
		this.listenerList.remove(ALDSwingValueChangeListener.class, listener);
	}

	/**
	 * Sends an event to all registered listeners.
	 * @param ev		Event to be send to all listeners.
	 */
	public void fireALDSwingValueChangeEvent(ALDSwingValueChangeEvent ev){
		
		// only fire events if allowed
		if (!ALDDataIOManagerSwing.getInstance().isTriggerValueChangeEvents())
			return;
		
		// get list of listeners 
		Object[] listeners = this.listenerList.getListenerList();
		
		/* listeners will always be non-null as getListenerList() is guaranteed 
		 * to return a non-null array... */

		// process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i]==ALDSwingValueChangeListener.class) {
				// lazily create the event:
				((ALDSwingValueChangeListener)listeners[i+1]).handleValueChangeEvent(ev);
			}
		}
	}
}
