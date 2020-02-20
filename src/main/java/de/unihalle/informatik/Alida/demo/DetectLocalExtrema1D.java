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

import java.util.Comparator;
import java.util.LinkedList;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator to detect local extrema in the data of an {@link ExperimentalData1D}.
 * <p>
 * If a local extremum is formed by consecutive identical values,
 * the mid of theses positions yields one local extremum.
 * 
 * @author posch
 */

@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class DetectLocalExtrema1D extends ALDOperator {

	public enum ExtremaType {
		MAXIMUM, MINIMUM
	}

	/** 1D Experiment
	 */
	@Parameter( label= "1D Experiment", required = true, 
			direction = Parameter.Direction.IN, 
			description = "1D Experiment",
			dataIOOrder = 1)
	protected ExperimentalData1D experiment;

	/** Type of extrema
	 */
	@Parameter( label = "Type of extrema", required = true,
			direction = Parameter.Direction.IN,
			description = "Type of extrema (min,max)",
			dataIOOrder = 2)
	ExtremaType extremaType = ExtremaType.MINIMUM;

	/** Local extrema detected
	 */
	@Parameter( label= "Extrema",  
			direction = Parameter.Direction.OUT, 
			description = "Local Extrema",
			dataIOOrder = 1)
	protected Extrema1D extrema;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public DetectLocalExtrema1D() throws ALDOperatorException {
	}

	/**
	 * are we approaching the next extremum, are values equal, or
	 * are we departing the last extremum?
	 */
	private enum State {APPROACHING, EQUAL, DEPARTING};
	
	@Override
	protected void operate() {
		extrema = new Extrema1D();
		
		Double[] data = experiment.getData();
		Comparator<Double> comparator;
		if ( extremaType == ExtremaType.MAXIMUM) 
			comparator = new ApproachMax();
		else
			comparator = new ApproachMin();
	
		State state;
		int numEqualStates = 0;
		
		if ( data.length > 2) {
			if ( comparator.compare(data[0], data[1]) > 0 ) {
				state = State.APPROACHING;
			} else if ( comparator.compare(data[0], data[1]) == 0 ) {
				state = State.EQUAL;
				numEqualStates = 1;
			} else {
				state = State.DEPARTING;
			}
			
			for ( int i = 2; i < data.length ; i++) {
				switch ( state ) {
				case APPROACHING:
					if ( comparator.compare(data[i-1], data[i]) > 0 ) {
						state = State.APPROACHING;
					} else if ( comparator.compare(data[i-1], data[i]) == 0 ) {
						state = State.EQUAL;
						numEqualStates = 1;
					} else {
						extrema.addPoint( new Double(i-1), data[i-1]);
						state = State.DEPARTING;
					}
					break;
					
				case EQUAL:
					if ( comparator.compare(data[i-1], data[i]) > 0 ) {
						state = State.APPROACHING;
					} else if ( comparator.compare(data[i-1], data[i]) == 0 ) {
						state = State.EQUAL;
						numEqualStates++;
					} else {
						extrema.addPoint( new Double(i-1-numEqualStates/2), data[i-1-numEqualStates/2]);
						state = State.DEPARTING;
					}
					break;
					
				case DEPARTING:
					if ( comparator.compare(data[i-1], data[i]) > 0 ) {
						state = State.APPROACHING;
					} else if ( comparator.compare(data[i-1], data[i]) == 0 ) {
						state = State.DEPARTING;
					} else {
						state = State.DEPARTING;
					}
					break;
				}
			}
		}
	}

	/**
	 * @return the experiment
	 */
	public ExperimentalData1D getExperiment() {
		return experiment;
	}

	/**
	 * @param experiment the experiment to set
	 */
	public void setExperiment(ExperimentalData1D experiment) {
		this.experiment = experiment;
	}

	/**
	 * @return the extremaType
	 */
	public ExtremaType getExtremaType() {
		return extremaType;
	}

	/**
	 * @param extremaType the extremaType to set
	 */
	public void setExtremaType(ExtremaType extremaType) {
		this.extremaType = extremaType;
	}

	/**
	 * @return the extremaPositions
	 */
	public Extrema1D getExtrema() {
		return extrema;
	}

	/**
	 * @author posch
	 *
	 * Return {@code +1} if approaching the next minimum, {@code -1} if departing a minimum,
	 * i.e. approaching a maximum, and {@code 0} if the numbers are equal.
	 */
	private class ApproachMin implements Comparator<Double> {
		@Override
		public int compare ( Double a, Double b) {
			if ( a > b )
				return 1;
			else if ( a < b )
				return -1;
			else
				return 0;
		}
	}

	/**
	 * @author posch
	 *
	 * Return {@code +1} if approaching the next maximum, {@code -1} if departing a maximum,
	 * i.e. approaching a maximum, and {@code 0} if the numbers are equal.
	 */
	private class ApproachMax implements Comparator<Double> {
		@Override
		public int compare ( Double a, Double b) {
			if ( a < b )
				return 1;
			else if ( a > b )
				return -1;
			else
				return 0;
		}
	}
}