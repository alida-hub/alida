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

import de.unihalle.informatik.Alida.annotations.ALDClassParameter;
import de.unihalle.informatik.Alida.annotations.ALDParametrizedClass;
import de.unihalle.informatik.Alida.operator.ALDData;

/**
 * This class is a toy implementation of experimental data for
 * a demo of Alida's parametrized classes.
 * <p>
 * The data consist of a time series of measurements of variable length.
 * Additional information is a descriptive string, the time resolution in milliseconds,
 * and whether baseline correction has been applied.
 * 
 * @author posch
 *
 */

@ALDParametrizedClass
public class ExperimentalData1D extends ALDData {
	
	/** Description
	 */
    @ALDClassParameter(label="description",
    		dataIOOrder = 1)
    private String description = null;

    /** The data
     */
    @ALDClassParameter(label="data",
    		dataIOOrder = 2)
    private Double[] data = null;

    /** Are the data baseline corrected?
     */
    @ALDClassParameter(label="Baseline corrected",
    			dataIOOrder = 3)
    private boolean baselineCorrected = false;
    
    @ALDClassParameter(label="Time resolution in milliseconds",
    		dataIOOrder = 4)
    private Float timeResolution = Float.NaN;

    /** 
     * Standard constructor is required
      */
    public ExperimentalData1D() {
    }

    /** Constructor for an experiment.
      * Baseline correction is assumed to be false and nothung known about
      * the time resolution.
      *
      * @param  description   a textual description of the experiment
      * @param  data   measurements
      */
    public ExperimentalData1D( String description, Double[] data) {    
        this( description, data, false, Float.NaN);
    }

    /** Constructor for an experiment.
      *
      * @param  description   a textual description of the experiment
      * @param  data   measurements
      * @param  baselineCorrected   true if the data are baseline corrected
      * @param timeResolution time resolution in millisecconds
      */
    public ExperimentalData1D( String description, Double[] data, boolean baselineCorrected, Float timeResolution) {    
        this.baselineCorrected = baselineCorrected;
        this.description = description;
        this.timeResolution = timeResolution;
        this.setData( data, baselineCorrected);
    }

    public void print() {
    	System.out.println("Experimental data: \n" + this.description);
    	System.out.println("  Number of measurements = " + this.getNumberOfMeasurements());
    	
    	System.out.print( "     ");
    	for ( int f = 0 ; f < this.getNumberOfMeasurements()-1 ; f++ )
    		System.out.print( this.data[f] + ",");
    	System.out.println( this.data[this.getNumberOfMeasurements()-1]);
    }
    
    /** Return number of measurements, i.e. the length of the 1D data
      * @return number of measurements
    */
    public int getNumberOfMeasurements() {
        return data.length;
    }

   /** Return the data
     * @return data 
     */
    public Double[] getData() {
        return this.data;
    }

    /** Set the data
     * @param data 
     * @param baselineCorrected are the data baseline corected?
     */
    private void setData( Double[] data, boolean baselineCorrected) {
        this.data = data;
        this.baselineCorrected = baselineCorrected;

    }

    /** Get description.
      * @return description
      */
    public String getDescription(){
        return description;
    }

    /** Get time resolution in milliseconds.
     * @return time resolution
     */
   public Float getTimeResolution(){
       return timeResolution;
   }

	/**
	 * Query if data are baseline corrected.
	 * 
	 * @return the normalized
	 */
	public boolean isBaselineCorrected() {
		return baselineCorrected;
	}
}