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

package de.unihalle.informatik.Alida.version;

import java.util.Hashtable;

import de.unihalle.informatik.Alida.helpers.ALDEnvironmentConfig;

/**
 * Factory for dynamic instantiation of version providers at runtime.
 * <p>
 * Here JVM properties are evaluated to dynamically configure the way, 
 * how Alida handles software versions. Usually there are different 
 * possibilities from where to get software version information. 
 * The probably most popular way is to query software repositories like 
 * CVS, SVN or Git. However, other options can be imagined as well.
 * <p>
 * Alida supports dynamic configuration of the version data handling. 
 * This factory instantiates a concrete version provider based on the 
 * environment property <i>alida_versionprovider_class</i>. 
 * This property should contain the name of a class extending base class 
 * {@link ALDVersionProvider}. The generated instance of this class will 
 * be used for all software version requests triggered during Alida 
 * operator invocations.  
 * 
 * @author moeller
 */
public class ALDVersionProviderFactory {
	
	/**
	 * Fallback provider if nothing else specified.
	 */
	private static final String defaultVersionProvider =
		"de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar";
	
	/**
	 * Stores name of current version provider class.
	 */
	private static String currentVersionProvider = null;
	
	/**
	 * Hashmap with global version provider objects.
	 */
	private static Hashtable<String, ALDVersionProvider> 
		providerInstances = new Hashtable<String, ALDVersionProvider>();
	
	/**
	 * Returns a reference to the currently selected provider object.
	 * <p>
	 * The provider class can be specified by environment properties.
	 * This method guarantees to never return null.
	 * 
	 * @return Instance of previously configured version provider.
	 */
	public static ALDVersionProvider getProviderInstance() {
		// get name of version provider class from environment
		String providerClass = getClassName();
		// instantiate object
		ALDVersionProvider vprof = getProviderInstance(providerClass);
		// if null, fall back to default provider
		if (vprof == null)
			vprof = getProviderInstance(defaultVersionProvider);
		return vprof; 
	}
	
	/**
	 * Returns a reference to the provider object of the specified class.
	 * 
	 * @param providerClass 		Name of desired provider class.
	 * @return Corresponding provider object.
	 */
	public static ALDVersionProvider getProviderInstance(
			String providerClass) {
		if (providerClass == null)
			return null;
		// check if provider object is already in hash; if not instantiate it
		if (   !providerClass.isEmpty() 
				&& !providerInstances.containsKey(providerClass)) {
			Object instance = instantiateObject(providerClass);
			if (instance != null) {
				providerInstances.put(
						providerClass, (ALDVersionProvider)instance);
				return (ALDVersionProvider)instance;
			}
			return null;
		}
		return providerInstances.get(providerClass);
	}

	/**
	 * Explicitly request a certain class from the code.
	 * <p>
	 * Note: this setting overrides potentially existing 
	 * 	environment settings!
	 *  
	 * @param pclass	Desired class for provider objects.
	 */
	public static void setProviderClass(String pclass) {
		currentVersionProvider = pclass;
	}
	
	/**
	 * Instantiates an object of the specified version provider class.
	 * 
	 * @param	providerClass		
	 * 						Identifier for provider class to be instantiated.
	 * @return Instantiated provider class object, null in case of failure.
	 */
	private static Object instantiateObject(String providerClass) {
		Object providerObject = null;
		try {
			// generate a new object instance
			Class<?> myclass = Class.forName(providerClass);
			providerObject = myclass.newInstance();

			// check if object is of correct type
			if (!(providerObject instanceof ALDVersionProvider)) {
				System.err.println("ALDVersionProviderFactory: " +
				"Cannot instantiate version provider object, invalid class!");
				return null;
			}
    } catch (ClassNotFoundException ex) {
    	System.err.println("ALDVersionProviderFactory: " + 
    			               "Could not find class " + providerClass);
    	return null;
    } catch (IllegalAccessException ex) {
    	System.err.println("ALDVersionProviderFactory: " + 
    			               "Could not access class " + providerClass);
    	return null;
    } catch (InstantiationException ex) {
    	System.err.println("ALDVersionProviderFactory: " + 
    			               "Could not instantiate class " + providerClass);
    	return null;
    }
    return providerObject;
	}

	/**
	 * Returns version provider class according to 
	 * environment configuration.
	 * 
	 * @return Name of version provider class. 
	 */
	protected static String getClassName() {
		String env = ALDEnvironmentConfig.getConfigValue("alida","",
																							"versionprovider_class");
		if (env == null)
			return currentVersionProvider;
		return env;
	}
	
	/**
	 * Returns true if the factory can be properly configured.
	 * @return True if factory can be configured.
	 */
	public static boolean isClassNameSpecified() {
		if (currentVersionProvider != null)
			return true;
		if (    ALDEnvironmentConfig.getConfigValue("alida","", 
																	"versionprovider_class") != null
    		&& !ALDEnvironmentConfig.getConfigValue("alida","", 	
																	"versionprovider_class").isEmpty()) {
			return true;
		}
		return false;
	}
}
