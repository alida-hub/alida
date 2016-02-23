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
 * Operator implementing a workflow using {@link ExperimentalData1D}.
 * 
 * @author posch
 */

@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class DemoWorkflow1D extends ALDOperator {

	/** 1D Experiment
	 */
	@Parameter( label= "1D Experiment", required = true, 
			direction = Parameter.Direction.IN, 
			description = "1D Experiment",
			dataIOOrder = 1)
	protected ExperimentalData1D experiment;

	/** Smoothed 1D Experiment
	 */
	@Parameter( label= "Smoothed 1D Experiment", required = true, 
			direction = Parameter.Direction.IN, 
			description = "Smoothed 1D Experiment",
			dataIOOrder = 1)
	protected ExperimentalData1D smoothedExperiment;

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
	public DemoWorkflow1D() throws ALDOperatorException {
	}

	@Override
	protected void operate() throws ALDOperatorException, ALDProcessingDAGException {
		// detect maxima in data
		DetectLocalExtrema1D detectLE = new DetectLocalExtrema1D();
		detectLE.setExperiment( this.experiment);
		detectLE.setExtremaType( ExtremaType.MAXIMUM);
		detectLE.runOp();
		
		DetectLocalExtrema1D detectLES = new DetectLocalExtrema1D();
		detectLES.setExperiment( this.smoothedExperiment);
		detectLES.setExtremaType( ExtremaType.MAXIMUM);
		detectLES.runOp();
		
		// refine maxima
		RefineLocalExtrema1D refineOp = new RefineLocalExtrema1D();
		refineOp.setExtrema( detectLE.getExtrema());
		refineOp.setExtremaSmoothedData( detectLES.getExtrema());
		refineOp.setEpsilon( 3);
		refineOp.runOp();
		
		// detect baseline
		DetectBaseline1D baselineOp = new DetectBaseline1D();
		baselineOp.setExperiment( this.experiment);
		baselineOp.runOp();
		
		// correct maxima
		CorrectForBaseline1D correctOp = new CorrectForBaseline1D();
		correctOp.setExtrema( refineOp.getRefinedExtrema());
		correctOp.setBaseline( baselineOp.getBaseline());
		correctOp.runOp();
		
		this.correctedExtrema = correctOp.getCorrectedExtrema();
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
	 * @return the smoothedExperiment
	 */
	public ExperimentalData1D getSmoothedExperiment() {
		return smoothedExperiment;
	}

	/**
	 * @param smoothedExperiment the smoothedExperiment to set
	 */
	public void setSmoothedExperiment(ExperimentalData1D smoothedExperiment) {
		this.smoothedExperiment = smoothedExperiment;
	}

	/**
	 * @return the correctedExtrema
	 */
	public Extrema1D getCorrectedExtrema() {
		return correctedExtrema;
	}

}
