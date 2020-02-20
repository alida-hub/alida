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

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator returning the minimum value of an 1D array.
 * 
 * @author posch
 */
@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
		level=ALDAOperator.Level.STANDARD)
public class ALDArrayMin extends ALDSummarizeArrayOp {

	@Override
	protected void operate() {
		this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
						"Starting to compute minimum of a matrix..."));

		summary = data[0];
		for ( int i = 1 ; i < data.length ; i++ )
			if ( data[i] < summary )
				summary = data[i];
	}

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDArrayMin() throws ALDOperatorException {
	}

	/**
	 * Constructor.
	 * @param data  Input array.
	 * @throws ALDOperatorException
	 */
	public ALDArrayMin( Double[] data) throws ALDOperatorException {
		this.data = data;
	}
	
	@Override
	public String getDocumentation() {
		return "<ul><li>\n" + 
				"<p><b>input:</b>\n" + 
				"<ul><li>\n" + 
				"<p>one-dimensional array of type <i>Double []</i></p>\n" + 
				"</li></ul>\n" + 
				"</p>\n" + 
				"</li><li>\n" + 
				"<p><b>output:</b>\n" + 
				"<ul><li>\n" + 
				"<p>value of type <i>Double</i></p>\n" + 
				"</li></ul>\n" + 
				"</p>\n" + 
				"</li></ul>\n" + 
				"\n" + 
				"<p>The operator extracts the minimum value from the given array.</p>\n" + 
				"\n" + 
				"<p>\n" + 
				"It extends the class \n" + 
				"<i>\n" + 
				"<a href=\"de.unihalle.informatik.Alida.demo.ALDSummarizeArrayOp.html\">\n" + 
				"ALDSummarizeArrayOp\n" + 
				"</a>\n" + 
				"</i>\n";
	}
}