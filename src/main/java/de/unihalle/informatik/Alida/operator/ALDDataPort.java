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

import java.util.*;

import org.graphdrawing.graphml.xmlns.*;


/** A data port acts as the reference for a data object in the processing history.
  */

public class ALDDataPort extends ALDPort {

	/** The processing history as graphml if any.
     * This history is most cases read from a file along with the data object itself.
	 */
 	private GraphmlType 	graphmlHistory;

	/** The properties of the underlying MTBData at the time of creation 
	 * of this Dataport
	 */
	private Hashtable<String, String> properties;

	/** Create a data port for an object.
	 *	
	 * @param	obj	object to be represented by this data port.
	 */

	public ALDDataPort(Object obj) {
		super( "DataPort" );
		this.graphmlHistory = null;
		if (obj instanceof ALDData) {
			this.properties = ((ALDData)obj).cloneProperties();
		}
		else {
			this.properties = new Hashtable<String, String>();
		}
	}

	/** Get the grahphml history of this data port
	 *
	 * @return history of this data port
	 */
	public GraphmlType getGraphmlHistory() {
		return graphmlHistory;
	}

	/** Set the grahphml history of this data port
	 *
	 * @param history history of this data port
	 */
	public void setGraphmlHistory(GraphmlType history) {
		this.graphmlHistory = history;
	}

    /** Get the keys of all properties set in this data object
     *
     * @return  property keys
     */
    public Enumeration<String> getPropertyKeys() {
        return this.properties.keys();
    }

    /** Set the property with key to new value o.
     *
     * @param key   key of property to set
     * @param o     new value of this property
     */
    public void setProperty( String key, Object o) {
        this.properties.put( key, o.toString());
    }

    /** Get a property value for the given <code>key</code>.
     *
     * @param key   key of property to get
     */
    public String getProperty( String key) {
        return this.properties.get( key);
    }

    /** Get the location property, i.e. property with key location
     */
    public String getLocation() {
        return this.properties.get( "location");
    }


	/** Print some information to System.out.
	 */
	public void print() {
		System.out.println( "MTBPort " + this + //" of type data for " + data +
							" with origin " + getOrigin());
	}

}
