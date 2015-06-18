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
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;

/**
 * Demo operator computing the PCA for experimental data.
 * <p>
 * The result is again of type experimental data where the sub experiments
 * are projections into the subspace computed by the PCA.
 * 
 * @author posch
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
                level=ALDAOperator.Level.APPLICATION)
public class ALDPCAOp extends ALDOperator {

    /**
     * Input data
     */
    @Parameter(label= "Experimental data", required = true, 
          direction = Parameter.Direction.IN, description = "Experimental data to subject to PCA")
    private ExperimentalData experiment = null;

    /**
     * Number of components to be used for the sub space to project into.
     */
    @Parameter(label = "Number of components", direction = Parameter.Direction.IN,
    		description = "Number of components to be used for the sub space")
    private Integer	numComponents;

    /**
	 * @return the numComponents
	 */
	public Integer getNumComponents() {
		return numComponents;
	}

	/**
	 * @param numComponents the numComponents to set
	 */
	public void setNumComponents(Integer numComponents) {
		this.numComponents = numComponents;
	}

	/**
     * Experiment with sub experiments
     * are projections into the subspace computed by the PCA
     */
    @Parameter( label= "Normalized experiment",  
          direction = Parameter.Direction.OUT, description = "Normalized experiment")
    private transient ExperimentalData result = null;

   /**
     * Default constructor.
     * @throws ALDOperatorException
     */
    public ALDPCAOp() throws ALDOperatorException {
    }

    /**
     * Constructor.
     * 
     * @param _experiment    Experimental data
     * @throws ALDOperatorException
     */
    public ALDPCAOp(ExperimentalData _experiment) throws ALDOperatorException {
        this.experiment = _experiment;
    }

    @Override
    protected void operate()  throws ALDOperatorException,ALDProcessingDAGException {
        // this is currently just a dummy
        this.result = new ExperimentalData( this.experiment.getDescription() + " (PCA)", 
        		this.experiment.getData(), false);
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
    public void setExperiment( de.unihalle.informatik.Alida.demo.ExperimentalData value){
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
<li>
<p>number of components to keep, i.e. dimension of target sub-space
</ul>
</li><li>
<p><b>output:</b>
<ul><li>
<p>data set with reduced dimensionality of type <i>ExperimentalData</i>
</li>
</ul>
</p>
</li></ul>

<p>This operator performs a principal component analysis (PCA) on the given
data set. The data is then projected into a sub-space as specified by the user
in terms of the number of components to be retained.
 
END_MITOBO_ONLINE_HELP*/

