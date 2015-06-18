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

package de.unihalle.informatik.Alida.dataio.provider.cmdline;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDParser;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

/**
 * DataIO provider for primitive datatypes, Java wrapper types, and arrays (1D and 2D) from comamand line.
 * As this provider extends {@link ALDStandardizedDataIOCmdline} it
 * implements the Alida syntax conventions.
 * 
 * @author moeller
 *
 */
@ALDDataIOProvider
public class ALDNativeDataIOCmdline extends ALDStandardizedDataIOCmdline {

	@Override
    public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( String.class);
		classes.add( boolean.class);
		classes.add( byte.class);
		classes.add( double.class);
		classes.add( float.class);
		classes.add( int.class);
		classes.add( long.class);
		classes.add( short.class);
		classes.add( Boolean.class);
		classes.add( Byte.class);
		classes.add( Double.class);
		classes.add( Float.class);
		classes.add( Integer.class);
		classes.add( Long.class);
		classes.add( Short.class);

		classes.add( Boolean[].class);
		classes.add( Byte[].class);
		classes.add( Double[].class);
		classes.add( Float[].class);
		classes.add( Integer[].class);
		classes.add( Short[].class);
		classes.add( String[].class);
		classes.add( boolean[].class);
		classes.add( byte[].class);
		classes.add( double[].class);
		classes.add( float[].class);
		classes.add( int[].class);
		classes.add( short[].class);

		classes.add( Boolean[][].class);
		classes.add( Byte[][].class);
		classes.add( Double[][].class);
		classes.add( Float[][].class);
		classes.add( Integer[][].class);
		classes.add( Short[][].class);
		classes.add( String[][].class);
		classes.add( boolean[][].class);
		classes.add( byte[][].class);
		classes.add( double[][].class);
		classes.add( float[][].class);
		classes.add( int[][].class);
		classes.add( short[][].class);

