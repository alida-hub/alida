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

package de.unihalle.informatik.Alida.operator;


/** This is the abstract super class of all ports which 
 *  act as  proxies to receive or supply objects within a processing graph.
 */

abstract public class ALDPort {

	/** Typename of this port. Used only for printing.
	 */
	public final String typeName;

	/** The origin/producer of this port.
	 *  This is used to construct the processing graph.
	 */
	private ALDPort origin;

	/** Construct a port object 
	 *
	 * @param typeName	type of the port as a String
	 */
	public ALDPort(String typeName) {
		this.typeName = new String( typeName);
	}

	/** Get the origin of this port, which is the parent
	 *  in the processing graph.
	 *
	 * @return	origin of this port, i.e. parent in processing graph.
	 */
	public ALDPort getOrigin() {
		return origin;
	}

	/** Set the origin of this port, which is the parent
	 *  in the processing graph.
	 *
	 * @param	p origin of this port, i.e. parent in processing 
	 */
	void setOrigin( ALDPort p) {
		origin = p;
	}

	/** Print some information on System.out
	 */
	public void print() {
		print( "");
	}

	/** Print some information on System.out
	 */
	public void print( String indent) {
		System.out.println( indent + "ALDPort " + this + " of type " + typeName);
	}
}
