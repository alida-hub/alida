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

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.provider.ALDDataIOCmdline;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOProviderException.ALDDataIOProviderExceptionType;
import de.unihalle.informatik.Alida.helpers.ALDClassInfo;
import de.unihalle.informatik.Alida.helpers.ALDParser;
import de.unihalle.informatik.Alida.operator.ALDData;
import de.unihalle.informatik.Alida.operator.ALDOperator;

import java.lang.reflect.Field;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Abstract class providing basic methods for cmdline DataIO providers
 * according to Alida conventions.
 *<p>
 * These conventions are detailed in the documentation if the methods <code>readData</code> and <code>writeData</code> in this class below.
 * They handle IO from/to file and reading derived classes of the class handled by an dataIO provider.
 *<p>
 * Classes extending this class are expected to override the methods <code>parse</code> and
 * <code>formatAsString</code> which do the actual reading/parsing or writing/formating
 * subsequent to generic handling of Alida convention with respect to derived classes
 * and IO form/to file.
 *
 * @author posch
 *
 */
public abstract class ALDStandardizedDataIOCmdline implements ALDDataIOCmdline {

	/** As a convention a parameter value starting with this character indicates
	 * that the actual parameter should be read from or writen to a file.
	 * The filename is the remaining string after removing this character.
	 */
	public static final char FILEIO_CHAR = '@';

	/** As a convention this character starts a derived class name,
	 *  and is terminated by ':'
	 */
	public static final char DERIVEDCLASS_CHAR = '$';

	/**
	 * debugging output
	 */
	private boolean debug = false;


	/** Returns an object instantiated from valueString.
	 * For the class of the object to be read see {@link ALDDataIOManagerCmdline#readData(Field,Class,String)}.
	 * This method is assumed to directly parse the <code>valueString</code> and make no
	 * prior interpretation regarding a file to use or derived class to return.
	 *
	 * @param	field Field of object to be returned
	 * @param cl Class of object to be returned.
	 * @param valueString   Source from where to read data (e.g. a filename).
	 * @return Object read from valueString
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 *
	 *  @see de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline
	 */
	abstract public Object parse( Field field, Class<?> cl, String valueString) 
			throws ALDDataIOProviderException, ALDDataIOManagerException;

	/** Returns the string representations of this object
	 * This method is assumed to directly format the <code>obj</code> into the string return and make no
	 * prior interpretation regarding a file to be used.
	 *
	 * @param obj	object to format
	 * @return string representations of the object
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 */
	abstract public String formatAsString( Object obj) 
			throws ALDDataIOManagerException, ALDDataIOProviderException;

	/** Returns the external representations of this object using a formatString.
	 * The <code>formatString</code> may be used define parts of the object to be formated
	 * or specify format charateristics.
	 * However, <code>formatString</code> should not interpreted in order to decide to
	 * output the object directly to a file (as this is handled by <code>writeData</code>).
	 *<p>
	 *  The default implementation is equivalent to <code>formatAsString(obj)</code>.
	 * @param obj	object to format
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 */
	public String formatAsString( Object obj, String formatString) throws ALDDataIOManagerException, ALDDataIOProviderException {
		return formatAsString( obj);
	}
	
	/**
	 * Method to indicate if provider relies on newlines in format string.
	 * <p>
	 * This method is called by {@link #getValueStringFromFile(String)} upon 
	 * reading an input file to figure out if a derived provider requires 
	 * newlines in the file to be preserved in the returned format string. 
	 * If a provider requires newlines this methods needs to be overwritten and 
	 * 'true' be returned.
	 * 
	 * @return If true newlines are preserved when reading input files.
	 */
	protected boolean requiresNewlines() {
		return false;
	}

