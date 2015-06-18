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


/**
 * Abstract main class for exceptions in Alida.
 * 
 * @author posch
 */
public abstract class ALDException extends Exception {

	/**
	 * Exception comment.
	 */
	protected String comment;
	
	/**
	 * Returns string characterising exception type.
	 */
	protected String getExceptionID() {
		return "ALDException";
	}
	
	/**
	 * Returns a (hopefully unique) identification string.
	 */
	public abstract String getIdentString();
	
	/**
	 * Returns comment string.
	 */
	public final String getCommentString() {
		if (this.comment != null)
			return this.comment;
		return new String();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	@Override
	public final String getMessage() {
		return getExceptionID() + ": " + 
			getIdentString() + " " + getCommentString();
	}
}
