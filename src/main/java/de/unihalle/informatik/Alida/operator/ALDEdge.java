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


/** This class is used to represent edges of the processing history during
 * construction of the explicit history graph.
 * It connects to ports and represents the dataflow between these ports.
 */

public class ALDEdge {
	/** Source port of the data flow
	 */
	private	ALDPort	sourcePort;

	/** Target port of the data flow
	 */
	private	ALDPort	targetPort;

	/** Construct an edge for given source and target ports.
     */
	ALDEdge( ALDPort sourcePort, ALDPort targetPort) {
		this.sourcePort = sourcePort;
		this.targetPort = targetPort;
	}

	/** Return the source port of the edge.
     */
	public ALDPort getSourcePort() {
		return sourcePort;
	}

	/** Return the target port of the edge.
     */
	public ALDPort getTargetPort() {
		return targetPort;
	}

	/** Print this edge to stdout.
     */
	public void print() {
		print( "");
	}

	/** Print this edge to stdout with a given ident string.
     */
	public void print( String indent) {
		System.out.println( indent + "ALDEdge\n\t" + sourcePort + "-->\n\t " + targetPort);
		if ( sourcePort != null ) { 
			System.out.print( "   "); 
			sourcePort.print( indent); 
		} else {
			System.out.println( "   Port == null");
		}

		if ( targetPort != null ) { 
			System.out.print( "   "); 
			targetPort.print( indent); 
		} else {
			System.out.println( "   Port == null");
		}
	}
}
