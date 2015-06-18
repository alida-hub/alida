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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;

/**
 * Demo operator clustering experimental data using k-means.
 * 
 * @author posch
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
                level=ALDAOperator.Level.APPLICATION)
public class ALDKmeansOp extends ALDOperator {

    /**
     * Input data
     */
    @Parameter( label= "Experimental data", required = true, 
          direction = Parameter.Direction.IN, description = "Experimental data to cluster")
    private ExperimentalData experiment = null;

    /**
     * Number of components to be used for the sub space to project into.
     */
    @Parameter(label = "Number of cluster", direction = Parameter.Direction.IN,
    		description = "Number of cluster to be computed")
    private Integer	numCluster;

	/**
     * Clusters represented as a list of clusters, where each cluster
     * is a set of experiment Ids, i.e. indices into to input experimental data.
     */
    @Parameter( label= "Clusters",  
          direction = Parameter.Direction.OUT, description = "Clusters")
    private transient List<Set<Integer>> clusters= null;

   /**
     * Default constructor.
     * @throws ALDOperatorException
     */
    public ALDKmeansOp() throws ALDOperatorException {
    }

    /**
     * Constructor.
     * 
     * @param _experiment    Experimental data
     * @throws ALDOperatorException
     */
    public ALDKmeansOp(ExperimentalData _experiment) throws ALDOperatorException {
        this.experiment = _experiment;
    }

    @Override
    protected void operate()  throws ALDOperatorException,ALDProcessingDAGException {
        // this is currently just a dummy
    	// just for the time being: each sub experiment gets its own cluster
        clusters = new Vector<Set<Integer>>(experiment.getNumExperiments());
        for ( int id = 0 ; id < experiment.getNumExperiments() ; id++ ) {
        	HashSet<Integer> cluster = new HashSet<Integer>(1);
        	cluster.add(id);
        	clusters.add( cluster);
        }
    }
 
    /** Get value of result.
      * Explanation: Normalized experiment.
      * @return value of result
      */
    public List<Set<Integer>> getClusters(){
        return this.clusters;
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
    
    /**
	 * @return the numCluster
	 */
	public Integer getNumCluster() {
		return numCluster;
	}

	/**
	 * @param numCluster the numCluster to set
	 */
	public void setNumCluster(Integer numCluster) {
		this.numCluster = numCluster;
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
<p>number of clusters to calculate, type <i>Integer</i>
</ul>
</li><li>
<p><b>output:</b>
<ul><li>
<p>cluster results, type <i>List&lt;Set&lt;Integer&gt;&gt;</i></p>
</li>
</ul>
</p>
</li></ul>

<p>This operator clusters the given experimental data applying k-means.
 
END_MITOBO_ONLINE_HELP*/