		return classes;
	}
	
	/**
	 * Method to parse native data from a string.
	 * <p>
	 * Note that the <code>field</code> argument is ignored here.
	 * <p>
	 * The <code>cl</code>passed to the method should contain the  
	 * class which is to be returned. If it is null, null is
	 * returned. Likewise if something else goes wrong, the return value is null.
	 * <p>
	 * Note that 1D arrays have to be encoded in one of the following ways,
	 * i.e. with or without enclosing brackets:
	 * <ul>
	 * <li> "1,2,3,4,5,6,7"
	 * <li> "[1,2,3,4,5,6,7]"
	 * </ul>
	 * For arrays containing only a single element using brackets is obligatory.
	 * <p>
	 * 2D arrays have to be encoded as follows:
	 * <ul>
	 * <li> [[1,2,3],[4,5,6]]
	 * </ul>
	 * 
	 */
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIO#readData(Field, Class<?>, java.lang.String)
	 */
	@Override
	public Object parse(Field field, Class<?> cl, String valueString) 
			throws ALDDataIOProviderException {

		if (cl == null)
			return null;
		
		// just a string...
		if (cl.equals(String.class)) 
			return valueString;
		
		valueString = valueString.trim().replaceAll("\\s","");

		try {
			// native types
			if (cl.equals(boolean.class))
				return Boolean.valueOf(valueString);
			else if (cl.equals(byte.class))
				return Byte.valueOf(valueString);
			else if (cl.equals(double.class))
				return Double.valueOf(valueString);
			else if (cl.equals(float.class))
				return Float.valueOf(valueString);
			else if (cl.equals(int.class))
				return Integer.valueOf(valueString);
			else if (cl.equals(long.class))
				return Long.valueOf(valueString);
			else if (cl.equals(short.class))
				return Short.valueOf(valueString);
			// wrapper
			else if (cl.equals(Boolean.class)) 
				return Boolean.valueOf(valueString);
			else if (cl.equals(Byte.class))
				return Byte.valueOf(valueString);
			else if (cl.equals(Double.class))
				return Double.valueOf(valueString);
			else if (cl.equals(Float.class))
				return Float.valueOf(valueString);
			else if (cl.equals(Integer.class)) 
				return Integer.valueOf(valueString);
			else if (cl.equals(Long.class))
				return Long.valueOf(valueString);
			else if (cl.equals(Short.class))
				return Short.valueOf(valueString);
		} catch (Exception e) {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR, 
					"ALDNativeDataIOCmdline::parse cannot read number of type " +
							cl.getCanonicalName() + ">" +
							" from <" + valueString + ">\n");
		}

		// 2D-arrays
		if (cl.getName().startsWith("[[")) {
			Object res = ALDParser.readArray2D(cl, valueString);
			return res;
		}
		// 1D-arrays
		else if (cl.getName().startsWith("[")) {
			return ALDParser.readArray1D(cl, valueString);
			
		}
		
		throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
				"ALDNativeDataIOCmdline::parse unknown class <" + cl.getCanonicalName() + ">");
	}
	
	@Override
  public String formatAsString(Object obj) {
		StringBuffer str = new StringBuffer("");
		
		// handle 2D arrays
		if (obj.getClass().getName().startsWith("[[")) {
			Class<?> cl = obj.getClass();
			if (cl.equals(int[][].class )) {
				int[][]array = (int[][])obj;
				int ydim = array.length;
				int xdim = array[0].length;
				str.append("[");
				for (int y=0;y<ydim;++y) {
					if (y == 0)
						str.append("[");
					else
						str.append(",[");
					for (int x=0;x<xdim;++x) {
						if (x == 0)
							str.append(array[y][x]);
						else
							str.append("," + array[y][x]);
					}
					str.append("]");
				}
				str.append("]");
			}
			if (cl.equals(boolean[][].class )) {
				boolean[][]array = (boolean[][])obj;
				int ydim = array.length;
				int xdim = array[0].length;
				str.append("[");
				for (int y=0;y<ydim;++y) {
					if (y == 0)
						str.append("[");
					else
						str.append(",[");
					for (int x=0;x<xdim;++x) {
						if (x == 0)
							str.append(array[y][x]);
						else
							str.append("," + array[y][x]);
					}
					str.append("]");
				}
				str.append("]");
			}
			if (cl.equals(byte[][].class )) {
				byte[][]array = (byte[][])obj;
				int ydim = array.length;
				int xdim = array[0].length;
				str.append("[");
				for (int y=0;y<ydim;++y) {
					if (y == 0)
						str.append("[");
					else
						str.append(",[");
					for (int x=0;x<xdim;++x) {
						if (x == 0)
							str.append(array[y][x]);
						else
							str.append("," + array[y][x]);
					}
					str.append("]");
				}
				str.append("]");
			}
			if (cl.equals(double[][].class )) {
				double[][]array = (double[][])obj;
				int ydim = array.length;
				int xdim = array[0].length;
				str.append("[");
				for (int y=0;y<ydim;++y) {
					if (y == 0)
						str.append("[");
					else
						str.append(",[");
					for (int x=0;x<xdim;++x) {
						if (x == 0)
							str.append(array[y][x]);
						else
							str.append("," + array[y][x]);
					}
					str.append("]");
				}
				str.append("]");
			}
			if (cl.equals(float[][].class )) {
				float[][]array = (float[][])obj;
				int ydim = array.length;
				int xdim = array[0].length;
				str.append("[");
				for (int y=0;y<ydim;++y) {
					if (y == 0)
						str.append("[");
					else
						str.append(",[");
					for (int x=0;x<xdim;++x) {
						if (x == 0)
							str.append(array[y][x]);
						else
							str.append("," + array[y][x]);
					}
					str.append("]");
				}
				str.append("]");
			}
			if (cl.equals(short[][].class )) {
				short[][]array = (short[][])obj;
				int ydim = array.length;
				int xdim = array[0].length;
				str.append("[");
				for (int y=0;y<ydim;++y) {
					if (y == 0)
						str.append("[");
					else
						str.append(",[");
					for (int x=0;x<xdim;++x) {
						if (x == 0)
							str.append(array[y][x]);
						else
							str.append("," + array[y][x]);
					}
					str.append("]");
				}
				str.append("]");
			}
			
			// wrapper datatypes
			if (   cl.equals(Boolean[][].class) 
					|| cl.equals(Byte[][].class) 
					|| cl.equals(Double[][].class) 
					|| cl.equals(Float[][].class) 
					|| cl.equals(Integer[][].class) 
					|| cl.equals(Short[][].class)
					|| cl.equals(String[][].class)) {
				Object[][]array = (Object[][])obj;
				int ydim = array.length;
				int xdim = array[0].length;
				str.append("[");
				for (int y=0;y<ydim;++y) {
					if (y == 0)
						str.append("[");
					else
						str.append(",[");
					for (int x=0;x<xdim;++x) {
						if (x == 0)
							str.append(array[y][x]);
						else
							str.append("," + array[y][x]);
					}
					str.append("]");
				}
				str.append("]");
			}
		}
		// 1D arrays
		else if (obj.getClass().getName().startsWith("[")) {
			if (obj.getClass().equals(boolean[].class )) {
				return Arrays.toString((boolean[])obj);
			}
			if (obj.getClass().equals(byte[].class )) {
				return Arrays.toString((byte[])obj);
			}
			if (obj.getClass().equals(double[].class )) {
				return Arrays.toString((double[])obj);
			}
			if (obj.getClass().equals(float[].class )) {
				return Arrays.toString((float[])obj);
			}
			if (obj.getClass().equals(int[].class )) {
				return Arrays.toString((int[])obj);
			}
			if (obj.getClass().equals(short[].class )) {
				return Arrays.toString((short[])obj);
				
			} else {

				Object [] array = (Object[])obj;
				str.append("[");
				int index = 0;
				for (Object o: array) {
					if (index == 0)
						str.append(o.toString());
					else
						str.append("," + o.toString());
					++index;
				}
				str.append("]");
			}
		} 
		else {
			// non-arrays are just printed to standard out

			str.append(obj.toString());
		}

		return new String( str);
	}
}
