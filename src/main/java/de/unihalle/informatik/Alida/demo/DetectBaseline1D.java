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

import de.unihalle.informatik.Alida.demo.DetectLocalExtrema1D.ExtremaType;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator to detect a baseline in the data of an ExperimentalData1D.
 * <p>
 * This is accomplished first detecting local minima in the data
 * and then fitting a line through these minima.
 * If less then two minima are detected a line is fit through the first
 * and last data point of the data.
 * 
 * @author posch
 */

@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class DetectBaseline1D extends ALDOperator {

	/** 1D Experiment
	 */
	@Parameter( label= "1D Experiment", required = true, 
			direction = Parameter.Direction.IN, 
			description = "1D Experiment",
			dataIOOrder = 1)
	protected ExperimentalData1D experiment;
	
	/**
	 * The baseline detected
	 */
	@Parameter( label= "Baseline",  
			direction = Parameter.Direction.OUT, 
			description = "Baseline",
			dataIOOrder = 1)
	protected Baseline1D baseline;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public DetectBaseline1D() throws ALDOperatorException {
	}

	@Override
	protected void operate() throws ALDOperatorException, ALDProcessingDAGException {
		// detect minima
		DetectLocalExtrema1D detectExtremaOp = new DetectLocalExtrema1D();
		detectExtremaOp.setExperiment( experiment);
		detectExtremaOp.setExtremaType(ExtremaType.MINIMUM);
		detectExtremaOp.runOp();
		Extrema1D minima = detectExtremaOp.getExtrema();
		
		if ( minima.size() >= 2) {
			double[] x = new double[minima.size()];
			double[] y = new double[minima.size()];
			
			for ( int i=0 ; i < minima.size() ; i++) {
				x[i] = minima.getX(i);
				y[i] = minima.getY(i);
			}

			baseline = fit( x, y);
		} else {
			Double[] data = experiment.getData();
			Double slope = (data[data.length-1] - data[0]);
			slope /= data.length-1;
			
			baseline = new Baseline1D( slope, data[0]);
		}
	}
	
	/** least squares fit through points given with their
	 *  {@code x} and {@code y} coordinates.
	 *  The arrays {@code x} and {@code y} are assumed to be of the same length.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Baseline1D fit( double[] x, double y[]) {
		double sumx = 0.0;
		double sumy = 0.0;

		for ( int i = 0 ; i < x.length ; i++) {
			sumx += x[i];
			sumy += y[i];
		}
		double xbar = sumx / x.length;
		double ybar = sumy / x.length;

		// second pass: compute summary statistics
		double xxbar = 0.0, xybar = 0.0;
		for (int i = 0; i < x.length; i++) {
			xxbar += (x[i] - xbar) * (x[i] - xbar);
			xybar += (x[i] - xbar) * (y[i] - ybar);
		}
		
		double slope = xybar / xxbar;
		return new Baseline1D( slope, ybar - slope * xbar); 
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
	 * @return the baseline
	 */
	public Baseline1D getBaseline() {
		return baseline;
	}
}