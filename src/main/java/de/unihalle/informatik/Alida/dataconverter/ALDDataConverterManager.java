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
 * $Rev: 6309 $
 * $Date: 2012-11-23 17:19:53 +0100 (Fr, 23 Nov 2012) $
 * $Author: moeller $
 * 
 */

package de.unihalle.informatik.Alida.dataconverter;

import de.unihalle.informatik.Alida.annotations.ALDDataConverterProvider;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDDataConverterException;
import de.unihalle.informatik.Alida.exceptions.ALDDataIOManagerException;
import de.unihalle.informatik.Alida.exceptions.ALDProviderManagerException.ALDProviderManagerExceptionType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * This class implements a provider manager for data conversion.
 * <p>
 * For data input, it essentially looks up the correct provider for GUI-based 
 * execution using the method of its super class and invokes its method.
 * <p>
 * It does its work in collaboration with 
 * {@link de.unihalle.informatik.Alida.dataconverter.ALDDataConverter}.
 * 
 * @author posch
 *
 */
public class ALDDataConverterManager {
	
	private static boolean debug = false;
	
	
	/**
	 * Hashtable containing mappings of class pairs to a collection of provider class names.
	 */
	protected HashMap<String, Collection<String>> mapTable = null;

	/** 
	 * The singleton instance of this class.
	 */
	static final ALDDataConverterManager instance;

	static {
			instance = new ALDDataConverterManager();
	}

	/** 
	 * Private constructor which initializes the provider map.
	 * @throws ALDDataConverterManagerException 
	 */
	private ALDDataConverterManager() {
		this.mapTable = initMapTable();
	}

	/** 
	 * Return the single instance of this class
	 * @return Single instance.
	 */
	public static ALDDataConverterManager getInstance() {
		return instance;
	}

    /**
     * Convert the <code>sourceObject</code> into an object of class
     * <code>targetClass</code>.
     * 
     * @param sourceObject
     * @param sourceTypes
     * @param targetClass
     * @param targetTypes
     * @return
     * @throws ALDDataConverterException
     * @throws ALDDataConverterManagerException 
     */
    public Object convert( Object sourceObject, Type[] sourceTypes, Class<?> targetClass, 
    		Type[] targetTypes) 
    		throws ALDDataConverterException, ALDDataConverterManagerException {
    	
		ALDDataConverter provider =  
				this.getProvider(sourceObject.getClass(), sourceTypes, targetClass, targetTypes);
		return provider.convert( sourceObject, sourceTypes, targetClass, targetTypes);
    }
    
    /**
     * Convert the <code>sourceObject</code> into an object of class
     * <code>targetClass</code>.
     * 
     * @param sourceObject
     * @param sourceTypes
     * @param targetClass
     * @param targetTypes
     * @return
     * @throws ALDDataConverterManagerException
     * @throws ALDDataConverterException
     */
    public Object convert( ALDDataConverter provider, Object sourceObject, Field sourceField, Class<?> targetClass, 
    		Field targetField) 
    		throws ALDDataConverterManagerException, ALDDataConverterException {
    	
		Type sourceType = sourceField.getGenericType(); 
		Type[] sourceTypes = null;
		if (sourceType instanceof ParameterizedType) {  
			ParameterizedType pt = (ParameterizedType) sourceType;  
			sourceTypes = pt.getActualTypeArguments();
		}
		
		Type[] targetTypes = null;
		Type targetType = targetField.getGenericType();  
		if (targetType instanceof ParameterizedType) {  
			ParameterizedType pt = (ParameterizedType) targetType;  
			targetTypes = pt.getActualTypeArguments();
		}
    	
		return provider.convert( sourceObject, sourceTypes, targetClass, targetTypes);
    }
    
    public Object convert( Object sourceObject, Field sourceField, Class<?> targetClass, 
    		Field targetField) 
    		throws ALDDataConverterManagerException, ALDDataConverterException {
    	
		Type sourceType = sourceField.getGenericType(); 
		Type[] sourceTypes = null;
		if (sourceType instanceof ParameterizedType) {  
			ParameterizedType pt = (ParameterizedType) sourceType;  
			sourceTypes = pt.getActualTypeArguments();
		}
		
		Type[] targetTypes = null;
		Type targetType = targetField.getGenericType();  
		if (targetType instanceof ParameterizedType) {  
			ParameterizedType pt = (ParameterizedType) targetType;  
			targetTypes = pt.getActualTypeArguments();
		}
    	
		return this.convert( sourceObject, sourceTypes, targetClass, targetTypes);
    }
   /**
     * Convert the <code>sourceObject</code> into an object of class
     * <code>targetClass</code>.
     * 
     * @param sourceObject
     * @param targetClass
     * @return converted object
     * @throws ALDDataConverterManagerException 
     * @throws ALDDataConverterException 
     */
    public Object convert( Object sourceObject, Class<?> targetClass) 
    		throws ALDDataConverterManagerException, ALDDataConverterException {
    	
		return this.convert( sourceObject, (Type[])null, targetClass, (Type[])null);
    }
    
