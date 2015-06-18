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

package de.unihalle.informatik.Alida.workflows.events;

import de.unihalle.informatik.Alida.operator.events.ALDEvent;

/**
 * Event type related to Alida work flows.
 * 
 * @author posch
 */
public class ALDWorkflowClassEvent extends ALDEvent {

	/**
	 * Types of events.
	 */
	public static enum ALDWorkflowClassEventType {
		/**		
		/**
		 * Load the work flow. The event.info is of type ALDWorkflowStorageInfo
		 */
		LOAD_WORKFLOW,
	}
	
	/**
	 * Type of the event.
	 */
	protected ALDWorkflowClassEventType eType;
	
	/**
	 * ID of work flow object
	 */
	protected Object info;
	
	/**
	 * Constructor.
	 *
	 * @param s	Source object of the event.
	 * @param e Event type.
	 */
	public ALDWorkflowClassEvent(Object s, ALDWorkflowClassEventType e) {
		this(s, e, null, null);
	}
	
	/** Constructor.
	 * 
	 * @param aldWorkflow Source object of the event.
	 * @param e Event type.
	 * @param info Information associated with this event.
	 */
	public ALDWorkflowClassEvent(Object s, ALDWorkflowClassEventType e, Object info) {
		this( s, e, null, info);
	}
	

	/** Constructor.
	 * 
	 * @param s Source object of the event.
	 * @param e Event type.
	 * @param msg  Message for the event.
	 * @param info   Id of work flow object associated with this event.
	 */
	public ALDWorkflowClassEvent(Object s, ALDWorkflowClassEventType e,String msg, Object info) {
	  super(s);
	  this.eType = e;
	  this.eventMessage = msg;
	  this.info = info;
  }

	/**
	 * Returns type of event.
	 * 
	 * @return event type
	 */
	public ALDWorkflowClassEventType getEventType() {
		return this.eType;
	}
	
	/**
	 * Returns the index of the even
	 * 
	 * @return the index
	 */
	public Object getId() {
		return info;
	}
}
