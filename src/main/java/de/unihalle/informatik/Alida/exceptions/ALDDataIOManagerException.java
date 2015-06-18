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

package de.unihalle.informatik.Alida.exceptions;


/**
 * Alida exception thrown by data IO managers. 
 * 
 * @author moeller
 */
public class ALDDataIOManagerException extends ALDDataIOException {

	/**
	 * Possible exception types.
	 */
	public static enum ALDDataIOManagerExceptionType {
		/**
		 * No provider found.
		 */
		NO_PROVIDER_FOUND,
		/**
		 * An unspecified error occurred.
		 */
		UNSPECIFIED_ERROR
	}

	/**
	 * Identifier string.
	 */
	private static final String typeID= "ALDDataIOManagerException";
	
	/**
	 * Type of exception.
	 */
	protected ALDDataIOManagerExceptionType type;
	
	/**
	 * Constructor.
	 * 
	 * @param t		Exception type.
	 * @param c		Comment string.
	 */
	public ALDDataIOManagerException(ALDDataIOManagerExceptionType t, String c) {
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
			return typeID + ": data IO manager call failed - unknown reason!";			
		}
		return new String("");
	}

	/**
	 * Returns the type of this exception.
	 */
	public ALDDataIOManagerExceptionType getType() {
		return this.type;
	}
}
