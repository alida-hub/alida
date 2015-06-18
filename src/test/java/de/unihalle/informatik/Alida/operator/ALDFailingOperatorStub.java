/*
 * This file is part of MiToBo, the Microscope Image Analysis Toolbox.
 *
 * Copyright (C) 2010 - @YEAR@
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

/* 
 * Most recent change(s):
 * 
 * $Rev$
 * $Date$
 * $Author$
 * 
 */

package de.unihalle.informatik.Alida.operator;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;

/**
 * Stub operator class always failing on execution. 
 * @author moeller
 */
public class ALDFailingOperatorStub extends ALDOperator {

	/**
	 * Default constructor.
	 * @throws ALDOperatorException
	 */
	public ALDFailingOperatorStub() throws ALDOperatorException {
	  super();
  }

	@Override
  protected void operate() throws ALDOperatorException {
		throw new ALDOperatorException(OperatorExceptionType.OPERATE_FAILED,
				"[ALDFailingOperatorStub] operator execution failed!");
  }

}
