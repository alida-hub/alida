/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
 *
 * Copyright (C) 2010
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
 * Fore more information on MiToBo, visit
 *
 *    http://www.informatik.uni-halle.de/mitobo/
 *
 */

// toy operator which re recursively calls itself decrementing its counter,
// until the counter is zero

import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.annotations.*;
import de.unihalle.informatik.MiToBo.core.exceptions.*;
import de.unihalle.informatik.MiToBo.core.operator.*;

public class Counter extends MTBOperator {
	public Counter()  throws ALDOperatorException {
		completeDAG = true;
	}

    @Parameter( label= "inImg", required=true, direction=Parameter.Direction.IN,
                            description = "Input image")
    Image inImg;

    @Parameter( label= "resultImg", direction=Parameter.Direction.OUT,
                            description = "Result image")
    Image resultImg;

    @Parameter( label= "count", direction=Parameter.Direction.IN,
                            description = "counter")
    Integer count;

	protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		System.out.println( "Counter::operate");

		Image in = (Image)(this.inImg);

		// no actual work yet
		if ( count > 0 ) {
			this.count--;
			this.inImg = in;
			this.runOp( null);

		} else {
			// inner operator without port connection
			MTBFillHoles fOp = new MTBFillHoles();
			fOp.setInImg( new Image());
			fOp.setVerbose( true);
			//##fOp.runOp( true);
			fOp.runOp( null);
	
			// inner operator with port connection
			MTBMedian mOp = new MTBMedian();
			mOp.setInImg( inImg);
			mOp.setVerbose( true);
			//mOp.runOp( true);
			mOp.runOp( null);

			// with connection
			this.resultImg = mOp.getResultImg();
			// with out connection
			//this.resultImg = new Image();
		}

System.out.println( "COUNTER " + this.resultImg);
	}
   
	/** Get value of count.
	  * Explanation: counter.
	  * @return value of count
	  */
	public java.lang.Integer getCount(){
		return count;
	}

	/** Set value of count.
	  * Explanation: counter.
	  * @param value New value of count
	  */
	public void setCount( java.lang.Integer value){
		this.count = value;
	}

	/** Get value of inImg.
	  * Explanation: Input image.
	  * @return value of inImg
	  */
	public Image getInImg(){
		return inImg;
	}

	/** Set value of inImg.
	  * Explanation: Input image.
	  * @param value New value of inImg
	  */
	public void setInImg( Image value){
		this.inImg = value;
	}

	/** Get value of resultImg.
	  * Explanation: Result image.
	  * @return value of resultImg
	  */
	public Image getResultImg(){
		return resultImg;
	}

	/** Set value of resultImg.
	  * Explanation: Result image.
	  * @param value New value of resultImg
	  */
	public void setResultImg( Image value){
		this.resultImg = value;
	}

}
