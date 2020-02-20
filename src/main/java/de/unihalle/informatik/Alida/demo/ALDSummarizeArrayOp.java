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

package de.unihalle.informatik.Alida.demo;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;

/**
 * A abstract summarizing operator taking a 1D array as input and
 * return a summarizing scalar. 
 * Examples are the mean or the maxium value.
 * 
 * @author posch
 */
abstract public class ALDSummarizeArrayOp extends ALDOperator {

  /**
   * Input 1D array
   */
  @Parameter( label= "Input 1D array", required = true, 
  		direction = Parameter.Direction.IN, 
  		description = "Input array (1D).")
  protected Double[] data;

  /**
   * Summarizing scalar
   */
  @Parameter( label= "Summarizing scalar",  
  		direction = Parameter.Direction.OUT, 
  		description = "Summarizing scalar of the 1D arra")
  protected transient Double summary = null;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDSummarizeArrayOp() throws ALDOperatorException {
	}

	/**
	 * Returns the 1D array
	 * @return data array
	 */
	public Double[] getData() {
		return this.data;
	}

	/**
	 * Sets the 1D array
	 * @param _data
	 */
	public void setData( Double[] _data) {
		this.data = _data;
	}

	/**
	 * Returns the Summarizing scalar
	 * @return row or column wise sums
	 */
	public Double getSummary() {
		return this.summary;
	}
	
	@Override
	public String getDocumentation() {
		return "<p>" +
				"This class is an abstract super class for operators performing calculations \n" + 
				"on 1D arrays. In particular, the operator defines as input a 1D array of \n" + 
				"numerical values and as output a single numerical value, which, e.g., can be \n" + 
				"the mean, minimum or maximum of the input array.\n" + 
				"<ul><li>\n" + 
				"<p><b>input:</b>\n" + 
				"<ul>\n" + 
				"<li><p>one-dimensional array of type <i>Double []</i></p>\n" + 
				"</ul>\n" + 
				"</p>\n" + 
				"</li><li>\n" + 
				"<p><b>output:</b>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<p>summary value in terms of a <i>Double</i>\n" + 
				"</li></ul>\n" + 
				"</p>\n" + 
				"</li></ul>\n" + 
				"\n" + 
				"<p>\n";
	}
}
