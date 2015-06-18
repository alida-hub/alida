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

import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;
import de.unihalle.informatik.Alida.annotations.ALDAOperator.Level;
import de.unihalle.informatik.Alida.annotations.indexing.SezPozAdapter;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.ALDOperatorLocation;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import net.java.sezpoz.Index;
import net.java.sezpoz.IndexItem;

/**
 * Supplies helper methods to, e.g., lookup classes with annotations.
 * 
 * @author posch
 */
/**
 * @author posch
 *
 */
public class ALDClassInfo {

	/**
	 * Debug flag for internal usage only.
	 */
	private static boolean debug = false;

    /**
     * Collects all derived class of given class.
     * <p>
     * The method searches for all derived classes of the given class
     * among all classes annotated with @ALDDerivedClass that are found
     * in the classpath. In this context derived classes are classes which
     * either implement the specified interface or extend the given class. 
     *
     * @param       cl      Class for which derived classes are requested.
     * @return      Set of extending classes including class itself.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Set<Class> lookupExtendingClasses( Class cl) {
            // init result list
            HashSet<Class> extendingClasses = new HashSet<Class>();

            if ( debug )
                    System.out.println("ALDDataIOManager::lookupExtendingClasses class "+cl);

            // run through all classes annotated as Alida derived class
            Index<ALDDerivedClass,Object> indexItems =
                            SezPozAdapter.load(ALDDerivedClass.class, Object.class);
            for( final IndexItem<ALDDerivedClass,Object> item : indexItems) {
            	if ( debug ) {
            		System.out.println( "ALDDataIOManager::lookupExtendingClasses looking up <"+ 
            				item.className() + ">");
            	}

            	try {
            		if ( cl.isAssignableFrom( Class.forName( item.className())) ) {
            			extendingClasses.add( Class.forName( item.className()));
            			if ( debug )
            				System.out.println("ALDDataIOManager::lookupExtendingClasses found " +
            						item.className());
            		}
            	} catch (Exception e) {
            		//e.printStackTrace();
            	}
            }
            
    		for ( String opPackes : getOperatorSearchPaths()) {
    			try {
    				for ( Class<?> clazz : SubclassFinder.findInstantiableSubclasses(cl, opPackes) ) {
    					extendingClasses.add( clazz);

    					if ( debug ) 
    						System.out.println("ALDDataIOManager::lookupExtendingClasses found via package " + clazz.getName());
    				}
    			} catch (ClassNotFoundException e) {
    			} catch (IOException e) {
    			}
    		}

            return extendingClasses;
    }

	/**
	 * Collects all ALDOperators annotated with the requested level and execution mode
	 * and all ALDOperators found in a package specified by a JVM property alida_oprunner_favoriteops or
	 *  the environment variable ALIDA_OPRUNNER_OPERATORPATH as a colon separated list of package names.
	 * <p>
	 * If <code>level</code> is <code>Level.APPLICATION</code> then 
	 * only operators with this level will be return.
	 * If  <code>level</code> is <code>Level.STANDARD</code> all operator levels are accepted.
	 * 
	 * @param	level	Class for which derived classes are requested.
	 * @return	List of operator names with requested level and execution mode 
	 */
	public static Collection<ALDOperatorLocation> lookupOperators( ALDAOperator.Level level, ALDAOperator.ExecutionMode executionMode) {
		// init result list
		LinkedList<ALDOperatorLocation> allClasses = new LinkedList<ALDOperatorLocation>();

		if ( debug ) 
			System.out.println("ALDDataIOManager::lookupOperators class, level =  " + level +
					", execution mode = " + executionMode);
				
		for ( String classname : lookupOperatorClassnames( level, executionMode)) {
			allClasses.add( ALDOperatorLocation.createClassLocation( classname));
			if ( debug )
				System.out.println("ALDDataIOManager::lookupOperators found " + 
						classname);
		}
		
		return allClasses;
	}
	
	/**
	 * Collects all ALDOperators annotated with the requested level and execution mode
	 * and all ALDOperators found in a package specified by a JVM property alida_oprunner_favoriteops or
	 * the environment variable ALIDA_OPRUNNER_OPERATORPATH as a colon separated list of package names.
	 * <p>
	 * If <code>level</code> is <code>Level.APPLICATION</code> then 
	 * only operators with this level will be return.
	 * If  <code>level</code> is <code>Level.STANDARD</code> all operator levels are accepted.

	 * @param level
	 * @param executionMode
	 * @return Set of operator names.
	 */
	public static Set<String> lookupOperatorClassnames( 
			ALDAOperator.Level level, ALDAOperator.ExecutionMode executionMode) {
		
		HashSet<String> opClassnames = new HashSet<String>();
		
		if ( debug ) 
			System.out.println("ALDDataIOManager::lookupOperatorClassnames class, level =  " + level +
					", execution mode = " + executionMode);
		
		// first find all annotated ALDOperators
		Index<ALDAOperator, ALDOperator> indexItems = SezPozAdapter.load(
				ALDAOperator.class, ALDOperator.class);
		for (final IndexItem<ALDAOperator, ALDOperator> item : indexItems) {

			try {
				if ( (item.annotation().genericExecutionMode() == ALDAOperator.ExecutionMode.ALL || 
						item.annotation().genericExecutionMode() == executionMode)
						&& (level != Level.APPLICATION || item.annotation().level() == level) ) {
					opClassnames.add( item.className());
					if ( debug )
						System.out.println("ALDDataIOManager::lookupOperatorClassnames found " + 
								item.className());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		// now add classes found as class files
		for ( String opPackage : getOperatorSearchPaths()) {
			try {
				for ( Class<?> clazz : SubclassFinder.findInstantiableSubclasses(ALDOperator.class, opPackage) ) {
					opClassnames.add( clazz.getName());

					if ( debug ) 
						System.out.println("ALDDataIOManager::lookupOperatorClassnames found via package " + clazz.getName());
				}
			} catch (ClassNotFoundException e) {
			} catch (IOException e) {
			}
		}

		return opClassnames;

	}
	
	private static Collection<String> getOperatorSearchPaths() {
		LinkedList<String> pathes = new LinkedList<String>();
		
		String operatorPath = ALDEnvironmentConfig.getConfigValue( "OPRUNNER", "OPERATORPATH");
		if ( operatorPath != null ) {
			String[] pathesArray = operatorPath.split(":");

			for ( int i = 0 ; i < pathesArray.length ; i++) {
				pathes.add( pathesArray[i]);
			}
		}
		
		return new LinkedList<String>( pathes);
	}
}