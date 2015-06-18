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

import java.io.*;

import de.unihalle.informatik.Alida.exceptions.*;
import de.unihalle.informatik.Alida.operator.*;
import de.unihalle.informatik.Alida.annotations.*;
import de.unihalle.informatik.MiToBo.core.exceptions.*;
import de.unihalle.informatik.MiToBo.core.operator.*;

public class Cell_Segmentation extends MTBOperator implements Serializable {
    @Parameter( label= "sigma", required = true, direction=Parameter.Direction.IN,
                description = "Standard deviation of Gauss")
    double sigma;

    @Parameter( label= "maxIter", required = true, direction=Parameter.Direction.IN,
                description = "Maximal number of iterations")
    Integer maxIter;

    @Parameter( label= "gamma", required = true, direction=Parameter.Direction.IN,
                description = "gamma")
    Float gamma;

	@Parameter( label= "nucChannel", required = true, direction=Parameter.Direction.IN,
                                description = "(DAPI) channel to detect nuclei")
        Image nucChannel;

	@Parameter( label= "pbChannel", required = true, direction=Parameter.Direction.IN,
                                description = "(PB) channel to detect cells")
        Image pbChannel;

	@Parameter( label= "resultImg", required = true, direction=Parameter.Direction.OUT,
                                description = "Result image")
        Image resultImg;

	@Parameter( label= "medianImg", required = true, direction=Parameter.Direction.OUT,
                                description = "Median filtered PB channel")
        Image medianImg;


	Cell_Segmentation() throws ALDOperatorException {
		completeDAG = false;
	}

	protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		System.out.println( "Cell_Segmentation::operate");

		CellSegmenation cellSegmenter = new CellSegmenation();

		cellSegmenter.setVerbose( true);
		cellSegmenter.sigma = this.sigma ;
		cellSegmenter.maxIter = this.maxIter ;
		cellSegmenter.gamma = this.gamma ;
		cellSegmenter.nucChannel = this.nucChannel ;
		cellSegmenter.pbChannel = this.pbChannel   ;

		cellSegmenter.runOp(HidingMode.HIDDEN); 
		//cellSegmenter.runOp(HidingMode.HIDE_CHILDREN); 

		this.resultImg = cellSegmenter.resultImg;
		this.medianImg = cellSegmenter.medianImg;
	}

	public static void main(String [] args) throws ALDException {
		ALDProcessingDAG.setDebug( false);
		// read in from file, create from scratch ...

		//Image in = new Image();
		//if ( args.length > 0 ) 
		//	in.readHistory( args[0]);
		//in.setProperty( "me", "in");

		Image in = null;
		if ( args.length > 0 ) {
			ReadImage readOp = new ReadImage( args[0]);
			readOp.runOp( null);
			in = (Image)readOp.resultImg;
		} else {
			in = new Image();
		}

		in.print();

		Image in2 = null;
		if ( args.length > 1 ) {
			ReadImage readOp = new ReadImage( args[1]);
			readOp.runOp( null);
			in2 = (Image)readOp.resultImg;
		} else {
			in2 = new Image();
		}
		in2.print();

		Cell_Segmentation op = new Cell_Segmentation();
		op.setVerbose( true);
		op.sigma = 3.0;
		op.maxIter = 100;
		op.gamma = 0.01f;
		op.nucChannel = in;
		op.pbChannel = in2;

		op.runOp( HidingMode.VISIBLE);
      
		System.out.println( "================ ALDOpNode statistics");
		ALDOpNode.printInstanceStatistics();

		System.out.println( "================ write resultImg");
		Image out = (Image)op.getResultImg();
		System.out.println( "Write history resultImg-opnode");
		MTBOperator.writeHistory( out, "resultImg-opnode", ALDProcessingDAG.HistoryType.OPNODETYPE);
		System.out.println( "Write history resultImg-data");
		MTBOperator.writeHistory( out, "resultImg-data", ALDProcessingDAG.HistoryType.DATADEPENDENCIES);
		System.out.println( "Write history resultImg-complete");
		MTBOperator.writeHistory( out, "resultImg-complete", ALDProcessingDAG.HistoryType.COMPLETE);
		System.out.println( "Write history resultImg-ignoreH");
		MTBOperator.writeHistory( out, "resultImg-ignoreH", ALDProcessingDAG.HistoryType.COMPLETE, true);
		
		System.out.println( "================ write medianImg");
		System.out.println( "Write history medianImg-complete");
		MTBOperator.writeHistory( op.medianImg, "medianImg-complete", ALDProcessingDAG.HistoryType.COMPLETE);
		System.out.println( "Write history medianImg-opnode");
		MTBOperator.writeHistory( op.medianImg, "medianImg-opnode", ALDProcessingDAG.HistoryType.OPNODETYPE);
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
