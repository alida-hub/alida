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

import de.unihalle.informatik.MiToBo.core.operator.*;
import de.unihalle.informatik.MiToBo.core.exceptions.*;

//    fake implementation of an image

public class Image extends MTBData {

	private boolean verbose = true;

	public Image() throws MTBOperatorException {
		// we should also give the data port the properties we want to be known
		// in the operator (node) DAG

		//setProperty( "nix", "was");
	}

	public Image( String filename) throws MTBOperatorException {
		// we should also give the data port the properties we want to be known
		// in the operator (node) DAG

		//readHistory( filename);
		MTBPortHashAccess.readHistory( this, filename);
		setLocation( filename);
		setProperty( "hall", "was");
	}

}
