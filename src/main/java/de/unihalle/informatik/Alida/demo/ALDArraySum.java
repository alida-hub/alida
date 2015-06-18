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
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.ALDDerivedClass;

/**
 * Operator returning the sum of an 1D array.
 * 
 * @author posch
 */
//SNIPPETCODE:constructorBegin
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
        level=ALDAOperator.Level.STANDARD)
@ALDDerivedClass
public class ALDArraySum extends ALDSummarizeArrayOp {
//SNIPPETCODE:constructorEnd

    @Override
    protected void operate() {
		this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
					"Starting summation a matrix..."));

        summary = 0.0;
        for ( int i = 0 ; i < data.length ; i++ )
            summary += data[i];
    }

    /**
     * Default constructor.
     * @throws ALDOperatorException
     */
    public ALDArraySum() throws ALDOperatorException {
    }

    /**
     * Constructor.
     * @param data  Input array.
     * @throws ALDOperatorException
     */
    public ALDArraySum( Double[] data) throws ALDOperatorException {
        this.data = data;
    }

}
/*BEGIN_MITOBO_ONLINE_HELP

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

<p>The operator calculates the sum of all entries of the given array.</p>

<p>
It extends the class 
<i>
<a href="de.unihalle.informatik.Alida.demo.ALDSummarizeArrayOp.html">
ALDSummarizeArrayOp
</a>
</i>

END_MITOBO_ONLINE_HELP*/
