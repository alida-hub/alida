/*
 * This file is part of Alida, a Java library for 
 * Advanced Library for Integrated Development of Data Analysis Applications.
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
 * Fore more information on Alida, visit
 *
 *    http://www.informatik.uni-halle.de/alida/
 *
 */

package de.unihalle.informatik.Alida.operator;

import de.unihalle.informatik.Alida.exceptions.ALDOperatorException;
import de.unihalle.informatik.Alida.exceptions.ALDOperatorException.OperatorExceptionType;
import de.unihalle.informatik.Alida.operator.ALDOperator;

/** This class is just a dummy opNode acting as the first opNode with
 * the stack of active opNodes of each thread.
 * This operator is never to be instantiated by users or invoked via its runOp method.
 */

class ALDToplevelOperator extends ALDOperator {

	/** Construct a (dummy) top level opNode
     */
	public ALDToplevelOperator() throws ALDOperatorException {
		completeDAG = false;
    }

	/** The abstract method operate needs to be implemented, but this
	 * top levels operator is never to be invoked.
     */
	protected void operate() throws ALDOperatorException {
		throw new ALDOperatorException( OperatorExceptionType.OPERATE_FAILED, "ALDToplevelOperator.operate() should never be invoked");
	}
}
