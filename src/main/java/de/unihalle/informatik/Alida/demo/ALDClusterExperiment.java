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

import java.util.List;
import java.util.Set;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDProcessingDAGException;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;

/**
 * Demo operator to cluster experimental data.
 * <p>
 * First, optionally the data are normalized.
 * Second, again optionally, the data are subjected to a PCA.
 * Finally clustered via k-means.
 * 
 * @author posch
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
                level=ALDAOperator.Level.APPLICATION)
public class ALDClusterExperiment extends ALDOperator {

    /**
     * Input data
     */
    @Parameter(label= "Experimental data", required = true, 
          direction = Parameter.Direction.IN, description = "Experimental data to cluster")
    private ExperimentalData experiment = null;

    @Parameter(label="Normalize data", direction = Parameter.Direction.IN, required=false,
    		description = "Initially normalize the experimental data")
    private Boolean doNormalize = true;

    @Parameter(label="use PCA", direction = Parameter.Direction.IN, required=false,
    		description = "apply PCA before clustering")
    private Boolean doPCA = true;

	/**
     * Clusters represented as a list of clusters, where each cluster
     * is a set of experiment Ids, i.e. indices into to input experimental data.
     */
    @Parameter( label= "Clusters",  
          direction = Parameter.Direction.OUT, description = "Clusters")
    private transient List<Set<Integer>> clusters= null;

    /**
     * Normalization experiment in case normalization was requested.
     */
    @Parameter(label= "Normalized experimental data",
          direction = Parameter.Direction.OUT, description = "Normalized experimental data")
    private ExperimentalData normalizedExperiment = null;

  /**
     * Default constructor.
     * @throws ALDOperatorException
     */
    public ALDClusterExperiment() throws ALDOperatorException {
    }

    /**
     * Constructor.
     * 
     * @param _experiment    Experimental data
     * @throws ALDOperatorException
     */
    public ALDClusterExperiment(ExperimentalData _experiment) throws ALDOperatorException {
        this.experiment = _experiment;
    }

    @Override
    protected void operate()  throws ALDOperatorException,ALDProcessingDAGException {
    	if ( verbose ) {
    		System.out.println( "ALDClusterExperiment");
    		System.out.println();
    		experiment.print();
    	}
    	
    	if ( doNormalize ) {
    		if ( ! experiment.isNormalized()) {

    			NormalizeExperimentalDataOp normalizeOp = new NormalizeExperimentalDataOp(experiment);
    			normalizeOp.runOp();
    			normalizedExperiment = normalizeOp.getResult();

    			if ( verbose ) {
    				System.out.println( "   normalize");
    				experiment.print();
    			}
    		} else {
    			normalizedExperiment = experiment;
    		}
    	}
    	
    	ExperimentalData pcaExperiment;
    	if ( doPCA ) {
    		ALDPCAOp pcaOp = new ALDPCAOp();
    		pcaOp.setExperiment(experiment);
    		pcaOp.setNumComponents(3);
    		pcaOp.runOp();
    		pcaExperiment = pcaOp.getResult();
    		
    		if ( verbose ) {
    			System.out.println( "   do PCA");
    			pcaExperiment.print();
    		}

    	} else {
    		pcaExperiment = experiment;
    	}
    	
    	ALDKmeansOp kmeansOp = new ALDKmeansOp();
    	kmeansOp.setExperiment(pcaExperiment);
    	kmeansOp.setNumCluster(3);
    	kmeansOp.runOp();
    	this.clusters = kmeansOp.getClusters();
    	
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
	 * @return the doNormalize
	 */
	public Boolean getDoNormalize() {
		return doNormalize;
	}

	/**
	 * @param doNormalize the doNormalize to set
	 */
	public void setDoNormalize(Boolean doNormalize) {
		this.doNormalize = doNormalize;
	}

	/**
	 * @return the doPCA
	 */
	public Boolean getDoPCA() {
		return doPCA;
	}

	/**
	 * @param doPCA the doPCA to set
	 */
	public void setDoPCA(Boolean doPCA) {
		this.doPCA = doPCA;
	}

	/**
	 * @return the normalizedExperiment
	 */
	public ExperimentalData getNormalizedExperiment() {
		return normalizedExperiment;
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
<p>flag of type <i>Boolean</i> to indicate if data should be normalized
<li>
<p>flag of type <i>Boolean</i> to indicate if PCA should be applied
</ul>
</li><li>
<p><b>output:</b>
<ul><li>
<p>cluster results, type <i>List&lt;Set&lt;Integer&gt;&gt;</i></p>
</li>
<li>
<p>if requested, the normalized data set
</li></ul>
</p>
</li></ul>

<p>This operator clusters the given experimental data:
<ul>
<li> first, optionally the data are normalized
<li> second, again optionally, the data are subjected to a PCA
<li> finally, the clustering is done using k-means
</ul>
 
END_MITOBO_ONLINE_HELP*/


