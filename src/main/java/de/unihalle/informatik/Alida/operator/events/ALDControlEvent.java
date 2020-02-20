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
 * Event type for controlling operator threads.
 * 
 * @author moeller
 */
public class ALDControlEvent extends ALDEvent {

	/**
	 * Event types on which a controllable operator should react.
	 */
	public static enum ALDControlEventType {
		/**
		 * Start calculations.
		 */
		RUN_EVENT,
		/**
		 * Stop calculations as soon as possible without option for resume.
		 * <p>
		 * Note that the operator should try to conserve result data.
		 */
		STOP_EVENT,
		/**
		 * Do the next step in step-wise operator execution.
		 */
		STEP_EVENT,
		/**
		 * Pause calculations.
		 */
		PAUSE_EVENT,
		/**
		 * Continue calculations after previous pause event.
		 */
		RESUME_EVENT,
		/**
		 * Stop all calculations immediately, data is probably lost.
		 */
		KILL_EVENT,
	}
	
	/**
	 * Type of the event.
	 */
	protected ALDControlEventType eType;
	
	/**
	 * Default constructor.
	 *
	 * @param s	Source object of the event.
	 * @param e Event type.
	 */
	public ALDControlEvent(Object s, ALDControlEventType e) {
	  super(s);
	  this.eType = e;
  }
	
	/**
	 * Default constructor with arguments.
	 *
	 * @param s		Source object of the event.
	 * @param e 	Event type.
	 * @param msg	Freely configurable message.
	 */
	public ALDControlEvent(Object s, ALDControlEventType e, String msg) {
	  super(s);
	  this.eType = e;
	  this.eventMessage = msg;
  }

	/**
	 * Returns type of event.
	 */
	public ALDControlEventType getEventType() {
		return this.eType;
	}
}
