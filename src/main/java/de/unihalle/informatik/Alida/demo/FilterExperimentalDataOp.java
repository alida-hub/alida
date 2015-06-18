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

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;

/**
 * Demo operator to filter experimental data.
 * Remove features with low variance.
 * 
 * @author posch
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
                level=ALDAOperator.Level.APPLICATION)
public class FilterExperimentalDataOp extends ALDOperator {

    /**
     * Input data
     */
    @Parameter(label= "Experimental data", required = true, 
          direction = Parameter.Direction.IN, description = "Experimental data to be filtered")
    private ExperimentalData experiment = null;

    /**
     * Filtered experiment to be returned
     */
    @Parameter(label= "Filtered experiment",  
          direction = Parameter.Direction.OUT, description = "Filtered experiment")
    private transient ExperimentalData result = null;

    /**
     * Default constructor.
     * @throws ALDOperatorException
     */
    public FilterExperimentalDataOp() throws ALDOperatorException {
    }

    /**
     * Constructor.
     * 
     * @param _experiment    Experimental data
     * @throws ALDOperatorException
     */
    public FilterExperimentalDataOp(ExperimentalData _experiment) throws ALDOperatorException {
        this.experiment = _experiment;
    }

    @Override
    protected void operate()  throws ALDOperatorException,ALDProcessingDAGException {
        // this is currently just a dummy

        this.result = new ExperimentalData( this.experiment.getDescription() + " (Filtered)", 
        		experiment.getData(), false);
    }
 
    /** Get value of result.
      * Explanation: Normalized experiment.
      * @return value of result
      */
    public ExperimentalData getResult(){
        return this.result;
    }

    /** Get value of experiment.
      * Explanation: Experimental data to be normalized.
      * @return value of data
      */
    public ExperimentalData getExperiment(){
        return this.experiment;
    }

    /** Set value of data.
      * Explanation: Experimental data to be normalized.
      * @param value New value of data
      */
    public void setExperiment( ExperimentalData value){
        this.experiment = value;
    }
}

/*BEGIN_MITOBO_ONLINE_HELP

<b>Attention! This is just a dummy operator without real functionality!</b>

<ul><li>
<p><b>input:</b>
<ul>
<li>
<p>data set of type <i>ExperimentalData</i></p>
</ul>
</li><li>
<p><b>output:</b>
<ul>
<li>
<p>filtered data set of type <i>ExperimentalData</i>
</li></ul>
</p>
</li></ul>

<p>This operator filters the given data. Currently the filtering operation is
identity, i.e. the output data equals the input data.
 
END_MITOBO_ONLINE_HELP*/
