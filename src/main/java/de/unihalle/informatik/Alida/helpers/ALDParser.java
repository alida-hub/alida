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

package de.unihalle.informatik.Alida.helpers;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;


/**
 * Helper class for parsing/formatting objects for data I/O in Alida.
 * <p>
 * This class supplies convenience methods to parse and format objects
 * according to Alida's DataIO conventions.
 * 
 * @author posch
 */
public class ALDParser {

	private static boolean debug = false;
	
	/** 
	 * Hashmap to hold pairs of opening and closing brackets. 
	 */
	public static HashMap<Character,Character> brackets = 
		new HashMap<Character,Character>();
	static {
		brackets.put( '(', ')');
		brackets.put( '{', '}');
		brackets.put( '[', ']');
	}

	/** 
	 * Parses a string for a matching bracket.
	 * <p>
	 * The first character of this string is interpreted as opening backet,
   * the closing bracket is assumed to coincide with the internal
   * definition in <code>brackets</code>. Upon return the outmost matching 
   * pair of brackets is removed from <code>str</code>.
	 *
	 * @param	str 	String to parse
	 * @return	String enclosed by outmost pair of matching brackets.
	 */
	public static String parseBracket( String str) {
		if ( str.length() < 1 ) return null;

		Character closeBracket = brackets.get( str.charAt(0));
		if ( closeBracket != null )
			return parseBracket( str, closeBracket);
		else
			return null;
	}
		
	/** 
	 * Parses a string for a matching bracket.
	 * <p>
	 * The first character of this string is interpreted as opening backet,
   * the character <code>closeBracket</code> as closing bracket.
	 *
	 * @param	str 					String to parse.
	 * @param	closeBracket  Closing bracket character.
	 * @return	String enclosed by most outer pair of matching brackets.
	 */
	public static String parseBracket( String str, char closeBracket) {
		if ( str.length() < 1 ) return null;

		char openBracket = str.charAt(0);
		int count = 1; // open bracket
		int idx = 1;

		while ( count > 0 && idx < str.length() ) {
			if ( str.charAt( idx) == closeBracket ) 
				count--;
			else if ( str.charAt( idx) == openBracket )
				count++;

			idx++;
		}

		if ( count == 0 )
			return str.substring( 1, idx-1);
		else
			return null;
	}

	/** 
	 * Parses a comma separated list of 'name=value' pairs into a hash map.
	 * <p>
	 * The names found are used as keys in the hash map, while the values 
	 * are put into the map as corresponding values.
	 *
	 * @param	str String to parse.
	 * @return	Hash map with name and value pairs.
	 * @throws ALDDataIOProviderException 
	 */
	public static HashMap<String,String> parseNameValuePairs( String str) throws ALDDataIOProviderException {
		if ( debug ) {
			System.out.println( "ALDParser::parseNameValuePairs using <" +
					str + ">");
		}
		HashMap<String,String> map = new HashMap<String,String>();

		for ( String pair : ALDParser.split( str, ',') ) {
			ArrayList<String> parts = 
				new ArrayList<String>(ALDParser.split( pair.trim(), '='));
			if ( parts.size() == 1 ) {
				map.put( parts.get(0).trim(), "");

			} else if ( parts.size() != 2 ) {
				throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR,
						"ALDParser::parseNameValuePairs found = sign " 
								+	(parts.size() -1) + " times, instead of once");
			} else {
				map.put( parts.get(0).trim(), parts.get(1).trim());
			}
		}

