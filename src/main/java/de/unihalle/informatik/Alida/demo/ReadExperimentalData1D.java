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
	protected void operate() {
		this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
						"Starting to read 1D Data..."));
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename.getFileName()));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		String descr;
		String str;
		try {
			//first line is description if starting with '#'
			str = reader.readLine();
			if ( str != null && str.length() > 0 && str.charAt(0) == '#') {
				descr = str;
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
			experiment.print();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
