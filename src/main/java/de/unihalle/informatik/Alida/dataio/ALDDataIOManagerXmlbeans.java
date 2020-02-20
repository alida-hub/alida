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

package de.unihalle.informatik.Alida.dataio;

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOXmlbeans;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida_xml.ALDXMLDocument;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.xmlbeans.XmlException;

/**
 * This class implements a DataIO manager for reading/writing from xml using xmlbeans.
 * For reading and writing, it essentially looks up the correct provider for xml 
 * using the method of its super class and invokes its method.
 * <p>
 * It does its work in collaboration with {@link de.unihalle.informatik.Alida.dataio.provider.ALDDataIOXmlbeans}.
 * 
 * @author posch
 *
 */

public class ALDDataIOManagerXmlbeans extends ALDDataIOManager {
	
	/**
	 * If true writeData should try to write the history to file
	 * if the object itself is written to a file
	 */
	private boolean doHistory = false;

	/**
	 * If true additional fields/variables and so on should be tolerated by providers
	 */
	private boolean allowAdditionalFields = false;
	
	/** The singleton instance of this class
	 */
	static final ALDDataIOManagerXmlbeans instance;

	static {
		instance = new ALDDataIOManagerXmlbeans();
	}

	/** private constructor 
	 */
	private ALDDataIOManagerXmlbeans() {
		this.mapTable = initMapTable(ALDDataIOXmlbeans.class);
	}

	/** Return the single instance of this class
	 * @return single instance
	 */
	public static ALDDataIOManagerXmlbeans getInstance() {
		return instance;
	}

