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
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;

//SNIPPETCODE:declareBeginSNIPPETCODE:declareBegin
/**
 * Demo operator to calculate colum or row sums of a 2D array.
 * 
 * @author posch
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL,
				level=ALDAOperator.Level.APPLICATION)
public class MatrixSum extends ALDOperator {
//SNIPPETCODE:declareEnd

  /** Choose row or colum wise sum
    */
  public static enum SummarizeMode {
	/** row wise */
	ROW,

	/** column wise */
	COLUMN
  }

  //SNIPPETCODE:parametersBegin
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
  		direction = Parameter.Direction.IN, description = "Sum over columns or rows?")
  private SummarizeMode summarizeMode = SummarizeMode.ROW;

  /**
   * 1D Array of sums.
   */
  @Parameter( label= "sums",  
  		direction = Parameter.Direction.OUT, description = "Row or column wise sums.")
  private transient Double[] sums = null;
  //SNIPPETCODE:parametersEnd

	//SNIPPETCODE:constructorBegin
	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public MatrixSum() throws ALDOperatorException {
	}

	/**
	 * Constructor.
	 * 
	 * @param _matrix	Input matrix.
	 * @throws ALDOperatorException
	 */
	public MatrixSum(Double[] [] _matrix) throws ALDOperatorException {
		this.matrix = _matrix;
	}
	//SNIPPETCODE:constructorEnd

	@Override
	protected void operate() throws ALDOperatorException {
		if ( this.matrix == null ) {
    	throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED, 
    			"[MatrixSum::operate()] data matrix is null!");
		}

		// calculate sums
		if ( this.summarizeMode == SummarizeMode.ROW ) {
			this.sums = new Double[this.matrix.length];
			for ( int row = 0 ; row < this.matrix.length ; row++ ) {
				this.sums[row] = new Double(0.0);
				for ( int col = 0 ; col < this.matrix[0].length ; col++ )
					this.sums[row] += this.matrix[row][col];
			}
		} else {
			this.sums = new Double[this.matrix[0].length];
			for ( int col = 0 ; col < this.matrix[0].length ; col++ ) {
				this.sums[col] = new Double(0.0);
				for ( int row = 0 ; row < this.matrix.length ; row++ )
					this.sums[col] += this.matrix[row][col];
			}
		}
	}
	
	/**
	 * Returns the calculated sums.
	 * @return row or column wise sums
	 */
	public Double[] getSums() {
		return this.sums;
	}
}
/*BEGIN_MITOBO_ONLINE_HELP

<ul><li>
<p><b>input:</b>
<ul><li>
<p>two-dimensional array of type <i>Double [][]</i></p>
</li></ul>
</p>
</li><li>
<p><b>output:</b>
<ul><li>
<p>one-dimensional array of type <i>Double []</i></p>
</li></ul>
</p>
</li></ul>

<p>The operator sums-up either all rows or columns of the given array.</p>
END_MITOBO_ONLINE_HELP*/
