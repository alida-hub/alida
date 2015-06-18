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

import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.annotations.*;
import de.unihalle.informatik.MiToBo.core.operator.*;
import de.unihalle.informatik.MiToBo.core.exceptions.*;

public class ActiveContours extends MTBOperator {
	public  ActiveContours() throws ALDOperatorException {
		completeDAG = true;
	}

    @Parameter( label= "maxIter", required = true, direction=Parameter.Direction.IN,
                description = "Maximal number of iterations")
    Integer maxIter;

    @Parameter( label= "gamma", required = true, direction=Parameter.Direction.IN,
                description = "gamma")
    Float gamma;

	@Parameter( label= "inImg", required = true, direction=Parameter.Direction.IN,
                                description = "Input image")
    Image inImg;

	@Parameter( label= "nucImg", required = true, direction=Parameter.Direction.IN,
                                description = "Binary image with detected nuclei")
    Image nucImg;

	@Parameter( label= "resultImg", required = true, direction=Parameter.Direction.OUT,
                                description = "Result image")
    Image resultImg;

	protected void operate( ) throws ALDOperatorException {
		System.out.println( "ActiveContours::operate");

		Image in = this.inImg;
		// no actual work yet
		Image res = new Image();
		this.resultImg = res;
	}

/** Get value of gamma.
  * Explanation: gamma.
  * @return value of gamma
  */
public java.lang.Float getGamma(){
	return gamma;
}
/** Set value of gamma.
  * Explanation: gamma.
  * @param value New value of gamma
  */
public void setGamma( java.lang.Float value){
	this.gamma = value;
}

/** Get value of maxIter.
  * Explanation: Maximal number of iterations.
  * @return value of maxIter
  */
public java.lang.Integer getMaxIter(){
	return maxIter;
}
/** Set value of maxIter.
  * Explanation: Maximal number of iterations.
  * @param value New value of maxIter
  */
public void setMaxIter( java.lang.Integer value){
	this.maxIter = value;
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

/** Get value of nucImg.
  * Explanation: Binary image with detected nuclei.
  * @return value of nucImg
  */
public Image getNucImg(){
	return nucImg;
}
/** Set value of nucImg.
  * Explanation: Binary image with detected nuclei.
  * @param value New value of nucImg
  */
public void setNucImg( Image value){
	this.nucImg = value;
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
