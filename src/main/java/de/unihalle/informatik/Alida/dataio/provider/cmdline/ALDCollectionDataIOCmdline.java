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

package de.unihalle.informatik.Alida.dataio.provider.cmdline;

import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDCollectionDataIOHelper;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDParser;

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
 * DataIO provider for collections from command line.
 * As this provider extends {@link ALDStandardizedDataIOCmdline} it
 * implements the Alida syntax conventions.
 * 
 * @author posch
 *
 */
@ALDDataIOProvider
public class ALDCollectionDataIOCmdline extends ALDStandardizedDataIOCmdline {

	/** output debugging info?
	 */
	private boolean debug = false;

	@Override
    public Collection<Class<?>> providedClasses() {
		LinkedList<Class<?>> classes = new LinkedList<Class<?>>();

		classes.add( Collection.class);

		return classes;
	}
	
	/** Returns a collection instantiated from <code>valueString</code>.
     * <code>valueString</code> is assume to contain a comma separated list of nested <code>valueString</code>
     * for each element of the collection enclosed in matching square brackets,
	 * e.g. <code>[1.2 , 2.2 , 3.1]</code>.
	 * The class of the elements of the collection is determined 
	 * using {@link de.unihalle.informatik.Alida.dataio.provider.helpers.ALDCollectionDataIOHelper#lookupType}.
     * 
     * @param   field Field of object to be returned
     * @param cl Class of object to be returned.
     * @param valueString   Source from where to read data (e.g. a filename).
     * @return Collection read from valueString.
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 */

	@Override
  	public Object parse(Field field, Class<?> cl, String valueString) 
  			throws ALDDataIOProviderException, ALDDataIOManagerException {
		Type elementType = ALDCollectionDataIOHelper.lookupType( field);
		if ( debug ) 
			System.out.println( "ALDCollectionDataIOCmdline::parse " + elementType);

		// parse the value string
		valueString = valueString.trim();
		if ( valueString.length() < 2 || valueString.charAt(0) != '[' || 
				valueString.charAt(valueString.length()-1) != ']' ) {

			throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.SYNTAX_ERROR, 
					"ALDCollectionDataIOCmdline::parse no enclosing square brackets in <" + 
							valueString + ">");
		}

        Collection res = null;
        try {
        	res = (Collection)cl.newInstance();
        } catch (Exception e) {
        	throw new ALDDataIOProviderException(ALDDataIOProviderExceptionType.OBJECT_INSTANTIATION_ERROR,
        			"ALDCollectionDataIOCmdline::parse cannot instantiate collection of class <" +
        					cl.getCanonicalName() + ">");
        }
		
        Object value = null;
        for ( String elementValueString : ALDParser.split( valueString.substring( 1, valueString.length()-1), ',') ) {
			if ( debug ) 
            	System.out.println("ALDCollectionDataIOCmdline::parse one element from " + elementValueString);
			
			try {
				value = ALDDataIOManagerCmdline.getInstance().readData( null, 
						(Class)elementType, elementValueString);
			} catch (ALDDataIOManagerException e) {
				throw new ALDDataIOManagerException( e.getType(), 
						"ALDCollectionDataIOCmdline::parse cannot read element of class <" +
								((Class)elementType).getCanonicalName() + "> from <" + 
								elementValueString + ">" + 
								"\nwithin <" + valueString + ">" + e.getCommentString());
			} catch (ALDDataIOProviderException e) {
				throw new ALDDataIOProviderException( e.getType(), 
						"ALDCollectionDataIOCmdline::parse cannot read element of class <" +
								((Class)elementType).getCanonicalName() + "> from <" + 
								elementValueString + ">" + 
								"\nwithin <" + valueString + ">" + e.getCommentString());
			}
            res.add( value);
        }

		return res;
	}
	
	/** Returns the string representations of collection.
	 * The format is a specified for parsing.
     * @param obj   object to format
     * @return string representations of the object
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 * @see ALDCollectionDataIOCmdline#parse(Field,Class,String)
	 */
	@Override
	public String formatAsString(Object obj) throws ALDDataIOManagerException, ALDDataIOProviderException {
		StringBuffer strbuf = new StringBuffer("[ ");

		int i = 0;
		for ( Object element : (Collection<?>)obj) {
			try {
				strbuf.append( ALDDataIOManagerCmdline.getInstance().writeData( element, "-"));
			} catch (ALDDataIOManagerException e) {
				throw new ALDDataIOManagerException( e.getType(), 
						"ALDCollectionDataIOCmdline::formatAsString cannot write " + i + 
						"-th element of class <" +
								obj.getClass().getCanonicalName() + "> to -\n");
			}
			if ( i != ((Collection<?>)obj).size()-1 )
				strbuf.append( " , ");
			i++;
		}
		strbuf.append( " ]");

		return new String( strbuf);
	}
}
