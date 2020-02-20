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
 * Alida exception thrown by batch data IO providers. 
 * 
 * @author moeller
 */
public class ALDBatchIOProviderException extends ALDBatchIOException {

	/**
	 * Possible exception types.
	 */
	public static enum ALDBatchIOProviderExceptionType {
		/**
		 * Syntax error.
		 */
		SYNTAX_ERROR,
		/**
		 * Syntax error.
		 */
		OBJECT_TYPE_ERROR,
		/**
		 * Object instantiation problem.
		 */
		OBJECT_INSTANTIATION_ERROR,
		/**
		 * File IO error.
		 */
		FILE_IO_ERROR,
		/**
		 * GUI element is invalid. 
		 */
		INVALID_GUI_ELEMENT,
		/**
		 * Data value could not be set in provider.
		 */
		SET_VALUE_FAILED,
		/**
		 * An unspecified error.
		 */
		UNSPECIFIED_ERROR
	}

	/**
	 * Identifier string.
	 */
	private static final String typeID= "ALDBatchIOProviderException";
	
	/**
	 * Type of exception.
	 */
	protected ALDBatchIOProviderExceptionType type;
	
	/**
	 * Constructor.
	 * 
	 * @param t		Exception type.
	 * @param c		Comment string.
	 */
	public ALDBatchIOProviderException(ALDBatchIOProviderExceptionType t, 
																			String c) {
		this.type= t;
		this.comment= c;
	}

	@Override
	public String getIdentString() {
		switch(this.type)
		{
		case SYNTAX_ERROR:
			return typeID + ": syntax problem in input data!";
		case FILE_IO_ERROR:
			return typeID + ": something went wrong in file access!";
		case SET_VALUE_FAILED:
			return typeID + ": value could not be set!";
		case UNSPECIFIED_ERROR:
			return typeID + ": data IO provider call failed - unknown reason!";			
		}
		return new String("");
	}

	/**
	 * Returns the type of this exception.
	 */
	public ALDBatchIOProviderExceptionType getType() {
		return this.type;
	}
}
