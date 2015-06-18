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

/**
 * Event type for updates of operator parameters.
 * <p>
 * Possible reasons for updates are, e.g., changes in the graphical UI or
 * a parameter reload from file.
 * 
 * @author moeller
 */
public class ALDOpParameterUpdateEvent extends ALDEvent {

	/**
	 * Possible types of events.
	 */
	public static enum EventType {
		/**
		 * Some values were changed.
		 */
		CHANGED,
		/**
		 * A complete configuration was (re-)loaded from file.
		 */
		LOADED
	}
	
	/**
	 * Type of this event.
	 */
	protected EventType type;
	
	/**
	 * Constructor with message and type.
	 *
	 * @param s		Source object of the event.
	 * @param t		Type of event.
	 */
	public ALDOpParameterUpdateEvent(Object s, EventType t) {
	  super(s);
	  this.type = t;
  }

	/**
	 * Constructor with message and type.
	 *
	 * @param s		Source object of the event.
	 * @param msg	Freely configurable message.
	 * @param t		Type of event.
	 */
	public ALDOpParameterUpdateEvent(Object s, String msg, EventType t) {
	  super(s,msg);
	  this.type = t;
  }
	
	/**
	 * Get the event's type.
	 * @return	Type of event.
	 */
	public EventType getType() {
		return this.type;
	}
}
