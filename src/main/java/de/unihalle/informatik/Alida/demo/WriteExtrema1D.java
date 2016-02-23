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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import de.unihalle.informatik.Alida.datatypes.ALDFileString;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator to write {@code ExtremaalData1D} to a file.
 * <p>
 * For a description of the format see {@link ReadExtrema1D}
 * 
 * @author posch
 */

@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class WriteExtrema1D extends ALDOperator {
	/** Filename
	 */
	@Parameter( label = "Filename", required = true,
			direction = Parameter.Direction.IN,
			description = "Filename",
			dataIOOrder = 1)
	ALDFileString filename;

	/** 1D Experiment
	 */
	@Parameter( label= "1D Experiment",
			direction = Parameter.Direction.IN, 
			description = "1D Extrema",
			dataIOOrder = 2)
	protected Extrema1D extrema;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public WriteExtrema1D() throws ALDOperatorException {
	}

	@Override
	protected void operate() throws ALDOperatorException, ALDProcessingDAGException {
		this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
						"Starting to write 1D Extrema..."));
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(filename.getFileName()));
		} catch (FileNotFoundException e1) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"WriteExtremaalData1D can not open file " + filename);
		} catch (IOException e) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"WriteExtremaalData1D can not open file " + filename + " for writing as it is a directory");
		}
		
		try {
			for ( int i = 0 ; i < extrema.getX().size() ; i++ ) {
				writer.write(  extrema.getX(i) + "," + extrema.getY(i));
				writer.newLine();
			}
		} catch (IOException e) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"WriteExtremaalData1D error writing to file " + filename);
		}	
		
		try {
			writer.close();
		} catch (IOException e) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"WriteExtremaalData1D can not close file " + filename);
		}
		
		ALDOperator.writeHistory( extrema, filename.getFileName());
	}
}
