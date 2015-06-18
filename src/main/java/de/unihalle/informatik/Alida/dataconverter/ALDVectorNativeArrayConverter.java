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
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterException.*;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOperator;

/**
 * Converts 1D arrays of native data types to {@link Vector} data types. 
 * 
 * @author moeller
 */
@ALDDataConverterProvider
public class ALDVectorNativeArrayConverter extends ALDOperator 
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

	private boolean debug = false;
	
	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDVectorNativeArrayConverter() throws ALDOperatorException {
		super();
	}

	private Collection<Class<?>> sourceClasses = new LinkedList<Class<?>>();
	{
		sourceClasses.add( Vector.class);
	}

	private Collection<Class<?>> targetsClasses = new LinkedList<Class<?>>();
	{
		targetsClasses.add( Boolean[].class);
		targetsClasses.add( Byte[].class);
		targetsClasses.add( Integer[].class);
		targetsClasses.add( Long[].class);
		targetsClasses.add( Short[].class);
		targetsClasses.add( Double[].class);
		targetsClasses.add( Float[].class);
		targetsClasses.add( String[].class);
		targetsClasses.add( boolean[].class);
		targetsClasses.add( byte[].class);
		targetsClasses.add( int[].class);
		targetsClasses.add( long[].class);
		targetsClasses.add( short[].class);
		targetsClasses.add( double[].class);
		targetsClasses.add( float[].class);
	}

	@Override
	public Collection<ALDSourceTargetClassPair> providedClasses() {
		LinkedList<ALDSourceTargetClassPair> res = new LinkedList<ALDSourceTargetClassPair>();
		
		for ( Class sourceClass : sourceClasses ) {
			for ( Class targetClass : targetsClasses ) {
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
		
		if ( sourceClass == null || targetClass == null || sourceTypes == null ||
				sourceTypes.length < 1)
			return false;
		
		Type sourceType = sourceTypes[0];
		
		if ( debug ) {
			System.out.println("ALDVectorNativeArrayConverter::supportConversion " +
					(Number.class.isAssignableFrom((Class<?>) sourceType)));
		}
		
		return 
				// String
				(targetClass == String[].class && sourceType == String.class) ||
				
				// Boolean or boolean
			   ( (targetClass == Boolean[].class || targetClass == boolean[].class) &&
			     (sourceType == Boolean.class || sourceType == boolean.class) ) ||
			     
			     // tragetClass must be an array of numbers
			    ( Number.class.isAssignableFrom((Class<?>) sourceType) || 
			    		sourceType == byte.class ||
			    		sourceType == short.class ||
			    		sourceType == int.class ||
			    		sourceType == long.class ||
			    		sourceType == float.class ||
			    		sourceType == double.class);
	}

	@Override
	public Object convert(Object sourceObject, Type[] sourceTypes,
			Class<?> targetClass, Type[] targetTypes)
			throws ALDDataConverterException {
		this.sourceObject = sourceObject;
		this.targetClass = targetClass;
		if (   !(this.sourceObject instanceof Vector<?>))
			throw new ALDDataConverterException(
					ALDDataIOProviderExceptionType.CANNOT_CONVERT, 
					"[ALDVectorNativeArrayConverter] invalid source object of type <" 
							+ this.sourceObject.getClass().getName() + ">");	
		if (((Vector<?>)this.sourceObject).size() == 0) 
			throw new ALDDataConverterException(
					ALDDataIOProviderExceptionType.CANNOT_CONVERT, 
					"[ALDVectorNativeArrayConverter] input vector is empty!");	
		try {
			this.runOp();
		} catch (Exception e) {
			throw new ALDDataConverterException(
					ALDDataIOProviderExceptionType.CANNOT_CONVERT, 
					"[ALDVectorNativeArrayConverter] invalid request <" 
							+ this.sourceObject.getClass().getName() +
							"> to <" + this.targetClass.getName() + ">");	
		}	
		return this.targetObject;
	}

	@Override
	protected void operate() {
		if ( sourceObject == null ) {
			targetObject = null;
			return;
		}
		Vector<?> inputVec = (Vector<?>)this.sourceObject;
		int size = inputVec.size();
		if (this.targetClass.equals(Boolean[].class)) {
			Boolean[] targetArray = new Boolean[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = (Boolean)o;
				++i;
			}
			this.targetObject = targetArray;					
		}
		else if (this.targetClass.equals(Byte[].class)) {
			Byte[] targetArray = new Byte[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Number)o).byteValue();
				++i;
			}
			this.targetObject = targetArray;					
		}
		else if (this.targetClass.equals(Long[].class)) {
			Long[] targetArray = new Long[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Number)o).longValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(Integer[].class)) {
			Integer[] targetArray = new Integer[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Number)o).intValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(Short[].class)) {
			Short[] targetArray = new Short[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Number)o).shortValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(String[].class)) {
			String[] targetArray = new String[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = (String)o;
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(boolean[].class)) {
			boolean[] targetArray = new boolean[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Boolean)o).booleanValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(Double[].class)) {
			Double[] targetArray = new Double[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Number)o).doubleValue();
				++i;
			}
			this.targetObject = targetArray;		
		}
		else if (this.targetClass.equals(Float[].class)) {
			Float[] targetArray = new Float[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Number)o).floatValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(byte[].class)) {
			byte[] targetArray = new byte[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Byte)o).byteValue();
				++i;
			}
			this.targetObject = targetArray;					
		}
		else if (this.targetClass.equals(int[].class)) {
			int[] targetArray = new int[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Integer)o).intValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(long[].class)) {
			long[] targetArray = new long[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Integer)o).longValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(short[].class)) {
			short[] targetArray = new short[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Short)o).shortValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(double[].class)) {
			double[] targetArray = new double[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Double)o).doubleValue();
				++i;
			}
			this.targetObject = targetArray;
		}
		else if (this.targetClass.equals(float[].class)) {
			float[] targetArray = new float[size];
			int i=0;
			for (Object o : inputVec) {
				targetArray[i] = ((Float)o).floatValue();
				++i;
			}
			this.targetObject = targetArray;
		}
	}

}
