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
import de.unihalle.informatik.Alida.operator.ALDOpParameterDescriptor;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;
import de.unihalle.informatik.Alida.annotations.Parameter.Direction;
import de.unihalle.informatik.Alida.annotations.Parameter.ParameterModificationMode;

/**
 * Operator to test dynamic parameters and callback function.
 * 
 * @author posch
 */
@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
		level=ALDAOperator.Level.STANDARD)
public class ALDDynamicOp extends ALDOperator {
	private final static String intParameterName = "intIn";
	private final static String floatParameterName = "floatIn";
	@SuppressWarnings("unused")
	private final static String nameNotAnnotatedParameter = "notAnnotatedString";

	
	@Parameter( label="infoText", info=true, required=true, direction=Direction.IN, dataIOOrder=0)
	private String infoText= "<html>This might be an explanation,<br> I think!</html>";
	
	@Parameter( label="Info:", info=true, required=true, direction=Direction.IN, dataIOOrder=1)
	private String info= "This might be an explanation,\n I think!";
	
	//SNIPPETCODE:parametersBegin
	@Parameter( label= "useRealData", required = true, dataIOOrder = 2,
			paramModificationMode = ParameterModificationMode.MODIFIES_INTERFACE,
			callback = "initDataType", direction = Parameter.Direction.IN,	
			description = "Should we use real or integral data?.")
	private boolean useRealData = false;
	//SNIPPETCODE:parametersEnd
	
	@Parameter( label= "intiIn", required = true, dataIOOrder = 3,
	direction = Parameter.Direction.IN,	description = "int in")
	private int intIn = 0;
	
	@Parameter( label= "float in", required = true, dataIOOrder = 3,
			direction = Parameter.Direction.IN,	description = "float in")
	private float floatIn;

	@Parameter( label="Result:", direction=Direction.OUT)
	private String result;

	
	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDDynamicOp() throws ALDOperatorException {
		initDataType();
	}

	@Override
    protected void operate() throws ALDOperatorException {
		if ( useRealData ) {
			result = "float value = " + this.getParameter( floatParameterName);
			System.out.println( "   float value = " + this.getParameter( floatParameterName));
		} else {
			result = "int value = " + this.getParameter( intParameterName);
			System.out.println( "   int value = " + this.getParameter( intParameterName));
		}
	}
	
	//SNIPPETCODE:Begin
	public void initDataType() throws ALDOperatorException {
		if ( verbose ) System.out.println( "ALDDynamicOp::initDataType");
		if ( useRealData) {
			if ( hasParameter(intParameterName)) {
				this.removeParameter(intParameterName);
			}

			if ( ! hasParameter(floatParameterName)) {
				this.addParameter( floatParameterName);
			}
		} else {
			if ( hasParameter(floatParameterName)) {
				this.removeParameter(floatParameterName);
			}

			if ( ! hasParameter(intParameterName)) {
				this.addParameter( intParameterName);
 			}
		}

		// add not annotated string parameter
		if ( !hasParameter( nameNotAnnotatedParameter) ) {
			ALDOpParameterDescriptor descr = new ALDOpParameterDescriptor(nameNotAnnotatedParameter, Direction.IN, false, String.class, "not annotated String parameter", 
					nameNotAnnotatedParameter, false, null, 10, Parameter.ExpertMode.STANDARD, false, "", 
					ParameterModificationMode.MODIFIES_NOTHING, false);
			this.addParameter(descr);
		}
		
		if ( verbose ) this.printInterface();
	}
	//SNIPPETCODE:End

}
/*BEGIN_MITOBO_ONLIINE_HELP

<ul><li>
<p><b>input:</b>
<ul><li>
<p>one-dimensional array of type <i>Double []</i></p>
</li></ul>
</p>
</li><li>
<p><b>output:</b>
<ul><li>
<p>value of type <i>Double</i></p>
</li></ul>
</p>
</li></ul>

<p>The operator calculates the mean value of all entries of the given array.</p>
END_MITOBO_ONLIINE_HELP*/
