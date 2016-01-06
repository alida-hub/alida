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
package de.unihalle.informatik.Alida.demo;

import java.util.LinkedList;
import java.util.List;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 *  Refine extrema {@code smoothedExtrema} found in smoothed data by extrema {@code extrema} of the un-smoothed
 *  data within a given epsilon neighborhood.
 *  <p>
 *  For each extremum of the smoothed data the neighborhood of {@code +/- epsilon}
 *  positions is considered. Of the extrema of the un-smoothed within this neighborhood
 *  the closest with respect to positions is returned, if any.
 *  
 * @author posch
 */

@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class RefineLocalExtrema1D extends ALDOperator {


	/** Extrema of un-smoothed data
	 */
	@Parameter( label= "Extrema",  
			direction = Parameter.Direction.IN, required = true,
			description = " Extrema in un-smoothed data",
			dataIOOrder = 1)
	protected Extrema1D extrema;

	/** Extrema detected in smoothed data
	 */
	@Parameter( label= "Smoothed extrema",  
			direction = Parameter.Direction.IN, required = true,
			description = "Extrema in smoothed data",
			dataIOOrder = 2)
	protected Extrema1D smoothedExtrema;

	/** Size of neighborhood is {@code 2*epsilon+1}
	 */
	@Parameter( label = "Epsilon", required = true,
			direction = Parameter.Direction.IN,
			description = "Type of extrema (min,max)",
			dataIOOrder = 3)
	Integer epsilon = 3;

	/** Refined positions of the extrema detected in smoothed data
	 */
	@Parameter( label= "Refined positions of Extrema",  
			direction = Parameter.Direction.OUT, required = true,
			description = "Refined positions of Extrema",
			dataIOOrder = 1)
	protected Extrema1D refinedExtrema;


	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public RefineLocalExtrema1D() throws ALDOperatorException {
	}

	@Override
	public void validateCustom() throws ALDOperatorException {
		if ( epsilon < 0) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"RefineLocalExtrema1D epsilon may not be negative");

		}
	};
	
	@Override
	protected void operate() {
		refinedExtrema = new Extrema1D();
		
		List<Integer> extremaPositionsList = new LinkedList<Integer>();
		for ( int i = 0 ; i < extrema.size() ; i++ ) {
			extremaPositionsList.add( extrema.getX(i).intValue());
		}
		
		for ( int i = 0 ; i < smoothedExtrema.size() ; i++) {
			Integer pos = smoothedExtrema.getX( i).intValue();
			for ( int d = 0 ; d <= this.epsilon ; d++) {
				if ( extremaPositionsList.contains(pos+d) ) {
					int l = extremaPositionsList.lastIndexOf(pos+d);
					refinedExtrema.addPoint( new Double( pos+d), extrema.getY(l));
					break;
				}
				if ( extremaPositionsList.contains(pos-d) ) {
					int l = extremaPositionsList.lastIndexOf(pos-d);

					refinedExtrema.addPoint( new Double( pos-d),  extrema.getY(l));
					break;
				}
			}
		}
	}

}