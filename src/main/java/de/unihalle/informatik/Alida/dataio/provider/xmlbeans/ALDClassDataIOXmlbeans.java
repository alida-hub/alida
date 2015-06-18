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
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida_xml.ALDXMLAnyType;
import de.unihalle.informatik.Alida_xml.ALDXMLEnumType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.xmlbeans.XmlString;

/**
 * DataIO provider for enumerations xml using xmlbeans.
 * As this provider extends {@link ALDStandardizedDataIOXmlbeans} it
 * implements the Alida syntax conventions.
 * 
 * @author posch
 *
 */
@ALDDataIOProvider
public class ALDClassDataIOXmlbeans extends ALDStandardizedDataIOXmlbeans {

	@Override
    public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( Class.class);

		return classes;
	}
	
	@Override
  	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject, Object obj) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		if ( aldXmlObject == null || aldXmlObject.isNil()) 
			return null;

		// TODO: probably we should check if aldXmlObject holds an object of the correct type before casting

		@SuppressWarnings("rawtypes")
		Class myClass = null;
		
		String className = null;
		try {
			className = ((XmlString)((ALDXMLAnyType)aldXmlObject).getValue()).getStringValue();

			myClass = Class.forName( className);
		} catch (ClassNotFoundException e) {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"ALDClassDataIOXmlbeans::readData cannot load class object for <" + className + ">");
		}
		return myClass;
	}
	
	@Override
  	public ALDXMLObjectType writeData(Object obj ) {
		@SuppressWarnings("rawtypes")
		Class myClass = (Class)obj;
		
		XmlString xmlString = XmlString.Factory.newInstance();
		xmlString.setStringValue(myClass.getName());
		
		ALDXMLAnyType aldXmlObject = ALDXMLAnyType.Factory.newInstance();
		aldXmlObject.setClassName( Class.class.getName());
		aldXmlObject.setValue( xmlString);
		
		
		return aldXmlObject;
	}
}
