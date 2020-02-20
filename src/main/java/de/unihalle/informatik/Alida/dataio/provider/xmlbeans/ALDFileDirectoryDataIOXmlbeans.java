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
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOXmlbeans;
import de.unihalle.informatik.Alida.datatypes.ALDDirectoryString;
import de.unihalle.informatik.Alida.datatypes.ALDFileString;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida_xml.ALDXMLAnyType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;

import java.lang.reflect.Field;
import java.util.*;

import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.XmlString;

/**
 * DataIO provider for filenames and directory paths xml using xmlbeans.
 *
 * @author moeller
 *
 */
@ALDDataIOProvider
public class ALDFileDirectoryDataIOXmlbeans implements ALDDataIOXmlbeans {

	@Override
	public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();
		classes.add(ALDFileString.class);
		classes.add(ALDDirectoryString.class);
		return classes;
	}
	
	@Override
	public Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject) 
			throws ALDDataIOProviderException {
		if ( aldXmlObject == null || aldXmlObject.isNil()) 
			return null;

		if (cl.equals(ALDFileString.class) && 
				aldXmlObject.getClassName().equals(ALDFileString.class.getName())) {
			return new ALDFileString(((XmlString)((ALDXMLAnyType)aldXmlObject).getValue()).getStringValue());
			
		} else if (cl.equals(ALDDirectoryString.class) && 
				((ALDXMLAnyType)aldXmlObject).getClassName().equals(ALDDirectoryString.class.getName())) {
			return new ALDDirectoryString(((XmlString)((ALDXMLAnyType)aldXmlObject).getValue()).getStringValue());
		} else {
			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR, 
					"ALDFileDirectoryDataIOXmlbeans::readData cannot read object of type " +
							cl.getCanonicalName() + ">" +
							" from <" + aldXmlObject.toString() + ">\n");
		}
	}

	@Override
	public ALDXMLObjectType writeData(Object obj) {
		String str;
		if (obj instanceof ALDFileString)
			str = ((ALDFileString)obj).getFileName();
		else
			str = ((ALDDirectoryString)obj).getDirectoryName();
		
		XmlString xmlString = XmlString.Factory.newInstance();
		xmlString.setStringValue(str);
		
		ALDXMLAnyType aldXmlObject = ALDXMLAnyType.Factory.newInstance();
		aldXmlObject.setClassName(obj.getClass().getName());
		aldXmlObject.setValue(xmlString);
		
		return aldXmlObject;
	}
}
