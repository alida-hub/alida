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

public class Counter_plugin extends MTBOperator {

	public  Counter_plugin()  throws ALDOperatorException {
		completeDAG = false;
	}

    @Parameter( label= "in", required=true, direction=Parameter.Direction.IN,
                            description = "Input image")
    Image in;

    @Parameter( label= "out", direction=Parameter.Direction.OUT,
                            description = "Result image")
    Image out;

    @Parameter( label= "count", direction=Parameter.Direction.IN,
                            description = "counter")
    Integer count;

	@Override
	protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		System.out.println( "Counter_plugin::operate");

		Counter cOp = new Counter();
		cOp.setVerbose( true);
		System.out.println( "Counter_plugin::operate got cOp");

		in = new Image();
		
		cOp.setInImg( this.in);
		cOp.setCount( count);

		cOp.runOp( null);


		MTBGaussFilter gOp = new MTBGaussFilter();
		gOp.setSigma( 2.0f);
		gOp.setInImg( (Image)cOp.getResultImg());
		gOp.runOp( null);

		out = (Image)gOp.getResultImg();
	}

	public static void main(String [] args)  throws ALDException {
		// read in from file, create from scratch ...
		Image in;
		
		if ( args.length > 0 ) {
        	ReadImage readOp = new ReadImage( args[0]);
         	readOp.runOp( null);
            in = (Image)readOp.resultImg;
        } else {
        	in = new Image();
        }


		Counter_plugin op = new Counter_plugin();
		//op.setVerbose( false);
		op.in = in;
		op.count = 3;
		op.runOp( null);

		System.out.println( "================ write cnt");
		Image out = (Image)(op.out);

		//WriteImage wOp = new WriteImage();
		//wOp.setParameter( "filename", "cnt.xml");
		//wOp.setInput( "inImg", out);
		//wOp.runOp( null);
		MTBOperator.writeHistory( out, "cnt");

		//out.writeHistory( "cnt");
		
	}
}
