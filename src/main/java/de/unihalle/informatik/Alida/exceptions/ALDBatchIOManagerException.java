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
 * Alida exception thrown by batch I/O managers. 
 * 
 * @author moeller
 */
public class ALDBatchIOManagerException extends ALDBatchIOException {

	/**
	 * Possible exception types.
	 */
	public static enum ALDBatchIOManagerExceptionType {
		/**
		 * No provider found.
		 */
		NO_PROVIDER_FOUND,
		/**
		 * Provider does not implement proper interface.
		 */
		PROVIDER_INTERFACE_ERROR,
		/**
		 * Class could not be found.
		 */
		PROVIDER_INSTANTIATION_ERROR,
		/**
		 * Error on calling a provider's method.
		 */
		PROVIDER_EXECUTION_ERROR,
		/**
		 * An unspecified error occurred.
		 */
		UNSPECIFIED_ERROR
	}

	/**
	 * Identifier string.
	 */
	private static final String typeID= "ALDBatchIOManagerException";
	
	/**
	 * Type of exception.
	 */
	protected ALDBatchIOManagerExceptionType type;
	
	/**
	 * Constructor.
	 * 
	 * @param t		Exception type.
	 * @param c		Comment string.
	 */
	public ALDBatchIOManagerException(ALDBatchIOManagerExceptionType t, 
																			String c) {
		this.type= t;
		this.comment= c;
	}
	
	@Override
	public String getIdentString() {
		switch(this.type)
		{
		case NO_PROVIDER_FOUND:
			return typeID + ": no provider found!!!";
		case UNSPECIFIED_ERROR:
			return typeID + ": batch IO manager call failed - unknown reason!";			
		}
		return new String("");
	}

	/**
	 * Returns the type of this exception.
	 */
	public ALDBatchIOManagerExceptionType getType() {
		return this.type;
	}
}
