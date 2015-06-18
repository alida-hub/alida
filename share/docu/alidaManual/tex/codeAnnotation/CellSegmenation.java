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

public class CellSegmenation extends MTBOperator {

    @Parameter( label= "sigma", required = true, direction=Parameter.Direction.IN,
                description = "Standard deviation of Gauss")
    Double sigma;

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


	public CellSegmenation() throws ALDOperatorException {
		completeDAG = false;
	}

	protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		System.out.println( "CellSegmenation::operate");

		// Detect Nuclei
		DetectNuclei nuc = new DetectNuclei( this.nucChannel);
		nuc.setVerbose( this.getVerbose());

		nuc.runOp( null);


		// Median
		MTBMedian median = new MTBMedian();
		median.setVerbose( this.getVerbose());
		median.inImg = this.getPbChannel();

		median.runOp( true);

		//MTBPortHashAccess.writeHistory( median.getResultImg(), "medianFromCell");

		// detect cells
		ActiveContours activeC = new ActiveContours();
		activeC.setMaxIter( this.getMaxIter());
		activeC.setGamma( this.getGamma());
		activeC.setNucImg( nuc.getResultImg());
		activeC.setInImg( median.getResultImg());

		activeC.runOp( null );

		this.setResultImg( activeC.getResultImg());
		this.setMedianImg( median.getResultImg());

		// write intermediate result
		System.out.println( "writeHistory to create nucInterm-complete");
		//MTBPortHashAccess.writeHistory( nuc.getResultImg(), "nucInterm-complete", MTBProcessingDAG.HistoryType.COMPLETE);
		System.out.println( "writeHistory to create nucInterm-opnode");
		//MTBPortHashAccess.writeHistory( nuc.getResultImg(), "nucInterm-opnode", MTBProcessingDAG.HistoryType.OPNODETYPE);
	}

    public static void main(String [] args) throws ALDException {
        ALDProcessingDAG.setDebug( false);

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

        CellSegmenation op = new CellSegmenation();
        op.setVerbose( true);
        op.sigma = 3.0;
        op.maxIter = 100;
        op.gamma = 0.01f;
        op.nucChannel = in;
        op.pbChannel = in2;

        op.runOp( null);

        System.out.println( "================ write resultImg");
		Image out = (Image)op.getResultImg();
		MTBOperator.writeHistory( out, "resultImg-ignoreH", ALDProcessingDAG.HistoryType.COMPLETE, true);
		//MTBPortHashAccess.writeHistory( out, "resultImg-ignoreH", MTBProcessingDAG.HistoryType.COMPLETE, true);

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

	/** Get value of sigma.
	  * Explanation: Standard deviation of Gauss.
	  * @return value of sigma
	  */
	public java.lang.Double getSigma(){
		return sigma;
	}

	/** Set value of sigma.
	  * Explanation: Standard deviation of Gauss.
	  * @param value New value of sigma
	  */
	public void setSigma( java.lang.Double value){
		this.sigma = value;
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

	/** Get value of pbChannel.
	  * Explanation: (PB) channel to detect cells.
	  * @return value of pbChannel
	  */
	public Image getPbChannel(){
		return pbChannel;
	}

	/** Set value of pbChannel.
	  * Explanation: (PB) channel to detect cells.
	  * @param value New value of pbChannel
	  */
	public void setPbChannel( Image value){
		this.pbChannel = value;
	}

	/** Get value of nucChannel.
	  * Explanation: (DAPI) channel to detect nuclei.
	  * @return value of nucChannel
	  */
	public Image getNucChannel(){
		return nucChannel;
	}

	/** Set value of nucChannel.
	  * Explanation: (DAPI) channel to detect nuclei.
	  * @param value New value of nucChannel
	  */
	public void setNucChannel( Image value){
		this.nucChannel = value;
	}

	/** Get value of medianImg.
	  * Explanation: Median filtered PB channel.
	  * @return value of medianImg
	  */
	public Image getMedianImg(){
		return medianImg;
	}

	/** Set value of medianImg.
	  * Explanation: Median filtered PB channel.
	  * @param value New value of medianImg
	  */
	public void setMedianImg( Image value){
		this.medianImg = value;
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
