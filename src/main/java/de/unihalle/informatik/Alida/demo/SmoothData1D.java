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

import java.util.ArrayList;
import java.util.Collections;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator to smooth the data of an ExperimentalData1D
 * 
 * @author posch
 */
@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.STANDARD)
public class SmoothData1D extends ALDOperator {

	public enum SmoothingMethod {
		MEDIAN, MEAN, GAUSSIAN
	}

	/** 1D Experiment
	 */
	@Parameter( label= "1D Experiment", required = true, 
			direction = Parameter.Direction.IN, 
			description = "1D Experiment",
			dataIOOrder = 1)
	protected ExperimentalData1D experiment;

	/** Smoothing method
	 */
	@Parameter( label = "Smoothing method", required = true,
			direction = Parameter.Direction.IN,
			callback = "smoothingMethodChanged",
			description = "Smoothing method",
			dataIOOrder = 2)
	SmoothingMethod smoothingMethod = SmoothingMethod.MEDIAN;

	/** Window width
	 */
	@Parameter( label = "Window width", required = true,
			direction = Parameter.Direction.IN,
			description = "Smoothing method (should be uneven)",
			dataIOOrder = 3)
	Integer width = 3;

	/** Standard deviation of Gaussian
	 */
	@Parameter( label = "Standdard deviation of Gaussian", required = true,
			direction = Parameter.Direction.IN,
			description = "Standdard deviation of Gaussian",
			dataIOOrder = 3)
	Float sigma = 1.0F;

	/** Smoothed 1D Experiment
	 */
	@Parameter( label= "Smothed 1D Experiment",  
			direction = Parameter.Direction.OUT, 
			description = "Smothed1D Experiment",
			dataIOOrder = 1)
	protected ExperimentalData1D smoothedExperiment;


	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public SmoothData1D() throws ALDOperatorException {
	}

