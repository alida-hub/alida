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

import de.unihalle.informatik.Alida.dataio.provider.ALDDataIO;
import de.unihalle.informatik.Alida.dataio.provider.helpers.ALDParametrizedClassDummy;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException.ALDDataIOManagerExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDataIOProvider;
import de.unihalle.informatik.Alida.annotations.ALDParametrizedClass;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Vector;
import java.util.LinkedList;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.annotation.Annotation;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Class to manage data input and output in Alida.
 * This is used, e.g., in Alida's mechanism to automaticaly generate user interfaces for
 * Alida operators.
 * This abstract class provides means to find a DataIO provider object for a given class.
 * DataIO provider classes need to be annotated with {@link ALDDataIOProvider} and are expected to
 * implement an interface which is derived from the interface {@link ALDDataIO}.
 * <p>
 * Each non-abstract class extending ALDDataIOManager will typically come in pair
 * with an interface which extends ALDDataIO.
 * The class extending ALDDataIOManager defines methods to read and write data 
 * which are delegated to the corresponding corresponding classes defined in the pairing interface
 * and thus have to be defined there.
 * <p>
 * 
 * @author moeller,posch
 * @see ALDDataIO
 *
 */
public abstract class ALDDataIOManager {

	/** 
	 * For internal debugging purposes 
    */
	protected static boolean debug = false;

	/**
	 * Name of the method which returns all classes supported by a DataIO provider class.
     * This method is declared by the interface {@link ALDDataIO} and thus to be implemented
	 * by each DataIO provider class.
	 */
	private static String providedClassesMethodName = "providedClasses";
	
	/**
	 * Hashtable containing mappings of datatypes to DataIO provider classes.
	 * This is initialized using indices generated via the @ALDDataIOProvider annotation,
     * see <code>initMapTable()</code>.
     * The dataIO provider is specified with its full class name.
	 */
	protected HashMap<Class, String> mapTable = null;

	/** 
	 * Default constructor. 
	 */
	protected ALDDataIOManager() {
		 //nothing to do here to be overridden in extending classes
	}
	
	/**
	 * Method to initialize the hashmap which registers DataIO providers.
	 * Looking up DataIO providers is facilitated with annotions of type {@link ALDDataIO}, i.e.
     * a DataIO provider will only be found and registerd, if it is annotated with @ALDDataIO.
     * In addition it has to implement the interface <code>interfaceRequired</code>.
	 *
	 * @param interfaceRequired Interface which all providers registered are to implement.
	 *                          Is has to be derived from <code>ALDDataIO</code>.
	 */
	protected static HashMap<Class, String> initMapTable( Class interfaceRequired ) {
		HashMap<Class, String> mapTable = new HashMap<Class, String>();

		// this a parallel map to mapTable, which represents the priority of the provider class as define
		// by the annotation of the DataIO provider.
		// for use only during initialization of the mapTable
		HashMap<Class, Integer> priorityMap = new HashMap<Class, Integer>();
		
		Index<ALDDataIOProvider,ALDDataIO> indexItems = 
				SezPozAdapter.load(ALDDataIOProvider.class,ALDDataIO.class);
		for ( final IndexItem<ALDDataIOProvider,ALDDataIO> item : indexItems ) {

			// class name of DataIO provider
			String className = item.className();

			// and its priority
			int priority = item.annotation().priority();

			if ( debug ) 
				System.out.println( "found:  " + className);

			try {
				if ( ! interfaceRequired.isAssignableFrom( Class.forName( className)) ) {
					if ( debug ) 
						System.out.println( "    is not assignable from " + interfaceRequired);
				} else {
					Class params[] = {};
					Method method = Class.forName( className).getDeclaredMethod( providedClassesMethodName, params);

					ALDDataIO provider = (ALDDataIO)(Class.forName( className).newInstance());
					Object paramsObj[] = {};
					Collection<Class<?>> supportedClasses = (Collection<Class<?>>)(method.invoke( provider, paramsObj));

					for ( Class supportedClass : supportedClasses ) {
	
						if ( ! mapTable.containsKey( supportedClass) ||
							priority > priorityMap.get( supportedClass) ) {

							if ( debug )
								System.out.println( "    supported class (priority = " + priority + "):"  +
												 supportedClass.getName());

							mapTable.put( supportedClass, className);
							priorityMap.put( supportedClass, priority);
						}
					}
				}
			} catch (Exception e) {
				System.err.println( "ALDDataIOManager::initMapTable cannot create an instance for " + className);
			}
		}

		return mapTable;
	}
	
