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
 * Exception thrown in batch mode data IO contexts. 
 * 
 * @author moeller
 */
public abstract class ALDBatchIOException extends ALDException {

	/**
	 * Identifier string.
	 */
	private static final String typeID= "ALDBatchIOException";
	
	/**
	 * Returns string characterising exception type.
	 */
	@Override
  protected String getExceptionID() {
		return typeID;
	}
}