	/** Read an object using <code>valueString</code>.
	 * For the class of the object to be read see {@link ALDDataIOCmdline}.
	 * <p>
	 * If <code>valueString</code> starts with <code>FILEIO_CHAR</code> the value is read from a file
	 * where the name of this file is the remaining string of <code>valueString</code>.
	 * subsequent to <code>FILEIO_CHAR</code>.
	 * <p>
	 * The next step in interpreting the value string is scrutinze
	 * whether an instance of a class deriving the class defined in <code>field</code>  or 
	 * the class <code>cl</code> is to be returned.
	 * This is indicated by starting the value string (either passed directly via <code>valueString</code>
	 * or the string read from file) with <code>DERIVEDCLASS_CHAR</code>.
	 * by <code>field</code> 
	 * If this is the case all charaters up, but excluding, the next colon are
	 * interpreted as this fully qualified class name.
	 * Next it is checked if this class indeed is a proper extension
	 * which in turn required this class to be annotated with {@link de.unihalle.informatik.Alida.annotations.ALDDerivedClass}.
	 * If this check passes
	 * a dataIO provider of this class is looked up and its <code>readData</code>
	 * invoked with the value string after the deliminating colon.
	 * <p>
	 * If no deriving class is requested, the parse method of the 
	 * class defined in <code>field</code>  or 
	 * the class <code>cl</code> is invoked on the value string.
	 *
	 * @param field Field of object to be returned.
	 * @param cl Class of object to be returned.
	 * @param valueString   Source from where to read data (e.g. a filename).
	 * @return      Object read from source.
	 * @throws ALDDataIOProviderException 
	 * @throws ALDDataIOManagerException 
	 *
	 */

	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIO#writeData(java.lang.Object, java.lang.String)
	 */
	@Override
	public Object readData(Field field, Class<?> cl, String valueString) 
			throws ALDDataIOProviderException, ALDDataIOManagerException {
		if ( debug ) {
			System.out.println( "ALDStandardizedDataIOCmdline::readData for class <" + cl.getCanonicalName() +
					"> with valueString <" + valueString + ">");
		}
		if ( field != null )
			cl = field.getType();

		// potentially read the value string from file and remember filename
		String filename = null;
		valueString = valueString.trim();
		if ( valueString.length() > 0 && valueString.charAt(0) == FILEIO_CHAR ) {
			filename = valueString.substring( 1);
			valueString = getValueStringFromFile( filename);
		}
		
		Object obj;
		
		valueString = valueString.trim();
		if ( valueString.length() == 0 || valueString.charAt(0) != DERIVEDCLASS_CHAR ) {
			// not a derived class, parse the string 
			obj = parse( field, cl, valueString);
		} else {
			// handle derived classes

			int indexOfSep = valueString.indexOf( ':', 1);
			if ( indexOfSep == -1 ) {
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.SYNTAX_ERROR,
						"ALDStandardizedDataIOCmdline::readData derived class without <:> in " + valueString);
			}

			String className = valueString.substring( 1, indexOfSep);
			valueString = valueString.substring( indexOfSep+1);
			LinkedList<Class> derivedClassesFound = new LinkedList<Class>();

			Collection<Class> derivedClasses = ALDClassInfo.lookupExtendingClasses( cl);
			for ( Class derivedClass : derivedClasses ) {
				if ( className.equals( derivedClass.getName()) ) {
					derivedClassesFound.clear();
					derivedClassesFound.add( derivedClass);
					break;
				} else if ( derivedClass.getName().endsWith( className) ) {
					derivedClassesFound.add( derivedClass);
				}
			}

			if ( derivedClassesFound.size() != 1 ) {
				StringBuffer msg = new StringBuffer("ALDStandardizedDataIOCmdline::readData found " +
						derivedClassesFound.size() + " derived classes matching <" + className + ">");
				
				msg.append( "      derived classes available:");
				for ( Class derivedClass : derivedClasses ) {
					msg.append( "\t" + derivedClass.getName());
				}
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.OBJECT_TYPE_ERROR,
						new String( msg));
			}
		
