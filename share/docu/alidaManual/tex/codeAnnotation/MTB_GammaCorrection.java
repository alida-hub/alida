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

public class MTB_GammaCorrection extends MTBOperator {

	public MTB_GammaCorrection() throws ALDOperatorException {
		completeDAG = false;
	}

    @Parameter( label= "in", required = true, direction=Parameter.Direction.IN,
                                description = "Image to gamma correct")
    Image in;

    @Parameter( label= "out", required = true, direction=Parameter.Direction.OUT,
                                description = "Gamma corrected image")
    Image out;

    @Parameter( label= "gamma", required = true, direction=Parameter.Direction.IN,
                                description = "amm")
    Float gamma = 1.0f;

	@Override
	protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		System.out.println( "MTB_GammaCorrection::operate");

		Image in;
		MTBGammaCorrection gOp = new MTBGammaCorrection();
		gOp.setVerbose( true);
		System.out.println( "MTB_GammaCorrection::operate got gOp");
		in = (Image)this.getIn();
		gOp.setParameter( "gamma", this.gamma);
		gOp.setInImg( in);

		gOp.runOp( null);

		Image out = (Image)gOp.getResultImg();
		this.setOut( out);
	}

	public static void main(String [] args)  throws ALDException {
		// read in from file, create from scratch ...
		Image in = new Image();
		if ( args.length > 0 )  {
			//MTBPortHashAccess.readHistory( in, args[0]);
            ReadImage readOp = new ReadImage( args[0]);
            readOp.runOp( null);
            in = (Image)readOp.resultImg;
		}
		in.print();

		MTB_GammaCorrection op = new MTB_GammaCorrection();
		op.setVerbose( false);
		op.setIn( in);
		op.setParameter( "gamma", 1.8f);
		op.runOp( null);

		System.out.println( "================ write resultImg");
		Image out = (Image)(op.getOut());

		//out.writeHistory( "MTB_GammaCorrection.pgm");
		String name;
		if ( args.length > 1 )
			name = args[1];
		else
			name = "MTB_GammaCorrection.pgm";

		MTBOperator.writeHistory( out, name);
	}

	/** Get value of in.
	  * Explanation: (DAPI) channel to detect nuclei.
	  * @return value of in
	  */
	public Image getIn(){
		return in;
	}

	/** Set value of in.
	  * Explanation: (DAPI) channel to detect nuclei.
	  * @param value New value of in
	  */
	public void setIn( Image value){
		this.in = value;
	}

	/** Get value of out.
	  * Explanation: Median filtered PB channel.
	  * @return value of out
	  */
	public Image getOut(){
		return out;
	}

	/** Set value of out.
	  * Explanation: Median filtered PB channel.
	  * @param value New value of out
	  */
	public void setOut( Image value){
		this.out = value;
	}

}