	@Override
	protected void operate() {
		this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
						"Starting to smooth 1D Data..."));

		Double[] smoothedData;
		if ( smoothingMethod == SmoothingMethod.MEDIAN) {
			smoothedData = median( experiment.getData(), this.width);
		} else {
			Double[]  kernel;

			if ( smoothingMethod == SmoothingMethod.MEAN ) {				
				kernel = new Double[width];
				for ( int i = 0 ; i < width ; i++)
					kernel[i] = 1.0/width;
			} else { // GAUSSIAN
				Float floatWidth = 5.0F*sigma;
				int width =  floatWidth.intValue();
				if ( width % 2 != 1)
					width++;
				int halfWidth = (int)(Math.floor(width/2.0));

				kernel = new Double[ width];
				kernel[halfWidth] = 1.0;
				Double sum = 1.0;
				for ( int i = 1 ; i <= halfWidth ; i++ ) {
					Double val =  Math.exp( -(i*i)/(sigma*sigma));
					kernel[halfWidth+i] = val;
					kernel[halfWidth-i] = val;
					sum += (val+val);
				}

				for ( int i = 0 ; i < width ; i++)
					kernel[i] /= sum;

			}

			if ( verbose ) {
				System.out.println("Kernel");
				for ( int i = 0 ; i < kernel.length ; i++)
					System.out.println( kernel[i]);
			}

			smoothedData = convolve( experiment.getData(), kernel);
		}

		smoothedExperiment = new ExperimentalData1D( experiment.getDescription() + " (smoothed)", 
				smoothedData, experiment.isBaselineCorrected(), experiment.getTimeResolution());
	}

	/** Compute the median of {@code data} within a window of width {@code width}. Values outside the range
	 * of {@code data} are ignored.
	 * 
	 * @param data
	 * @param width
	 * @return
	 */
	private Double[] median(Double[] data, Integer width) {
		Double[] smoothedData = new Double[ data.length];
		ArrayList<Double> window = new ArrayList<Double>(width);

		int lowerHalfWidth = (int) Math.floor( width/2.0);
		int upperHalfWidth = width-1-lowerHalfWidth;

		for ( int i = 0 ; i < lowerHalfWidth ; i++ ) {
			window.clear();
			for ( int l = 0 ; l <= i + upperHalfWidth ; l++) {
				window.add( data[l]);
			}
			Collections.sort(window);
			smoothedData[i] = window.get( window.size()/2);
		}

		for ( int i = lowerHalfWidth ; i < data.length - upperHalfWidth ; i++) {
			window.clear();
			for ( int l = i - lowerHalfWidth ; l <= i + upperHalfWidth ; l++)
				window.add( data[l]);

			Collections.sort(window);
			smoothedData[i] = window.get(window.size()/2);
		}

		for ( int i =data.length - upperHalfWidth ; i < data.length ; i++) {
			window.clear();
			for ( int l = i - lowerHalfWidth ; l < data.length; l++) 
				window.add( data[l]);

			Collections.sort(window);
			smoothedData[i] = window.get(window.size()/2);
		}

		return smoothedData;
	}

	/** Convolve {@code data} with the {@code kernel}. Values outside the range
	 * of {@code data} are ignored.
	 * 
	 * @param data
	 * @param kernel
	 * @return
	 */
	private Double[] convolve(Double[] data, Double[] kernel) {
		Double[] smoothedData = new Double[ data.length];

		int lowerHalfWidth = (int) Math.floor( kernel.length/2.0);
		int upperHalfWidth = kernel.length-1-lowerHalfWidth;

		for ( int i = 0 ; i < lowerHalfWidth ; i++ ) {
			double sum = 0;
			double sumWeights = 0.0;
			for ( int l = 0 ; l <= i + upperHalfWidth ; l++) {
				//System.out.println("    " + (l + kernel.length -1 - (i+upperHalfWidth)));
				sum += data[l] * kernel[ l + kernel.length -1 - (i+upperHalfWidth) ];
				sumWeights += kernel[l + kernel.length -1 - (i+upperHalfWidth)];
			}
			smoothedData[i] = sum/sumWeights;
		}

		double sumWeights = 0.0;
		for ( int l = 0 ; l < kernel.length ; l++)
			sumWeights += kernel[l];

		for ( int i = lowerHalfWidth ; i < data.length - upperHalfWidth ; i++) {
			double sum = 0;
			for ( int l = i - lowerHalfWidth ; l <= i + upperHalfWidth ; l++)
				sum += data[l] * kernel[l-(i-lowerHalfWidth)];
			smoothedData[i] = sum/sumWeights;
		}

		for ( int i =data.length - upperHalfWidth ; i < data.length ; i++) {
			double sum = 0;
			sumWeights = 0.0;
			for ( int l = i - lowerHalfWidth ; l < data.length; l++) {
				//System.out.println(l- (i-lowerHalfWidth));
				sum += data[l] * kernel[l- (i-lowerHalfWidth)];
			}
			smoothedData[i] = sum/sumWeights;
		}

		return smoothedData;
	}

	/**
	 * Callback routine to change parameters on change of smoothing method.
	 * @throws ALDOperatorException 
	 */
	@SuppressWarnings("unused")
	private void smoothingMethodChanged() throws ALDOperatorException {
		try {
			if (this.smoothingMethod == SmoothingMethod.GAUSSIAN) {
				if (this.hasParameter("width")) {
					this.removeParameter("width");
				}

				if (!this.hasParameter("sigma")) {
					this.addParameter("sigma");
				}
			} else {
				if (this.hasParameter("sigma")) {
					this.removeParameter("sigma");
				}

				if (!this.hasParameter("width")) {
					this.addParameter("width");
				}
			}

		} catch (SecurityException e) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"[SmoothData1D::smoothingMethodChanged()] failedl!");
		} catch (ALDOperatorException e) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"[SmoothData1D::smoothingMethodChanged()] failedl!");
		}
	}


}
