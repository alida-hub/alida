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
import de.unihalle.informatik.Alida.annotations.ALDAOperator;
import de.unihalle.informatik.Alida.annotations.Parameter;

/**
 * Operator for transposing matrices.
 * 
 * @author moeller
 */
@ALDAOperator(genericExecutionMode=ALDAOperator.ExecutionMode.ALL, 
	allowBatchMode=false)
public class ALDTransposeMatrix extends ALDOperator {

  /**
   * Input data array.
   */
  @Parameter( label= "Input data", required = true, 
  		direction = Parameter.Direction.IN, description = "Input data.")
  private int [][] data;

  /**
   * Output data array.
   */
  @Parameter( label= "Transposed array",  
  		direction = Parameter.Direction.OUT, description = "Result.")
  private transient int [][] tdata = null;

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDTransposeMatrix() throws ALDOperatorException {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param _data	Input data.
	 * @throws ALDOperatorException
	 */
	public ALDTransposeMatrix(int [][] _data) throws ALDOperatorException {
		super();
		this.data = _data;
	}

	@Override
	protected void operate() {
		int height = this.data.length;
		int width = this.data[0].length;
		this.tdata = new int[width][height];
		for (int h=0; h<height; ++h) {
			for (int w=0; w<width; ++w) {
				this.tdata[w][h] = this.data[h][w];
			}
		}
	}
}

/*BEGIN_MITOBO_ONLINE_HELP

<ul><li>
<p><b>input:</b>
<ul>
<li><p>two-dimensional array of type <i>int [][]</i>
</ul>
</p>
</li><li>
<p><b>output:</b>
<ul>
<li><p>two-dimensional array of type <i>int [][]</i>
</li></ul>
</p>
</li></ul>

<p>The 2D input array is interpreted as matrix of size m x n. 
The operator transposes this matrix and returns a transposed version sized
n x m.

END_MITOBO_ONLINE_HELP*/
