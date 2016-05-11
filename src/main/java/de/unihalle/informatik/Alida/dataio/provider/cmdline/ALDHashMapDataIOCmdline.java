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

package de.unihalle.informatik.Alida.dataio.provider.cmdline;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Data I/O provider for hash maps on the command line.
 * <p>
 * As this provider extends {@link ALDStandardizedDataIOCmdline} it
 * follows general Alida syntax conventions.
 * 
 * @author moeller
 *
 */
@ALDDataIOProvider
public class ALDHashMapDataIOCmdline 
	extends ALDStandardizedDataIOCmdline {

	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(HashMap.class);
		return classes;
	}
	
	/**
	 * Input of hash maps is not yet supported.
	 * 
	 * @param field 				Field of object to be returned
	 * @param cl 						Class of object to be returned.
	 * @param valueString   Source from where to read data.
	 * @return Hash map read from string.
	 * @throws ALDDataIOProviderException Thrown in case of failure. 
	 */
	@Override
	public Object parse(Field field, Class<?> cl, String valueString) 
			throws ALDDataIOProviderException {
		throw new ALDDataIOProviderException(
				ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR, 
					"[ALDHashMapDataIOCmdline::parse] " 
							+ "reading hash maps not yet supported!");
	}

	/** 
	 * Returns a string representation of the given hash map.
	 * 
	 * @param obj   Hash map to format.
	 * @return String representation of the object.
	 * @see ALDHashMapDataIOCmdline#parse(Field,Class,String)
	 */
	@Override
	public String formatAsString(Object obj) {
		
		StringBuffer strbuf = new StringBuffer("{ ");

		HashMap<?,?> map = (HashMap<?,?>)obj;
		Set<?> keys = map.keySet();
		for ( Object key : keys ) {
			strbuf.append("\n");
			strbuf.append("\t" + key.toString() + ": " 
					+ map.get(key).toString());
		}
		strbuf.append("\n");
		strbuf.append(" }");
		return new String( strbuf);
	}
}
