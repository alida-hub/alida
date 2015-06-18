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
 * $Rev: 4370 $
 * $Date: 2011/11/18 14:02:18 $
 * $Author: posch $
 * 
 */

package de.unihalle.informatik.Alida.dataconverter;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;

import de.unihalle.informatik.Alida.dataconverter.ALDDataConverterManager.ALDSourceTargetClassPair;
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterException;


/**
 *	Super class for all data converter providers in Alida.
 *  A provider is required to convert objects of any type announced by <code>sourceClasses</code>
 *  to an object of any type announced by <code>targetClasses</code>.
 *	
 *	@author posch
 */
public interface ALDDataConverter {
	
	/**
	 * Name of the method which returns all source classes supported.
	 */
	static String providesMethodName = "providedClasses";



    /**
     * Announce all classes pairs which the converter can handle.
     * <p>
     * Note: for parameterized types there is only indicates that the
     * converter can in principle handle conversion for these classes but depending
     * und the type parameters still may refuse to convert
     * 
     * @return  Collection of classes provided
     */
    public Collection<ALDSourceTargetClassPair> providedClasses();
    
    
    /**
     * Does the converter support this kind of conversion.
     * For not parameterized types type type arrays are ignored, of course.
     * 
     * @param sourceClass
     * @param sourceTypes
     * @param targetClass
     * @param targetTypes
     * @return
     */
    public boolean supportConversion(Class<?> sourceClass, Type[] sourceTypes, 
    		Class<?> targetClass, Type[] targetTypes);
    
    /**
     * Convert the <code>sourceObject</code> into an object of class
     * <code>targetClass</code>.
     * 
     * @param sourceObject
     * @param targetClass
     * @return converted object
     * @throws ALDDataConverterException 
     */
    public Object convert( Object sourceObject,Type[] sourceTypes, Class<?> targetClass, Type[] targetTypes ) throws ALDDataConverterException;

}
