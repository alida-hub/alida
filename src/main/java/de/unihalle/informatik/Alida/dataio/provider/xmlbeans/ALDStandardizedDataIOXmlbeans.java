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

package de.unihalle.informatik.Alida.dataio.provider.xmlbeans;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOXmlbeans;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;

import java.io.File;
import java.lang.reflect.Field;
import org.apache.xmlbeans.XmlException;

/**
 * Abstract class providing basic methods for xml DataIO using xmlbeans
 * according to Alida conventions.
 *<p>
 * These conventions are detailed in the documentation if the methods <code>readData</code> and <code>writeData</code> in this class below.
 * They handle IO from/to file and reading derived classes of the class handled by an dataIO provider.
 *<p>
 * Classes extending this class are expected to override the methods <code>eadData</code> 
 *  which do the actual reading
 * subsequent to generic handling of Alida convention with respect to derived classes
 * and IO form/to file.
 *
 * @author posch
 *
 */
/**
 * @author posch
 *
 */
public abstract class ALDStandardizedDataIOXmlbeans implements ALDDataIOXmlbeans {

	/**
	 * debugging output
	 */
	private boolean debug = false;


	/** Returns an object instantiated from <code>aldXmlObject</code>.
	 * For the class of the object to be read see {@link ALDDataIOManagerXmlbeans#readData(Field,Class,ALDXMLObjectType)}.
	 * This method is assumed to directly parse the <code>aldXmlObject</code> and make no
	 * prior interpretation regarding a file to use or derived class to return.
	 * 
	 *  @see de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans
	 *
	 * @param	field Field of object to be returned
	 * @param   cl Class of object to be returned.
	 * @param   aldXmlObject   Source from where to read data.
	 * @param   object if non null the xml representation is to be parse in this  instance. Otherwise a new instance is created. 

	 * 
	 * @return Object with member fields set from <code>xmlObject</code>
	 * 
	 * @throws ALDDataIOProviderException
	 * @throws ALDDataIOManagerException
	 */
	abstract public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject, Object object)
			throws ALDDataIOProviderException, ALDDataIOManagerException;

	 /** Returns an object instantiated from an <code>ALDXMLObjectType</code> read from a file with name <code>filename</code>.
	  * See {@link #readData(Field, Class, ALDXMLObjectType, Object)} for details
	 * @param field
	 * @param cl
	 * @param filename
	 * @param object
	 * @return
	 * @throws ALDDataIOProviderException
	 * @throws ALDDataIOManagerException
	 * @throws XmlException
	 */
	public Object readData(Field field, Class<?> cl, String filename, Object object)
			throws ALDDataIOProviderException, ALDDataIOManagerException, XmlException {
	 
			return readData(field, cl, new File( filename), object);
		}

	 /** Returns an object instantiated from an <code>ALDXMLObjectType</code> read from  <code>file</code>.
	  * See {@link #readData(Field, Class, ALDXMLObjectType, Object)} for details
	  * 
	 * @param field
	 * @param cl
	 * @param file
	 * @param object
	 * @return
	 * @throws ALDDataIOProviderException
	 * @throws XmlException
	 * @throws ALDDataIOManagerException
	 */
	public Object readData(Field field, Class<?> cl, File file, Object object) 
			throws ALDDataIOProviderException, XmlException, ALDDataIOManagerException {
		ALDXMLObjectType aldXmlObject = ALDDataIOManagerXmlbeans.parseXml( file, cl);
		return readData(field, cl, aldXmlObject, object);
	}

	/** Try to read an object from  <code>aldXmlObject</code> with the class stored in the
	 * xml representation using {@link #readData(Field,Class,ALDXMLObjectType,Object)}  from <code>aldXmlObject</code>.
	 * <p>
	 * If this fails try to read an object of class <code>cl</code>  or the class retrieved from
	 * <code>field</code> if this is non null from <code>aldXmlObject</code>
	 * again using  {@link #readData(Field,Class,ALDXMLObjectType,Object)}.
	 * 
	 */
	@Override
	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {

		if ( aldXmlObject == null || aldXmlObject.isNil())
			return null;

		if ( field != null )
			cl = field.getType();

		if ( debug ) {
			System.out.println("ALDStandardizedDataIOXmlbeans::readData found class <" +
					aldXmlObject.getClassName() + "> in xml-document");
		}

		// try to get class from xml to instantiate an object of this class
		try {
			Class<?> clazz =Class.forName(aldXmlObject.getClassName()); 

			return readData(field, clazz, aldXmlObject, null);

		} catch (ClassNotFoundException e) {
			// probably we get into trouble if we can not find the class name
			// but give it a try and invoke readData with obj== null
			if ( debug ) {
				System.out.println("ALDStandardizedDataIOXmlbeans::readData cannot create class object for <" +
						aldXmlObject.getClassName() + ">");
			}
			
			return readData(field, cl, aldXmlObject, null);

		}

	}
	
	
	/** Checks compatibility of wrapper and primitive classes.
	 * E.g. int and Integer are compatible.
	 * @param cl1
	 * @param cl2
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	private boolean compatible( Class cl1, Class cl2) {
		return ( (cl1 == long.class || cl1 == Long.class) &&
				(cl2 == long.class || cl2 == Long.class)    ) ||
		     ( (cl1 == int.class || cl1 == Integer.class) &&
				(cl2 == int.class || cl2 == Integer.class)    ) ||
			 ( (cl1 == short.class || cl1 == Short.class) &&
				(cl2 == short.class || cl2 == Short.class)    ) ||
			 ( (cl1 == byte.class || cl1 == Byte.class) &&
				(cl2 == byte.class || cl2 == Byte.class)    ) ||
			 ( (cl1 == double.class || cl1 == Double.class) &&
			    (cl2 == double.class || cl2 == Double.class)    ) ||
			 ( (cl1 == float.class || cl1 == Float.class) &&
				(cl2 == float.class || cl2 == Float.class)    ) ||
			 ( (cl1 == boolean.class || cl1 == Boolean.class) &&
				(cl2 == boolean.class || cl2 == Boolean.class)    );
		
	}

}

