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

package de.unihalle.informatik.Alida.helpers;


/**
 * Helper class to access environment variables and properties.
 * <p>
 * This class serves as helper class for reading and writing environment
 * variables and Java environment properties, respectively.
 * <p>
 * Every Alida operator and also every other class can define properties. 
 * To ensure a certain structure of the properties and avoid chaos in property 
 * names the properties defined should respect the following convention:
 * 
 * <code>alida.operatorname.property</code>
 * 
 * The corresponding environment variable will then be 
 * 
 * ALIDA_OPERATORNAME_PROPERTY
 * 
 * following common Unix/Linux conventions. 
 * 
 * @author moeller
 */
public class ALDEnvironmentConfig {

	/**
	 * Gets the value of a property from the environment.
	 * <p>
	 * Environment properties can be defined in terms of environment variables,
	 * or java properties passed to the virtual machine. The options are 
	 * checked in exactly this order. If the requested property is not found
	 * in either of the two configuration environments, null is returned.
	 * <p>
	 * The actual name of the property for which the environment is queried
	 * is assembled from the specified prefix, the given operator name and 
	 * the environment variable name (in this order).
	 * For checking environment variables all strings are converted to 
	 * upper-case and linked together by inserting '_' in between.
	 * For checking JVM properties, dots are inserted in between and all strings
	 * are converted to lower-case.
	 * 
	 * @param _prefix				Prefix.
	 * @param _operator			Name of the operator.
	 * @param _propname			Name of the property.
	 * @return	Value of property, <code>null</code> if not existing.
	 */
	public static String getConfigValue(String _prefix, String _operator, 
																			String _propname) {

		// some validity checks
		String prefix = (_prefix == null || _prefix.isEmpty()) ? 
																											"" : _prefix + "_";
		String operator = (_operator == null || _operator.isEmpty()) ? 
																											"" : _operator + "_";
		String property = (_propname == null || _propname.isEmpty()) ? 
																											"" : _propname;

		// compose the final name
		String envVarNameTmp= prefix + operator + property;
		String envVarName= envVarNameTmp.toUpperCase();
		
		prefix = (_prefix == null || _prefix.isEmpty()) ? 
																								"" : _prefix + ".";
		operator = (_operator == null || _operator.isEmpty()) ? 
																								"" : _operator + ".";
		property = (_propname == null || _propname.isEmpty()) ? 
																								"" : _propname;

		String envPropNameTmp= prefix + operator + property;
		String envPropName= envPropNameTmp.toLowerCase();
		
		// search for a corresponding environment variable
    String tmpValue= null, value= null;
		if ((tmpValue = System.getenv(envVarName)) != null) 
      value= tmpValue;
		// if not successful, check java properties
		else if ((tmpValue = System.getProperty(envPropName)) != null) 
      value= tmpValue;
		return value;
	}
	
	/**
	 * Reads the value of the specified property from the environment.
	 * <p>
	 * Here the default prefix "alida" is assumed. It is combined with the
	 * provided operator and environment variable names in this order.
	 * 
	 * @param _operator			Name of the operator.
	 * @param _propname			Name of the property.
	 * @return	Value of property, <code>null</code> if not existing.
	 */
	public static String getConfigValue(String _operator, String _propname) {
		return getConfigValue("alida", _operator, _propname);
	}

	/**
	 * Reads the value of the specified property from environment variables.
	 * <p>
	 * Default prefix is "alida".
	 * 
	 * @param _operator			Name of operator, ignored if null.
	 * @param _propname			Name of the property.
	 * @return	Value of property, <code>null</code> if not existing.
	 */
	public static String getEnvVarValue(String _operator, String _propname) {
		String operator = (_operator == null || _operator.isEmpty()) ? 
																											"" : _operator + "_";
		String property = (_propname == null || _propname.isEmpty()) ? 
																											"" : _propname;
		String envVarNameTmp= "alida_" + operator + property;
		String envVarName= envVarNameTmp.toUpperCase();		
		return System.getenv(envVarName); 
	}
	
	/**
	 * Reads the value of the specified property from JVM properties.
	 * 
	 * @param _prefix				Prefix, ignored if null.
	 * @param _operator			Name of operator, ignored if null.
	 * @param _propname			Name of the property.
	 * @return	Value of property, <code>null</code> if not existing.
	 */
	public static String getJVMPropValue(String _prefix, String _operator, 
																				String _propname) {
		String prefix = (_prefix == null || _prefix.isEmpty()) ? 
																												"" : _prefix + ".";
		String operator = (_operator == null || _operator.isEmpty()) ? 
																												"" : _operator + ".";
		String property = (_propname == null || _propname.isEmpty()) ? 
																												"" : _propname;

		String envPropNameTmp= prefix + operator + property;
		String envPropName= envPropNameTmp.toLowerCase();
		return System.getProperty(envPropName);
	}
	
	/**
	 * Reads the value of specified property from JVM properties.
	 * <p>
	 * Default prefix is "alida".
	 * 
	 * @param _operator			Name of operator, ignored if null.
	 * @param _propname			Name of the property.
	 * @return	Value of property, <code>null</code> if not existing.
	 */
	public static String getJVMPropValue(String _operator, String _propname){
		return getJVMPropValue("alida", _operator, _propname);
	}
}
