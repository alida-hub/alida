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

package de.unihalle.informatik.Alida.dataio.provider.helpers;


import java.lang.reflect.*;

/**
 * Class supplying support for generic loading/saving of Collections in Alida.
 * 
 * @author posch
 *
 */
public class ALDCollectionDataIOHelper {

    /** 
     * Flag to output debug infos.
     */
    private static boolean debug = false;

	/* Return the type of a collection as found in field.
	 * Probably works not for nested collection an to forth.
	 *
	 * @param field
	 * @return 
	 */
	// TODO document restrictions on the class of the collections's elements
  	public static Type lookupType(Field field) {
		Type type = field.getGenericType();  

		if (type instanceof ParameterizedType) {  
			ParameterizedType pt = (ParameterizedType) type;  
			if ( debug ) {
				System.out.println("raw type: " + pt.getRawType());  
				System.out.println("owner type: " + pt.getOwnerType());  
				System.out.println("actual type args:");  
				for (Type t : pt.getActualTypeArguments()) {  
					System.out.println("    " + t);  
				}  
			}  
			// return first type
			return pt.getActualTypeArguments()[0];
		}
		System.err.println( "ALDCollectionDataIO::getType " + field 
																							+ " is not ParameterizedType");  
		return null;
	}
}
