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
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;

/**
 * Operator for calculating the mean of given double values.
 * 
 * @author moeller
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
	level=ALDAOperator.Level.APPLICATION,
	shortDescription="Computes the mean of the input double array and " 
			+ "optionally makes the data mean-free.")
public class ALDCalcMeanArray extends ALDOperator {

  /**
   * Input data array.
   */
  @Parameter( label= "Input data", required = true, 
  		direction = Parameter.Direction.IN, 
  		description = "Input data.")
  private Double[] data;

  /**
   * Compute mean free data
   */
  @Parameter( label= "Compute mean free data", required = false, 
  		direction = Parameter.Direction.INOUT, 
  		description = "Optionally compute mean free data.")
  private Boolean doMeanFree = new Boolean(false);

  /**
   * Calculated average value.
   */
  @Parameter( label= "Mean value",  
  		direction = Parameter.Direction.OUT, 
  		description = "Result.")
  private Double mean = null;

  /**
   * Mean free data
   */
  @Parameter( label= "Mean free data",  
  		direction = Parameter.Direction.OUT, 
  		description = "Result.")
  private Double[] meanFreeData = null;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDCalcMeanArray() throws ALDOperatorException {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param _data	Input data.
	 * @throws ALDOperatorException
	 */
	public ALDCalcMeanArray(Double[] _data) throws ALDOperatorException {
		super();
		this.data = _data;
	}

	@Override
	protected void operate() {
		this.fireOperatorExecutionProgressEvent(
			new ALDOperatorExecutionProgressEvent(this, 
				"Starting to calculate the mean..."));

		// calculate the mean of the data
		double tmpMean = 0.0;
		for( Double val : this.data ) {
			tmpMean += val.doubleValue();
		}
		this.mean = new Double(tmpMean/this.data.length);

		if ( this.doMeanFree.booleanValue() ) {
			this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
					"Starting to calculate mean free data..."));

			this.meanFreeData = new Double[this.data.length];
			for ( int i = 0 ; i < this.data.length ; i++ ){
				this.meanFreeData[i] = 
					new Double(this.data[i].doubleValue() - this.mean.doubleValue());
			}
		}
	}
	
	/**
	 * Returns the calculated mean value.
	 */
	public Double getResultMean() {
		return this.mean;
	}
	
	@Override
	public String getDocumentation() {
		return "<p>The operator calculates the mean value of all entries of the given array\n" + 
				"and optionally also calculates mean-free data from the given input.</p>\n" +
				"<p>It defines the following inputs and outputs:</p>\n" +
				"<ul><li>\n" + 
				"<p><b>input:</b>\n" + 
				"<ul>\n" + 
				"<li><p>one-dimensional array of type <i>Double []</i></p></li>\n" + 
				"<li><p>optional: flag of type <i>Boolean</i> to also return mean-free data</p>\n" + 
				"</li>\n" + 
				"</ul>\n" + 
				"</p>\n" + 
				"</li><li>\n" + 
				"<p><b>output:</b>\n" + 
				"<ul>\n" + 
				"<li>\n" + 
				"<p>value of type <i>Double</i>, i.e. the mean of the data</p>\n" + 
				"<li>\n" + 
				"<p>an array of type <i>Double []</i> containing the mean-free data</p>\n" + 
				"</li></ul>\n" + 
				"</p>\n" + 
				"</li></ul>\n" + 
				"\n" + 
				"\n";
	}
}
