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

import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerCmdline;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerSwing;
import de.unihalle.informatik.Alida.dataio.ALDDataIOManagerXmlbeans;
import java.util.*;


/** A helper class to adminstrate the ALDIA dataIO system.
 * Prints the mapping of java classes to registered dataIO providers
 * for the interface type requested.
 */
public class PrintDataIOProvider {

	/** verbose flag to outout additional information to stdout.
     */
    private boolean verbose;

	/** debug flag for additional debuging information.
     */
    private boolean debug;

	/** command line arguments.
     */
	private String [] args;

	private String manager;
	
	/** Print usage to stderr.
	 */
    private static void printUsage() {
        System.err.println( "Usage: PrintDataIOProvider [cmdline|swing|xmlbeans]" 
            );
    }

	/** Print verbose usage to stderr.
     */
	private void printUsageVerbose() {
		printUsage();
		System.err.print( "\n" );
	}

	/** Construct a <code>PrintDataIOProvider</code> using <code>args</code>
     *
     * @param args	command line arguments
	 */
	public PrintDataIOProvider( String [] args) {
		this.verbose = false;
		this.debug = false;
		this.args = args;

		if ( args.length > 1 ) {
			printUsage();
			System.exit(1);
		} 
		
		if ( args.length == 0 || args[0].equalsIgnoreCase( "swing") )
			this.manager = "swing";
		else if ( args[0].equalsIgnoreCase( "cmdline") )
			this.manager = "cmdline";
		else if ( args[0].equalsIgnoreCase( "xmlbeans") )
			this.manager = "xmlbeans";
		else {
			printUsage();
			System.exit(1);
		}
	}

	/** Main routine of <code>ALDOpRunner</code> , see usage.
     */
    public static void main(String [] args) {
		PrintDataIOProvider runner = new PrintDataIOProvider( args);
		runner.runIt( );
	}

	/** This method does the complete work to scan arguments, read and write parameters
     * and <code>runOp</code> the operator.
     */
	public void runIt() {
		HashMap<Class, String> mapTable =  new HashMap<Class, String>();
		if (this.manager.equals("swing")) {
			System.out.println( "Mappings provided by  " + 
																ALDDataIOManagerSwing.class.getSimpleName());
			mapTable = ALDDataIOManagerSwing.getInstance().getProviderMap();
		} else if (this.manager.equals("cmdline")) {
			System.out.println( "Mappings provided by  " + 	
																ALDDataIOManagerCmdline.class.getSimpleName());
			mapTable = ALDDataIOManagerCmdline.getInstance().getProviderMap();
		} else if (this.manager.equals("xmlbeans")) {
			System.out.println( "Mappings provided by  " + 	
																ALDDataIOManagerXmlbeans.class.getSimpleName());
			mapTable = ALDDataIOManagerXmlbeans.getInstance().getProviderMap();
		}

		for ( Class cl : mapTable.keySet() ) {
			System.out.println( "\t" + cl.getName() + " --> " + mapTable.get( cl));
		}
	}
}