	/**
	 * Method to return an instance of the data converter provider for given classes.
	 * <p>
	 * @param	 sourceClass	
	 * @param	 targetClass	
	 * @return	Provider instance.
	 * @throws ALDDataConverterManagerException 
	 * @throws ALDDataIOManagerException 
	 */
	public ALDDataConverter getProvider( Class<?> sourceClass, Type[] sourceTypes, 
    		Class<?> targetClass, Type[] targetTypes) throws ALDDataConverterManagerException  {
		//TODO modify to DataConverterManagerException or generic manager exception

		if ( debug ) {
			System.out.println("ALDDataConverterManager::getProvider sourceClass <" +
		sourceClass.getName() + "> targetClass <" + targetClass + ">");
		}
		// first look up a provider name
		ALDSourceTargetClassPair sourceTargetPair = new ALDSourceTargetClassPair(sourceClass, targetClass);
		String key = sourceTargetPair.toString();
		Collection<String> providerNames = mapTable.get( key);
		
		if ( debug ) {
			System.out.println("    for " + key + " found " + providerNames.size() + " providers");
			for ( String providerName : providerNames) 
				System.out.println("       provider " + providerName);
		}
		
		if ( providerNames != null ) {
			for ( String providerName : providerNames) {
				// try to instantiate an instance of the provider
				try {
					Class<?> providerClass = Class.forName( providerName);
					ALDDataConverter providerObj = (ALDDataConverter) providerClass.newInstance();

					if ( providerObj.supportConversion(sourceClass, sourceTypes, targetClass, targetTypes)) {
						if ( debug ) 
							System.out.println( "ALDDataConverterManager::getProvider found provider " +
									providerName);
						return (ALDDataConverter)providerObj;
					}
				} catch (InstantiationException e) {
					throw new ALDDataConverterManagerException(
							ALDProviderManagerExceptionType.PROVIDER_INSTANTIATION_ERROR, 
							"ALDBatchInputManager: Provider instantiation failed!");
				} catch (IllegalAccessException e) {
					throw new ALDDataConverterManagerException(
							ALDProviderManagerExceptionType.UNSPECIFIED_ERROR, 
							"ALDBatchInputManager: Illegal access noticed!");
				} catch (ClassNotFoundException e) {
					throw new ALDDataConverterManagerException(
							ALDProviderManagerExceptionType.PROVIDER_INSTANTIATION_ERROR, 
							"ALDBatchInputManager: " + 
							"Provider class not found, instantiation failed!");
				}

			}
		}
		
		// no provider found or none of the provides can handle
		throw new ALDDataConverterManagerException(
				ALDProviderManagerExceptionType.NO_PROVIDER_FOUND, 
				"ALDDataConverterManager: No provider for class " + 
						sourceClass + " --> " + targetClass + " found!");
		
	}
	
	public ALDDataConverter getProvider(Class<?> sourceClass, Field sourceField,
			Class<?> targetClass, Field targetField) throws ALDDataConverterManagerException {
		Type sourceType = sourceField.getGenericType(); 
		Type[] sourceTypes = null;
		if (sourceType instanceof ParameterizedType) {  
			ParameterizedType pt = (ParameterizedType) sourceType;  
			sourceTypes = pt.getActualTypeArguments();
		}
		
		Type[] targetTypes = null;
		Type targetType = targetField.getGenericType();  
		if (targetType instanceof ParameterizedType) {  
			ParameterizedType pt = (ParameterizedType) targetType;  
			targetTypes = pt.getActualTypeArguments();
		}
		return this.getProvider(sourceClass, sourceTypes, targetClass, targetTypes);
	}

