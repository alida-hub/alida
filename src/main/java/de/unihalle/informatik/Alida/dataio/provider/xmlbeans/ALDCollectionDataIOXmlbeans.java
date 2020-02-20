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

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.cmdline.ALDStandardizedDataIOCmdline;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDCollectionDataIOHelper;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDParser;
import de.unihalle.informatik.Alida_xml.ALDXMLAnyType;
import de.unihalle.informatik.Alida_xml.ALDXMLArrayType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;


/**
 * DataIO provider for collections xml using xmlbeans.
 * As this provider extends {@link ALDStandardizedDataIOCmdline} it
 * implements the Alida syntax conventions.
 * 
 * @author posch
 *
 */
@ALDDataIOProvider
public class ALDCollectionDataIOXmlbeans extends ALDStandardizedDataIOXmlbeans {

	/** output debugging info?
	 */
	private boolean debug = false;

	@Override
    public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( Collection.class);

		return classes;
	}
	
	/** Returns a collection instantiated from <code>aldXmlObject</code>.
	 * The class of the elements of the collection is determined 
	 * using {@link de.unihalle.informatik.Alida.dataio.provider.helpers.ALDCollectionDataIOHelper#lookupType}.
     * 
     * @param   field Field of object to be returned
     * @param cl Class of object to be returned.
     * @return Collection read from valueString.
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */

	@Override
  	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject, Object obj) 
  			throws ALDDataIOProviderException, ALDDataIOManagerException {
		
		if ( aldXmlObject == null || aldXmlObject.isNil()) 
			return null;

		ALDXMLArrayType xmlArray = (ALDXMLArrayType) aldXmlObject;
		Type elementType;
		
		if ( !  (xmlArray.getElementClassName() == null ||
				 xmlArray.getElementClassName().isEmpty()) ) {
			try {
				elementType = Class.forName(xmlArray.getElementClassName());
			} catch (ClassNotFoundException e1) {
				throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR, 
						"ALDCollectionDataIOXmlbeans::readData cannot get class for <" +
								xmlArray.getElementClassName() + ">");
			}
		} else {
			elementType = ALDCollectionDataIOHelper.lookupType( field);
		}
		
		if ( debug ) 
			System.out.println( "ALDCollectionDataIOCmdline::parse " + elementType);

		int length = xmlArray.getArrayValuesArray().length;

		Collection collection;
		if ( obj == null)
			try {
				collection = (Collection) cl.newInstance();
			} catch (Exception e) {
	        	throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR,
	        			"ALDCollectionDataIOXmlbeans::readData cannot instantiate collection of class <" +
	        					cl.getCanonicalName() + ">");
			}
		else
			collection = (Collection) obj;
	
		for ( int index = 0 ; index < length ; index++) {
			Object element = (Object) ALDDataIOManagerXmlbeans.getInstance().readData(null, (Class<?>) elementType, 
					xmlArray.getArrayValuesArray(index));
			collection.add( element);
		}
		return collection;
	}
	
	/** Returns the string representations of collection.
	 * The format is a specified for parsing.
     * @param obj   object to format
     * @return string representations of the object
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @see ALDCollectionDataIOXmlbeans#parse(Field,Class,String)
	 */
	@Override
	public ALDXMLObjectType writeData(Object obj) throws ALDDataIOManagerException, ALDDataIOProviderException {
		ALDXMLArrayType xmlArray = ALDXMLArrayType.Factory.newInstance();
		xmlArray.setClassName(obj.getClass().getName());

		boolean foundClass = false;
		Collection<?> collection = (Collection<?>)obj;
		if ( collection.size() > 0 ) {

			int index = 0;
			for ( Object element : collection) {
				if ( ! foundClass && element != null) {
					xmlArray.setElementClassName(element.getClass().getName());
				}
				
				ALDXMLObjectType xmlElement = ALDDataIOManagerXmlbeans.getInstance().writeData(element);
				xmlArray.insertNewArrayValues(index);
				xmlArray.setArrayValuesArray(index, xmlElement);
				++index;
			}
		} else {
			xmlArray.setElementClassName("");
		}
		return xmlArray;
	}
}
