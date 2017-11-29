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

package de.unihalle.informatik.Alida.workflows.events;

import de.unihalle.informatik.Alida.operator.events.ALDEvent;

/**
 * Event type related to Alida work flows.
 * 
 * @author posch
 */
public class ALDWorkflowEvent extends ALDEvent {

	/**
	 * Types of events.
	 */
	public static enum ALDWorkflowEventType {
		/**
		 * Node added to work flow, event.info gives id of this node
		 */
		ADD_NODE,
		
		/**
		 * A copy of a node was added to the work flow, event.info gives id of the
		 * new copied node
		 */
		COPY_NODE,

		/**
		 * Edge added to work flow, event.info gives id of this edge
		 */
		ADD_EDGE,

		/**
		 * Node deleted from work flow, event.info gives id of this node
		 */
		DELETE_NODE,

		/**
		 * Edge deleted from work flow, event.info gives id of this edge
		 */
		DELETE_EDGE,

		/**
		 * source of an edge redirect in work flow, event.info gives id of this edge
		 */
		REDIRECT_EDGE_SOURCE,

		/**
		 * target of an edge redirect in work flow, event.info gives id of this edge
		 */

		REDIRECT_EDGE_TARGET,

		/**
		 * The state of one or more nodes has changed. The event.info is
		 * a Collection of ALDWorkflowNodeId
		 */
		NODE_STATE_CHANGE,

		/**
		 * The operator execution progress of one or more nodes has changed. The event.info is
		 * a Collection of ALDWorkflowNodeId
		 */
		NODE_EXECUTION_PROGRESS,

		/**
		 * The parameter of one node has changed. The event.info is
		 * Collection of ALDWorkflowNodeId
		 */
		NODE_PARAMETER_CHANGE,

		/**
		 * Save the work flow. The event.info is of type ALDWorkflowStorageInfo
		 */
		SAVE_WORKFLOW,

		/**
		 * Show the results of this node, event.info gives id of this node
		 */
		SHOW_RESULTS,

		/**
		 * The workflow has been renamed, event.info gives new name
		 */
		RENAME,

		/**
		 * Running an operator failed, event.info gives a <code>ALDRunFailureInfo</code> object
		 */
		RUN_FAILURE,

		/**
		 * User request to interrupt the current execution of (part of) the work flow
		 */
		USER_INTERRUPT, 
		
		/**
		 * The execution of an runWorkflow or runOp finished.
		 */
		EXECUTION_FINISHED
		
		 
	}

	/**
	 * Type of the event.
	 */
	protected ALDWorkflowEventType eType;

	/**
	 * ID of work flow object
	 */
	protected Object info;

	/**
	 * This is not a real time stamp rather an integer incremented each time
	 * a new event is instantiate with the only exception of using
	 * <code>new ALDWorkflowEvent( event)</code>
	 */
	private final Integer timeStamp;

	/**
	 * Count for (almost) unique time stamps (we allow wrap around).
	 */
	private static Integer nextTimeStamp = 0;
	
	/**
	 * Constructor.
	 *
	 * @param s	Source object of the event.
	 * @param e Event type.
	 */
	public ALDWorkflowEvent(Object s, ALDWorkflowEventType e) {
		this(s, e, null, null);
	}

	/** Constructor.
	 * 
	 * @param aldWorkflow Source object of the event.
	 * @param e Event type.
	 * @param info Information associated with this event.
	 */
	public ALDWorkflowEvent(Object s, ALDWorkflowEventType e, Object info) {
		this( s, e, null, info);
	}


	/** Constructor.
	 * 
	 * @param s Source object of the event.
	 * @param e Event type.
	 * @param msg  Message for the event.
	 * @param info   Id of work flow object associated with this event.
	 */
	public ALDWorkflowEvent(Object s, ALDWorkflowEventType e,String msg, Object info) {
		super(s);
		this.eType = e;
		this.eventMessage = msg;
		this.info = info;
		this.timeStamp = nextTimeStamp++;
	}

	/** Constructor.
	 * essentially clone the event
	 * 
	 * @param event 
	 */
	public ALDWorkflowEvent(ALDWorkflowEvent event) {
		super(event.source);
		this.eType = event.eType;
		this.eventMessage = event.getEventMessage();
		this.info = event.info;
		this.timeStamp = event.timeStamp;
	}


	/**
	 * Returns type of event.
	 * 
	 * @return event type
	 */
	public ALDWorkflowEventType getEventType() {
		return this.eType;
	}

	/**
	 * Get the info object of this workflow event.
	 * <p>
	 * The info object may contain additional information about the event, 
	 * e.g., in case of failures it informs about which exceptions were thrown
	 * that caused the workflow to fail.
	 * 
	 * @return Info object linked to the event with additional information.
	 */
	public Object getInfo() {
		return this.info;
	}

	/**
	 * Returns the timestamp of this event.
	 * 
	 * @return the timeStamp
	 */
	public Integer getTimeStamp() {
		return this.timeStamp;
	}


	/**
	 * Create a clone of this event with the same time stamp
	 * 
	 * @return
	 */
	public ALDWorkflowEvent createCopy() {
		return new ALDWorkflowEvent(this);
	}
	
}
