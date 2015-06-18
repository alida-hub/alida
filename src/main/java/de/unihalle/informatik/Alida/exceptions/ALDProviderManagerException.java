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
 * $Rev: 6243 $
 * $Date: 2012-11-15 22:20:39 +0100 (Do, 15 Nov 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.exceptions;


/**
 * Alida exception thrown by batch I/O managers. 
 * 
 * @author moeller
 */
abstract public class ALDProviderManagerException extends ALDException {

	/**
	 * @author posch
	 *
	 */
	public class ALDConverterManagerException {

		/**
		 * 
		 */
		public ALDConverterManagerException() {
			// TODO Auto-generated constructor stub
		}

	}

	/**
	 * Possible exception types.
	 */
	public static enum ALDProviderManagerExceptionType {
		/**
		 * No provider found.
		 */
		NO_PROVIDER_FOUND,
		/**
		 * Provider does not implement proper interface.
		 */
		PROVIDER_INTERFACE_ERROR,
		/**
		 * Class could not be instantiated.
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
	private static final String typeID= "ALDProviderManagerException";
	
	/**
	 * Type of exception.
	 */
	protected ALDProviderManagerExceptionType type;
	
	/**
	 * Constructor.
	 * 
	 * @param t		Exception type.
	 * @param c		Comment string.
	 */
	public ALDProviderManagerException(ALDProviderManagerExceptionType t, String c) {
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
		case PROVIDER_EXECUTION_ERROR:
			return typeID + ": nError on calling a provider's method!!!";
		case PROVIDER_INSTANTIATION_ERROR:
			return typeID + ": Class could not be instantiated!!!";
		case PROVIDER_INTERFACE_ERROR:
			return typeID + ": Provider does not implement proper interface!!!";
		}
		return new String("");
	}

	/**
	 * Returns the type of this exception.
	 */
	public ALDProviderManagerExceptionType getType() {
		return this.type;
	}
}