	/**
	 * Method to initialize the hashmap which registers data conversion providers.
	 * @throws ALDDataConverterManagerException 
	 */
	@SuppressWarnings("unchecked")
	protected static HashMap<String, Collection<String>> initMapTable()  {
		if ( debug ) {
			System.out.println("ALDDataConverterManager::initMapTable");
		}
		
		// temporary containing mappings of class pairs to a collection of providers
		// to collect provider and subsequently sort
		HashMap<String, LinkedList<ALDDataConverter>> tmpMapTable = 
				new HashMap<String, LinkedList<ALDDataConverter>>();
		
		// map of provider names to its priority
		// required as we cannot get hold of the annotation besides sezpoz Index
		final HashMap<String,Integer> priorityMap = new HashMap<String, Integer>(); 

		Index<ALDDataConverterProvider,ALDDataConverter> indexItems = 
			SezPozAdapter.load(ALDDataConverterProvider.class,
					ALDDataConverter.class);
		for (final IndexItem<ALDDataConverterProvider,ALDDataConverter> item : indexItems ) {

			// class name of provider
			String className = item.className();

			// and its priority
			int priority = item.annotation().priority();

			if ( debug ) 
				System.out.println( "found:  " + className);

			Method providesMethod;
			ALDDataConverter provider = null;
			try {
				provider = (ALDDataConverter)(Class.forName( className).newInstance());
				priorityMap.put(provider.getClass().getName(), priority);
			} catch (Exception e) {
				// do not throw an exception as we can still try to continue
				System.err.println( 
					"ALDDataConverterManager::initMap cannot create an instance for " 
						+ className);
				continue;
			}
			
			Collection<ALDSourceTargetClassPair> sourceTargetPairs = null;
			try {	
				Class<?> params[] = {};
				Object paramsObj[] = {};
				
				providesMethod = 
						Class.forName(className).getDeclaredMethod(
								ALDDataConverter.providesMethodName, params);
				sourceTargetPairs =
						(Collection<ALDSourceTargetClassPair>)(providesMethod.invoke( provider, paramsObj));

			} catch (Exception e) {
				// do not throw an exception as we can still try to continue
				System.err.println( 
						"ALDDataConverterManager::initMap failed to invoke method " +
						ALDDataConverter.providesMethodName +
						" of converter provider " + className +
						" to get provided class pairs");
			}

			if ( sourceTargetPairs != null ) {
				for ( ALDSourceTargetClassPair sourceTargetPair : sourceTargetPairs ) {

					String key = sourceTargetPair.toString();
					if ( debug )
						System.out.println( "    supported class (priority = " + 
								priority + "):"  +  sourceTargetPair.getSourceClass().getName() +
								" -> " + sourceTargetPair.getTargetClass().getName() +
								" key = " + key);

					LinkedList<ALDDataConverter> providers = tmpMapTable.get(key);
					if ( providers == null)
						providers = new LinkedList<ALDDataConverter>();

					providers.add(provider);
					tmpMapTable.put( key, providers);
				}	
			}
		}
		
		//TODO: sort each list of provider names according to priority
		HashMap<String, Collection<String>> mapTable = new HashMap<String, Collection<String>>();
		for ( String sourceTargetPair : tmpMapTable.keySet()) {
			LinkedList<ALDDataConverter> providers = tmpMapTable.get(sourceTargetPair);

			java.util.Collections.sort(providers, 
					new Comparator<ALDDataConverter>() {

						@Override
						public int compare(ALDDataConverter o1,
								ALDDataConverter o2) {
							int p1 = priorityMap.get(o1.getClass().getName());
							int p2 = priorityMap.get(o2.getClass().getName());
							return p2 - p1;
						}
					});
			LinkedList<String> providerNames = new LinkedList<String>();
			for ( ALDDataConverter provider : providers) {
				providerNames.add( provider.getClass().getName());
			}
			mapTable.put( sourceTargetPair, providerNames);

		}

		if ( debug )
			printMap(mapTable);
		return mapTable;
	}
	
	public static void printMap(HashMap<String, Collection<String>> mapTable) {
		System.out.println("Map of data converter providers");
		for ( String sourceTargetPair : mapTable.keySet()) {
			System.out.println("  " + sourceTargetPair);
			for ( String providerName : mapTable.get(sourceTargetPair)) {
				System.out.println( "      " + providerName);	
			}
		}

	}
	
	// ====================================================================================
	// local class
	/**
	 * A pair of source and target pair (of a converter)
	 * 
	 * @author posch
	 *
	 */
	public static class ALDSourceTargetClassPair  {
		public ALDSourceTargetClassPair(Class<?> sourceClass, Class<?> targetClass) {
			this.sourceClass = sourceClass;
			this.targetClass = targetClass;
		}
		
		private Class<?> sourceClass;
		private Class<?> targetClass;
		
		@Override
		public boolean equals( Object obj) {
			if ( obj instanceof ALDSourceTargetClassPair ) {
				ALDSourceTargetClassPair pair = (ALDSourceTargetClassPair)obj;
				return ( this.getSourceClass() == pair.getSourceClass() &&
						this.getTargetClass() == pair.getTargetClass());
			} else {
				return false;
			}
			
		}
		@Override
		public String toString() {
			return new String( sourceClass.getName() + ";" + targetClass.getName());
		}
		
		/**
		 * @return the sourceClass
		 */
		 public Class<?> getSourceClass() {
			return sourceClass;
		}
		/**
		 * @param sourceClass the sourceClass to set
		 * @return 
		 */
		protected void setSourceClass(Class<?> sourceClass) {
			this.sourceClass = sourceClass;
		}
		/**
		 * @return the targetClass
		 */
		public Class<?> getTargetClass() {
			return targetClass;
		}
		/**
		 * @param targetClass the targetClass to set
		 */
		protected void setTargetClass(Class<?> targetClass) {
			this.targetClass = targetClass;
		}
	}
}
