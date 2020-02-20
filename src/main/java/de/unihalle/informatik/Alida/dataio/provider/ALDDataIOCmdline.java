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
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;


/**
 * This is the interface for Alida's dataIO provider from command line.
 * All classes providing dataIO for command line have to implement this interface
 * and need to be annotated with {@link de.unihalle.informatik.Alida.annotations.ALDDataIOProvider}.
 * @author moeller
 *
 */
public interface ALDDataIOCmdline extends ALDDataIO {

	/**
	 * Interface for reading data from the given source.
     * This interface work in conjunction with {@link de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline}
     * where the method is documented in more detail.
	 * 
	 * @param field Field of object to be returned.
	 * @param cl Class of object to be returned.
	 * @param valueString	Source from where to read data (e.g. a filename).
	 * @return	Object read from source.
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 *
	 */
	public abstract Object readData(Field field, Class<?> cl, String valueString) 
			throws ALDDataIOProviderException, ALDDataIOManagerException;
	
	/**
	 * Interface for writing the object data to the target location.
     * This interface work in conjunction with {@link de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline}
     * where the method is documented in more detail.
	 * 
	 * @param obj	Object to be saved.
	 * @param locatationString	String indicated whether to return the value of where to write the value to.
	 * @return  String formated from obj
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 *
     * @see de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline
	 */
	public abstract String writeData(Object obj, String locatationString) 
			throws ALDDataIOManagerException, ALDDataIOProviderException;

}
