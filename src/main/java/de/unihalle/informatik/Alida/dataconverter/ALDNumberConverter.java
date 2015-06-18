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

package de.unihalle.informatik.Alida.dataconverter;

import java.lang.reflect.Type;
import java.util.*;

import de.unihalle.informatik.Alida.annotations.ALDDataConverterProvider;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.dataconverter.ALDDataConverter;
import de.unihalle.informatik.Alida.dataconverter.ALDDataConverterManager.ALDSourceTargetClassPair;
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterException.*;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOperator;

/**
 * Converts numbers. 
 * 
 * @author posch
 */
@ALDDataConverterProvider
public class ALDNumberConverter extends ALDOperator 
	implements ALDDataConverter {

	@Parameter(label = "Source Object", required = true, 
	           direction = Parameter.Direction.IN, supplemental = false, 
	           description = "Source object to convert.")
	Object sourceObject;
	
	@Parameter(label = "Target Class", required = true, 
	           direction = Parameter.Direction.IN, supplemental = false, 
	           description = "Class of target object.")
	Class<?> targetClass;
	
	@Parameter(label = "Target Object", 
			   direction = Parameter.Direction.OUT, supplemental = false, 
	           description = "Target object.")
	Object targetObject;
	
	


	
	private static LinkedList<Class> numberClasses = new LinkedList<Class>();
	{
		 Class[] numberClassesA = new Class[]{byte.class, Byte.class, short.class,
			Short.class, int.class, Integer.class, long.class, Long.class, float.class, Float.class,
			double.class, Double.class};

		numberClasses = new LinkedList<Class>();
		for ( int i = 0 ; i < numberClassesA.length ; i++)
			numberClasses.add( numberClassesA[i]);
	}
	
	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDNumberConverter() throws ALDOperatorException {
		super();


	}
	@Override
	public Collection<ALDSourceTargetClassPair> providedClasses() {
		LinkedList<ALDSourceTargetClassPair> res = new LinkedList<ALDSourceTargetClassPair>();
		
		for ( Class sourceClass : numberClasses ) {
			for ( Class targetClass : numberClasses ) {
			ALDSourceTargetClassPair pair = 
					new ALDSourceTargetClassPair(sourceClass, targetClass);
				res.add(pair);
			}
		}
		
		return res;
	}

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.dataconverter.ALDDataConverter#convert(java.lang.Object, java.lang.Class)
	 */
	@Override
	public boolean supportConversion(Class<?> sourceClass, Type[] sourceTypes,
			Class<?> targetClass, Type[] targetTypes) {
		return numberClasses.contains(sourceClass) && numberClasses.contains(targetClass);
	}

	@Override
	public Object convert(Object sourceObject, Type[] sourceTypes, Class<?> targetClass, Type[] targetTypes) 
			throws ALDDataConverterException {
		this.sourceObject = sourceObject;
		this.targetClass = targetClass;
		try {
			this.runOp();
		} catch (Exception e) {
			throw new ALDDataConverterException(
					ALDDataIOProviderExceptionType.CANNOT_CONVERT, 
					"[ALDVectorNumberConverter] invalid request <" 
							+ this.sourceObject.getClass().getName() +
							"> to <" + this.targetClass.getName() + ">");	
		}	
		return this.targetObject;
	}

	@Override
	protected void operate() throws ALDOperatorException {
		System.out.println("CONVERT");
		if ( sourceObject == null) {
			targetObject = null;
			return;
		}
		if ( targetClass == Double.class || targetClass == double.class) {
			Double targetNumber = ((Number)sourceObject).doubleValue();
			targetObject = targetNumber;
		} else if ( targetClass == Float.class || targetClass == float.class) {
			Float targetNumber = ((Number)sourceObject).floatValue();		
			targetObject = targetNumber;
		} else if ( targetClass == Long.class | targetClass == long.class) {
			Long targetNumber = ((Number)sourceObject).longValue();		
			targetObject = targetNumber;
		} else if ( targetClass == Integer.class | targetClass == int.class) {
			Integer targetNumber = ((Number)sourceObject).intValue();		
			targetObject = targetNumber;
		} else if ( targetClass == Long.class | targetClass == long.class) {
			Long targetNumber = ((Number)sourceObject).longValue();		
			targetObject = targetNumber;
		} else if ( targetClass == Short.class | targetClass == short.class) {
			Short targetNumber = ((Number)sourceObject).shortValue();		
			targetObject = targetNumber;
		} else if ( targetClass == Byte.class | targetClass == byte.class ) {
			Byte targetNumber = ((Number)sourceObject).byteValue();		
			targetObject = targetNumber;
		} else {
			throw new ALDOperatorException(
			OperatorExceptionType.INVALID_CLASS,
					"[ALDVectorNumberConverter] invalid request <" 
							+ this.sourceObject.getClass().getName() +
							"> to <" + this.targetClass.getName() + ">");	

		}
		
	}

}
