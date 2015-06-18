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

package de.unihalle.informatik.Alida.tools;

import de.unihalle.informatik.Alida.operator.*;

/**
 * Generates code for getter/setter methods for an ALDOperator.
 * <p>
 * This tool is mostly helpful for non-IDE users who need to explicitly
 * code getters and setters for their operators. Note, that the tool assumes 
 * that names of variables are identical to the name of the member/field. 
 * 
 * @author posch
 */
public class GenerateGetterSetter {

	/**
	 * Main function.
	 * 
	 * @param args	Name of class to document.
	 */
	public static void main(String [] args) {
		System.out.print(" " +
				"<Alida>  Copyright (C) 2010-2011  \n" +
				"This program comes with ABSOLUTELY NO WARRANTY; \n" +
				"This is free software, and you are welcome to redistribute it\n" +
				"under the terms of the GNU General Public License.\n\n\n");
		
		if ( args.length != 1 ) {
			System.err.println( "usage: GenerateGetterSetter classname");
			System.exit(-1);
		}

		ALDOperator op = null;
		try {
			System.out.println( Class.forName( args[0]));
			System.out.println();

			op = (ALDOperator)(Class.forName( args[0]).newInstance());
		} catch (Exception e) {
			System.err.println( "GenerateGetterSetter: cannot instantiate "+args[0]);
			e.printStackTrace();
		}


		try {
        	for ( String pName : op.getParameterNames() )
            	System.out.println( generateAccessFunction( op.getParameterDescriptor( pName), "Parameter"));

		} catch (Exception e) {
			System.err.println( "GenerateGetterSetter: got excpetion");
			e.printStackTrace();
		}

	}

	/**
	 * Generate access function code.
	 * 
	 * @param descriptor	Argument descriptor.
	 * @param argType			Type of the argument.
	 * @return	String with source code for access function.
	 */
	private static String generateAccessFunction( 
			ALDOpParameterDescriptor descriptor,	String argType) {
		return new String(  "\t/** Get value of " + descriptor.getName() + ".\n" +
							"\t  * Explanation: " + descriptor.getExplanation() + ".\n" +
							"\t  * @return value of " + descriptor.getName() + "\n" +
							"\t  */\n" +
							"\tpublic " + descriptor.getMyclass().getName() +
							" get" + Character.toUpperCase( descriptor.getName().charAt(0)) +
                            descriptor.getName().substring(1) + "(){\n" +
							"\t\treturn " + descriptor.getName() + ";\n" +
							"\t}\n" +

							"\n" + 

							"\t/** Set value of " + descriptor.getName() + ".\n" +
							"\t  * Explanation: " + descriptor.getExplanation() + ".\n" +
							"\t  * @param value New value of " + descriptor.getName() + "\n" +
							"\t  */\n" +
							"\tpublic void " + 
							"set" + Character.toUpperCase( descriptor.getName().charAt(0)) +
                            descriptor.getName().substring(1) + "( " + descriptor.getMyclass().getName() + " value){\n" +
							"\t\tthis." + descriptor.getName() + " = value;\n" +
							"\t}\n" 

					);
	}

	/**
	 * @param descriptor
	 * @param argType
	 * @return	String with source code for access function.
	 * 
	 * @deprecated
	 */
	@Deprecated
	private static String generateAccessFunctionNoAnnotation(
			ALDOpParameterDescriptor descriptor, String argType) {
		return new String(  "/** Get value of " + argType + " argument " + descriptor.getName() + ".\n" +
							"  * @return value of " + descriptor.getName() + "\n" +
							"  */\n" +
							"public " + descriptor.getMyclass().getName() +
							" get" + Character.toUpperCase( descriptor.getName().charAt(0)) + 
							descriptor.getName().substring(1) +
							"()" + " throws ALDOperatorException {\n\treturn (" + descriptor.getMyclass().getName() + ")" +
							"(this.get" + argType + "(\"" +descriptor.getName() + "\"));\n}\n" +

							"/** Set value of " + argType + " argument " + descriptor.getName() + ".\n" +
							"  * @param value New value for " + descriptor.getName() + "\n" +
							"  */\n" +
							"public void" +
							" set" + Character.toUpperCase( descriptor.getName().charAt(0)) + 
							descriptor.getName().substring(1) +
							"( " + descriptor.getMyclass().getName() + " value )" + 
							" throws ALDOperatorException {\n\t" + 
							" this.set" + argType + "(\"" +descriptor.getName() + "\", value);\n}\n" 
					);
	}

}
