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
 * Demo operator to normalize experimental data.
 * Normalization just makes each of the features mean free.
 * 
 * @author posch
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
                level=ALDAOperator.Level.APPLICATION)
public class NormalizeExperimentalDataOp extends ALDOperator {

    /**
     * Input data
     */
    @Parameter(label= "Experimental data", required = true, 
          direction = Parameter.Direction.IN, description = "Experimental data to be normalized")
    private ExperimentalData experiment = null;

    /**
     * Normalized experiment to be returned
     */
    @Parameter(label= "Normalized experiment",  
          direction = Parameter.Direction.OUT, description = "Normalized experiment")
    private transient ExperimentalData result = null;

    /**
     * Default constructor.
     * @throws ALDOperatorException
     */
    public NormalizeExperimentalDataOp() throws ALDOperatorException {
    }

    /**
     * Constructor.
     * 
     * @param _experiment    Experimental data
     * @throws ALDOperatorException
     */
    public NormalizeExperimentalDataOp(ExperimentalData _experiment) throws ALDOperatorException {
        this.experiment = _experiment;
    }

    @Override
    protected void operate()  throws ALDOperatorException,ALDProcessingDAGException {
        
	//SNIPPETCODE:useBegin
        ApplyToMatrix normalizeOp = new ApplyToMatrix( this.experiment.getData());
        normalizeOp.setSummarizeMode( ApplyToMatrix.SummarizeMode.COLUMN);
        normalizeOp.setSummarizeOp( new ALDArrayMean());
        normalizeOp.runOp();
	//SNIPPETCODE:useEnd

        Double[][] normalizedData = (Double[][])(this.experiment.getData().clone());
        for ( int e = 0; e < this.experiment.getNumExperiments(); e++ ) {
            for ( int f = 0 ; f < this.experiment.getNumFeatures()  ; f++ ) {
                normalizedData[e][f] -=  normalizeOp.getSummaries()[f];
            }
        }

        this.result = new ExperimentalData( this.experiment.getDescription() + " (Normalized)", normalizedData, true);
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

<ul><li>
<p><b>input:</b>
<ul><li>
<p>data of type 
<i>
<a href="de.unihalle.informatik.Alida.demo.ExperimentalData.html">
ExperimentalData
</a>
</i></p>
</li></ul>
</p>
</li><li>
<p><b>output:</b>
<ul><li>
<p>dat of type <i>
<a href="de.unihalle.informatik.Alida.demo.ExperimentalData.html">
ExperimentalData
</a>
</i> </p>
</li></ul>
</p>
</li></ul>

<p>The operator takes as input an object of the parametrized class 
<i>
<a href="de.unihalle.informatik.Alida.demo.ExperimentalData.html">
ExperimentalData
</a></i>, 
which contains data in terms of a two-dimensional array, 
a descriptive string and a flag for indicating data normalization. 
If the input data is already normalized, it is just copied to the output element, 
otherwise data normalization is performed.</p>

<p>Note that the input data object can be configured similiar to operators, i.e. clicking the corresponding configure button pops-up a window where the different parameters of the class can be edited.</p>
END_MITOBO_ONLINE_HELP*/