	/**
	 * Reads data of given class from a specified source.
     * If both <code>field</code> and <code>cl</code> are non-null, the class defined in <code>field</code> is used
     * and <code>cl</code> ignored.
     * 
     * If one of <code>field or</code> <code>cl</code> is null, the other non null argument will be used.
     * Some objects can only be read if <code>field</code> is supplied, e.g. Collections.
	 * <p>
	 * The <code>aldXmlObject</code> is used to actually read the data. The interpretation is
	 * specific to the class to be read and defined by the corresponding provider class.
	 * As a minimum this xml object contains the class name of the object represented.
	 * 
	 * @param field	field of object to be returned.
	 * @param cl	Class of data to be read.
	 * @param aldXmlObject	xml object to read data from.
	 * @return	Read data object
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 */
	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject) 
			throws ALDDataIOManagerException, ALDDataIOProviderException {
		if ( field != null )
			cl = field.getType();
		if ( cl == null ) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"ALDDataIOManagerXmlbeans::readData cannot read object if class equals null");
		}
      	ALDDataIOXmlbeans provider = (ALDDataIOXmlbeans)getProvider( cl, ALDDataIOXmlbeans.class);
      	return provider.readData( field, cl, aldXmlObject);
	}
	
	/**
	 * Writes data to the specified location.
     * This method returns a xml object with a representation of parameters value.
	 * 
	 * @param obj	Object to write.
	 * @return  ALDXMLObjectType representing the <code>obj</code>
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 */
	public ALDXMLObjectType writeData(Object obj) 
			throws ALDDataIOManagerException, ALDDataIOProviderException {
		if ( obj == null ) {
			return null;
		}

		Class cl = obj.getClass();
      	ALDDataIOXmlbeans provider = (ALDDataIOXmlbeans)getProvider( cl, ALDDataIOXmlbeans.class);
      	return provider.writeData(obj);
	}

	/**
	 * @return the writeHistory
	 */
	public boolean isDoHistory() {
		return this.doHistory;
	}

	/**
	 * @param doHistory the writeHistory to set
	 */
	public void setDoHistory(boolean doHistory) {
		this.doHistory = doHistory;
	}
	
	/**
	 * @return the allowAdditionalFields
	 */
	public boolean isAllowAdditionalFields() {
		return this.allowAdditionalFields;
	}

	/**
	 * @param allowAdditionalFields the allowAdditionalFields to set
	 */
	public void setAllowAdditionalFields(boolean allowAdditionalFields) {
		this.allowAdditionalFields = allowAdditionalFields;
	}

	/**
	 * Write to object to a file using xmlbeans providers
	 * 
	 * @param filename
	 * @param obj
	 * @throws ALDDataIOProviderException
	 * @throws ALDDataIOManagerException
	 */
	public static void writeXml( String filename, Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		ALDDataIOManagerXmlbeans.writeXml(new File( filename), obj);
	}
	
	/**
	 * Write the object to a file using xmlbeans providers
	 * 
	 * @param file
	 * @param obj
	 * @throws ALDDataIOProviderException
	 * @throws ALDDataIOManagerException
	 */
	public static void writeXml( File file, Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		ALDDataIOXmlbeans provider;
		
		// we need to get class load of the object to write to find the .xsb files in the jar
		ClassLoader mainLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader objLoader = obj.getClass().getClassLoader();
		if (objLoader != null ) {
			Thread.currentThread().setContextClassLoader(objLoader);
		}

		if ( debug ) {
			System.out.println("ALDDataIOManagerXmlbeans::writeXml: new classloader = " + Thread.currentThread().getContextClassLoader() +
					", old: " + mainLoader);
		}

		provider = 	(ALDDataIOXmlbeans)ALDDataIOManagerXmlbeans.getInstance().
				getProvider(obj.getClass(), ALDDataIOXmlbeans.class);	
		ALDXMLObjectType xmlObj = provider.writeData(obj);

		BufferedWriter bufWriter = null;

		try {
			bufWriter = new BufferedWriter(new FileWriter( file));
		} catch (IOException e) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.FILE_IO_ERROR,
					"ALDDataIOManagerXmlbeans::writeXml cannot open writer for filename <" +
							file.getAbsolutePath() + ">");				

		}
		try {

			try {
				ALDXMLDocument doc = ALDXMLDocument.Factory.newInstance();
				doc.setALDXML(xmlObj);
				doc.save(bufWriter);
				bufWriter.flush();
				bufWriter.close();
			} catch (IOException e1) {
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.FILE_IO_ERROR,
						"ALDDataIOManagerXmlbeans::writeXml error writing to file <" +
								file.getAbsolutePath() + ">");
			}
		} finally {
			Thread.currentThread().setContextClassLoader(mainLoader);
		}

	}
	
	/**
	 * Read Object from a file using xmlbeans providers
	 *
	 * @param filename
	 * @param clazz
	 * @return Object read from XML file.
	 * @throws XmlException
	 * @throws IOException
	 * @throws ALDDataIOManagerException
	 * @throws ALDDataIOProviderException
	 */
	public static Object readXml( String filename, Class<?> clazz) 
			throws XmlException, ALDDataIOManagerException, ALDDataIOProviderException {
		return ALDDataIOManagerXmlbeans.readXml(new File( filename), clazz);
	}
	
	/**
	 * Read Object from a file using xmlbeans providers
	 *
	 * @param file
	 * @param clazz
	 * @return Object read from XML file.
	 * @throws XmlException
	 * @throws IOException
	 * @throws ALDDataIOManagerException
	 * @throws ALDDataIOProviderException
	 */
	public static Object readXml( File file, Class<?> clazz) 
			throws XmlException, ALDDataIOManagerException, ALDDataIOProviderException {
		ALDDataIOXmlbeans provider;
		Object obj;

		ALDXMLObjectType xmlObj = parseXml( file, clazz);

		provider = (ALDDataIOXmlbeans)(ALDDataIOManagerXmlbeans.getInstance().
				getProvider( clazz, ALDDataIOXmlbeans.class));
		obj = provider.readData( null, clazz, xmlObj);


		return obj;

	}

	/**
	 * Parse a <code>ALDXMLObjectType</code> from a file using xmlbeans 
	 *
	 * @param file
	 * @param clazz
	 * @return <code>ALDXMLObjectType</code> read from XML file.
	 * @throws XmlException
	 * @throws IOException
	 * @throws ALDDataIOManagerException
	 * @throws ALDDataIOProviderException
	 */
	public static ALDXMLObjectType parseXml( File file, Class<?> clazz) 
			throws XmlException, ALDDataIOProviderException {
		BufferedReader reader;
		ALDXMLDocument document;

		try {
			reader = new BufferedReader( new FileReader(file));
		} catch (FileNotFoundException e) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.FILE_IO_ERROR,
					"ALDDataIOManagerXmlbeans::readXml error reading from file <" +
							file.getAbsolutePath() + ">");
		}
		
		// we need to get the class loader of the class to read to find the .xsd files in the jar
		ClassLoader mainLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader objLoader = clazz.getClassLoader();
		if (objLoader != null ) {
			Thread.currentThread().setContextClassLoader(objLoader);
		}

		if ( debug ) {
			System.out.println("ALDDataIOManagerXmlbeans::readXml new classloader = " + Thread.currentThread().getContextClassLoader() +
					", old: " + mainLoader);
		}
		
		ALDXMLObjectType xmlObj;
		try {
			try {

				document = ALDXMLDocument.Factory.parse(reader);
			} catch (IOException e) {
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.SYNTAX_ERROR,
						"ALDDataIOManagerXmlbeans::readXml cannot read xml form file <" +
								file.getAbsolutePath() + ">");
			}

			xmlObj = document.getALDXML();
		} finally {
			Thread.currentThread().setContextClassLoader(mainLoader);
		}
		return xmlObj;
	}

}
