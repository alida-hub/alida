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

package de.unihalle.informatik.Alida.dataio.provider.helpers;

import de.unihalle.informatik.Alida.annotations.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Helper class to configure and handle parametrized class objects.
 * 
 * @author posch
 * @author moeller
 *
 */
public class ALDParametrizedClassDataIOHelper {
	
	/** Return all fields for member variables of <code>myclass</code> or any of its super classes
     * which is annotated with {@link ALDClassParameter}.
	 *
	 * @param myclass 
	 * @return fields of annotated member variables
	 */
	public static HashMap<String,Field> getAnnotatedFields( Class<?> myclass) {

		HashMap<String,Field> fieldMap = new HashMap<String,Field>();

		// loop for this class and all super classes over all declared fields to find Annotations
		Class<?> currentClass = myclass;
		do {
			for ( Field field : currentClass.getDeclaredFields() ) {
				String name = field.getName();

				ALDClassParameter pAnnotation = field.getAnnotation( ALDClassParameter.class);
				// if field is annotated as ALDClassParameter 
				// and not already declared in a class downstream of the inheritance hierachy
				// (as we add the first field for each name we find when walking up the inheritance hierachy
				// add it to the hash map
				if ( pAnnotation != null && fieldMap.get( name) == null ) {
					fieldMap.put( name, field);
				}
			} 

			currentClass = currentClass.getSuperclass();
        } while ( currentClass != null );

		return fieldMap;
	}

	/** Return the value of the member variable associated with <code>field</code> in
	 * the object <code>obj</code>.
	 *
	 * @param field field of the member variable
	 * @param obj from which to return the value
	 * @return value of the field in obj
     */
	public static Object getValue( Field field, Object obj) {
		try {
			field.setAccessible(true);
			return field.get( obj);
		} catch ( IllegalAccessException e ) {
            e.printStackTrace();
        }
		return null;
	}

	/** 
	 * Sets the value of the member variable associated with <code>field</code> 
	 * in the object <code>obj</code>.
	 * <p>
	 * Primitive datatype can not be set from a value == null.
	 * 
	 * @param field field of the member variable
	 * @param obj from which to return the value
	 * @value value to set the field in obj to
	 * @throws IllegalAccessException
     */
	public static void setValue( Field field, Object obj, Object value) 
		throws IllegalAccessException {
		
		Class<?> cl = field.getType();
		if ( value != null || 
				!(cl == byte.class || cl == short.class || cl == int.class ||
				 cl == long.class || cl == float.class || cl == double.class ||
				 cl == boolean.class) ) { 
			field.setAccessible(true);
			field.set( obj, value);
			
			// call the value change hook, if defined
			ALDClassParameter pAnnotation = 
					field.getAnnotation( ALDClassParameter.class);
			String hookFunction = pAnnotation.changeValueHook();
			if (hookFunction != null && !hookFunction.isEmpty()) {
				try {
	        Method method = obj.getClass().getDeclaredMethod(hookFunction);
	        // change accessibility, if method is protected or private
	        method.setAccessible(true);
	        method.invoke(obj);
        } catch (SecurityException e) {
        	e.printStackTrace();
        	new IllegalAccessException("[ALDParametrizedClassDataIOHelper] " +
        		" couldn't call hook, object state maybe inconsistent...!");
        } catch (NoSuchMethodException e) {
        	e.printStackTrace();
        	new IllegalAccessException("[ALDParametrizedClassDataIOHelper] " +
          		" couldn't call hook, object state maybe inconsistent...!");
        } catch (IllegalArgumentException e) {
        	e.printStackTrace();
        	new IllegalAccessException("[ALDParametrizedClassDataIOHelper] " +
          		" couldn't call hook, object state maybe inconsistent...!");
        } catch (InvocationTargetException e) {
        	e.printStackTrace();
        	new IllegalAccessException("[ALDParametrizedClassDataIOHelper] " +
          		" couldn't call hook, object state maybe inconsistent...!");
        }
			}
		} else {
			System.err.println("[ALDParametrizedClassDataIOHelper] setValue(): " +
				"class of field = " + cl.getName() + " , value is null or native " +
				" - skipping...!");
		}
	}

	/**
	 * Sets the field with given <code>name</code> of object <code>obj</code>to <code>value</code>.
	 * <p>
	 * The method recursively iterates over all class fields and fields of
	 * super classes to find the requested field. An exception is thrown if
	 * the requested field could not be found or accessed.
	 * 
	 * @param name		Name of field to set.
	 * @param obj			Object in which field is to set.
	 * @param value		Value to set for the field.
	 * @throws IllegalAccessException
	 */
	public static void setValue( String name, Object obj, Object value) 
		throws IllegalAccessException {
		
		// search the right field:
		// loop for the class and all super classes over all declared fields 
		Class<?> myclass = obj.getClass();
		do {
			for ( Field field : myclass.getDeclaredFields() ) {
				String fieldName = field.getName();
				if (fieldName.equals(name)) {
					ALDParametrizedClassDataIOHelper.setValue(field, obj, value);
					return;
				}
			} 
			myclass = myclass.getSuperclass();
		} while ( myclass != null );
		// no field found...
		throw new IllegalAccessException("ALDParametrizedClassDataIOHelper: " +
				"no field with name \"" + name + "\" found!");
	}
}
