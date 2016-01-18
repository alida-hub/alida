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

import de.unihalle.informatik.Alida.annotations.ALDClassParameter;
import de.unihalle.informatik.Alida.annotations.ALDParametrizedClass;
import de.unihalle.informatik.Alida.operator.ALDData;

/**
 * This class holds a set of extrema with x and y coordinate of a 1D function.
 * <p>
 * Currently no unit of measurement is supported.
 *  
 * @author posch
 *
 */

@ALDParametrizedClass
public class Extrema1D extends ALDData {
	
	/** x coordinates
	 */
    @ALDClassParameter(label="x",
    		dataIOOrder = 1)
    private ArrayList<Double> x;
    
    /** y coordiates
     */
    @ALDClassParameter(label="y",
    		dataIOOrder = 2)
    private ArrayList<Double> y;

    /** 
     * Standard constructor is required
      */
    public Extrema1D() {
    	x = new ArrayList<Double>();
    	y = new ArrayList<Double>();
    }

    /** Add an extremal point {@code (x,y)}
     * 
     * @param x
     * @param y
     */
    public void addPoint( Double x, Double y) {
    	this.x.add(x);
    	this.y.add(y);
    }
    
    /** Return the x coordinate of the i-th extremal point
     * 
     * @param i
     * @return
     */
    public Double getX( int i) {
    	assert i>= 0 && i < x.size();
    	return x.get(i);
    }
    
    /** Return the y coordinate of the i-th extremal point
     * 
     * @param i
     * @return
     */
    public Double getY( int i) {
    	assert i>= 0 && i < y.size();
    	return y.get(i);
    }
    
    /** Return the number of extremal points
     * @return
     */
    public int size() {
    	return x.size();
    }

	/**
	 * @return all x coordinates
	 */
	public ArrayList<Double> getX() {
		return x;
	}

	/**
	 * @return all y coordinates
	 */
	public ArrayList<Double> getY() {
		return y;
	}
}
