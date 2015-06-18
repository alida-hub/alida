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

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;

import java.lang.reflect.Field;

/**
 * This class implements a DataIO manager for reading/writing from command line.
 * For reading and writing, it essentially looks up the correct provider for command line execution 
 * using the method of its super class and invokes its method.
 * <p>
 * It does its work in collaboration with {@link de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline}.
 * 
 * @author posch
 *
 */

public class ALDDataIOManagerCmdline extends ALDDataIOManager {
	
	/**
	 * If true writeData should try to write the history to file
	 * if the object itself is written to a file
	 */
	private boolean doHistory = false;

	/** The singleton instance of this class
	 */
	static final ALDDataIOManagerCmdline instance;

	static {
		instance = new ALDDataIOManagerCmdline();
	}

	/** private constructor 
	 */
	private ALDDataIOManagerCmdline() {
		this.mapTable = initMapTable(ALDDataIOCmdline.class);
	}

	/** Return the single instance of this class
	 * @return single instance
	 */
	public static ALDDataIOManagerCmdline getInstance() {
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
	 * The <code>valueString</code> is used to actualy read the data. The interpretation is
	 * specific to the class to be read and defined by the corresponding provider class.
	 * As a convention, if <code>valueString</code> starts with <code>FILEIO_CHAR</code>  
     * (see {@link de.unihalle.informatik.Alida.dataio.provider.cmdline.ALDStandardizedDataIOCmdline})  data are to
	 * be read from file, where the remaining value of <code>valueString</code> defines the filename.
	 * Otherwise <code>valueString</code> is directly parsed for the value.
	 * 
	 * @param field	field of object to be returned.
	 * @param cl	Class of data to be read.
	 * @param valueString	Source or value string to read data.
	 * @return	Read data object
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 */
	public Object readData(Field field, Class<?> cl, String valueString) 
			throws ALDDataIOManagerException, ALDDataIOProviderException {
		if ( field != null )
			cl = field.getType();
		if ( cl == null ) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"ALDDataIOManagerCmdline::readData cannot read object if class equals null");
		}
      	ALDDataIOCmdline provider = (ALDDataIOCmdline)getProvider( cl, ALDDataIOCmdline.class);
      	return provider.readData( field, cl, valueString);
	}
	
	/**
	 * Writes data to the specified location.
     * This method may return a String with a representation of parameters value
     * or may choose to write the value itself, e.g. to a file.
     * The latter will by convention be true, if <code>locatationString</code> starts with <code>cmdlineFILEIO_CHAR</code>  
     * (see {@link de.unihalle.informatik.Alida.dataio.provider.cmdline}).
	 * 
	 * @param obj	Object to write.
	 * @param locatationString	String indicated whether to return the value of where to write the value to.
	 * @return  String formated from obj
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 */
	public String writeData(Object obj, String locatationString) 
			throws ALDDataIOManagerException, ALDDataIOProviderException {
		if ( obj == null ) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
					"ALDDataIOManagerCmdline::writeData cannot write object if equals to null");
		}

		Class cl = obj.getClass();
      	ALDDataIOCmdline provider = (ALDDataIOCmdline)getProvider( cl, ALDDataIOCmdline.class);
      	return provider.writeData(obj, locatationString);
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
}
