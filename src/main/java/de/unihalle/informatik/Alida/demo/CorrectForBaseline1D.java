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

import de.unihalle.informatik.Alida.demo.DetectLocalExtrema1D.ExtremaType;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator to correct the extrema  with respect to a baseline.
 * <p>
 * Specifically, the baseline is subtracted from the functions values of the
 * extrema.
 * 
 * @author posch
 */
@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class CorrectForBaseline1D extends ALDOperator {
	
	/** Local extrema 
	 */
	@Parameter( label= "Extrema",  
			direction = Parameter.Direction.IN, required = true,
			description = "Extrema",
			dataIOOrder = 1)
	protected Extrema1D extrema;

	/**
	 * The baseline detected
	 */
	@Parameter( label= "Baseline",  
			direction = Parameter.Direction.IN, required = true, 
			description = "Baseline",
			dataIOOrder = 1)
	protected Baseline1D baseline;

	/** Corrected extrema 
	 */
	@Parameter( label= "Corrected extrema",  
			direction = Parameter.Direction.OUT,
			description = "Corrected extrema",
			dataIOOrder = 1)
	protected Extrema1D correctedExtrema;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public CorrectForBaseline1D() throws ALDOperatorException {
	}

	@Override
	protected void operate() throws ALDOperatorException, ALDProcessingDAGException {
		correctedExtrema = new Extrema1D();
		
		for( int i = 0 ; i < extrema.size() ; i++) {
			correctedExtrema.addPoint( extrema.getX(i), extrema.getY(i) - baseline.getY(extrema.getX(i)));
		}
	}
	
}