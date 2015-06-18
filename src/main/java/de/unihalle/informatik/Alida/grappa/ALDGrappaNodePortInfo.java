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

package de.unihalle.informatik.Alida.grappa;

import java.io.Serializable;

import com.mxgraph.model.mxCell;

import de.unihalle.informatik.Alida.operator.ALDOpParameterDescriptor;

/**
 * Serializable object attached to node ports to store meta information.
 * @author moeller
 */
public class ALDGrappaNodePortInfo implements Serializable {

	/**
	 * Node to which the port is attached.
	 */
	private mxCell node;
	
	/**
	 * Label of the associated operator parameter.
	 */
	private String portLabel;
	
	/**
	 * Variable name of the associated operator parameter.
	 */
	private String portName;
	
	/**
	 * Explanation of the parameter.
	 */
	private String portExplanation;
	
	/**
	 * (Simple) class name of the parameter datatype.
	 */
	private String portClassName;
	
	/**
	 * Direction of the port.
	 * <p>
	 * May have one of the following values:
	 * - "IN"
	 * - "OUT"
	 * - "INOUT"
	 * - "UNKNOWN"
	 */
	private String direction;
	
	/**
	 * Default constructor.
	 * @param pnode		Parent node.
	 * @param descr		Operator parameter descriptor of port.
	 */
	public ALDGrappaNodePortInfo(mxCell pnode, ALDOpParameterDescriptor descr) {
		this.node = pnode;
		this.portName = descr.getName();
		this.portExplanation = descr.getExplanation();
		this.portClassName = descr.getMyclass().getSimpleName();
		this.portLabel = descr.getLabel();
		// map parameter direction to internal string identifiers
		switch(descr.getDirection())
		{
		case IN: this.direction = "IN"; break;
		case INOUT: this.direction = "INOUT"; break;
		case OUT: this.direction = "OUT"; break;
		case UNKNOWN: this.direction = "UNKNOWN";
		}
	}
	
	public ALDGrappaNodePortInfo() {
	}

	/**
	 * Returns the label of the port/parameter.
	 * @return	Label of parameter.
	 */
	public String getPortLabel() {
		return this.portLabel;
	}
	
	/**
	 * Returns the name of the port/parameter.
	 * @return	(Variable) name of parameter.
	 */
	public String getPortName() {
		return this.portName;
	}
	
	/**
	 * Returns the class name of the port/parameter datatype.
	 * @return	Data type class of parameter.
	 */
	public String getPortClassName() {
		return this.portClassName;
	}
	
	/**
	 * Returns the explanation of the port/parameter.
	 * @return	Explanation of parameter.
	 */
	public String getPortExplanation() {
		return this.portExplanation;
	}
	
	/**
	 * Returns the direction of the port/parameter.
	 * @return	Direction string of parameter.
	 */
	public String getPortDirection() {
		return this.direction;
	}

	/**
	 * @return the node
	 */
	public mxCell getNode() {
		return node;
	}

	/**
	 * @param node the node to set
	 */
	public void setNode(mxCell node) {
		this.node = node;
	}

	/**
	 * @param portLabel the portLabel to set
	 */
	public void setPortLabel(String portLabel) {
		this.portLabel = portLabel;
	}

	/**
	 * @param portName the portName to set
	 */
	public void setPortName(String portName) {
		this.portName = portName;
	}

	/**
	 * @param portExplanation the portExplanation to set
	 */
	public void setPortExplanation(String portExplanation) {
		this.portExplanation = portExplanation;
	}

	/**
	 * @param portClassName the portClassName to set
	 */
	public void setPortClassName(String portClassName) {
		this.portClassName = portClassName;
	}

	/**
	 * @param direction the direction to set
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * Returns an identifier string.
	 * <p>
	 * The length of the string is dynamically updated according to the size of
	 * the parent node. The different numerical values used in this function 
	 * have been empirically determined and might not be optimal in all cases.
	 * 
	 * @return	ID string.
	 */
	@Override
  public String toString() {
		int maxLength = (int)(this.node.getGeometry().getWidth()/10.0 - 7);
		if (this.portLabel.length() > maxLength)
			return this.portLabel.substring(0, maxLength-1) + "...";
		return this.portLabel;
	}
	
}