	/**
	 * Method to return an instance of the DataIO provider class for the class <code>cl</code>
     * which implements the requested interface <code>interfaceRequired</code> 
	 * (which has itself to implement ALDDataIO).
	 * <p>
	 * There are the following extensions if no DataIO provider for class <code>cl</code> is found.
	 * If <code>cl</code> is an enumeration class, a provider for Enum.class is returned (if found).
	 * If <code>cl</code> is an ALDOperator class, a provider for ALDOperator.class is returned (if found).
	 * If <code>cl</code> is an array, a provider for Array.class is returned (if found).
	 * If <code>cl</code> is a Collection, a provider for Collection.class is returned (if found).
	 * If <code>cl</code> is annotated as a parametrized class via {@link ALDParametrizedClass}, 
     * a provider for {@link ALDParametrizedClassDummy} is returned (if found).
	 *
	 * @param	cl	class to get an provider for
	 * @param	interfaceRequired	interface needed to be implemented by the provider
	 * @return	provider instance
	 * @throws ALDDataIOManagerException 
	 */
	public ALDDataIO getProvider( Class cl, Class interfaceRequired) 
			throws ALDDataIOManagerException {
		if ( debug) {
			System.out.println("ALDDataIOManager::getProvider find provider for <" +
					cl.getName() + ">");
		}
		
		// first look up a provider name
		String providerName;
		providerName = mapTable.get(cl);
		if ( providerName == null) {
			// try other means to find a provider
			// enum or ALDOperator or Collection or ALDParametrizedClass
			if ( ! ( ( cl.getEnumConstants() != null && 
						(providerName = mapTable.get( Enum.class)) != null ) ||
					 ( ALDOperator.class.isAssignableFrom( cl) &&
						(providerName = mapTable.get( ALDOperator.class)) != null ) ||
					 ( cl.isArray() &&
						(providerName = mapTable.get( Array.class)) != null ) ||
					 ( EnumSet.class.isAssignableFrom( cl) &&
						(providerName = mapTable.get( EnumSet.class)) != null ) ||
					 ( Collection.class.isAssignableFrom( cl) &&
						(providerName = mapTable.get( Collection.class)) != null ) ||
					 ( cl.getAnnotation( ALDParametrizedClass.class) != null &&
						(providerName = mapTable.get( ALDParametrizedClassDummy.class)) != null ) ) ) {
				// nothing found
				throw new ALDDataIOManagerException(
						ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND,
						"ALDDataIOManager::getProvider no provider found for class " + cl.getName());
			}
		}
		
		if ( debug) {
			System.out.println("ALDDataIOManager::getProvider found  <" +
					providerName + ">");
		}


		// try to instantiate an instance of the provider
		Class<?> providerClass = null;
		try {
			providerClass = Class.forName( providerName);
		} catch (ClassNotFoundException e1) {
			throw new ALDDataIOManagerException(
					ALDDataIOManagerExceptionType.UNSPECIFIED_ERROR,
					"ALDDataIOManager::getProvider cannnot find a class for annotated provider <" + 
					providerName + "> found for class " + cl.getName() +">");
		}
		
		Object providerInstance = null;
		try {
			providerInstance = providerClass.newInstance();

        } catch (Exception e) {
			throw new ALDDataIOManagerException(
					ALDDataIOManagerExceptionType.UNSPECIFIED_ERROR,
					"ALDDataIOManager::getProvider cannnot instantiate annotated provider <" + 
					providerName + "> found for class " + cl.getName() +">");
        }
		
		// check if provider is of correct type
		if ( ! interfaceRequired.isAssignableFrom( providerClass ) ) {
			throw new ALDDataIOManagerException(
					ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND,
					"ALDDataIOManager::getProvider provider found for class " + cl.getName() +
					"does not implement <" + interfaceRequired.getName() +">");

		}

		return (ALDDataIO)providerInstance;
	}

	/**
	 * Method to return a clone of the mapping of classes to dataIO providers.
	 * This method will typically used only be admin tools.
     * The dataIO provider is specified with its full class name.
	 */
	public HashMap<Class, String> getProviderMap() {
		return (HashMap<Class, String>)(mapTable.clone());
	}
}
