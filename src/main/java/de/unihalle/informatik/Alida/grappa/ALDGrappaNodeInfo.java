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

/**
 * Serializable object attached to nodes in Grappa to store meta information.
 * @author moeller
 */
public class ALDGrappaNodeInfo implements Serializable {

	/**
	 * Name of the node, i.e. name of corresponding operator.
	 */
	private String nodeName = null;
	
	/**
	 * Reference ID in workflow, required for loading from file.
	 */
	private Integer refID = null;
	
	/**
	 * Default constructor.
	 */
	public ALDGrappaNodeInfo() {
		this.nodeName = null;
		this.refID = null;
	}
	
	/**
	 * Constructor with predefined name.
	 * @param name	Name of the node.
	 */
	public ALDGrappaNodeInfo(String name) {
		this.nodeName = name;
	}
	
	/**
	 * Operator with predefined name and ID.
	 * @param name	Name of the operator.
	 * @param id		Reference ID.
	 */
	public ALDGrappaNodeInfo(String name, Integer id) {
		this.nodeName = name;
		this.refID = id;
	}
	
	/**
	 * Returns name of node.
	 * @return	Node name.
	 */
	public String getNodeName() {
		return this.nodeName;
	}
	
	/**
	 * Set (new) name for operator.
	 * @param name	New name of operator.
	 */
	public void setNodeName(String name) {
		this.nodeName = name;
	}

	/**
	 * Set (new) reference ID.
	 * @param id	Reference ID of node.
	 */
	public void setRefID(Integer id) {
		this.refID = id;
	}
	
	/**
	 * Returns the reference ID of the node.
	 * @return	Reference ID.
	 */
	public Integer getRefID() {
		return this.refID;
	}
	
	@Override
  public String toString() {
		return this.nodeName;
	}
	
}
