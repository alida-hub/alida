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
 * {@link de.unihalle.informatik.Alida.operator.ALDOperator} 
 * in case of failures. 
 * 
 * @author posch
 */
public class ALDOperatorException extends ALDException {

	/**
	 * Possible operator exception types.
	 * 
	 * @author moeller
	 */
	public static enum OperatorExceptionType {

		/**
		 * Operator parameters could not be validated (e.g. wrong type or range).
		 */
		VALIDATION_FAILED,

		/**
		 * Call of runOp() failed somehow.
		 */
		OPERATE_FAILED,

		/**
		 * Parameter name is invalid.
		 */
		INVALID_PARAMETERNAME,

		/**
		 * call back function not executable.
		 */
		CALLBACK_ERROR,

		/**
		 * Wrong class of object.
		 */
		INVALID_CLASS,

		/**
		 * Function requires an object of type 
		 * {@link de.unihalle.informatik.Alida.operator.ALDData}.
		 */
		ALD_DATA_REQUIRED,

		/**
		 * File extension does not match expected one.
		 */
		ILLEGAL_EXTENSION,
		
		/**
		 * Operator object cannot be instantiated.
		 */
		INSTANTIATION_ERROR,
		
		/**
		 * error handling parameters, e.g. setting of value failed
		 */
		PARAMETER_ERROR,
		
		/**
		 * An unspecified error.
		 */
		UNSPECIFIED_ERROR
	}

	/**
	 * Identifier string.
	 */
	private static final String typeID= "ALDOperatorException";
	
	/**
	 * Type of exception object.
	 */
	protected OperatorExceptionType type;
	
	/**
	 * Constructor.
	 * 
	 * @param t		Exception type.
	 * @param c		Comment string.
	 */
	public ALDOperatorException(OperatorExceptionType t, String c) {
		this.type= t;
		this.comment= c;
	}

	/**
	 * Returns the type of this exception.
	 * @return Type of exception.
	 */
	public OperatorExceptionType getType() {
		return this.type;
	}
	
	@Override
	public String getIdentString() {
		switch(this.type)
		{
		case VALIDATION_FAILED:
			return typeID + ": argument validation failed!\n "
				+ "--> mandatory arguments are missing or wrong parameter range!!!\n";
		case OPERATE_FAILED:
			return typeID + ": operate()-call failed!";
		case INVALID_PARAMETERNAME:
			return typeID + ": invalid parameter name: ";
		case CALLBACK_ERROR:
			return typeID + ": error invoking callback function of a parameter";
		case INVALID_CLASS:
			return typeID + ": invalid class for input or output argument";
		case ALD_DATA_REQUIRED:
			return typeID + ": class derived from ALDData required";
		case ILLEGAL_EXTENSION:
			return typeID + ": illegal extension in filename: ";
		case PARAMETER_ERROR:
			return typeID + ": parameter handling error: ";
		case INSTANTIATION_ERROR:
			return typeID + ": operator object cannot be instantiated ";
		case UNSPECIFIED_ERROR:
			return typeID + ": operator run failed - unknown reason!";			
		}
		return null;
	}
}
