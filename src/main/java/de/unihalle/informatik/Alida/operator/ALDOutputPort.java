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

package de.unihalle.informatik.Alida.operator;


import java.util.*;


/** Output port within an <code>opNode</code>.
 */
public class ALDOutputPort extends ALDOpNodePort {

	/** Hash to hold properties copied upon return from runOp()
	 */
	private Hashtable<String, String> properties;

    /** Construct an output port within the given <code>opNode</code> with index <code>idx</code> and
     * <code>descriptorName</code> as role of the port.
     */

	public ALDOutputPort( ALDOpNode opNode, int idx, String descriptorName) {
		super( new String("OutputPort"), opNode, idx, descriptorName);
		this.properties = new Hashtable<String, String>();
	}

	/** Return all keys of the property hash of this output port
	 */
	public Enumeration<String> getPropertyKeys() {
		return this.properties.keys( );
	}

	/** Set property with key to object o
	 */
	public void setProperty( String key, Object o) {
		this.properties.put( key, o.toString());
	}

	/** Return property with key 
	 */
	public String getProperty( String key) {
		return this.properties.get( key);
	}

	/** Print information to standard out
	 */
	public void print() {
		super.print();
		
		Enumeration<String> keys = properties.keys();

		while ( keys.hasMoreElements() ) {
			String key = keys.nextElement();
			System.out.println( "    " + key + " --> " + getProperty( key));
		}
	}
}
