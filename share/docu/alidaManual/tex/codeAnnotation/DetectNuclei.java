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

public class DetectNuclei extends MTBOperator {
    @Parameter( label= "inImg", direction=Parameter.Direction.IN,
                            description = "Input image")
    Image inImg;

    @Parameter( label= "resultImg", required = true, direction=Parameter.Direction.OUT,
                                description = "Result image")
    Image resultImg;

	public DetectNuclei( Image img) throws ALDOperatorException {
		this.inImg = img;
	}

	public DetectNuclei() throws ALDOperatorException {
		completeDAG = false;
	}

	protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		System.out.println( "DetectNuclei::operate");

		MTBOtsuThresholding otsu = new MTBOtsuThresholding();
		otsu.setVerbose( this.getVerbose());
		otsu.setInImg( this.inImg);
		otsu.runOp( null);

		MTBFillHoles fill = new MTBFillHoles();
		fill.setVerbose( this.getVerbose());
		fill.setInImg( otsu.getResultImg());
		fill.runOp( null);

		this.resultImg = fill.getResultImg();
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
