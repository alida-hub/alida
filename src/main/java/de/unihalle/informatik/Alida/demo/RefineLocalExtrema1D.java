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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 *  Refine extrema found in smoothed data by extrema of the un-smoothed
 *  data within a given epsilon neighborhood.
 *  <p>
 *  For each extremum of the smoothed data the neighborhood of {@code +/- epsilon}
 *  positions is considered. Of the extrema of the un-smoothed within this neighborhood
 *  the closest wrt to positions is returned, if any.
 * @author posch
 */
@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class RefineLocalExtrema1D extends ALDOperator {


	/** Positions, i.e. indices, of local extrema detected
	 */
	@Parameter( label= "Positions of Extrema",  
			direction = Parameter.Direction.IN, required = true,
			description = "Positions of local Extrema in un-smoothed data",
			dataIOOrder = 1)
	protected Integer[] extremaPositions;

	/** Positions, i.e. indices, of local extrema detected in smoothed data
	 */
	@Parameter( label= "Positions of SmoothExtrema",  
			direction = Parameter.Direction.IN, required = true,
			description = "Positions of local Extrema in smoothed data",
			dataIOOrder = 2)
	protected Integer[] smoothedExtremaPositions;

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
	protected Integer[] refinedExtremaPositions;


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
		LinkedList<Integer> extrema = new LinkedList<Integer>();
		List<Integer> extremaPositionsList = Arrays.asList( extremaPositions);
		for ( int i = 0 ; i < smoothedExtremaPositions.length ; i++) {
			Integer pos = smoothedExtremaPositions[i];
			for ( int d = 0 ; d <= this.epsilon ; d++) {
				if ( extremaPositionsList.contains(pos+d) ) {
					extrema.add(pos+d);
					break;
				}
				if ( extremaPositionsList.contains(pos-d) ) {
					extrema.add(pos-d);
					break;
				}
			}
		}
		refinedExtremaPositions = extrema.toArray(new Integer[0]);
	}

}