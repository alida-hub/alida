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

package de.unihalle.informatik.Alida.dataio.provider;

import java.lang.reflect.Field;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.xmlbeans.XmlObject;

import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;
import de.unihalle.informatik.Alida_xml.ALDXMLObjectType;


/**
 * This is the interface for Alida's dataIO provider from xml using xmlbeans.
 * All classes providing dataIO for xml have to implement this interface
 * and need to be annotated with {@link de.unihalle.informatik.Alida.annotations.ALDDataIOProvider}.
 * 
 * @author posch
 *
 */
public interface ALDDataIOXmlbeans extends ALDDataIO {
	/**
	 * Interface for reading data from the given source.
     * This interface work in conjunction with {@link de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans}
     * where the method is documented in more detail.
	 * 
	 * @param field Field of object to be returned.
	 * @param cl Class of object to be returned.
	 * @param aldXmlObject	Source from where to read data.
	 * @return	Object read from source.
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 *
	 */
	public abstract Object readData(Field field, Class<?> cl, ALDXMLObjectType aldXmlObject) 
			throws ALDDataIOProviderException, ALDDataIOManagerException;
	
	/**
	 * Interface for writing the object data to the target location.
     * This interface work in conjunction with {@link de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans}
     * where the method is documented in more detail.
	 * 
	 * @param obj	Object to be saved.
	 * @return    an ALDXMLObjectType representing the <code>obj</code>
	 * @throws ALDDataIOManagerException
	 * @throws ALDDataIOProviderException
	 */
	public abstract ALDXMLObjectType writeData(Object obj) 
			throws ALDDataIOManagerException, ALDDataIOProviderException;


}
