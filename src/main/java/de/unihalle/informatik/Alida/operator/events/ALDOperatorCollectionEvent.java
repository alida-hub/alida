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

import de.unihalle.informatik.Alida.operator.ALDOperatorCollection;
import de.unihalle.informatik.Alida.operator.ALDOperatorCollectionElement;

/**
 * Event class related to {@link ALDOperatorCollection}.
 * 
 * @author moeller
 */
public class ALDOperatorCollectionEvent extends ALDEvent {

	/**
	 * Types of events.
	 */
	public enum ALDOperatorCollectionEventType {
		/**
		 * Something went wrong during initialization.
		 */
		INIT_FAILURE,
		/**
		 * Operator is not properly configured, event.info gives UID of operator.
		 */
		OP_NOT_CONFIGURED,
		/**
		 * Results are available, event.info gives id of corresponding node.
		 */
		RESULTS_AVAILABLE,
		/**
		 * Running an operator failed, event.info gives either a 
		 * <code>ALDRunFailureInfo</code> object or exception comment.
		 */
		RUN_FAILURE,
		/**
		 * Event of unknown type.
		 */
		UNKNOWN
	}

	/**
	 * Type of the event.
	 */
	protected ALDOperatorCollectionEventType eType;

	/**
	 * Info object.
	 */
	protected ALDOperatorCollectionEventInfo info;

	/**
	 * This is not a real time stamp rather an integer incremented each time
	 * a new event is instantiate with the only exception of using
	 * <code>new {@link ALDOperatorCollectionElement}( event)</code>
	 */
	private final int timeStamp;

	/**
	 * Count for (almost) unique time stamps (we allow wrap around).
	 */
	private static int nextTimeStamp = 0;
	
	/**
	 * Constructor.
	 *
	 * @param s	Source object of the event.
	 * @param e Event type.
	 */
	public ALDOperatorCollectionEvent(Object s, 
			ALDOperatorCollectionEventType e) {
		this(s, e, null, null);
	}

	/** 
	 * Constructor.
	 * 
	 * @param s Source object of the event.
	 * @param e Event type.
	 * @param i Information associated with this event.
	 */
	public ALDOperatorCollectionEvent(Object s, 
			ALDOperatorCollectionEventType e, ALDOperatorCollectionEventInfo i) {
		this( s, e, null, i);
	}


	/** 
	 * Constructor.
	 * 
	 * @param s Source object of the event.
	 * @param e Event type.
	 * @param msg  Message for the event.
	 * @param i Information associated with this event.
	 */
	public ALDOperatorCollectionEvent(Object s, 
			ALDOperatorCollectionEventType e, String msg, ALDOperatorCollectionEventInfo i) {
		super(s);
		this.eType = e;
		this.eventMessage = msg;
		this.info = i;
		this.timeStamp = nextTimeStamp++;
	}

	/** 
	 * Copy constructor.
	 * 
	 * @param event	Event to clone. 
	 */
	public ALDOperatorCollectionEvent(ALDOperatorCollectionEvent event) {
		super(event.source);
		this.eType = event.eType;
		this.eventMessage = event.getEventMessage();
		this.info = event.info;
		this.timeStamp = event.timeStamp;
	}


	/**
	 * Returns type of event.
	 * 
	 * @return Event type.
	 */
	public ALDOperatorCollectionEventType getEventType() {
		return this.eType;
	}

	/**
	 * Get the info object of this operator collection event.
	 * <p>
	 * The info object may contain additional information about the event, 
	 * e.g., in case of failures it informs about which exceptions were thrown
	 * that caused the workflow to fail.
	 * 
	 * @return Info object linked to the event with additional information.
	 */
	public ALDOperatorCollectionEventInfo getInfo() {
		return this.info;
	}

	/**
	 * Returns the timestamp of this event.
	 * 
	 * @return the timeStamp
	 */
	public int getTimeStamp() {
		return this.timeStamp;
	}

	/**
	 * Create a clone of this event with the same time stamp
	 * 
	 * @return	New operator collection event clone.
	 */
	public ALDOperatorCollectionEvent createCopy() {
		return new ALDOperatorCollectionEvent(this);
	}
}

