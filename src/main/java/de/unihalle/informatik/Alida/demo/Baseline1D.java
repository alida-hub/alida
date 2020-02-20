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

import de.unihalle.informatik.Alida.annotations.ALDClassParameter;
import de.unihalle.informatik.Alida.annotations.ALDParametrizedClass;
import de.unihalle.informatik.Alida.operator.ALDData;

/**
 * This class holds a baseline for 1D data which essentially is a
 * straight line as a 1D function.
 * <p>
 * Currently no unit of measurement is supported.
 *  
 * @author posch
 *
 */

@ALDParametrizedClass
public class Baseline1D extends ALDData {
	
	/** slope
	 */
    @ALDClassParameter(label="slope",
    		dataIOOrder = 1)
    private Double slope;

    /** intercept
     */
    @ALDClassParameter(label="intercept",
    		dataIOOrder = 2)
    private Double intercept;

    /** 
     * Standard constructor is required
      */
    public Baseline1D() {
    }

    public Baseline1D( Double slope, Double intercept) {    
        this.slope = slope;
        this.intercept = intercept;
    }

	/**
	 * @return the slope
	 */
	public Double getSlope() {
		return slope;
	}

	/**
	 * @return the intercept
	 */
	public Double getIntercept() {
		return intercept;
	}
	
	/** Return the function value of this baseline for 
	 * coordinate {@code x}.
	 * 
	 * @param x
	 * @return
	 */
	public Double getY( Double x) {
		return this.slope * x + this.intercept;
	}
}
