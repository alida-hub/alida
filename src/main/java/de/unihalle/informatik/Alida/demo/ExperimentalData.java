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

import de.unihalle.informatik.Alida.annotations.*;
import de.unihalle.informatik.Alida.operator.ALDData;

/**
 * This class is a toy implementation of experimental data for
 * a demo of Alida's parametrized classes.
 * The complete experiment consists of a number of repetitions of
 * sub experiments. In each of these the same features (measurements) are recorded.
 * <p>
 * The measurements a represented in 
 * a 2D array of Doubles, where each row represents
 * one sub experiment and the rows the distinct features.
 * 
 * @author posch
 *
 */
//SNIPPETCODE:Begin
@ALDParametrizedClass
public class ExperimentalData extends ALDData {
    @ALDClassParameter(label="description")
    private String description = null;

    /**
     * The data
     */
    @ALDClassParameter(label="data")
    private Double[][] data = null;

    /** Are the data normalized to zero mean within each feature?
     */
    @ALDClassParameter(label="Is normalized")
    private boolean normalized;

    /** 
     * Standard constructor is required
      */
    public ExperimentalData() {
    }
//SNIPPETCODE:End

    /** Constructor for an experiment.
      * Normalized is assumed to be false.
      *
      * @param  description   a textual description of the experiment
      * @param  data   measurements
      */
    public ExperimentalData( String description, Double[][] data) {    
        this( description, data, false);
    }

    /** Constructor for an experiment.
      *
      * @param  description   a textual description of the experiment
      * @param  data   measurements
      * @param  normalized   true if the data are normalized
      */
    public ExperimentalData( String description, Double[][] data, boolean normalized) {    
        this.normalized = normalized;
        this.description = description;
        this.setData( data, normalized);
    }

    public void print() {
    	System.out.println("Experimental data: \n" + this.description);
    	System.out.println("  Number of experiments = " + this.getNumExperiments());
    	System.out.println("  Number of features =    " + this.getNumFeatures());
    	
    	for ( int n = 0 ; n < this.getNumExperiments() ; n++) {
    		System.out.print( "     ");
    		for ( int f = 0 ; f < this.getNumFeatures() ; f++ )
    			System.out.print( this.data[n][f] + ",");
    		System.out.println();
    	}
    }
    
    /** Return number of sub experiments
      * @return number of sub experiments
    */
    public int getNumExperiments() {
        return data.length;
    }

    /** Return number of features
      * @return number of features
    */
    public int getNumFeatures() {
        return data[0].length;
    }

    /**
     * Return the i-th feature across all sub experiments.
     * 
     * @param i
     * @return vector of Doubles with the value of the i-th feature
     *            for all sub experiments or null if <code>i<code> is out of bounds
     */
    public Double[] getFeature( int i) {
    	if ( i >= 0 && i < this.getNumFeatures() ) {
    		Double[] features = new Double[this.getNumExperiments()];
    		for ( int n= 0 ; n < this.getNumFeatures() ; n++)
    			features[n] = data[n][i];
    		
    		return features;
    	} else {
    		return null;
    	}
    }
    
    public Double[] getSubExperiment( int n) {
    	if ( n >= 0 && n < this.getNumExperiments() ) {
    		return data[n];
    	} else {
    		return null;
    	}
    }
    
   /** Return the data
     * @return data matrix
     */
    public Double[][] getData() {
        return this.data;
    }

    /** Set the data
     * @param data matrix
     * @param normalized are the data normalized?
     */
    private void setData( Double[][] data, boolean normalized) {
        this.data = data;
        this.normalized = normalized;

    }

    /** Get description.
      * @return description
      */
    public String getDescription(){
        return description;
    }

	/**
	 * Query if data are normalized to zero mean within each feature.
	 * 
	 * @return the normalized
	 */
	public boolean isNormalized() {
		return normalized;
	}

}
/*BEGIN_MITOBO_ONLINE_HELP

<p>This class demonstrates the use of parametrized classes as operator parameters. Its member are annotated as Alida class parameters which allow the graphical operator runner to automatically configure and generate objects of this type, and pass them to an operator.</p>
END_MITOBO_ONLINE_HELP*/
