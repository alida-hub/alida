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

package de.unihalle.informatik.Alida.dataio.provider.cmdline;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;

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
 * DataIO provider for enumerations from command line.
 * As this provider extends {@link ALDStandardizedDataIOCmdline} it
 * implements the Alida syntax conventions.
 * 
 * @author posch
 *
 */
@ALDDataIOProvider
public class ALDEnumDataIOCmdline extends ALDStandardizedDataIOCmdline {

	@Override
    public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( Enum.class);

		return classes;
	}
	
	@Override
  	public Object parse(Field field, Class<?> cl, String valueString) 
  			throws ALDDataIOProviderException {
		Object[]	consts = cl.getEnumConstants();
		for ( Object c : consts ) {
			if ( valueString.equals( c.toString()) ) {
				return c;
			}
		}

		StringBuffer msg = new StringBuffer("\n   existing values:\n");
		for ( Object c : consts ) {
			msg.append( "         " + c + "\n");
		}

		throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
				"ALDEnumDataIOCmdline::parse Enum " + cl.getName() + " has no constant: " +
						valueString + ", "+
						new String( msg));

	}
	
	@Override
  	public String formatAsString(Object obj ) {
		return obj.toString();
	}
}
