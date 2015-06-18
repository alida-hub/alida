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

package de.unihalle.informatik.Alida.dataio.provider.xmlbeans;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.xmlbeans.ALDStandardizedDataIOXmlbeans;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida_xml.ALDXMLAnyType;
import de.unihalle.informatik.Alida_xml.ALDXMLArrayType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlByte;
import org.apache.xmlbeans.XmlDouble;
import org.apache.xmlbeans.XmlFloat;
import org.apache.xmlbeans.XmlInt;
import org.apache.xmlbeans.XmlLong;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlShort;
import org.apache.xmlbeans.XmlString;

/**
 * DataIO provider for primitive datatypes, Java wrapper types, and arrays (1D and 2D) xml using xmlbeans.
 * As this provider extends {@link ALDStandardizedDataIOXmlbeans} it
 * implements the Alida syntax conventions.
 * 
 * @author moeller
 *
 */
@ALDDataIOProvider
public class ALDNativeDataIOXmlbeans extends ALDStandardizedDataIOXmlbeans {

	private boolean debug = true;

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

		classes.add( Array.class);

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
	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject, Object obj) 
			throws ALDDataIOProviderException {

		if (cl == null)
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"ALDNativeDataIOXmlbeans::readData cl == null");
		
		if ( aldXmlObject == null || aldXmlObject.isNil()) 
			return null;
		
		// TODO: probably we should check if adlXmlObject holds an object of the correct type before casting
		
		// native and wrapper types 
		if (cl.equals(String.class)) {
			return ((XmlString)((ALDXMLAnyType)aldXmlObject).getValue()).getStringValue();
		}
		else if (cl.equals(boolean.class) || cl.equals(Boolean.class))
			return ((XmlBoolean)((ALDXMLAnyType)aldXmlObject).getValue()).getBooleanValue();
		else if (cl.equals(byte.class) || cl.equals(Byte.class))
			return ((XmlByte)((ALDXMLAnyType)aldXmlObject).getValue()).getByteValue();
		else if (cl.equals(double.class) || cl.equals(Double.class))
			return ((XmlDouble)((ALDXMLAnyType)aldXmlObject).getValue()).getDoubleValue();
		else if (cl.equals(float.class) || cl.equals(Float.class))
			return ((XmlFloat)((ALDXMLAnyType)aldXmlObject).getValue()).getFloatValue();
		else if (cl.equals(int.class) || cl.equals(Integer.class))
			return ((XmlInt)((ALDXMLAnyType)aldXmlObject).getValue()).getIntValue();
		else if (cl.equals(long.class) || cl.equals(Long.class))
			return ((XmlLong)((ALDXMLAnyType)aldXmlObject).getValue()).getLongValue();
		else if (cl.equals(short.class) || cl.equals(Short.class))
			return ((XmlShort)((ALDXMLAnyType)aldXmlObject).getValue()).getShortValue();
		else if (cl.isArray()) {
			int index = 0;
			// wrapper datatypes
			try {
				ALDXMLArrayType xmlArray = (ALDXMLArrayType) aldXmlObject;
				int length = xmlArray.getArrayValuesArray().length;
				// native datatypes 
				if (cl.equals(boolean[].class )) {
					boolean [] array = new boolean[length];
					for ( index = 0 ; index < length ; index++) {
						array[index] = (Boolean) ALDDataIOManagerXmlbeans.getInstance().readData(null, boolean.class, 
								xmlArray.getArrayValuesArray(index));
					}
					return array;
				} else if (cl.equals(byte[].class )) {
					byte [] array = new byte[length];
					for ( index = 0 ; index < length ; index++) {
						array[index] = (Byte) ALDDataIOManagerXmlbeans.getInstance().readData(null, byte.class, 
								xmlArray.getArrayValuesArray(index));
					}
					return array;
				} else if (cl.equals(double[].class )) {
					double [] array = new double[length];
					for ( index = 0 ; index < length ; index++) {
						array[index] = (Double) ALDDataIOManagerXmlbeans.getInstance().readData(null, double.class, 
								xmlArray.getArrayValuesArray(index));
					}
					return array;
				} else if (cl.equals(float[].class )) {
					float [] array = new float[length];
					for ( index = 0 ; index < length ; index++) {
						array[index] = (Float) ALDDataIOManagerXmlbeans.getInstance().readData(null, float.class, 
								xmlArray.getArrayValuesArray(index));
					}
					return array;
				} else if (cl.equals(int[].class )) {
					int [] array = new int[length];
					for ( index = 0 ; index < length ; index++) {
						array[index] = (Integer) ALDDataIOManagerXmlbeans.getInstance().readData(null, int.class, 
								xmlArray.getArrayValuesArray(index));
					}
					return array;
				} else if (cl.equals(short[].class )) {
					short [] array = new short[length];
					for ( index = 0 ; index < length ; index++) {
						array[index] = (Short) ALDDataIOManagerXmlbeans.getInstance().readData(null, short.class, 
								xmlArray.getArrayValuesArray(index));
					}
					return array;
				}
				else {
					Class<?> elementClass;
					try {
						elementClass = Class.forName(xmlArray.getElementClassName());
					} catch (ClassNotFoundException e) {
						throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR, 
								"ALDCollectionDataIOXmlbeans::readData cannot get class for <" +
										xmlArray.getElementClassName() + ">");
					}

					Object[] array = (Object[]) Array.newInstance(elementClass, length);
					for ( index = 0 ; index < length ; index++) {
						array[index] = (Object) ALDDataIOManagerXmlbeans.getInstance().readData(null, elementClass, 
								xmlArray.getArrayValuesArray(index));
					}
					return array;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR, 
						"ALDParser::readArray1D cannot read " + index + "-th element\n" +
								"    " + e.toString());
			}


		} else {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"ALDNativeDataIOXmlbeans::readData cannot read object of type " +
							cl.getCanonicalName() + ">" +
							" from <" + aldXmlObject.toString() + ">\n");
		}		
	}
	
	@Override
	public ALDXMLObjectType writeData(Object obj) throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		if (obj.getClass().isArray()) {
			ALDXMLArrayType aldXmlArray = ALDXMLArrayType.Factory.newInstance();
			aldXmlArray.setClassName(obj.getClass().getName());
		
			if (obj.getClass().equals(boolean[].class )) {
				boolean[] arr = (boolean[])obj;
				aldXmlArray.setElementClassName(boolean.class.getName());

				for (int i = 0 ; i < arr.length ; i++) {
					ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(arr[i]);
					aldXmlArray.insertNewArrayValues(i);
					aldXmlArray.setArrayValuesArray(i, xmlElement);
				}
			} else 	if (obj.getClass().equals(byte[].class )) {
				byte[] arr = (byte[])obj;
				aldXmlArray.setElementClassName(byte.class.getName());

				for (int i = 0 ; i < arr.length ; i++) {
					ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(arr[i]);
					aldXmlArray.insertNewArrayValues(i);
					aldXmlArray.setArrayValuesArray(i, xmlElement);
				}
			} else if (obj.getClass().equals(double[].class )) {
				double[] arr = (double[])obj;
				aldXmlArray.setElementClassName(double.class.getName());

				for (int i = 0 ; i < arr.length ; i++) {
					ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(arr[i]);
					aldXmlArray.insertNewArrayValues(i);
					aldXmlArray.setArrayValuesArray(i, xmlElement);
				}
			} else if (obj.getClass().equals(float[].class )) {
				float[] arr = (float[])obj;
				aldXmlArray.setElementClassName(float.class.getName());

				for (int i = 0 ; i < arr.length ; i++) {
					ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(arr[i]);
					aldXmlArray.insertNewArrayValues(i);
					aldXmlArray.setArrayValuesArray(i, xmlElement);
				}
			} else if (obj.getClass().equals(int[].class )) {
				int[] arr = (int[])obj;
				aldXmlArray.setElementClassName(int.class.getName());

				for (int i = 0 ; i < arr.length ; i++) {
					ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(arr[i]);
					aldXmlArray.insertNewArrayValues(i);
					aldXmlArray.setArrayValuesArray(i, xmlElement);
				}
			} else if (obj.getClass().equals(short[].class )) {
				short[] arr = (short[])obj;
				aldXmlArray.setElementClassName(short.class.getName());

				for (int i = 0 ; i < arr.length ; i++) {
					ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(arr[i]);
					aldXmlArray.insertNewArrayValues(i);
					aldXmlArray.setArrayValuesArray(i, xmlElement);
				}
				
			} else {
				Object [] array = (Object[])obj;
				if ( array.length > 0 ) {
					aldXmlArray.setElementClassName(array[0].getClass().getName());

					int index = 0;
					for (Object o: array) {
						ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(o);
						aldXmlArray.insertNewArrayValues(index);
						aldXmlArray.setArrayValuesArray(index, xmlElement);
						++index;
					}
				} else {
					aldXmlArray.setElementClassName("");
				}
			}

			return aldXmlArray;
			
		} else {

			// native  and wrapper types
		    Class<?> cl = obj.getClass();

		    XmlObject xmlObject;
			if (cl.equals(boolean.class) || cl.equals(Boolean.class)) {
				XmlBoolean xmlBool = XmlBoolean.Factory.newInstance();
				xmlBool.setBooleanValue((Boolean)obj);
				xmlObject = xmlBool;				
			} else if (cl.equals(byte.class) || cl.equals(Byte.class)) {
				XmlByte xmlByte = XmlByte.Factory.newInstance();
				xmlByte.setByteValue((Byte)obj);
				xmlObject = xmlByte;				
			} else if (cl.equals(double.class) || cl.equals(Double.class) ) {
				XmlDouble xmlDouble = XmlDouble.Factory.newInstance();
				xmlDouble.setDoubleValue((Double)obj);
				xmlObject = xmlDouble;
			} else if (cl.equals(float.class) || cl.equals(Float.class)) {
				XmlFloat xmlFloat = XmlFloat.Factory.newInstance();
				xmlFloat.setFloatValue((Float)obj);
				xmlObject = xmlFloat;
			} else if (cl.equals(int.class) || cl.equals(Integer.class)) {
				XmlInt xmlInt = XmlInt.Factory.newInstance();
				xmlInt.setIntValue((Integer)obj);
				xmlObject = xmlInt;
			} else if (cl.equals(long.class) || cl.equals(Long.class) ) {
				XmlLong xmlLong = XmlLong.Factory.newInstance();
				xmlLong.setLongValue((Long)obj);
				xmlObject = xmlLong;
			} else if (cl.equals(short.class) || cl.equals(Short.class) ) {
				XmlShort xmlShort = XmlShort.Factory.newInstance();
				xmlShort.setShortValue((Short)obj);
				xmlObject = xmlShort;
			} else if ( cl.equals(String.class)) {
				XmlString xmlString = XmlString.Factory.newInstance();
				xmlString.setStringValue((String)obj);
				xmlObject = xmlString;
			} else {
				throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
						"ALDNativeDataIOXmlbeans::writeData invalid class<" +
						cl.getName() + ">");
			}
			ALDXMLAnyType aldXmlObject = ALDXMLAnyType.Factory.newInstance();
			aldXmlObject.setClassName(cl.getName());
			aldXmlObject.setValue(xmlObject);

			return aldXmlObject;
		}
	}
}
