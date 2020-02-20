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
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.operator.events.ALDOperatorExecutionProgressEvent;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;

/**
 * Demo operator to apply a summarizing operation to a 2D array in row or column wise fashion.
 * The summarizing operator is assumed to take a 1D array as input and
 * return a summarizing scalar, e.g. the mean or the maxium value.
 * 
 * @author posch
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
	level=ALDAOperator.Level.APPLICATION,
	shortDescription="Applies the given summarizing operator either row- " 
			+ "or column-wise to the input matrix.")
public class ApplyToMatrix extends ALDOperator {

    /** Choose row or colum wise sum
      */
    public static enum SummarizeMode {
      /** row wise */
      ROW,
      /** column wise */
      COLUMN
    }

    /**
     * Input matrix
     */
    @Parameter( label= "Input matrix", required = true, 
          direction = Parameter.Direction.IN, description = "Input matrix.")
    private Double[][] matrix;

    /**
     * Mode of summarizing
     */
    @Parameter( label= "Summarize mode", required = true, 
          direction = Parameter.Direction.IN, description = "Sum over columns or rows.")
    private SummarizeMode summarizeMode = SummarizeMode.ROW;

    //SNIPPETCODE:parametersBegin
    /**
     * Summarizing opererator
     */
    @Parameter( label= "Summarizing operator", required = true, 
          direction = Parameter.Direction.IN, 
          description = "Specifies the summarizing operation to apply")
    private ALDSummarizeArrayOp summarizeOp;
    //SNIPPETCODE:parametersEnd

    /**
     * 1D Array of summaries.
     */
    @Parameter( label= "summaries",  
          direction = Parameter.Direction.OUT, description = "Row or column wise summaries")
    private transient Double[] summaries = null;

    /**
     * Supplemental to request elapsed time to be returned
     */
    @Parameter( label= "Return elapsed time", 
          direction = Parameter.Direction.IN, description = "Request elapsed time consumed to be returned",
        supplemental=true)
    private boolean returnElapsedTime = false;

    /**
     * Elpased time 
     */
    @Parameter( label= "Elapsed time",  
          direction = Parameter.Direction.OUT, description = "Elapsed time of operation in milliseconds",
        supplemental=true)
    private long elapsedTime;

    /**
     * Default constructor.
     * @throws ALDOperatorException
     */
    public ApplyToMatrix() throws ALDOperatorException {
    }

    /**
     * Constructor.
     * 
     * @param _matrix    Input matrix.
     * @throws ALDOperatorException
     */
    public ApplyToMatrix(Double[] [] _matrix) throws ALDOperatorException {
        this.matrix = _matrix;
    }

    @Override
    protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		this.fireOperatorExecutionProgressEvent(
				new ALDOperatorExecutionProgressEvent(this, 
					"Starting to apply an operator to a matrix..."));

        if ( this.returnElapsedTime ) 
            this.elapsedTime = System.currentTimeMillis();

        if ( this.matrix == null ) {
        	throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED, 
        			"[ApplyToMatrix::operate()] data matrix is null!");
        }

        // calculate summaries
        if ( this.summarizeMode == SummarizeMode.ROW ) {
            this.summaries = new Double[this.matrix.length];
            for ( int row = 0 ; row < this.matrix.length ; row++ ) {
                this.summarizeOp.setData(this.matrix[row]);
                this.summarizeOp.runOp(HidingMode.HIDDEN);
                this.summaries[row] = this.summarizeOp.getSummary();
            }
        } else {
            this.summaries = new Double[this.matrix[0].length];
            Double[] tmp = new Double[this.matrix.length];
            for ( int col = 0 ; col < this.matrix[0].length ; col++ ) {
                for ( int row = 0 ; row < this.matrix.length ; row++ )
                    tmp[row] = this.matrix[row][col];

                this.summarizeOp.setData(tmp);
                this.summarizeOp.runOp(HidingMode.HIDDEN);
                this.summaries[col] = this.summarizeOp.getSummary();
            }
        }

        if ( this.returnElapsedTime ) 
            this.elapsedTime = System.currentTimeMillis() - this.elapsedTime;
    }
    
    // ==============================================================
    // Getter and setter methods
    /** Get value of returnElapsedTime.
      * Explanation: Request elapsed time consumed to be returned.
      * @return value of returnElapsedTime
      */
    public boolean getReturnElapsedTime(){
        return this.returnElapsedTime;
    }

    /** Set value of returnElapsedTime.
      * Explanation: Request elapsed time consumed to be returned.
      * @param value New value of returnElapsedTime
      */
    public void setReturnElapsedTime( boolean value){
        this.returnElapsedTime = value;
    }

    /** Get value of verbose.
      * Explanation: Verbose flag.
      * @return value of verbose
      */
    @Override
    public java.lang.Boolean getVerbose(){
        return this.verbose;
    }

    /** Set value of verbose.
      * Explanation: Verbose flag.
      * @param value New value of verbose
      */
    @Override
    public void setVerbose( java.lang.Boolean value){
        this.verbose = value;
    }

    /** Get value of summarizeOp.
      * Explanation: Specifies the summarizing operation to apply.
      * @return value of summarizeOp
      */
    public de.unihalle.informatik.Alida.demo.ALDSummarizeArrayOp getSummarizeOp(){
        return this.summarizeOp;
    }

    /** Set value of summarizeOp.
      * Explanation: Specifies the summarizing operation to apply.
      * @param value New value of summarizeOp
      */
    public void setSummarizeOp( de.unihalle.informatik.Alida.demo.ALDSummarizeArrayOp value){
        this.summarizeOp = value;
    }

    /** Get value of summarizeMode.
      * Explanation: Sum over columns or rows..
      * @return value of summarizeMode
      */
    public de.unihalle.informatik.Alida.demo.ApplyToMatrix.SummarizeMode getSummarizeMode(){
        return this.summarizeMode;
    }

    /** Set value of summarizeMode.
      * Explanation: Sum over columns or rows..
      * @param value New value of summarizeMode
      */
    public void setSummarizeMode( de.unihalle.informatik.Alida.demo.ApplyToMatrix.SummarizeMode value){
        this.summarizeMode = value;
    }

    /** Get value of elapsedTime.
      * Explanation: Elapdes time of operation in milliseconds.
      * @return value of elapsedTime
      */
    public long getElapsedTime(){
        return this.elapsedTime;
    }

    /** Set value of elapsedTime.
      * Explanation: Elapdes time of operation in milliseconds.
      * @param value New value of elapsedTime
      */
    public void setElapsedTime( long value){
        this.elapsedTime = value;
    }

    /** Get value of matrix.
      * Explanation: Input matrix..
      * @return value of matrix
      */
    public java.lang.Double[][] getMatrix(){
        return this.matrix;
    }

    /** Set value of matrix.
      * Explanation: Input matrix..
      * @param value New value of matrix
      */
    public void setMatrix( java.lang.Double[][] value){
        this.matrix = value;
    }

    /** Get value of summaries.
      * Explanation: Row or column wise summaries.
      * @return value of summaries
      */
    public java.lang.Double[] getSummaries(){
        return this.summaries;
    }

    /** Set value of summaries.
      * Explanation: Row or column wise summaries.
      * @param value New value of summaries
      */
    public void setSummaries( java.lang.Double[] value){
        this.summaries = value;
    }

    @Override
    public String getDocumentation() {
    	return "<ul><li>\n" + 
    			"<p><b>inputs:</b>\n" + 
    			"<ul><li>\n" + 
    			"<p>two-dimensional array of type <i>Double [][]</i></p>\n" + 
    			"</li><li>\n" + 
    			"<p>mode of operation on array, i.e. row- or column-wise</p>\n" + 
    			"</li><li>\n" + 
    			"<p>operator to be applied to array</p>\n" + 
    			"</li></ul>\n" + 
    			"</p>\n" + 
    			"</li><li>\n" + 
    			"<p><b>output:</b>\n" + 
    			"<ul><li>\n" + 
    			"<p>one-dimensional array of type <i>Double</i> with summarized values</p>\n" + 
    			"</li></ul>\n" + 
    			"</p>\n" + 
    			"</li></ul>\n" + 
    			"\n" + 
    			"<p>The operator applies the selected operator to each row or column (depending on the chosen operator mode) of the input array. Optionally the time can be measured and verbose output provided.</p>\n";
    }    
}