		return map;
	}

	/** 
	 * Split a string at each occurance of <code>sepChar</code>.
	 * <p> 
   * Note that occurances of <code>sepChar</code> enclosed in
	 * brackets are not considered as separators.
	 * 
	 * @param	str 		String to split.
	 * @param	sepChar Separating character.
	 * @return	Linked list containing the separated parts of <code>str</code>.
	 */
	public static LinkedList<String> split( String str, char sepChar) {
		LinkedList<String> parts = new LinkedList<String>();

		int startIdx = 0;
		int endIdx = 0;

		while ( endIdx < str.length() ) {
			//System.out.println( "split, endIdx = " + endIdx);
			if ( str.charAt( endIdx) == sepChar ) {
				parts.add( str.substring( startIdx, endIdx));
				endIdx++;
				startIdx = endIdx;
			} else if ( brackets.get( str.charAt( endIdx)) != null )  {
				char closeBracket = brackets.get( str.charAt( endIdx));
				String aux = parseBracket( str.substring( endIdx), closeBracket);
				//System.out.println( "split, found bracket: " + str.charAt( endIdx) + " - " + closeBracket + " aux = " + aux) ;
				if ( aux != null ) {
					endIdx += aux.length() + 2;
				} else {
					endIdx = str.length();
					//parts.add( str.substring( startIdx, endIdx));
				} 
			} else {
				endIdx++;
			}
		}

		if ( startIdx != endIdx ) {
			parts.add( str.substring( startIdx, endIdx));
		}

		return parts;
	}

	/** 
	 * Formats an array according to Alida data I/O conventions.
	 * 
	 * @param	obj	Array to be formatted.
	 * @return	Object formatted as string
	 **/
	public static String arrayToString( Object obj) {
		StringBuffer buf;

		if (obj.getClass().getName().startsWith("[")) {
			buf = new StringBuffer();
			Object [] array;

			// native datatypes 
			if ( obj.getClass().equals(boolean[].class )) {
				boolean[] pArray = (boolean[])obj;
				array = new Boolean[pArray.length];
				for ( int i=0 ; i < pArray.length ; i++ )
					array[i] = pArray[i];
			} else if ( obj.getClass().equals(byte[].class )) {
				byte[] pArray = (byte[])obj;
				array = new Byte[pArray.length];
				for ( int i=0 ; i < pArray.length ; i++ )
					array[i] = pArray[i];
			} else if ( obj.getClass().equals(double[].class )) {
				double[] pArray = (double[])obj;
				array = new Double[pArray.length];
				for ( int i=0 ; i < pArray.length ; i++ )
					array[i] = pArray[i];
			} else if ( obj.getClass().equals(float[].class )) {
				float[] pArray = (float[])obj;
				array = new Float[pArray.length];
				for ( int i=0 ; i < pArray.length ; i++ )
					array[i] = pArray[i];
			} else if ( obj.getClass().equals(int[].class )) {
				int[] pArray = (int[])obj;
				array = new Integer[pArray.length];
				for ( int i=0 ; i < pArray.length ; i++ )
					array[i] = pArray[i];
			} else if ( obj.getClass().equals(short[].class )) {
				short[] pArray = (short[])obj;
				array = new Short[pArray.length];
				for ( int i=0 ; i < pArray.length ; i++ )
					array[i] = pArray[i];
			} else {
				array = (Object[])obj;
			} 

			buf.append("[");
			int index = 0;
			for (Object o: array) {
				if (index == 0)
					buf.append(o.toString());
				else
					buf.append("," + o.toString());
				++index;
			}
			buf.append("]");
			return new String( buf);
		} else {
			return null;
		}
	}
	
	/**
	 * Parses a string into a 1D-array.
	 * <p>
	 * The type of the returned array depends on the specified class.
	 * Only primitive and data wrapper types as well as strings are support at the moment.
	 * The <code>valueString</code> is assume to contain a comma separated list 
	 * of nested <code>valueString</code>
     * for each element of the array enclosed in matching square brackets,
	 * e.g. <code>[1.2 , 2.2 , 3.1]</code>.
	 * 
	 * @param cl		Desired type of array.
	 * @param valueString	String to parse.
	 * @return	Array of specified type filled with elements from string.
	 * @throws ALDDataIOProviderException 
	 */
	public static Object readArray1D(Class<?> cl, String valueString) 
			throws ALDDataIOProviderException {

		String [] elements = null;
		String arrStr = valueString.trim();

		// if string contains a at least one "," we parse the string directly
		if (arrStr.contains("[")) {
			if (arrStr.startsWith("[")) {
				arrStr = arrStr.substring(1);
			}
			if (arrStr.endsWith("]")) {
				arrStr = arrStr.substring(0, arrStr.length()-1);
			}
			elements = arrStr.split(",");
		}
		// otherwise we interpret the string as file and try to parse the file
		else {
			BufferedReader bufRead = null;
			try {
				bufRead = new BufferedReader(new FileReader(valueString));
			} catch (FileNotFoundException e) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.FILE_IO_ERROR,
						"ALDParser::readArray1D cannot open array file " +
								"\"" + valueString + "\", exiting...");

			}
			try {
				arrStr = bufRead.readLine();
				if (arrStr.startsWith("[")) {
					arrStr = arrStr.substring(1);
				}
				if (arrStr.endsWith("]")) {
					arrStr = arrStr.substring(0, arrStr.length()-1);
				}
				elements = arrStr.split(",");
			} catch (IOException e) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.FILE_IO_ERROR,
						"ALDParser::readArray1D cannot read array from file " +
								"\"" + valueString + "\", exiting...");
			}
		}

		// split string into elements and do security check
		if (elements == null) {
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.SYNTAX_ERROR,
					"ALDParser::readArray1D cannot find 1D arrays in <" +
							 arrStr + ">");
		}

		int index = 0;
		// wrapper datatypes
		try {
			if (cl.equals(Boolean[].class )) {
				Boolean [] array = new Boolean[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Boolean.valueOf(e);
					++index;
				}
				return array;
			} else if (cl.equals(Byte[].class )) {
				Byte [] array = new Byte[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Byte.valueOf(e);
					++index;
				}
				return array;
			} else if (cl.equals(Double[].class )) {
				Double [] array = new Double[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Double.valueOf(e);
					++index;
				}
				return array;
			} else if (cl.equals(Float[].class )) {
				Float [] array = new Float[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Float.valueOf(e);
					++index;
				}
				return array;
			} else if (cl.equals(Integer[].class )) {
				Integer [] array = new Integer[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Integer.valueOf(e);
					++index;
				}
				return array;
			} else if (cl.equals(Short[].class )) {
				Short [] array = new Short[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Short.valueOf(e);
					++index;
				}
				return array;
			} else if (cl.equals(String[].class )) {
				return elements;

				// native datatypes 
			} else if (cl.equals(boolean[].class )) {
				boolean [] array = new boolean[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Boolean.valueOf(e).booleanValue();
					++index;
				}
				return array;
			} else if (cl.equals(byte[].class )) {
				byte [] array = new byte[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Byte.valueOf(e).byteValue();
					++index;
				}
				return array;
			} else if (cl.equals(double[].class )) {
				double [] array = new double[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Double.valueOf(e).doubleValue();
					++index;
				}
				return array;
			} else if (cl.equals(float[].class )) {
				float [] array = new float[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Float.valueOf(e).floatValue();
					++index;
				}
				return array;
			} else if (cl.equals(int[].class )) {
				int [] array = new int[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Integer.valueOf(e).intValue();
					++index;
				}
				return array;
			} else if (cl.equals(short[].class )) {
				short [] array = new short[elements.length];
				index = 0;
				for (String e: elements) {
					array[index] = Short.valueOf(e).shortValue();
					++index;
				}
				return array;
			}
		} catch (Exception e) {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR, 
					"ALDParser::readArray1D cannot read " + index + "-th element " +
							" from <" + elements[index] + ">\n" +
							"    " + e.toString());
		}
		
		throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
				"ALDParser::readArray1D unknown element type <" + cl.getCanonicalName() + ">");
	}

	/**
	 * Parses a string into a 2D-array.
	 * <p>
	 * The type of the returned array depends on the specified class.
	 * Only primitive and data wrapper types as well as strings are support at the moment.
	 * The <code>valueString</code> is assume to contain a comma separated list 
	 * of nested <code>valueString</code>
     * for each element of the array enclosed in matching square brackets,
     * These values string are as specified fo 1D-arrays, i.e. again
     * lists of comma separated values enclosed in matching square brackets.
	 * e.g. <code>[ [1 , 2] , [3, 4]]</code>.
	 * 
	 * @param cl		Desired type of array.
	 * @param valueString	String to parse.
	 * @return	Array of specified type filled with elements from string.
	 */
	/**
	 * Parses a string into a 2D-array.
	 * 
	 * @param cl		Desired type of array.
	 * @param valueString	Input string to parse.
	 * @return	Array of specified type filled with elements from string.
	 * @throws ALDDataIOProviderException 
	 */
	public static Object readArray2D(Class<?> cl, String valueString) throws ALDDataIOProviderException {

		if ( valueString == null) 
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.UNSPECIFIED_ERROR, 
					"ALDParser::readArray2D valueString must not be null");
		String elementString = null;
		String arrStr = valueString.trim();
		
		// if string contains a at least one "[" we parse the string directly
		if (arrStr.contains("[")) {
			if (arrStr.startsWith("[")) {
				arrStr = arrStr.substring(1);
			}
			if (arrStr.endsWith("]")) {
				arrStr = arrStr.substring(0, arrStr.length()-1);
			}
			elementString = arrStr;
		}
		// otherwise we interpret the string as file and try to parse the file
		// TODO: probably this is not necessary any more as standardized provider is used
		else {
			BufferedReader bufRead = null;
			try {
				bufRead = new BufferedReader(new FileReader(valueString));
			} catch (FileNotFoundException e) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.FILE_IO_ERROR,
						"ALDParser::readArray2D cannot open array file " +
								"\"" + valueString + "\", exiting...");
			}
			try {
				arrStr = bufRead.readLine();
				if (arrStr.startsWith("[")) {
					arrStr = arrStr.substring(1);
				}
				if (arrStr.endsWith("]")) {
					arrStr = arrStr.substring(0, arrStr.length()-1);
				}
				elementString = arrStr;
			} catch (IOException e) {
				throw new ALDDataIOProviderException(
						ALDDataIOProviderExceptionType.FILE_IO_ERROR,
						"ALDParser::readArray2D cannot read array from file " +
								"\"" + valueString + "\", exiting...");
			}
		}

		LinkedList<String> sublists = ALDParser.split(elementString,',');
		if (sublists.isEmpty())
			throw new ALDDataIOProviderException(
					ALDDataIOProviderExceptionType.SYNTAX_ERROR,
					"ALDParser::readArray2D cannot find 1D arrays in <" +
							 elementString + ">");

		// split each sublist into its components
		LinkedList<LinkedList<String>> sublistsSplitted = 
			new LinkedList<LinkedList<String>>();
		for (String slist: sublists) {
			slist = slist.trim();
			if (slist.startsWith("[")) {
				slist = slist.substring(1);
			}
			if (slist.endsWith("]")) {
				slist = slist.substring(0, slist.length()-1);
			}
			LinkedList<String> elements = ALDParser.split(slist,',');
			sublistsSplitted.add(elements);
		}

		int ydim = sublists.size();
		int xdim = sublistsSplitted.get(0).size();
 
		int x = 0;
		int y = 0;
		// wrapper datatypes
		try {
			if (cl.equals(Boolean[][].class )) {
				Boolean [][] array = new Boolean[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = Boolean.valueOf(sublistsSplitted.get(y).get(x));
					}
				}
				return array;
			} else if (cl.equals(Byte[][].class )) {
				Byte [][] array = new Byte[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = Byte.valueOf(sublistsSplitted.get(y).get(x));
					}
				}
				return array;
			} else if (cl.equals(Double[][].class )) {
				Double [][] array = new Double[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = Double.valueOf(sublistsSplitted.get(y).get(x));
					}
				}

				return array;
			} else if (cl.equals(Float[][].class )) {
				Float [][] array = new Float[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = Float.valueOf(sublistsSplitted.get(y).get(x));
					}
				}
				return array;
			} else if (cl.equals(Integer[][].class )) {
				Integer [][] array = new Integer[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = Integer.valueOf(sublistsSplitted.get(y).get(x));
					}
				}
				return array;
			} else if (cl.equals(Short[][].class )) {
				Short [][] array = new Short[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = Short.valueOf(sublistsSplitted.get(y).get(x));
					}
				}
				return array;
			} else if (cl.equals(String[][].class )) {
				String [][] array = new String[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = sublistsSplitted.get(y).get(x);
					}
				}

				// native datatypes 
			} else if (cl.equals(boolean[][].class )) {
				boolean [][] array = new boolean[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = 
								Boolean.valueOf(sublistsSplitted.get(y).get(x)).booleanValue();
					}
				}
				return array;
			} else if (cl.equals(byte[][].class )) {
				byte [][] array = new byte[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = 
								Byte.valueOf(sublistsSplitted.get(y).get(x)).byteValue();
					}
				}
				return array;
			} else if (cl.equals(double[][].class )) {
				double [][] array = new double[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = 
								Double.valueOf(sublistsSplitted.get(y).get(x)).doubleValue();
					}
				}
				return array;
			} else if (cl.equals(float[][].class )) {
				float [][] array = new float[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = 
								Float.valueOf(sublistsSplitted.get(y).get(x)).floatValue();
					}
				}
				return array;
			} else if (cl.equals(int[][].class )) {
				int [][] array = new int[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = 
								Integer.valueOf(sublistsSplitted.get(y).get(x)).intValue();
					}
				}
				return array;
			} else if (cl.equals(short[][].class )) {
				short [][] array = new short[ydim][xdim];
				for (y=0; y<ydim; ++y) {
					for (x=0; x<xdim; ++x) {
						array[y][x] = 
								Short.valueOf(sublistsSplitted.get(y).get(x)).shortValue();
					}
				}
				return array;
			}
		} catch (Exception e) {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR, 
					"ALDParser::readArray2D cannot read " + x + "-th element in " + y + "-th row" +
							" from <" + sublistsSplitted.get(y).get(x) + ">\n" +
							"    " + e.toString());
		}

		throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
				"ALDParser::readArray2D unknown element type <" + cl.getCanonicalName() + ">");
	}
}
