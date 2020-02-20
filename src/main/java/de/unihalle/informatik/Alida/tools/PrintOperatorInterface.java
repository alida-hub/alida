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

package de.unihalle.informatik.Alida.tools;

import de.unihalle.informatik.Alida.operator.*;

/**
 * Prints the interface of an operator (inputs/outputs/parameters) to console.
 * <p>
 * Usage: <i>java  PrintOperatorInterface  'classname'</i>
 * 
 * @author posch
 */
public class PrintOperatorInterface {

	/**
	 * Extracts operator interface from given class.
	 * 
	 * @param args	Name of operator class.
	 */
	public static void main(String [] args) {
		System.out.print(" " +
				"<Alida>  Copyright (C) 2010-2011  \n" +
				"This program comes with ABSOLUTELY NO WARRANTY; \n" +
				"This is free software, and you are welcome to redistribute it\n" +
				"under the terms of the GNU General Public License.\n\n\n");

		if ( args.length != 1 ) {
			System.err.println( "usage: PrintOperatorInterface classname");
			System.exit(-1);
		}

		ALDOperator op = null;
		try {

			op = (ALDOperator)(Class.forName( args[0]).newInstance());
			op.printInterface();
		} catch (Exception e) {
			System.err.println( "cannot instantiate " + args[0]);
			e.printStackTrace();
		}
	}
}
