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


/** Abstract class for ports residing within opNodes.
	Derived sub classes are input and output ports.
	Each port knows the opNode it resides in and its index in the corresponding
	array of input or output ports in the opNode.
	The typeName is used only for printing purposes.
	<p>
	Furthermore a ALDOpNodePort may record properties of the ALDData as it
	is passed into or out of the operator.
	Specifically, the properties of the data are retrieved an recorded 
	for input data just before the operate method is invoced,
	and for output data when the operate method returns.
    Also the explanation from the operator is copied as we may not keep a reference
    to it to allow garbage collection.
    Last the canaonical class name of the ALDData bound to the port
    is recorded, again for input data just before the operate method is invoced,
    and for output data when the operate method returns.
 */

public abstract class ALDOpNodePort extends ALDPort {

	/** The opNode this port belongs to
	 */
	ALDOpNode   opNode; 

	/** The index of this port within opNode
	 */
	private int portIndex; 

	/** The descriptor name of this port within opNode
	 */
	private String descriptorName; 

	/** The explanation of this port.
	 */
	private String explanation; 

	/** The canaonical class name of the ALDData bound to the port.
     *  For input data just before the operate method is invoced,
     * and for output data when the operate method returns.
	 */
	private String classname; 

	/** The properties retrieved from the data as it passed the port into or out of
	 *	the operator.
	 */
	private Object properties;

	/** Create a port for an opNode with given index within this opNode and type.
	 *
	 * @param	typeName	type of this port as a string, used only for printing
	 * @param	opNode		opNode this port resides in
	 * @param	idx			index of this port within the corresponding array of ports within opNode
	 */
	public ALDOpNodePort( String typeName, ALDOpNode opNode, int idx, String descriptorName) {
		super( typeName);
		this.opNode = opNode;
		this.portIndex = idx;
		this.descriptorName = descriptorName;
	}

	/** Return the opNode of this port
	 *
	 * @return	opNode the port resides in
	 */
	public ALDOpNode getOpNode() { 
		return opNode;
	}

	/** Return the index of the port within its opNode 
	 *
	 * @return	index of the port within ints opNode 
	 */
	public int getPortIndex() { 
		return portIndex;
	}

	/** Return the descriptor name of the port within its opNode 
	 *
	 * @return	descriptor name of the port within its opNode 
	 */
	public String getDescriptorName() { 
		return descriptorName;
	}

	/** Return the explanation of the port copied from the argument descriptor
	 *
	 * @return	explantation of the port 
	 */
	public String getExplanation() { 
		return explanation;
	}

	/** Set the explanation of the port copied from the argument descriptor
	 *
	 * @param	e	explanation of the port within ints opNode 
	 */
	public void setExplanation(String e) { 
		explanation = e;
	}

	/** Return the canonical classname of the ALDData associated with this port 
	 *
	 * @return	canonical classname of the ALDData associated with this port
	 */
	public String getClassname() { 
		return classname;
	}

	/** Set the canonical classname of the ALDData associated with this port
	 *
	 * @param	cn	canonical classname of the ALDData associated with this port
	 */
	public void setClassname(String cn) { 
		classname = cn;
	}

	/** Return the properties associated with this port which have been retrieved
	 * from the data as it passed the port.
	 *
	 * @return	properties of the port
	 */
	public Object getProperties() { 
		return properties;
	}

	/** Set the properties to be associated with this port.
	 *
	 * @param	properties to set for the port
	 */
	Object setProperties( Object properties) { 
		return properties;
	}

	/** Print information of this port onto System.out
	 */
	public void print( String indent) {
		System.out.println( indent + "ALDOpNodePort " + this + " of type " + typeName + 
				" (portIndex = " + portIndex + ")\n" +
				indent + "  for opnode = " + opNode + " " + opNode.getName() + "\n" +
				indent + "  with origin " + getOrigin());
	}
}
