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

import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.Parameter.Direction;
import de.unihalle.informatik.Alida.annotations.Parameter.ParameterModificationMode;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.operator.ALDOpParameterDescriptor;
import de.unihalle.informatik.Alida.operator.ALDOperator;

/**
 * Operator to test dynamic parameters and callback function.
 * 
 * @author posch
 */
@ALDDerivedClass
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
		level=ALDAOperator.Level.STANDARD)
public class ALDDynamicOpExtended extends ALDOperator {
	private final static String intParameterName = "intIn";
	private final static String floatParameterName = "floatIn";
	private final static String modParameter1Name = "modParameter1";
	private final static String modParameter2Name = "modParameter2";
	private final static String modParameter3Name = "modParameter3";

	@SuppressWarnings("unused")
	private final static String nameNotAnnotatedParameter = "notAnnotatedString";


	@Parameter( label="infoText", info=true, required=true, direction=Direction.IN, dataIOOrder=0)
	private String infoText= "<html>This might be an explanation,<br> I think!</html>";

	@Parameter( label="Info:", info=true, required=true, direction=Direction.IN, dataIOOrder=1)
	private String info= "This might be an explanation,\n I think!";

	//SNIPPETCODE:parametersBegin
	@Parameter( label= "useRealData", required = true, dataIOOrder = 2,
			paramModificationMode = ParameterModificationMode.MODIFIES_INTERFACE,
			callback = "initDataType", direction = Direction.IN,
			description = "Should we use real or integral data?.")
	private boolean useRealData = false;
	//SNIPPETCODE:parametersEnd

	@Parameter( label= "intiIn", required = true, dataIOOrder = 3,
	direction = Direction.IN,	description = "int in")
	private int intIn = 0;

	@Parameter( label= "float in", required = true, dataIOOrder = 3,
			direction = Direction.IN,	description = "float in")
	private float floatIn;

	@Parameter( label="Result:", direction=Direction.OUT)
	private String result;

	@Parameter( label= "modParameter1", required = true, dataIOOrder = 5,
			paramModificationMode = ParameterModificationMode.MODIFIES_INTERFACE,
			callback = "callback1", direction = Direction.IN,
			description = "First level dynamic parameter. If true, then <modParameter2> will be visible")
	private boolean modParameter1 = false;

	@Parameter( label= "modParameter2", required = true, dataIOOrder = 6,
			paramModificationMode = ParameterModificationMode.MODIFIES_INTERFACE,
			callback = "callback2", direction = Direction.IN,
			description = "Second level dynamic parameter. If true, then <modParameter3> will be visible")
	private boolean modParameter2 = false;

	@Parameter( label= "modParameter3", required = true, dataIOOrder = 7,
			direction = Direction.IN,
			description = "Third level dynamic parameter. This parameter is NOT modifying, only tested for existence")
	private boolean modParameter3 = false;

	@Parameter( label= "verbose", required = false, dataIOOrder = 11,
			paramModificationMode = ParameterModificationMode.MODIFIES_INTERFACE,
			direction = Direction.IN,
			description = "override to for debugging dynamic parameters form command line")
	private boolean verbose = false;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDDynamicOpExtended() throws ALDOperatorException {
		initDataType();
		callback1();
		callback2();
	}

	@Override
    protected void operate() throws ALDOperatorException {
		if ( verbose ) System.out.println( "ALDDynamicOp::initDataType");

		if ( useRealData ) {
			result = "float value = " + this.getParameter( floatParameterName);
			System.out.println( "   float value = " + this.getParameter( floatParameterName));
		} else {
			result = "int value = " + this.getParameter( intParameterName);
			System.out.println( "   int value = " + this.getParameter( intParameterName));
		}
		if ( hasParameter( modParameter1Name))
			System.out.println( "   modParameter1 = " + this.getParameter( modParameter1Name));
		if ( hasParameter( modParameter2Name))
			System.out.println( "   modParameter2 = " + this.getParameter( modParameter2Name));
		if ( hasParameter( modParameter3Name))
			System.out.println( "   modParameter3 = " + this.getParameter( modParameter3Name));
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

	public void callback1() throws ALDOperatorException {
		if ( verbose ) System.out.println( "ALDDynamicOp::callback1");
		if ( modParameter1) {
			if ( ! hasParameter(modParameter2Name)) {
				this.addParameter( modParameter2Name);
			}
		} else {
			if ( hasParameter(modParameter2Name)) {
				this.removeParameter(modParameter2Name);
			}
		}

		if ( verbose ) this.printInterface();
	}

	public void callback2() throws ALDOperatorException {
		if ( verbose ) System.out.println( "ALDDynamicOp::callback2");
		if ( modParameter2) {
			if ( ! hasParameter(modParameter3Name)) {
				this.addParameter( modParameter3Name);
			}
		} else {
			if ( hasParameter(modParameter3Name)) {
				this.removeParameter(modParameter3Name);
			}
		}

		if ( verbose ) this.printInterface();
	}

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
