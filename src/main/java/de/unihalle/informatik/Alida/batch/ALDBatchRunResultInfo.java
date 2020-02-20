/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
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
 * Fore more information on MiToBo, visit
 *
 *    http://www.informatik.uni-halle.de/mitobo/
 *
 */

package de.unihalle.informatik.Alida.batch;

import java.util.Vector;


/**
 * Info object summarizing all result data of a batch run.
 * 
 * @author moeller
 */
public class ALDBatchRunResultInfo {

	/**
	 * Name of parameter over which was iterated.
	 */
	protected String parameterName;
	
	/**
	 * List of input values for parameter.
	 */
	protected Vector<Object> inputParameterValues;
	
	/**
	 * List of corresponding result data objects.
	 */
	protected Vector<Object> resultDataVec;
	
	/**
	 * Default constructor, should/can never be called from external.	
	 */
	protected ALDBatchRunResultInfo() {
		// should never be called
	}
	
	/**
	 * Constructor.
	 * @param param		Name of parameter over which was iterated.
	 */
	public ALDBatchRunResultInfo(String param) {
		this.parameterName = param;
		this.resultDataVec = new Vector<Object>();
		this.inputParameterValues = new Vector<Object>();
	}
	
	/**
	 * Get a reference to the input parameter value vector.
	 * @return	Reference to vector of input parameter values.
	 */
	public Vector<Object> getParameterValueVec() {
		return this.inputParameterValues;
	}
	
	/**
	 * Get a reference to the result data vector.
	 * @return	Reference to result data vector.
	 */
	public Vector<Object> getResultDataVec() {
		return this.resultDataVec;
	}
	
}
