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

package de.unihalle.informatik.Alida.operator;

import java.util.*;

import de.unihalle.informatik.Alida.version.ALDVersionProvider;
import de.unihalle.informatik.Alida.version.ALDVersionProviderFactory;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;


/** This class is a wrapper for all parameters of a <code>ALDOperator</code>.
    It provides additional information of this <code>ALDOperator</code> and may be used, e.g., for
    serialization.
 */
 
class ALDParameterWrapper {
	private final String className;
	private final String packageName;
	private final String version;
	private Hashtable<String, Object> parameterHash;

	/** Create an instance for the parameters of <code>op</code>
      */
	ALDParameterWrapper( ALDOperator op) {
		className = op.getClass().getSimpleName();
		if ( op.getClass().getPackage() != null ) 
			packageName = op.getClass().getPackage().getName();
		else
			packageName = new String( "no package");
		version = ALDVersionProviderFactory.getProviderInstance().getVersion();


		// we get the paameter values via reflection
		this.parameterHash = new Hashtable<String, Object>( op.getNumParameters());

		for ( String pName : op.getParameterNames() ) {
			try {
				parameterHash.put( pName, op.getParameter( pName));
			} catch ( ALDOperatorException e ) {
					System.err.println( "ALDParameterWrapper::ALDParameterWrapper Error: cannot get value for parameter " +
									pName);
                	e.printStackTrace();
        	}
		}
	}


	String getClassName() {
		return className;
	}

	String getPackageName() {
		return packageName;
	}

	String getVersion() {
		return version;
	}

	Hashtable<String, Object> getParameteres() {
		return parameterHash;
	}
}
