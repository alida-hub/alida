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

import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.annotations.*;
import de.unihalle.informatik.MiToBo.core.exceptions.*;
import de.unihalle.informatik.MiToBo.core.operator.*;

public class MTBMedianExtended extends MTBMedian {
	
	@Parameter( label= "percentil", required = true, direction=Parameter.Direction.IN,
				description = "percentil")
	Double percentil = 2.0;

	@Parameter( label= "inImg", required = true, direction=Parameter.Direction.IN,
				description = "Input image")
	Image inImg;

	@Parameter( label= "inImg2", required = true, direction=Parameter.Direction.IN,
				description = "Input image 2")
	Image inImg2;

	public MTBMedianExtended()  throws ALDOperatorException {
		completeDAG = true;
	}

	protected void operate() throws ALDOperatorException {
		System.out.println( "MTBMedianExtended::operate");

		Image in = (Image)(this.getInImg());
		// no actual work yet
		Image res = new Image();
try {
	//res.readHistory( "abc.xml");
	MTBOperator.readHistory( this, "abc.xml");
} catch (Exception e) {
	System.err.println( "cannot read abc.xml");
}

		this.setResultImg( res);
	}

	/** Get value of percentil.
	  * Explanation: percentil.
	  * @return value of percentil
	  */
	public java.lang.Double getPercentil(){
		return percentil;
	}

	/** Set value of percentil.
	  * Explanation: percentil.
	  * @param value New value of percentil
	  */
	public void setPercentil( java.lang.Double value){
		this.percentil = value;
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
		inImg = value;
	}

	/** Get value of inImg2.
	  * Explanation: Input image 2.
	  * @return value of inImg2
	  */
	public Image getInImg2(){
		return inImg2;
	}

	/** Set value of inImg2.
	  * Explanation: Input image 2.
	  * @param value New value of inImg2
	  */
	public void setInImg2( Image value){
		this.inImg2 = value;
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
