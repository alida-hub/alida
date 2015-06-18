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


/** This is the abstract class from which all data classes have to be derived
  * which want properties to be recored when returned from an operator as result.
  * <p>
  * Each object of type ALDData holds properties like image type,
  * resolution or specification of acquisition devices.
  * There is one specific property with key "location" which is expected
  * to contain a file name or in general URI to the persistant storage
  * location. Applicable only if this data object was read from persistant storage
  * and not created from scratch.
  * Properties may change during the lifetime of a ALDData object.
  */

abstract public class ALDData {
	
	/** Properties of the data object
      */
	private Hashtable<String, String> properties;

	/** Create a data object.
	 * Initializes the member varaiables as appropriate.
	 */
	public ALDData() {
		this.properties = new Hashtable<String, String>();
	}

	/** Get the keys of all properties set in this data object
	 *
	 * @return	property keys
	 */
	public Enumeration<String> getPropertyKeys() {
		return this.properties.keys();
	}

	/** Set the property with key to new value o.
	 * Set the property also for associated port, if it is a ALDDataPort.
	 *
	 * @param key	key of property to set
	 * @param o		new value of this property
	 */
	public void setProperty( String key, Object o) {
		this.properties.put( key, o.toString());
		// set property also for associated port, if it is a ALDDataPort
		if ( ALDOperator.portHashAccess.getHistoryLink(this) instanceof ALDDataPort )
			((ALDDataPort)ALDOperator.portHashAccess.getHistoryLink(this)).setProperty( key, o.toString());
	}

	/** Get a property value for the given <code>key</code>.
	 *
	 * @param key	key of property to get
	 */
	public String getProperty( String key) {
		return this.properties.get( key);
	}

	/** Clone the property hash of this obejct.
	 *
	 * @return cloned porperties
	 */
	@SuppressWarnings("unchecked")
	public Hashtable<String, String>  cloneProperties() {
		return (Hashtable<String, String>)(this.properties.clone());
	}

	/** Set the location property, i.e. property with key location
	 *
	 * @param location		new value of location property
	 */
	public void setLocation( String location) {
		this.properties.put( "location", location);
		try { 
			ALDDataPort dp = (ALDDataPort)ALDOperator.portHashAccess.getHistoryLink(this);
			dp.setProperty("location", location);
		} catch ( Exception e ){
		}
	}

	/** Get the location property, i.e. property with key location
	 */
	public String getLocation() {
		return this.properties.get( "location");
	}

	/** Print this data object to standard out
	 */
	public void print() {
		System.out.println( "ALDData" + this);
		Enumeration<String> keys = properties.keys();

        while ( keys.hasMoreElements() ) {
			String key = keys.nextElement();
			System.out.println( "    " + key + " --> " + getProperty( key));
        }
    }


}
