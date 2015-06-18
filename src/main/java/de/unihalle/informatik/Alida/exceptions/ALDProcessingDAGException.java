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
 * $Rev: 3376 $
 * $Date: 2011-03-11 11:21:44 +0100 (Fr, 11 Mrz 2011) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.exceptions;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;

/**
 * Exception thrown in context of history graph reconstruction.
 * 
 * @author posch
 */
public class ALDProcessingDAGException extends ALDException {

	/**
	 * Possible graph exception types.
	 * 
	 * @author moeller
	 */
	public static enum DAGExceptionType {
		/**
		 * Thrown when something went wrong during graph extraction.
		 */
		INTERNAL_TRACING_ERROR
	}

	/**
	 * Exception identifier.
	 */
	private static final String typeID= "ALDProcessingDAGException";

	/**
	 * Type of exception object.
	 */
	protected DAGExceptionType type;

	/**
	 * Constructor.
	 * 
	 * @param t		Exception type.
	 * @param c		Comment string.
	 */
	public ALDProcessingDAGException(DAGExceptionType t, String c) {
		this.type= t;
		this.comment= c;
	}

	/**
	 * Returns the type of this exception.
	 */
	public DAGExceptionType getType() {
		return this.type;
	}
	
	@Override
	public String getIdentString() {
		switch(this.type)
		{
		case INTERNAL_TRACING_ERROR:
			return typeID + ": internal tracing error!";			
		}
		return null;
	}
}
