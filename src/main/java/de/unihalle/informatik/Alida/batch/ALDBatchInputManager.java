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
 * $Rev: 4370 $
 * $Date: 2011/11/20 20:18:51 $
 * $Author: posch $
 * 
 */

package de.unihalle.informatik.Alida.batch;

import de.unihalle.informatik.Alida.annotations.ALDBatchInputProvider;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.batch.provider.input.ALDBatchInputIterator;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDBatchIOManagerException.ALDBatchIOManagerExceptionType;

import java.util.HashMap;
import java.util.Collection;
import java.lang.reflect.Method;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Class to manage data inputs in batch processing with operators.
 * 
 * @author moeller
 */
public abstract class ALDBatchInputManager {

	/** 
	 * For internal debugging purposes only.
	 */
	protected static boolean debug = false;

	/**
	 * Name of the method which returns all classes supported.
	 */
	private static String providedInputClassesMethodName = "providedClasses";
	
	/**
	 * Hashtable containing mappings of datatypes to provider classes.
	 */
	protected HashMap<Class<?>, String> mapTable = null;

	/** 
	 * Default constructor. 
	 */
	protected ALDBatchInputManager() {
		 //nothing to do here to be overridden in extending classes
	}
	
	/**
	 * Method to initialize the hashmap which registers batch input providers.
	 * @param interfaceRequired Interface of which providers are registered.
	 */
	protected static HashMap<Class<?>, String> initMapTable( 
																								Class<?> interfaceRequired ) {
		HashMap<Class<?>, String> mapTable = new HashMap<Class<?>, String>();

		// this a parallel map to mapTable, which represents the priority of the 
		// provider class as defined by the annotation of the batch provider;
		// for use only during initialization of the mapTable
		HashMap<Class<?>, Integer> priorityMap = new HashMap<Class<?>, Integer>();
		
		Index<ALDBatchInputProvider,ALDBatchInputIterator> indexItems = 
			SezPozAdapter.load(ALDBatchInputProvider.class,
													ALDBatchInputIterator.class);
		for (final IndexItem<ALDBatchInputProvider,ALDBatchInputIterator> item : 
																																indexItems ) {

			// class name of provider
			String className = item.className();

			// and its priority
			int priority = item.annotation().priority();

			if ( debug ) 
				System.out.println( "found:  " + className);

			try {
				if (!interfaceRequired.isAssignableFrom( Class.forName( className))) {
					if ( debug ) 
						System.out.println( "    is not assignable from " + 
																													interfaceRequired);
				} 
				else {
					Class<?> params[] = {};
					Method method = 
							Class.forName(className).getDeclaredMethod(
																		providedInputClassesMethodName, params);

					ALDBatchInputIterator provider = 
							(ALDBatchInputIterator)(Class.forName( className).newInstance());
					Object paramsObj[] = {};
					@SuppressWarnings("unchecked")
					Collection<Class<?>> supportedClasses = 
							(Collection<Class<?>>)(method.invoke( provider, paramsObj));

					for ( Class<?> supportedClass : supportedClasses ) {
	
						if (  !(mapTable.containsKey( supportedClass)) 
								||  priority > priorityMap.get( supportedClass).intValue() ) {

							if ( debug )
								System.out.println( "    supported class (priority = " + 
																priority + "):"  +  supportedClass.getName());

							mapTable.put( supportedClass, className);
							priorityMap.put( supportedClass, new Integer(priority));
						}
					}
				}
			} catch (Exception e) {
				System.err.println( 
					"ALDBatchInputManager::initMapTable cannot create an instance for " 
																																	+ className);
			}
		}

		return mapTable;
	}
	
	/**
	 * Check if a provider has registered for the given class.
	 * <p>
	 * Note that this function does not try to instantiate a provider, thus,
	 * just a flat check is done, and on lateron requesting a provider object 
	 * errors are still possible.
	 * @param cl	Requested class.
	 * @return	True, if a provider has registered.
	 */
	public boolean providerAvailable(Class<?> cl) {
		if (this.mapTable.get(cl) != null) {
			return true;
		}
		return false;
	}
	
	/**
	 * Method to return an instance of the batch input provider for given class.
	 * <p>
	 * A provider is returned which implements the requested interface 
	 * <code>interfaceRequired</code> (which is by itself required to 
	 * implement ALDBatchInputIterator).
	 *
	 * @param	 cl									Class to get a provider for.
	 * @param	 interfaceRequired	Interface needed to be implemented by provider.
	 * @return	Provider instance.
	 * @throws ALDDataIOManagerException 
	 */
	public ALDBatchInputIterator getProvider( Class<?> cl, 
																							Class<?> interfaceRequired) 
			throws ALDBatchIOManagerException {
		// first look up a provider name
		String providerName;
		providerName = this.mapTable.get(cl);
//		if ( providerName == null) {
//			// try other means to find a provider
//			// enum or ALDOperator or Collection or ALDParametrizedClass
//			if ( ! ( ( cl.getEnumConstants() != null && 
//						(providerName = this.mapTable.get( Enum.class)) != null ) ||
//					 ( ALDOperator.class.isAssignableFrom( cl) &&
//						(providerName = this.mapTable.get( ALDOperator.class)) != null ) ||
//					 ( Collection.class.isAssignableFrom( cl) &&
//						(providerName = this.mapTable.get( Collection.class)) != null ) ||
//					 ( cl.getAnnotation( ALDParametrizedClass.class) != null &&
//						(providerName = this.mapTable.get( ALDParametrizedClassDummy.class)) != null ) ) ) {
//				// nothing found
//				throw new ALDDataIOManagerException(
//						ALDDataIOManagerExceptionType.NO_PROVIDER_FOUND,
//						"ALDDataIOManager: no provider found for class " + cl.getName());
//			}
//		}

		if (providerName == null) {
			throw new ALDBatchIOManagerException(
				ALDBatchIOManagerExceptionType.NO_PROVIDER_FOUND, 
				"ALDBatchInputManager: No provider for class " 
																								+ cl.toString() + " found!");
		}
		
		// try to intantiate an instance of the provider
		try {
			Class<?> providerClass = Class.forName( providerName);
			Object providerObj = providerClass.newInstance();

			// check if provider is of correct type
			if ( ! interfaceRequired.isAssignableFrom( providerClass ) ) {
				throw new ALDBatchIOManagerException(
					ALDBatchIOManagerExceptionType.PROVIDER_INTERFACE_ERROR, 
					"ALDBatchInputManager: Provider class does not implement " + 
													interfaceRequired.getName() + " , invalid class!");
			}
			return (ALDBatchInputIterator)providerObj;
		} catch (ClassNotFoundException e) {
			throw new ALDBatchIOManagerException(
					ALDBatchIOManagerExceptionType.PROVIDER_INSTANTIATION_ERROR, 
					"ALDBatchInputManager: " + 
							"Provider class not found, instantiation failed!");
		} catch (InstantiationException e) {
			throw new ALDBatchIOManagerException(
					ALDBatchIOManagerExceptionType.PROVIDER_INSTANTIATION_ERROR, 
					"ALDBatchInputManager: Provider instantiation failed!");
		} catch (IllegalAccessException e) {
			throw new ALDBatchIOManagerException(
					ALDBatchIOManagerExceptionType.UNSPECIFIED_ERROR, 
					"ALDBatchInputManager: Illegal access noticed!");
		}
	}
}
