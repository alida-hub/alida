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
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import de.unihalle.informatik.Alida.dataio.provider.cmdline.ALDStandardizedDataIOCmdline;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDCollectionDataIOHelper;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida_xml.ALDXMLArrayType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;

import java.lang.reflect.Type;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;


/**
 * DataIO provider for EnumSets xml using xmlbeans.
 * 
 * @author posch
 *
 */
@ALDDataIOProvider
public class ALDEnumSetDataIOXmlbeans extends ALDStandardizedDataIOXmlbeans {

	/** output debugging info?
	 */
	private boolean debug = false;

	@Override
    public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( EnumSet.class);

		return classes;
	}
	
	/** Returns a EnumSet instantiated from <code>aldXmlObject</code>.
     * 
     * @param   field Field of object to be returned
     * @param cl Class of object to be returned.
     * @param obj this object is used to read the data into (must be of correct type)
     * @return EnumSet read from valueString.
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
						"ALDEnumSetDataIOXmlbeans::readData cannot get class for <" +
								xmlArray.getElementClassName() + ">");
			}
		} else {
			elementType = ALDCollectionDataIOHelper.lookupType( field);
		}
		
		if ( debug ) 
			System.out.println( "ALDEnumSetDataIOCmdline::parse " + elementType);

		int length = xmlArray.getArrayValuesArray().length;

		EnumSet enumset;
		if ( obj == null)
			try {
				enumset = EnumSet.noneOf((Class<Enum>)elementType);
			} catch (Exception e) {
	        	throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR,
	        			"ALDEnumSetDataIOXmlbeans::readData cannot instantiate enumset of class <" +
	        					cl.getCanonicalName() + ">");
			}
		else
			enumset = (EnumSet) obj;
	
		for ( int index = 0 ; index < length ; index++) {
			Object element = (Object) ALDDataIOManagerXmlbeans.getInstance().readData(null, (Class<?>) elementType, 
					xmlArray.getArrayValuesArray(index));
			enumset.add( element);
		}
		return enumset;
	}
	
	/** Returns the string representations of collection.
	 * The format is a specified for parsing.
     * @param obj   object to format
     * @return string representations of the object
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @see ALDEnumSetDataIOXmlbeans#parse(Field,Class,String)
	 */
	@Override
	public ALDXMLObjectType writeData(Object obj) throws ALDDataIOManagerException, ALDDataIOProviderException {
		ALDXMLArrayType xmlArray = ALDXMLArrayType.Factory.newInstance();
		xmlArray.setClassName(obj.getClass().getName());

		boolean foundClass = false;
		EnumSet<?> collection = (EnumSet<?>)obj;
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
