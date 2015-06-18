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

public class MTBGaussFilter extends MTBOperator {

	public MTBGaussFilter()  throws ALDOperatorException {
		completeDAG = true;
	}

    @Parameter( label= "inImg", required=true, direction=Parameter.Direction.IN,
                            description = "Input image")
    Image inImg;

    @Parameter( label= "resultImg", direction=Parameter.Direction.OUT,
                            description = "Result image")
    Image resultImg;

    @Parameter( label= "sigma", direction=Parameter.Direction.IN,
                            description = "Standard deviation of Gauss")
    float sigma = 1.4f;

	protected void operate() throws ALDOperatorException {
		System.out.println( "MTBGaussFilter::operate");

		Image in = inImg;
		// no actual work yet
		Image res = new Image();
		//res.readHistory( "abc.xml");
		this.resultImg = res;
	}

	/** Get value of sigma.
	  * Explanation: Standard deviation of Gauss.
	  * @return value of sigma
	  */
	public java.lang.Float getSigma(){
		return sigma;
	}

	/** Set value of sigma.
	  * Explanation: Standard deviation of Gauss.
	  * @param value New value of sigma
	  */
	public void setSigma( java.lang.Float value){
		this.sigma = value;
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
