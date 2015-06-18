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

import de.unihalle.informatik.Alida.helpers.ALDConcReadWeakHashMap;

/** 
 * Hashmap keeping links between objects and data ports in processing history.
 * <p>
 * This class holds for each processed data item its link into the processing
 * history. All data items manipulated by any Alida operator during a session 
 * are stored in this hash for later documentation. 
 * <p>
 * The basic concept of Alida assumes that data objects are manipulated by
 * operators. Accordingly, to log data object manipulations it is 
 * straightforward to trace all operator calls with their configurations, i.e.
 * input and output objects and parameter settings. If in addition also the
 * order of operator calls is available it is possible to reconstruct the 
 * processing history for each single data object.
 * <p>
 * This hash stores for each data object a link (via a port) to the 
 * last manipulating operator. Internally, operators are further linked 
 * together allowing to trace back the whole processing history. 
 * 
 * @author moeller
 */
class ALDPortHash {

	/** 
	 * Hashmap of port links into the processing history.
   */
	private static ALDConcReadWeakHashMap historyAnchors = 
		new ALDConcReadWeakHashMap();

	/** 
	 * Constructor without function.
	 * <p>
	 * Note that there will only be one port hash
	 * per session and not many different objects of this type.
	 */
	private ALDPortHash() {
		// nothing to be done here
	}

	/**
	 * Returns the number of objects currently stored in the port hash.
	 * <p>
	 * Note that the number can be larger than the number of objects actually
	 * referenced from the Java process due to the management of weak references
	 * by the Java Garbage Collector.
	 */
	protected static int getEntryNum() {
		return historyAnchors.size();
	}
	
	/**
	 * Adds the object's port to the hash.
	 * <p>
	 * If there is no key in the database equal to the given object, a new
	 * hashmap entry is generated, i.e. a new {@link ALDDataPort} is initialized
	 * and put into the port database. .
	 * 
	 * @param obj	Object to be registered.
	 */
	protected static void register(Object obj) {
		if (!historyAnchors.containsKey(obj)) {
			historyAnchors.put(obj, new ALDDataPort(obj));
		}
	}
	
	/**
	 * Returns true if the given object is already registered in the hash.
	 */
	protected static boolean isRegistered(Object obj) {
		return historyAnchors.containsKey(obj);
	}
	
	/** 
	 * Get port to which object is currently linked in the history.
	 *
	 * @return Current port of this data, may be 'null'.
	 */
	protected static ALDPort getHistoryLink(Object obj) {
		return (ALDPort)historyAnchors.get(obj);
	}

	/** 
	 * Set port to which data is currently linked in the history.
	 * <p>
	 * Note that if the object was not registered before, this is done now.
	 * Subsequently the port is set to the given port object.
	 * 
	 * @param port New current port the data is linked to.
	 */
	protected static void setHistoryLink(Object obj, ALDPort port) { 
		if (port != null) {
			if (!isRegistered(obj))
				register(obj);
			historyAnchors.put(obj, port);
		}
	}
}
