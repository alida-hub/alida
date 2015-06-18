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

package de.unihalle.informatik.Alida.exceptions;

/**
 * Exception mainly thrown by 
 * {@link de.unihalle.informatik.Alida.workflows.ALDWorkflow} 
 * in case of failures. 
 * 
 * @author posch
 */
public class ALDWorkflowException extends ALDException {

	/**
	 * Possible work flow exception types.
	 * 
	 * @author posch
	 */
	/**
	 * @author posch
	 *
	 */
	public static enum WorkflowExceptionType {

		/**
		 * Operator object cannot be instantiated.
		 */
		INSTANTIATION_ERROR,
		
		/**
		 * node does not exist.
		 */
		NODE_DOESNOT_EXIST,
		
		/**
		 * edge does not exist.
		 */
		EDGE_DOESNOT_EXIST,
		
		/**
		 * error with parameter.
		 */
		PARAMETER_ERROR,
		
		/**
		 * multiple incident edges for an input parameter.
		 */
		MULTIPLE_INCIDENT_LINKS,
		
		/**
		 * duplicate edge
		 */
		DUPLICATE_EDGE,
		
		/**
		 * trying to create an edge failed 
		 */
		EDGE_CREATE_FAILED,
		
		/**
		 * node id out of range
		 */
		NODEID_OUTOFRANGE,
		
		/**
		 * node id out of range
		 */
		EDGEID_OUTOFRANGE,
		
		/**
		 * source parameter has not direction OUT or INOUT
		 */
		WRONG_SOURCE_PARAMETER_DIRECTION,
		
		/**
		 * target parameter has not direction INPUT or INOUT
		 */
		WRONG_TARGET_PARAMETER_DIRECTION,
		
		/**
		 * source and target parameter are of incompatible type
		 */
		INCOMPATIBLE_TYPES, 
		
		/**
		 * source and target parameter are of incompatible type but
		 * may be converted by a data converter
		 */
		INCOMPATIBLE_TYPES_BUT_CONVERTIBLE, 
		
		/**
		 * work flow gets cyclic
		 */
		CYCLIC,
		/**
		 * Save of work flow failed.
		 */
		SAVE_FAILED,
		
		/**
		 * Load of work flow failed.
		 */
		LOAD_FAILED,
		
		/**
		 * fatal internal error unknown cause
		 */
		FATAL_INTERNAL_ERROR,
		
		/**
		 * execution of (part of) the work flow failed
		 */
		RUN_FAILED,
		
		/**
		 * illegal graph structure 
		 */
		ILLEGAL_GRAPH_STRUCTURE,
		
		/**
		 * invalid class of operator 
		 */
		INVALID_OPERATOR
	}

	/**
	 * Identifier string.
	 */
	private static final String typeID= "ALDWorkflowException";
	
	/**
	 * Type of exception object.
	 */
	protected WorkflowExceptionType type;
	
	/**
	 * Constructor.
	 * 
	 * @param t		Exception type.
	 * @param c		Comment string.
	 */
	public ALDWorkflowException(WorkflowExceptionType t, String c) {
		this.type= t;
		this.comment= c;
	}

	/**
	 * Returns the type of this exception.
	 */
	public WorkflowExceptionType getType() {
		return this.type;
	}
	
	@Override
	public String getIdentString() {
		switch(this.type)
		{
		case INSTANTIATION_ERROR:
			return typeID + ": Operator cannot be instantiated!\n ";
		case NODE_DOESNOT_EXIST:
			return typeID + ": Node does not exist!\n ";		
		case EDGE_DOESNOT_EXIST:
			return typeID + ": Edge does not exist!\n ";
		case PARAMETER_ERROR:
			return typeID + ": Error with parameter!\n ";
		case MULTIPLE_INCIDENT_LINKS:
			return typeID + ": Multiple incident links for input parameter!\n ";
		case DUPLICATE_EDGE:
			return typeID + ": Duplicate edge!\n ";
		case EDGE_CREATE_FAILED:
			return typeID + ": trying to create an edge failed!\n ";			
		case NODEID_OUTOFRANGE:
			return typeID + ": Node id out of range!\n";
		case EDGEID_OUTOFRANGE:
			return typeID + ": Edge id out of range!\n";		
		case WRONG_SOURCE_PARAMETER_DIRECTION:
			return typeID + ": Source parameter has not direction OUT or INOUT!\n";
		case WRONG_TARGET_PARAMETER_DIRECTION:
			return typeID + ": Target parameter has not direction IN or INOUT!\n";
		case INCOMPATIBLE_TYPES:
			return typeID + ": Source and target parameter are of incompatible type!\n";
		case INCOMPATIBLE_TYPES_BUT_CONVERTIBLE:
			return typeID + ": Source and target parameter are not assignable but may be converted by a data converter!\n";
		case CYCLIC:
			return typeID + ": Work flow gets cyclic!\n";
		case SAVE_FAILED:
			return typeID + ": Save of work flow failed!\n ";
		case LOAD_FAILED:
			return typeID + ": Load of work flow failed!\n ";
		case FATAL_INTERNAL_ERROR:
			return typeID + ": Fatal internal error\n ";
		case RUN_FAILED:
			return typeID + ": Execution of (part of) the work flow failed!\n ";
		case ILLEGAL_GRAPH_STRUCTURE:
			return typeID + ": Illegal graph structure!\n ";
		case INVALID_OPERATOR:
			return typeID + ": Invalid operator class!\n ";
		}
		return null;
	}
	
	/**
	 * Returns the ident string in a user-friendly format without type info.
	 * @return		Condensed ident string.
	 */
	public String getIdentStringWithoutType() {
		return (this.getIdentString().split(":"))[1];
	}
}
