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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import de.unihalle.informatik.Alida.datatypes.ALDFileString;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator to read {@code ExperimentalData1D} from a file.
 * <p>
 * Subsequent lines at the beginning of the file with a leading {@code #} are interpreted as comment lines.
 * The following lines are assumed to contain exactly one double number, which represent
 * the measurements.
 * The first comment line, if present, excluding the leading {@code #} is interpreted as the 
 * description of the experiment.
 * <p>
 * Currently no unit of measurement is supported.
 * 
 * @author posch
 */

@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
              level=ALDAOperator.Level.APPLICATION)
public class ReadExperimentalData1D extends ALDOperator {
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
			direction = Parameter.Direction.OUT, 
			description = "1D Experiment",
			dataIOOrder = 1)
	protected ExperimentalData1D experiment;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ReadExperimentalData1D() throws ALDOperatorException {
	}

	@Override
	protected void operate() throws ALDOperatorException {
		this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
						"Starting to read 1D Experiment..."));
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename.getFileName()));
		} catch (FileNotFoundException e1) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"ReadExperimentalData1D can not open file " + filename);
		}
		
		String descr;
		String str;
		try {
			//first line is description if starting with '#'
			str = reader.readLine();
			if ( str != null && str.length() > 0 && str.charAt(0) == '#') {
				descr = str.substring( 1);
				str = reader.readLine();
			} else {
				descr = "";
			}
			
			// skip additional comment lines starting with '#'
			while ( str != null && str.length() > 0 && str.charAt(0) == '#' )
				str = reader.readLine();
			
			// read remaining lines assumed to contain one double each
			LinkedList<Double> values = new LinkedList<Double>();
			while ( str != null ) {
				Double value = Double.parseDouble( str);
				values.add( value);
		
				str = reader.readLine();
			}
			
			experiment = new ExperimentalData1D(descr, values.toArray( new Double[0]));
		} catch (IOException e) {
			throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
					"ReadExperimentalData1D failed to parse an ExperimentalData1D from file " + filename);
		}
	}
}