			ALDDataIOCmdline provider = null;
			if ( debug ) {
				System.out.println("ALDStandardizedDataIOCmdline::readData trying to find a provider for class <" +
						derivedClassesFound.peek() + ">");
			}
			provider = (ALDDataIOCmdline)(ALDDataIOManagerCmdline.getInstance().getProvider( derivedClassesFound.peek(), ALDDataIOCmdline.class));
			obj = provider.readData( null, derivedClassesFound.peek(), valueString);
		}
		
		// optionally read history
		if ( filename != null &&
				ALDDataIOManagerCmdline.getInstance().isDoHistory()) {
			ALDOperator.readHistory(obj, filename);
			if ( obj instanceof ALDData ) {
				((ALDData)obj).setLocation(filename);
			}
		}

		return obj;
	}

	/**
	 * Return a string representation of given object value or print it to a file.
	 * <p>
	 * If the given <code>formatString</code> starts with <code>FILEIO_CHAR</code> standard out is used as
	 * target location, otherwise the string remaining subsequent to <code>FILEIO_CHAR</code> is interpreted as file name.
	 * In turn, if the remaining string starts with <code>+</code> the output is apended to the file (with <code>+</code> 
	 * removed as filename).
	 * @throws ALDDataIOManagerException 
	 * @throws ALDDataIOProviderException 
	 */
	/* (non-Javadoc)
	 * @see de.unihalle.informatik.Alida.helpers.ALDDataIO#writeData(java.lang.Object, java.lang.String)
	 */
	@Override
	public String writeData(Object obj, String formatString) 
			throws ALDDataIOManagerException, ALDDataIOProviderException {
		// writer object
		BufferedWriter bufWriter = null;


		if ( formatString.length() > 0 && formatString.charAt( 0) == FILEIO_CHAR ) {
			boolean append = false;
			String filename = null;
			try {
				if ( formatString.length() > 1 && formatString.charAt( 1) == '+' ) {
					append = true;
					filename = formatString.substring(2);
				} else {
					filename = formatString.substring(1);
				}

				bufWriter = new BufferedWriter(new FileWriter( filename, append));
			} catch (IOException e) {
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.FILE_IO_ERROR,
						"ALDStandardizedDataIOCmdline::writeData cannot open writer for filename <" +
								filename + ">" + (append ? " in append mode" : ""));
				/* should we try to write to stdout ??
				bufWriter = new BufferedWriter(new PrintWriter(System.out));
				try {
					bufWriter.write(this.formatAsString( obj) + "\n");
					bufWriter.flush();
					bufWriter.close();
				} catch (IOException e2) {
					System.err.println("ALDEnumDataIOCmdline: printing to standard out failed");
					return null;
				}
				*/
				
			
			}
			
			try {
				bufWriter.write(this.formatAsString( obj) + "\n");
				bufWriter.flush();
				bufWriter.close();
			} catch (IOException e1) {
				throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.FILE_IO_ERROR,
						"ALDStandardizedDataIOCmdline::writeData error writing to file <" +
								filename + ">");
			}

			
			if ( ALDDataIOManagerCmdline.getInstance().isDoHistory()) {
				try {
					ALDOperator.writeHistory(obj, filename);
				} catch (ALDProcessingDAGException e) {
					// ignore this error
				} catch (ALDOperatorException e) {
					// ignore this error
				}
			}

			return null;
		} else {
			if ( formatString.length() > 0 && 
					ALDParser.brackets.get( formatString.charAt(0)) != null) {

				return this.formatAsString( obj, formatString);
			} else {
				return this.formatAsString( obj);
			}
		}
	}

	/** 
	 * The complete content of the given file is returned as one string.
	 *
	 * @param filename	Input file to be read.
	 * @return Contents of the file as a single string.
	 * @throws ALDDataIOProviderException Thrown in case of failure.
	 */
	private String getValueStringFromFile( String filename) 
			throws ALDDataIOProviderException {
		try {
			BufferedReader reader = new BufferedReader( new FileReader(filename));
			StringBuffer buf = new StringBuffer();
			String line;
			while ( (line = reader.readLine()) != null ) {
				buf.append( line );
				// if provider relies on newlines, add a newline at end of each line
				if (this.requiresNewlines())
					buf.append( "\n" );
			}
			reader.close();
			return( new String( buf));
		} catch (FileNotFoundException e) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.FILE_IO_ERROR,
					"ALDStandardizedDataIOCmdline::getValueStringFromFile cannot open reader for file <" +
							filename + ">");
		} catch (IOException e) {
			throw new ALDDataIOProviderException( ALDDataIOProviderExceptionType.FILE_IO_ERROR,
					"ALDStandardizedDataIOCmdline::getValueStringFromFile cannot read line from <" +
							filename + ">");
		}
	}
}

