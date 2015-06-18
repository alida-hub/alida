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

public class ReadImage extends MTBOperator {
    @Parameter( label= "filename", required = true, direction=Parameter.Direction.IN,
                    description = "Filename")
    String filename;

    @Parameter( label= "resultImg", required = true, direction=Parameter.Direction.OUT,
                                description = "Result image")
    Image resultImg;

	public ReadImage() throws ALDOperatorException {
		completeDAG = false;
	}

	public ReadImage( String filename) throws ALDOperatorException {
		this.filename = filename;
	}

	protected void operate() throws ALDOperatorException,ALDProcessingDAGException {
		System.out.println( "ReadImage::operate");

		Image in = new Image( (String)(this.filename));

		//Image in = new Image( );
		//in.readHistory( (String)(this.getParameter("filename")));
		in.setLocation( (String)(this.filename));

		this.resultImg = in;
	}
}
