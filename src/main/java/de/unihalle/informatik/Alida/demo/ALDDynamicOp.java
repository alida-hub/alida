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
import de.unihalle.informatik.Alida.exceptions.ALDWorkflowException;
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
	
	@Parameter( label="infoText", info=true, required=true, direction=Direction.IN)
	private String infoText= "<html>This might be an explanation,<br> I think!</html>";
	
	@Parameter( label="Info:", info=true, required=true, direction=Direction.IN)
	private String info= "This might be an explanation,\n I think!";

	@Parameter( label= "useRealData", required = true, dataIOOrder = 2,
			paramModificationMode = ParameterModificationMode.MODIFIES_INTERFACE,
			callback = "initDataType",
			direction = Parameter.Direction.IN,	description = "Should we use real or integral data?.")
	private boolean useRealData;

	@Parameter( label="Result:", info=true, required=true, direction=Direction.OUT)
	private String result = "The operator was run successfully!";

	@SuppressWarnings("unused")
	private Integer intIn;
	
//  just for testing, will result in an exception
//	@Parameter( label= "float In", required = true, dataIOOrder = 2,
//			direction = Parameter.Direction.IN,	description = "float in")
//	private Float floatIn;
	
	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDDynamicOp() throws ALDOperatorException {
		setParameter( "useRealData", false);
		setParameter(intParameterName, 0);
	}

	@Override
    protected void operate() throws ALDOperatorException {
		System.out.println( "ALDDynamicOp::operate useRealData = " + useRealData);

		if ( useRealData ) {
			System.out.println( "   float value = " + this.getParameter( floatParameterName));
		} else {
			System.out.println( "   int value = " + this.getParameter( intParameterName));
		}
	}

	public void initDataType() throws ALDOperatorException {
		System.out.println( "ALDDynamicOp::initDataType");
		if ( useRealData) {
			if ( hasParameter(intParameterName)) {
				this.removeParameter(intParameterName);
			}

			if ( ! hasParameter(floatParameterName)) {
				ALDOpParameterDescriptor descr;
				descr = new ALDOpParameterDescriptor(floatParameterName,
						Direction.IN, false, Float.class, "float input", floatParameterName, 
						false, null, 3, 
						Parameter.ExpertMode.STANDARD, false, "", 
						Parameter.ParameterModificationMode.MODIFIES_NOTHING, false);
				this.addParameter(descr);
			}
		} else {
			if ( hasParameter(floatParameterName)) {
				this.removeParameter(floatParameterName);
			}

			if ( ! hasParameter(intParameterName)) {
				ALDOpParameterDescriptor descr = null;
				try {
					descr = new ALDOpParameterDescriptor(intParameterName,
							Direction.IN, false, Integer.class, "int input", intParameterName, 
							true, this.getClass().getDeclaredField(intParameterName), 3, 
							Parameter.ExpertMode.STANDARD, false, "", 
							Parameter.ParameterModificationMode.MODIFIES_NOTHING, false);
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				this.addParameter(descr);
 			}
		}

		this.printInterface();
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
