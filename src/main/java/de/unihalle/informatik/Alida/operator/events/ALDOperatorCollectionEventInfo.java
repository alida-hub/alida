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

import de.unihalle.informatik.Alida.workflows.ALDWorkflowNodeID;

/**
 * This class holds information related to events in the context of {@link ALDOperatorCollection}.
 * 
 * @author Birgit Moeller
 */
public class ALDOperatorCollectionEventInfo {

	/**
	 * Corresponding workflow node's ID causing this event, maybe null.
	 */
	ALDWorkflowNodeID nid;
	
	/**
	 * Exception which caused the run failure, maybe null.
	 */
	private Exception exception;
	
	/**
	 * Default constructor.
	 * @param exception	Exception causing this event.
	 */
	public ALDOperatorCollectionEventInfo(Exception exception) {
		super();
		this.exception = exception;
	}

	/**
	 * Constructor with arguments.
	 * @param exception	Exception causing this event.
	 * @param id				ID of workflow node associated with this event.
	 */
	public ALDOperatorCollectionEventInfo(Exception exception, ALDWorkflowNodeID id) {
		super();
		this.exception = exception;
		this.nid = id;
	}

	/**
	 * Get exception, might be null.
	 * @return Exception thrown during operator execution.
	 */
	public Exception getException() {
		return this.exception;
	}
	
	/**
	 * Get workflow node ID, might be null.
	 * @return Node ID of corresponding workflow node.
	 */
	public ALDWorkflowNodeID getWorkflowNodeID() {
		return this.nid;
	}
}
